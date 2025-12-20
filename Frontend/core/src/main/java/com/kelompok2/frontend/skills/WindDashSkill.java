package com.kelompok2.frontend.skills;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.kelompok2.frontend.entities.GameCharacter;
import com.kelompok2.frontend.entities.MeleeAttack;
import com.kelompok2.frontend.entities.Projectile;

public class WindDashSkill extends BaseSkill {

    // Invulnerability state
    private boolean invulnerable = false;
    private float invulnerabilityDuration = 0.3f;
    private float invulnerabilityTimer = 0f;

    private GameCharacter dashedUser;

    public WindDashSkill() {
        super("Wind Dash", "Dash 400px + 0.3s invuln", 6f); // 6 second cooldown
    }

    @Override
    protected void executeSkill(GameCharacter user, Vector2 targetPos,
            Array<Projectile> projectiles,
            Array<MeleeAttack> meleeAttacks) {
        // Calculate dash direction and distance
        Vector2 userCenter = new Vector2(
                user.getPosition().x + user.getVisualWidth() / 2,
                user.getPosition().y + user.getVisualHeight() / 2);

        Vector2 direction = new Vector2(targetPos).sub(userCenter);
        float distance = Math.min(direction.len(), 400f); // Max 400 pixels
        direction.nor(); // Normalize

        // Calculate new position
        Vector2 dashTarget = userCenter.cpy().add(direction.scl(distance));

        // Adjust for character visual offset (center to bottom-left)
        float newX = dashTarget.x - user.getVisualWidth() / 2;
        float newY = dashTarget.y - user.getVisualHeight() / 2;

        // Teleport character
        user.setPosition(newX, newY);

        // Activate invulnerability
        invulnerable = true;
        invulnerabilityTimer = invulnerabilityDuration;
        dashedUser = user;

        System.out.println("[Wind Dash] Dashed " + distance + " pixels! Invulnerable for 0.3s");
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        // Update invulnerability
        if (invulnerable) {
            invulnerabilityTimer -= delta;
            if (invulnerabilityTimer <= 0) {
                invulnerable = false;
                invulnerabilityTimer = 0;
                System.out.println("[Wind Dash] Invulnerability ended");
            }
        }
    }

    public boolean isInvulnerable() {
        return invulnerable;
    }

    public float getInvulnerabilityTimeRemaining() {
        return invulnerabilityTimer;
    }

    public GameCharacter getDashedUser() {
        return dashedUser;
    }

    @Override
    public Skill copy() {
        return new WindDashSkill();
    }

    @Override
    public float onOwnerTakeDamage(GameCharacter owner, float amount) {
        if (invulnerable) {
            System.out.println("[Wind Dash] Invulnerable! Damage negated.");
            return 0f;
        }
        return amount;
    }
}
