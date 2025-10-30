package com.dam.project.engine;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.Set;

import com.dam.project.entities.Player;

public class GameLoop extends AnimationTimer {
    private final GraphicsContext gc;
    private final Player player;
    private final Set<String> keys;
    private long last = 0;

    private final int width = 800;
    private final int height = 600;

    public GameLoop(GraphicsContext gc, Player player, Set<String> keys) {
        this.gc = gc;
        this.player = player;
        this.keys = keys;
    }

    @Override
    public void handle(long now) {
        if (last == 0) { last = now; return; }
        double dt = (now - last) / 1_000_000_000.0;
        last = now;

        // UPDATE
        double dx = 0, dy = 0;
        if (keys.contains("W") || keys.contains("UP"))    dy -= 1;
        if (keys.contains("S") || keys.contains("DOWN"))  dy += 1;
        if (keys.contains("A") || keys.contains("LEFT"))  dx -= 1;
        if (keys.contains("D") || keys.contains("RIGHT")) dx += 1;
        player.update(dt, dx, dy, width, height);

        // RENDER
        gc.setFill(Color.web("#121212"));  // fondo gris oscuro
        gc.fillRect(0, 0, width, height);

        player.render(gc);
    }
}