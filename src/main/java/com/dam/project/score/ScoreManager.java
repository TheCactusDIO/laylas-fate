package com.dam.project.score;

public class ScoreManager {
    private int points = 0;
    private double time = 0;

    public void addTime(double dt) { time += dt; }
    public void addKill(int base, double phaseMultiplier) { points += (int)Math.round(base * phaseMultiplier); }

    public int getPoints() { return points + (int)Math.floor(time); }
    public double getSurvivalTime() { return time; }
}