package com.kelompok2.frontend.skills;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.kelompok2.frontend.entities.GameCharacter;
import com.kelompok2.frontend.entities.MeleeAttack;
import com.kelompok2.frontend.entities.Projectile;

public abstract class BaseSkill implements Skill {
    protected String name;
    protected String description;
    protected float cooldown; // Total cooldown duration
    protected float cooldownTimer; // Current cooldown remaining

    public BaseSkill(String name, String description, float cooldown) {
        this.name = name;
        this.description = description;
        this.cooldown = cooldown;
        this.cooldownTimer = 0f; // Start ready to use
    }

    @Override
    public void activate(GameCharacter user, Vector2 targetPos,
            Array<Projectile> projectiles,
            Array<MeleeAttack> meleeAttacks) {
        if (!canUse()) {
            System.out.println("[" + name + "] On cooldown: " +
                    String.format("%.1f", cooldownTimer) + "s remaining");
            return;
        }

        // Execute skill-specific behavior
        executeSkill(user, targetPos, projectiles, meleeAttacks);

        // Start cooldown
        cooldownTimer = cooldown;

        System.out.println("[" + name + "] Activated!");
    }

    protected abstract void executeSkill(GameCharacter user, Vector2 targetPos,
            Array<Projectile> projectiles,
            Array<MeleeAttack> meleeAttacks);

    @Override
    public boolean canUse() {
        return cooldownTimer <= 0;
    }

    @Override
    public void update(float delta) {
        if (cooldownTimer > 0) {
            cooldownTimer -= delta;
            if (cooldownTimer < 0) {
                cooldownTimer = 0;
            }
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public float getCooldown() {
        return cooldown;
    }

    @Override
    public float getRemainingCooldown() {
        return cooldownTimer;
    }
}
