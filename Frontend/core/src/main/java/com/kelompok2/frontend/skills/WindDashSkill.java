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

    private boolean comboActive = false;

    @Override
    protected boolean executeSkill(GameCharacter user, Vector2 targetPos,
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

        // Combo: Spawn circular slash attack at destination
        if (comboActive && meleeAttacks != null) {
            float attackSize = 200f; // Large area
            float damage = user.getArts() * 2.5f; // High damage

            MeleeAttack attack = new MeleeAttack(
                    dashTarget.x - attackSize / 2,
                    dashTarget.y - attackSize / 2,
                    attackSize,
                    attackSize,
                    damage,
                    0.2f, // Very short duration
                    "slash",
                    0f,
                    false, // No mark
                    true // Is Arts damage
            );
            attack.setVisible(false); // Invisible damage source
            meleeAttacks.add(attack);
            System.out.println("[Wind Dash] Combo Slash activated! Damage: " + damage);
        }

        System.out.println("[Wind Dash] Dashed " + distance + " pixels! Invulnerable for 0.3s");
        return true;
    }

    @Override
    public void onEquip(GameCharacter owner) {
        // Bonus for Whisperwind: Explosive arrival
        if (owner instanceof com.kelompok2.frontend.entities.Whisperwind) {
            this.comboActive = true;
            this.description = "Dash 400px + Arrival Damage (Arts x 2.5) - WHISPERWIND COMBO!";
            System.out.println("[Wind Dash] Combo activated for Whisperwind! Arrival damage enabled.");
        }
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
        WindDashSkill copy = new WindDashSkill();
        copy.comboActive = this.comboActive;
        copy.description = this.description;
        return copy;
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
