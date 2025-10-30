package com.dam.project.engine;

// JavaFX núcleo del bucle y render
import javafx.animation.AnimationTimer;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

// Utilidades Java
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

// Entidades y lógica de juego
import com.dam.project.entities.Player;
import com.dam.project.entities.Enemy;
import com.dam.project.combat.AttackController;
import com.dam.project.score.ScoreManager;

// UI y Base de Datos (para el diálogo de Game Over y guardado)
import com.dam.project.ui.ScoreSaveDialog;
import com.dam.project.db.DatabaseManager;
import com.dam.project.db.ScoreRepository;

/**
 * GameLoop controla:
 * - Tiempo (dt) por frame
 * - Input -> movimiento del jugador
 * - Spawner de enemigos + update de enemigos
 * - Ataque cuerpo a cuerpo con cooldown
 * - Puntuación (tiempo + kills)
 * - Detección de muerte y despliegue de diálogo "Game Over" para guardar score
 */
public class GameLoop extends AnimationTimer {

    // --- Render y escena ---
    private final GraphicsContext gc; // donde pintamos
    private final int width;          // ancho de la escena
    private final int height;         // alto de la escena

    // --- Jugador e input ---
    private final Player player;      // entidad jugador
    private final Set<String> keys;   // teclas actualmente pulsadas

    // --- Tiempo ---
    private long last = 0;            // timestamp del frame anterior (ns)

    // --- Enemigos ---
    private final List<Enemy> enemies = new ArrayList<>(); // lista de enemigos vivos
    private double spawnTimer = 0.0;    // acumulador para spawns
    private double spawnInterval = 1.5; // cada cuántos segundos aparece 1 enemigo (se acelera)

    // --- Estado de partida ---
    private boolean gameOver = false;        // flag de "has muerto"
    private boolean deathDialogShown = false; // para mostrar el diálogo UNA vez por muerte

    // --- Combate corto alcance ---
    private final AttackController attack = new AttackController(); // gestiona cooldown del ataque
    private double attackFxTimer = 0.0;   // cuánto dura el círculo FX del golpe
    private final double attackRadius = 48.0; // radio de golpe (px)

    // --- Puntuación ---
    private final ScoreManager score = new ScoreManager(); // +1/seg y +10 por kill

    public GameLoop(GraphicsContext gc, Player player, Set<String> keys, int width, int height) {
        this.gc = gc;
        this.player = player;
        this.keys = keys;
        this.width = width;
        this.height = height;
    }

    @Override
    public void handle(long now) {
        // 1) Calcular dt en segundos a partir de nanosegundos
        if (last == 0) { last = now; return; }
        double dt = (now - last) / 1_000_000_000.0;
        last = now;

        // 2) Si estamos en Game Over, aún renderizamos la pantalla de fin
        //    (el diálogo de guardado se lanza en cuanto detectamos la muerte).
        if (gameOver) {
            renderGameOver();
            return;
        }

        // 3) INPUT + movimiento básico (WASD o cursores)
        double dx = 0, dy = 0;
        if (keys.contains("W") || keys.contains("UP"))    dy -= 1;
        if (keys.contains("S") || keys.contains("DOWN"))  dy += 1;
        if (keys.contains("A") || keys.contains("LEFT"))  dx -= 1;
        if (keys.contains("D") || keys.contains("RIGHT")) dx += 1;

        // Actualiza jugador (con límites de pantalla)
        player.update(dt, dx, dy, width, height);

        // 4) Ataque cuerpo a cuerpo (SPACE o J) con cooldown
        attack.update(dt); // reduce el cooldown
        if (keys.contains("SPACE") || keys.contains("J")) {
            if (attack.tryAttack()) { // si estaba listo, ejecuta golpe
                performAttack();      // marcamos enemigos en radio como consumidos
                attackFxTimer = 0.12; // muestra breve círculo FX
            }
        }
        if (attackFxTimer > 0) attackFxTimer -= dt;

        // 5) Spawner de enemigos (cada vez más rápido hasta 0.6s)
        spawnTimer += dt;
        if (spawnTimer >= spawnInterval) {
            spawnTimer = 0;
            enemies.add(spawnAtEdge());
            if (spawnInterval > 0.6) spawnInterval -= 0.02;
        }

        // 6) Update de enemigos + colisión con jugador
        for (Enemy e : enemies) {
            e.update(dt, player.getX(), player.getY());
            // Si colisiona con el jugador, daño y el enemigo se consume (desaparece)
            if (e.intersects(player.getX(), player.getY(), player.getSize(), player.getSize())) {
                player.damage(1);
                e.markConsumed();
            }
        }
        enemies.removeIf(Enemy::isConsumed);

        // 7) Si el jugador muere -> activar Game Over y abrir diálogo de guardado una sola vez
        if (player.isDead()) {
            gameOver = true;

            if (!deathDialogShown) {
                deathDialogShown = true;

                // Base de datos en archivo (persistente) para el juego real
                DatabaseManager dbm = new DatabaseManager("jdbc:sqlite:laylas_fate.db");
                ScoreRepository repo = new ScoreRepository(dbm);

                // Mostrar UI en el hilo JavaFX
                javafx.application.Platform.runLater(() -> {
                    Stage st = (Stage) gc.getCanvas().getScene().getWindow();
                    // Pasamos callbacks:
                    // - onRetry: resetea partida y permite volver a jugar
                    // - onExit: cierra la ventana principal
                    ScoreSaveDialog.show(
                        st, repo, score.getPoints(), score.getTime(),
                        () -> { reset(); deathDialogShown = false; }, // Retry
                        () -> st.close()                               // Exit
                    );
                });
            }
        }

        // 8) Actualizar puntuación (suma el tiempo; los kills se suman en performAttack)
        score.update(dt);

        // 9) RENDER de escena
        // Fondo
        gc.setFill(Color.web("#121212"));
        gc.fillRect(0, 0, width, height);

        // FX de ataque (círculo alrededor del jugador durante breves ms)
        if (attackFxTimer > 0) {
            gc.setFill(Color.web("#7A5BF0", 0.25));
            double cx = player.getX() + player.getSize()/2.0;
            double cy = player.getY() + player.getSize()/2.0;
            gc.fillOval(cx - attackRadius, cy - attackRadius, attackRadius*2, attackRadius*2);
        }

        // Jugador y enemigos
        player.render(gc);
        for (Enemy e : enemies) e.render(gc);

        // HUD con HP, spawn, cooldown y puntuación
        drawHUD();
    }

    /** Dibuja información del HUD (parte superior). */
    private void drawHUD() {
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font(16));
        gc.fillText("HP: " + player.getHp(), 14, 22);
        gc.fillText(String.format(java.util.Locale.US, "Spawn: %.2fs", spawnInterval), 14, 42);

        double cd = attack.getTimer(); // tiempo restante de cooldown
        String cdText = (cd <= 0) ? "READY" : String.format(java.util.Locale.US, "%.2fs", cd);
        gc.fillText("Attack (SPACE/J): " + cdText, 14, 62);

        // Score arriba derecha
        String scoreText = "Score: " + score.getPoints();
        gc.fillText(scoreText, width - 130, 22);
    }

    /**
     * Ejecuta el golpe: cualquier enemigo cuyo centro esté dentro de "attackRadius"
     * se marca como consumido (muerto) y suma puntos de kill (+10).
     */
    private void performAttack() {
        double cx = player.getX() + player.getSize()/2.0;
        double cy = player.getY() + player.getSize()/2.0;
        for (Enemy e : enemies) {
            double ex = getEnemyCenter(e)[0];
            double ey = getEnemyCenter(e)[1];
            double dist = Math.hypot(ex - cx, ey - cy);
            if (dist <= attackRadius) {
                e.markConsumed();
                score.addKill(10); // +10 por kill
            }
        }
    }

    /** Obtiene el centro aproximado de un enemigo (usando reflexión si no hay getters públicos). */
    private double[] getEnemyCenter(Enemy e) {
        try {
            var fx = e.getClass().getDeclaredField("x");
            var fy = e.getClass().getDeclaredField("y");
            fx.setAccessible(true); fy.setAccessible(true);
            return new double[]{ fx.getDouble(e) + e.getSize()/2.0, fy.getDouble(e) + e.getSize()/2.0 };
        } catch (Exception ex) {
            return new double[]{0,0};
        }
    }

    /** Spawnea un enemigo en un borde aleatorio de la pantalla. */
    private Enemy spawnAtEdge() {
        Random r = new Random();
        int side = r.nextInt(4);
        double x=0, y=0;
        switch (side) {
            case 0: x = r.nextInt(width); y = -30; break;            // arriba
            case 1: x = width + 30; y = r.nextInt(height); break;    // derecha
            case 2: x = r.nextInt(width); y = height + 30; break;    // abajo
            default: x = -30; y = r.nextInt(height); break;          // izquierda
        }
        return new Enemy(x, y);
    }

    /** Dibuja la pantalla de Game Over (texto). El diálogo se lanza aparte. */
    private void renderGameOver() {
        gc.setFill(Color.web("#121212"));
        gc.fillRect(0, 0, width, height);
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font(28));
        gc.fillText("GAME OVER", width/2.0 - 100, height/2.0 - 10);
        gc.setFont(Font.font(16));
        gc.fillText("Saving dialog is open (or was shown).", width/2.0 - 140, height/2.0 + 20);
    }

    /**
     * Resetea el estado de partida para volver a jugar:
     * - limpia enemigos
     * - reinicia temporizadores y dificultad de spawn
     * - restaura HP del jugador y lo centra
     * - resetea el marcador
     */
    private void reset() {
        enemies.clear();
        spawnTimer = 0;
        spawnInterval = 1.5;

        player.resetHp(5);
        player.setPosition(width/2.0 - player.getSize()/2.0, height/2.0 - player.getSize()/2.0);

        score.reset();

        gameOver = false;
        // importante para poder volver a mostrar el diálogo en la próxima muerte
        deathDialogShown = false;
    }
}

