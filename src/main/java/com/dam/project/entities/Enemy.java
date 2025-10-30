package com.dam.project.entities;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Enemy {
    private double x, y;
    private final double size = 28;
    private double speed = 120;
    private boolean consumed = false;

    public Enemy(double x, double y) { this.x = x; this.y = y; }

    public void update(double dt, double tx, double ty) {
        double dx = tx - x, dy = ty - y;
        double len = Math.hypot(dx, dy);
        if (len > 0.0001) { x += (dx/len)*speed*dt; y += (dy/len)*speed*dt; }
    }

    public void render(GraphicsContext gc) {
        gc.setFill(Color.CRIMSON);
        gc.fillRoundRect(x, y, size, size, 6, 6);
    }

    public boolean intersects(double rx, double ry, double rw, double rh) {
        return (x < rx + rw && x + size > rx && y < ry + rh && y + size > ry);
    }

    public void markConsumed() { consumed = true; }
    public boolean isConsumed() { return consumed; }
    public double getSize() { return size; }
}