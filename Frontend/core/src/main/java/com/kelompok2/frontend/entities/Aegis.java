package com.kelompok2.frontend.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.kelompok2.frontend.managers.AssetManager;
import com.kelompok2.frontend.skills.ShieldStanceSkill;
import com.kelompok2.frontend.strategies.ShieldBashAttackStrategy;

public class Aegis extends GameCharacter {

    // Animation system
    private Animation<TextureRegion> idleAnimation;
    private float stateTime;

    // Shield Stance skill
    private ShieldStanceSkill shieldStanceSkill;
    private float skillCooldown = 10f;
    private float skillTimer = 0f;

    public Aegis(float x, float y) {
        super(x, y, 170f, 150f); // Speed 170, HP 150 (tank HP)

        // Stats sesuai GDD - Tank role
        this.atk = 15f; // Low ATK (compensated by DEF scaling)
        this.arts = 10f; // Low Arts
        this.def = 40f; // High Defence (main stat untuk Shield Bash)

        // Load placeholder sprite
        Texture spritesheet = AssetManager.getInstance().loadTexture("AegisPlaceholder.png");

        // Setup animation (assuming single frame for now - placeholder)
        // Jika spritesheet adalah animated, adjust accordingly
        TextureRegion[] frames = new TextureRegion[1];
        frames[0] = new TextureRegion(spritesheet);

        idleAnimation = new Animation<>(0.1f, frames);
        idleAnimation.setPlayMode(Animation.PlayMode.LOOP);

        stateTime = 0f;
        this.texture = spritesheet;

        // Setup visual dan hitbox (tank slightly bigger)
        float visualSize = 128f;
        this.renderWidth = visualSize;
        this.renderHeight = visualSize;

        // Hitbox lebih kecil dari visual
        float hitboxWidth = visualSize / 3f;
        float hitboxHeight = visualSize * (2f / 3f);
        this.bounds.setSize(hitboxWidth, hitboxHeight);

        this.boundsOffsetX = (visualSize - hitboxWidth) / 2f;
        this.boundsOffsetY = 0;

        setPosition(x, y);

        // Shield Bash attack strategy (ATK + DEF scaling, forward dash)
        this.attackStrategy = new ShieldBashAttackStrategy();
        this.autoAttack = true; // Hold to spam shield bash
        this.attackCooldown = 1.2f; // Slower than physical attackers (compensated by DEF scaling)

        // Initialize Shield Stance skill
        shieldStanceSkill = new ShieldStanceSkill();

        System.out.println("[Aegis] Created - HP: " + maxHp + ", ATK: " + atk +
                ", DEF: " + def + ", Arts: " + arts);
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        stateTime += delta;

        // Update skill cooldown
        if (skillTimer > 0) {
            skillTimer -= delta;
        }

        // Update Shield Stance skill
        if (shieldStanceSkill != null) {
            shieldStanceSkill.update(delta);
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        // Get current animation frame
        TextureRegion currentFrame = idleAnimation.getKeyFrame(stateTime);

        // Flip sprite based on facing direction
        if (!isFacingRight && !currentFrame.isFlipX()) {
            currentFrame.flip(true, false);
        } else if (isFacingRight && currentFrame.isFlipX()) {
            currentFrame.flip(true, false);
        }

        // Draw character
        batch.draw(currentFrame, position.x, position.y, renderWidth, renderHeight);

        // TODO: Draw shield icon overlay saat Shield Stance aktif
        // if (shieldStanceSkill.isActive()) {
        // Draw ShieldIconPlaceholder.png overlay
        // }
    }

    @Override
    public void takeDamage(float damage, GameCharacter attacker) {
        // Shield Stance blocking logic
        if (shieldStanceSkill != null && shieldStanceSkill.isActive()) {
            // Check if attack is from front (using dot product logic in skill)
            boolean isFrontal = true; // Default true (for projectiles/aoe?)

            // If attacker is known, check direction
            if (attacker != null) {
                isFrontal = shieldStanceSkill.isFrontalAttack(attacker.getPosition(), this.getPosition());
            }

            if (isFrontal) {
                System.out.println("[Aegis] Shield Stance BLOCKED attack from "
                        + (attacker != null ? attacker.getClass().getSimpleName() : "Unknown"));

                // Reflect damage if attacker exists
                if (attacker != null) {
                    float reflectDamage = damage * 0.5f; // 50% reflection
                    attacker.takeDamage(reflectDamage);
                    System.out.println(
                            "[Aegis] REFLECTED " + reflectDamage + " damage to " + attacker.getClass().getSimpleName());
                }

                // Block damage completely (or reduce to 0)
                damage = 0;

                // Visual/Sound effect for block could go here
                return;
            } else {
                System.out.println("[Aegis] Hit from BEHIND/SIDE during stance! Taking full damage.");
            }
        }

        // Balance Fix: Improved DEF scaling for tank role (1.5x instead of 1.0x)
        float mitigatedDamage = damage - (def * 1.5f);
        // Ensure minimum 30% damage is taken (prevent invincibility)
        mitigatedDamage = Math.max(mitigatedDamage, damage * 0.3f);

        super.takeDamage(mitigatedDamage, attacker);
    }

    @Override
    public void performInnateSkill() {
        // Call with facing direction
        Vector2 facing = new Vector2(isFacingRight ? 1 : -1, 0);
        Vector2 targetPos = position.cpy().add(facing.scl(100));
        performInnateSkill(targetPos);
    }

    @Override
    public void performInnateSkill(Vector2 targetPos) {
        // Check cooldown
        if (skillTimer > 0) {
            System.out.println("[Aegis] Shield Stance on cooldown: " +
                    String.format("%.1f", skillTimer) + "s remaining");
            return;
        }

        // Activate Shield Stance skill
        if (shieldStanceSkill != null) {
            shieldStanceSkill.activate(this, targetPos, null, null);
            skillTimer = skillCooldown;
        }
    }

    public ShieldStanceSkill getShieldStanceSkill() {
        return shieldStanceSkill;
    }

    public boolean isImmobilized() {
        return shieldStanceSkill != null && shieldStanceSkill.isActive();
    }

    @Override
    public float getInnateSkillTimer() {
        return skillTimer;
    }

    @Override
    public float getInnateSkillCooldown() {
        return skillCooldown;
    }

    @Override
    public String getAttackAnimationType() {
        return "slash"; // Shield bash uses slash animation
    }
}
