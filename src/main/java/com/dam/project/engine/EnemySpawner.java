package com.dam.project.engine;

public class EnemySpawner implements Runnable {
    private volatile boolean running = true;
    private long intervalMs = 1500;

    public void stop() { running = false; }
    public void setIntervalMs(long ms) { intervalMs = ms; }

    @Override
    public void run() {
        while (running) {
            try {
                // TODO: crear y publicar enemigos a una cola compartida
                Thread.sleep(intervalMs);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}