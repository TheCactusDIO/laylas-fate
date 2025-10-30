package com.dam.project.score;

public class ScoreManager {
    private int points = 0;
    private double time = 0.0;

    public void update(double dt) {
        time += dt;
        points += (int)Math.floor(dt); // +1/segundo aprox
    }

    public void addKill(int value) {
        points += value;
    }

    public void reset() {
        points = 0;
        time = 0;
    }

    public int getPoints() { return points; }
    public double getTime() { return time; }
}