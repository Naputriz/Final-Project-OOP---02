package com.kelompok2.frontend.skills;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.kelompok2.frontend.entities.GameCharacter;
import com.kelompok2.frontend.entities.MeleeAttack;
import com.kelompok2.frontend.entities.Projectile;

public class BladeFurySkill extends BaseSkill {

    // Active state tracking
    private boolean isActive = false;
    private float activeDuration = 1.5f; // Total duration of fury
    private float activeTimer = 0f;
    private int hitCount = 0;
    private int maxHits = 5;
    private float hitInterval = 0.3f; // 0.3s between hits
    private float nextHitTimer = 0f;

    private GameCharacter activeUser; // Track user during active state

    public BladeFurySkill() {
        super("Blade Fury", "Spin rapidly, 5 hits (ATK × 0.8 each)", 10f);
    }

    @Override
    protected void executeSkill(GameCharacter user, Vector2 targetPos, Array<Projectile> projectiles, Array<MeleeAttack> meleeAttacks) {
        // Start fury
        isActive = true;
        activeTimer = activeDuration;
        hitCount = 0;
        nextHitTimer = 0f; // First hit immediately
        activeUser = user;

        System.out.println("[Blade Fury] Started! Will deal 5 hits of ATK × 0.8");
    }

    @Override
    public void update(float delta) {
        super.update(delta); // Update cooldown

        // Update active fury state
        if (isActive) {
            activeTimer -= delta;
            nextHitTimer -= delta;

            // Deal hit if timer ready
            if (nextHitTimer <= 0 && hitCount < maxHits) {
                dealHit();
                hitCount++;
                nextHitTimer = hitInterval;
            }

            // End fury if duration expired or all hits done
            if (activeTimer <= 0 || hitCount >= maxHits) {
                isActive = false;
                activeTimer = 0;
                System.out.println("[Blade Fury] Ended! Total hits: " + hitCount);
            }
        }
    }

    private void dealHit() {
        if (activeUser == null)
            return;

        float damage = activeUser.getAtk() * 0.8f;
        float radius = 100f;

        System.out.println("[Blade Fury] Hit #" + (hitCount + 1) +
                " - Damage: " + damage + ", Radius: " + radius);
    }

    public boolean isSkillActive() {
        return isActive;
    }

    public float getCurrentHitDamage() {
        if (activeUser == null)
            return 0f;
        return activeUser.getAtk() * 0.8f;
    }

    public float getRadius() {
        return 100f;
    }

    public GameCharacter getActiveUser() {
        return activeUser;
    }

    @Override
    public Skill copy() {
        return new BladeFurySkill();
    }
}
