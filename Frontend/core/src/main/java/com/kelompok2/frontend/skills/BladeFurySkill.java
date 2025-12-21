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
    // private float radius = 75f; // Unused, getRadius() returns 100f

    public BladeFurySkill() {
        super("Blade Fury", "Spin rapidly, 5 hits (ATK × 0.8 each)", 10f);
    }

    private Array<MeleeAttack> activeMeleeAttacks; // Reference to add attacks later

    @Override
    protected boolean executeSkill(GameCharacter user, Vector2 targetPos, Array<Projectile> projectiles,
            Array<MeleeAttack> meleeAttacks) {
        // Start fury
        isActive = true;
        activeTimer = activeDuration;
        hitCount = 0;
        nextHitTimer = 0f; // First hit immediately
        activeUser = user;
        this.activeMeleeAttacks = meleeAttacks; // Store reference

        System.out.println("[Blade Fury] Started! Will deal 5 hits of ATK × 0.8");
        return true;
    }

    @Override
    public void update(float delta) {
        super.update(delta); // Update cooldown

        // Update active fury state
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
        if (activeUser == null || activeMeleeAttacks == null)
            return;

        float damage = activeUser.getAtk() * 0.8f;
        float radius = 75f; // Distance from center

        float centerX = activeUser.getPosition().x + activeUser.getVisualWidth() / 2;
        float centerY = activeUser.getPosition().y + activeUser.getVisualHeight() / 2;

        int attackCount = 4; // Spawn 4 random slashes per "hit" interval

        for (int i = 0; i < attackCount; i++) {
            // Random angle
            float angle = (float) (Math.random() * 360f);
            float angleRad = (float) Math.toRadians(angle);

            // Position at random distance within range
            float dist = (float) (Math.random() * radius);
            float x = centerX + (float) Math.cos(angleRad) * dist - 32f; // -32 centering (approx)
            float y = centerY + (float) Math.sin(angleRad) * dist - 32f;

            // Spawn melee attack
            MeleeAttack attack = new MeleeAttack(
                    x, y,
                    64f, 64f, // 64x64 size
                    damage,
                    0.2f, // Very short duration per slash
                    activeUser.getAttackAnimationType(), // Use character's own animation style
                    angle // Random rotation
            );

            activeMeleeAttacks.add(attack);
        }

        System.out.println("[Blade Fury] Flurry hit #" + (hitCount + 1));
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
        BladeFurySkill copy = new BladeFurySkill();
        copy.maxHits = this.maxHits;
        copy.activeDuration = this.activeDuration; // Copy duration too
        copy.hitInterval = this.hitInterval;
        copy.description = this.description;
        return copy;
    }

    @Override
    public void onEquip(GameCharacter owner) {
        // Bonus for Ryze, Insania, and Alice: 10 hits instead of 5
        if (owner instanceof com.kelompok2.frontend.entities.Ryze ||
                owner instanceof com.kelompok2.frontend.entities.Insania ||
                owner instanceof com.kelompok2.frontend.entities.Alice) {

            this.maxHits = 10;
            // Update active duration to match new hit count so it doesn't end early!
            this.activeDuration = this.maxHits * this.hitInterval;

            this.description = "Spin rapidly, 10 hits (ATK × 0.8 each) - COMBO BONUS!";
            System.out.println(
                    "[Blade Fury] Combo activated for " + owner.getClass().getSimpleName() + "! Hits increased to 10.");
        }
    }
}
