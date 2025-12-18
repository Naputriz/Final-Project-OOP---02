package com.kelompok2.frontend.skills;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.kelompok2.frontend.entities.GameCharacter;
import com.kelompok2.frontend.entities.MeleeAttack;
import com.kelompok2.frontend.entities.Projectile;

public class ShieldStanceSkill extends BaseSkill {

    private boolean isActive = false;
    private float duration = 2f; // 2 seconds duration
    private float timer = 0f;
    private Vector2 facingDirection = new Vector2(); // Direction player is facing

    public ShieldStanceSkill() {
        super("Here, I shall stand!",
                "Immobilized for 2s, blocks frontal damage, reflects 50% back",
                10f); // 10 second cooldown
    }

    @Override
    protected void executeSkill(GameCharacter user, Vector2 targetPos,
            Array<Projectile> projectiles,
            Array<MeleeAttack> meleeAttacks) {

        // Activate shield stance
        isActive = true;
        timer = duration;

        // Store facing direction berdasarkan target position
        float centerX = user.getPosition().x + user.getVisualWidth() / 2;
        float centerY = user.getPosition().y + user.getVisualHeight() / 2;
        facingDirection.set(targetPos.x - centerX, targetPos.y - centerY).nor();

        // Update character facing
        if (facingDirection.x > 0) {
            user.setFacingRight(true);
        } else if (facingDirection.x < 0) {
            user.setFacingRight(false);
        }

        System.out.println("[Aegis] Shield Stance activated! Immobilized for 2 seconds, frontal blocking active!");
    }

    @Override
    public void update(float delta) {
        super.update(delta); // Update cooldown

        // Update active duration
        if (isActive) {
            timer -= delta;
            if (timer <= 0) {
                isActive = false;
                timer = 0;
                System.out.println("[Aegis] Shield Stance ended");
            }
        }
    }

    public boolean isActive() {
        return isActive;
    }

    public float getRemainingDuration() {
        return timer;
    }

    public Vector2 getFacingDirection() {
        return facingDirection;
    }

    public boolean isFrontalAttack(Vector2 attackerPos, Vector2 defenderPos) {
        if (!isActive)
            return false;

        // Calculate direction dari defender ke attacker
        Vector2 attackDirection = new Vector2(
                attackerPos.x - defenderPos.x,
                attackerPos.y - defenderPos.y).nor();

        // Dot product > -0.2 means angle < ~100 degrees (total 200 degrees coverage)
        // User requested "bigger range"
        float dotProduct = facingDirection.dot(attackDirection);

        return dotProduct > -0.2f; // Frontal if angle is less than ~100 degrees
    }

    @Override
    public Skill copy() {
        return new ShieldStanceSkill();
    }
}
