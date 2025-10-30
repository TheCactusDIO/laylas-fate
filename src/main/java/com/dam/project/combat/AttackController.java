package com.dam.project.combat;

public class AttackController {
    private double cooldown = 0.5; // s
    private double timer = 0.0;

    public void update(double dt) { if (timer > 0) timer -= dt; }
    public boolean tryAttack() {
        if (timer <= 0) { timer = cooldown; return true; }
        return false;
    }
}