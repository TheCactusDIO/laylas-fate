package com.dam.project.score;

public class ScoreManager {
    private int killPoints = 0;
    private double time = 0.0; // segundos acumulados

    public void update(double dt) {
        time += dt; // el +1 por segundo lo calculamos al pedir el total
    }

    /** +value por kill (p.ej. 10) */
    public void addKill(int value) { killPoints += value; }

    public void reset() {
        killPoints = 0;
        time = 0.0;
    }

    /** Total = kills + segundos enteros vividos */
    public int getPoints() {
        return killPoints + (int)Math.floor(time);
    }

    public double getTime() { return time; }
}