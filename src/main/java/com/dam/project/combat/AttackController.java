package com.dam.project.combat;

public class AttackController {
    private double cooldown = 0.5; // segundos
    private double timer = 0.0;

    public void update(double dt) {
        if (timer > 0) timer -= dt;
    }

    /** Intenta atacar; devuelve true si el ataque se ejecuta (cooldown listo). */
    public boolean tryAttack() {
        if (timer <= 0) {
            timer = cooldown;
            return true;
        }
        return false;
    }

    /** Tiempo restante del cooldown (>0 si no está listo). */
    public double getTimer() {
        return Math.max(timer, 0.0);
    }

    public double getCooldown() {
        return cooldown;
    }

    public void setCooldown(double cooldownSeconds) {
        this.cooldown = Math.max(0.05, cooldownSeconds);
    }
}