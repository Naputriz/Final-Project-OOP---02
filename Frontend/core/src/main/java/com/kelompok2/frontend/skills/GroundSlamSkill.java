package com.kelompok2.frontend.skills;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.kelompok2.frontend.entities.GameCharacter;
import com.kelompok2.frontend.entities.MeleeAttack;
import com.kelompok2.frontend.entities.Projectile;

public class GroundSlamSkill extends BaseSkill {

    // Active shockwave tracking
    private boolean shockwaveActive = false;
    private float shockwaveTimer = 0f;
    private float shockwaveDuration = 0.3f; // Visual effect duration
    private Vector2 shockwavePosition;

    private GameCharacter activeUser;

    public GroundSlamSkill() {
        super("Ground Slam", "Shockwave that stuns (ATK × 3.0)", 12f); // 12 second cooldown
    }

    @Override
    protected boolean executeSkill(GameCharacter user, Vector2 targetPos,
            Array<Projectile> projectiles,
            Array<MeleeAttack> meleeAttacks) {
        // Create shockwave at character position
        shockwaveActive = true;
        shockwaveTimer = shockwaveDuration;
        activeUser = user;

        // Shockwave centered on character
        shockwavePosition = new Vector2(
                user.getPosition().x + user.getVisualWidth() / 2,
                user.getPosition().y + user.getVisualHeight() / 2);

        float damage = user.getAtk() * 3.0f;

        // Create large shockwave attack
        float radius = 120f;
        MeleeAttack attack = new MeleeAttack(
                shockwavePosition.x - radius,
                shockwavePosition.y - radius,
                radius * 2,
                radius * 2,
                damage,
                0.2f, // Lasts 0.2s
                "slash", // Use slash animation for now
                0);
        attack.setStunDuration(1.5f);
        meleeAttacks.add(attack);

        System.out.println("[Ground Slam] Shockwave at " + shockwavePosition + " - Damage: " + damage + ", Stun: 1.5s");
        return true;
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        // Update shockwave visual timer
        if (shockwaveActive) {
            shockwaveTimer -= delta;
            if (shockwaveTimer <= 0) {
                shockwaveActive = false;
                System.out.println("[Ground Slam] Shockwave ended");
            }
        }
    }

    public boolean isShockwaveActive() {
        return shockwaveActive;
    }

    public Vector2 getShockwavePosition() {
        return shockwavePosition;
    }

    public float getRadius() {
        return 120f;
    }

    public float getDamage() {
        if (activeUser == null)
            return 0f;
        return activeUser.getAtk() * 3.0f;
    }

    public float getStunDuration() {
        return 1.5f;
    }

    public GameCharacter getActiveUser() {
        return activeUser;
    }

    @Override
    public Skill copy() {
        return new GroundSlamSkill();
    }
}
