package com.kelompok2.frontend.skills;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.kelompok2.frontend.entities.GameCharacter;
import com.kelompok2.frontend.entities.MeleeAttack;
import com.kelompok2.frontend.entities.Projectile;

public class HellfirePillarSkill extends BaseSkill {
    private static final float COOLDOWN = 5f;
    private static final float DURATION = 2f;
    private static final float RADIUS = 40f;
    private static final float WARNING_DURATION = 0.5f; // Balance Fix: Warning before damage

    private boolean isActive = false;
    private boolean isWarning = false; // Warning phase (no damage yet)
    private float activeTimer = 0f;
    private float warningTimer = 0f;
    private Vector2 pillarPosition;

    public HellfirePillarSkill() {
        super("Hellfire Pillar", "Summons a pillar of fire.", COOLDOWN);
        this.pillarPosition = new Vector2();
    }

    @Override
    protected void executeSkill(GameCharacter user, Vector2 targetPos, Array<Projectile> projectiles,
            Array<MeleeAttack> meleeAttacks) {

        pillarPosition.set(targetPos);
        isWarning = true; // Start with warning phase
        isActive = false; // Not dealing damage yet
        warningTimer = WARNING_DURATION;
        // Don't set activeTimer here - it will be set when warning ends

        System.out.println("[HellfirePillarSkill] Warning phase started at " + targetPos);
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        // Warning phase
        if (isWarning) {
            warningTimer -= delta;
            if (warningTimer <= 0) {
                isWarning = false;
                isActive = true; // Now start dealing damage
                activeTimer = DURATION; // Set timer when entering active phase
                System.out.println("[HellfirePillarSkill] Warning ended, pillar now active!");
            }
        }

        // Active damage phase
        if (isActive) {
            activeTimer -= delta;
            if (activeTimer <= 0) {
                isActive = false;
            }
        }
    }

    public boolean isPillarActive() {
        return isActive;
    }

    public Vector2 getPillarPosition() {
        return pillarPosition;
    }

    public float getPillarRadius() {
        return RADIUS;
    }

    public boolean isInWarningPhase() {
        return isWarning;
    }

    // Balance Fix: Show visual during both warning and active phases
    public boolean shouldShowVisual() {
        return isWarning || isActive;
    }

    @Override
    public Skill copy() {
        return new HellfirePillarSkill();
    }
}
