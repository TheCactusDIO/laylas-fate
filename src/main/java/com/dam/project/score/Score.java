package com.dam.project.score;

public class Score {
    public final String name;
    public final int points;
    public final double survivalTime;
    public final String createdAt;

    public Score(String name, int points, double survivalTime, String createdAt) {
        this.name = name; this.points = points; this.survivalTime = survivalTime; this.createdAt = createdAt;
    }
}