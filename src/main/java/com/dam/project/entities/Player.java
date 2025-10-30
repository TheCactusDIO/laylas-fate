package com.dam.project.entities;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Player {
    private double x, y;
    private final double size = 36;
    private final double speed = 260;
    private int hp = 5;

    public Player(double x, double y) { this.x = x; this.y = y; }

    public void update(double dt, double dx, double dy, int width, int height) {
        double len = Math.hypot(dx, dy);
        if (len > 0) { dx /= len; dy /= len; }
        x += dx * speed * dt;
        y += dy * speed * dt;
        if (x < 0) x = 0;
        if (y < 0) y = 0;
        if (x + size > width)  x = width - size;
        if (y + size > height) y = height - size;
    }

    public void render(GraphicsContext gc) {
        gc.setFill(Color.web("#7A5BF0"));
        gc.fillRoundRect(x, y, size, size, 8, 8);
    }

    public void damage(int d) { hp -= d; }
    public void resetHp(int v) { hp = v; }
    public void setPosition(double nx, double ny) { x = nx; y = ny; }

    public int getHp() { return hp; }
    public boolean isDead() { return hp <= 0; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getSize() { return size; }
}