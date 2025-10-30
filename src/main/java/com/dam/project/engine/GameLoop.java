package com.dam.project.engine;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import java.util.*;
import com.dam.project.entities.Player;
import com.dam.project.entities.Enemy;
import com.dam.project.combat.AttackController;
import com.dam.project.score.ScoreManager;

public class GameLoop extends AnimationTimer {
    private final GraphicsContext gc;
    private final Player player;
    private final Set<String> keys;
    private final int width;
    private final int height;

    private long last = 0;
    private final List<Enemy> enemies = new ArrayList<>();
    private double spawnTimer = 0.0;
    private double spawnInterval = 1.5;
    private boolean gameOver = false;

    private final AttackController attack = new AttackController();
    private double attackFxTimer = 0.0;
    private final double attackRadius = 48.0;

    private final ScoreManager score = new ScoreManager();

    public GameLoop(GraphicsContext gc, Player player, Set<String> keys, int width, int height) {
        this.gc = gc;
        this.player = player;
        this.keys = keys;
        this.width = width;
        this.height = height;
    }

    @Override
    public void handle(long now) {
        if (last == 0) { last = now; return; }
        double dt = (now - last) / 1_000_000_000.0;
        last = now;

        if (gameOver) {
            renderGameOver();
            if (keys.contains("R")) reset();
            return;
        }

        // UPDATE: input + jugador
        double dx = 0, dy = 0;
        if (keys.contains("W") || keys.contains("UP"))    dy -= 1;
        if (keys.contains("S") || keys.contains("DOWN"))  dy += 1;
        if (keys.contains("A") || keys.contains("LEFT"))  dx -= 1;
        if (keys.contains("D") || keys.contains("RIGHT")) dx += 1;
        player.update(dt, dx, dy, width, height);

        // UPDATE: ataque
        attack.update(dt);
        if (keys.contains("SPACE") || keys.contains("J")) {
            if (attack.tryAttack()) {
                performAttack();
                attackFxTimer = 0.12;
            }
        }
        if (attackFxTimer > 0) attackFxTimer -= dt;

        // UPDATE: spawn enemigos
        spawnTimer += dt;
        if (spawnTimer >= spawnInterval) {
            spawnTimer = 0;
            enemies.add(spawnAtEdge());
            if (spawnInterval > 0.6) spawnInterval -= 0.02;
        }

        // UPDATE: enemigos
        for (Enemy e : enemies) {
            e.update(dt, player.getX(), player.getY());
            if (e.intersects(player.getX(), player.getY(), player.getSize(), player.getSize())) {
                player.damage(1);
                e.markConsumed();
            }
        }
        enemies.removeIf(Enemy::isConsumed);

        if (player.isDead()) gameOver = true;

        // SCORE update (cada frame)
        score.update(dt);

        // RENDER
        gc.setFill(Color.web("#121212"));
        gc.fillRect(0, 0, width, height);

        if (attackFxTimer > 0) {
            gc.setFill(Color.web("#7A5BF0", 0.25));
            double cx = player.getX() + player.getSize()/2.0;
            double cy = player.getY() + player.getSize()/2.0;
            gc.fillOval(cx - attackRadius, cy - attackRadius, attackRadius*2, attackRadius*2);
        }

        player.render(gc);
        for (Enemy e : enemies) e.render(gc);

        drawHUD();
    }

    private void drawHUD() {
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font(16));
        gc.fillText("HP: " + player.getHp(), 14, 22);
        gc.fillText(String.format(java.util.Locale.US, "Spawn: %.2fs", spawnInterval), 14, 42);

        double cd = attack.getTimer();
        String cdText = (cd <= 0) ? "READY" : String.format(java.util.Locale.US, "%.2fs", cd);
        gc.fillText("Attack (SPACE/J): " + cdText, 14, 62);

        // Score arriba derecha
        String scoreText = "Score: " + score.getPoints();
        gc.fillText(scoreText, width - 130, 22);
    }

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

    private double[] getEnemyCenter(Enemy e) {
        try {
            var fx = e.getClass().getDeclaredField("x");
            var fy = e.getClass().getDeclaredField("y");
            fx.setAccessible(true); fy.setAccessible(true);
            return new double[]{ fx.getDouble(e) + e.getSize()/2.0, fy.getDouble(e) + e.getSize()/2.0 };
        } catch (Exception ex) { return new double[]{0,0}; }
    }

    private Enemy spawnAtEdge() {
        Random r = new Random();
        int side = r.nextInt(4);
        double x=0, y=0;
        switch (side) {
            case 0: x = r.nextInt(width); y = -30; break;
            case 1: x = width + 30; y = r.nextInt(height); break;
            case 2: x = r.nextInt(width); y = height + 30; break;
            default: x = -30; y = r.nextInt(height); break;
        }
        return new Enemy(x, y);
    }

    private void renderGameOver() {
        gc.setFill(Color.web("#121212"));
        gc.fillRect(0, 0, width, height);
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font(28));
        gc.fillText("GAME OVER", width/2.0 - 100, height/2.0 - 10);
        gc.setFont(Font.font(16));
        gc.fillText("Press R to restart", width/2.0 - 80, height/2.0 + 20);
    }

    private void reset() {
        enemies.clear();
        spawnTimer = 0;
        spawnInterval = 1.5;
        player.resetHp(5);
        player.setPosition(width/2.0 - player.getSize()/2.0, height/2.0 - player.getSize()/2.0);
        score.reset();
        gameOver = false;
    }
}