package com.kelompok2.frontend.skills;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.kelompok2.frontend.entities.GameCharacter;
import com.kelompok2.frontend.entities.MeleeAttack;
import com.kelompok2.frontend.entities.Projectile;

public class FeralRushSkill extends BaseSkill {
    private boolean isActive = false;
    private float customDuration = 0.5f; // Total dash duration
    private float timer = 0f;
    private int attackCount = 0;
    private float attackInterval = 0.1f; // 5 attacks in 0.5s
    private float attackTimer = 0f;

    private GameCharacter user;
    private Vector2 dashDirection = new Vector2();
    private Array<MeleeAttack> meleeAttacksRef;

    public FeralRushSkill() {
        super("Feral Rush", "5x scratch in quick succession while dashing forward", 5f);
    }

    @Override
    protected boolean executeSkill(GameCharacter user, Vector2 targetPos, Array<Projectile> projectiles,
            Array<MeleeAttack> meleeAttacks) {
        this.user = user;
        this.meleeAttacksRef = meleeAttacks;
        this.isActive = true;
        this.timer = customDuration;
        this.attackCount = 0;
        this.attackTimer = attackInterval; // Force immediate first attack

        // Determine dash direction towards target (mouse) or facing
        Vector2 center = new Vector2(user.getPosition().x + user.getVisualWidth() / 2,
                user.getPosition().y + user.getVisualHeight() / 2);
        this.dashDirection.set(targetPos).sub(center).nor();

        // Update user facing
        if (dashDirection.x > 0) {
            user.setFacingRight(true);
        } else {
            user.setFacingRight(false);
        }

        System.out.println("[Alice] Feral Rush activated!");
        return true;
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        if (isActive && user != null) {
            timer -= delta;

            // Move user (Dash)
            float dashSpeed = 600f; // High speed dash
            Vector2 dashVelocity = new Vector2(dashDirection).scl(dashSpeed * delta);
            user.setPosition(user.getPosition().x + dashVelocity.x, user.getPosition().y + dashVelocity.y);

            // Spawn attacks
            attackTimer += delta;
            while (attackTimer >= attackInterval && attackCount < 5) {
                attackTimer -= attackInterval;
                spawnAttack();
                attackCount++;
            }

            if (timer <= 0) {
                isActive = false;
                user = null; // Clear reference
            }
        }
    }

    private void spawnAttack() {
        if (user == null || meleeAttacksRef == null)
            return;

        float width = 100f;
        float height = 100f;

        // Damage: 50% of ATK per hit (total 250% if all hit)
        float damage = user.getAtk() * 0.5f;

        float rangeOffset = 60f;
        float spawnX = user.getPosition().x + user.getVisualWidth() / 2 + dashDirection.x * rangeOffset - width / 2;
        float spawnY = user.getPosition().y + user.getVisualHeight() / 2 + dashDirection.y * rangeOffset - height / 2;

        // Rotation
        float angle = dashDirection.angleDeg();

        MeleeAttack attack = new MeleeAttack(spawnX, spawnY, width, height, damage, 0.2f, "scratch", angle);
        meleeAttacksRef.add(attack);
    }

    public boolean isActive() {
        return isActive;
    }

    @Override
    public Skill copy() {
        return new FeralRushSkill();
    }
}
