package com.kelompok2.frontend.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.kelompok2.frontend.skills.ShieldStanceSkill;
import com.kelompok2.frontend.strategies.ShieldBashAttackStrategy;

public class Aegis extends GameCharacter {

    // Animation system
    private com.kelompok2.frontend.states.AnimationState currentState;
    private com.kelompok2.frontend.states.AnimationState idleState;
    private com.kelompok2.frontend.states.AnimationState runningState;
    private float stateTime;

    // Velocity tracking for animation
    private Vector2 previousPosition;
    private boolean isMoving;

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

        // Initialize Animation States
        // Idle: 2x2, 4 frames
        idleState = new com.kelompok2.frontend.states.IdleState("Aegis/pcgp-aegis.png", 2, 2, 4, 0.15f);
        // Run: 3x4, 10 frames
        runningState = new com.kelompok2.frontend.states.RunningState("Aegis/pcgp-aegis-run.png", 3, 4, 10, 0.1f);

        currentState = idleState;
        currentState.enter(this);
        stateTime = 0f;

        // Initialize movement tracking
        previousPosition = new Vector2(x, y);
        isMoving = false;

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

        // Check movement
        checkMovementState();

        // Update animation state
        currentState.update(this, delta);

        // Update skill cooldown
        if (skillTimer > 0) {
            skillTimer -= delta;
        }

        // Update Shield Stance skill
        if (shieldStanceSkill != null) {
            shieldStanceSkill.update(delta);
        }
    }

    private void checkMovementState() {
        isMoving = !position.epsilonEquals(previousPosition, 0.1f);

        if (isMoving && currentState == idleState) {
            currentState.exit(this);
            currentState = runningState;
            currentState.enter(this);
            stateTime = 0f;
        } else if (!isMoving && currentState == runningState) {
            currentState.exit(this);
            currentState = idleState;
            currentState.enter(this);
            stateTime = 0f;
        }

        previousPosition.set(position);
    }

    @Override
    public void render(SpriteBatch batch) {
        // Get current animation frame
        TextureRegion currentFrame = currentState.getCurrentFrame(stateTime);

        // Set Color explicitly based on status
        batch.setColor(getRenderColor());

        // Flip sprite based on facing direction
        boolean needsFlip = (isFacingRight && !currentFrame.isFlipX()) || (!isFacingRight && currentFrame.isFlipX());
        if (needsFlip) {
            currentFrame.flip(true, false);
        }

        // Draw character
        batch.draw(currentFrame, position.x, position.y, renderWidth, renderHeight);

        // Reset color
        batch.setColor(com.badlogic.gdx.graphics.Color.WHITE);

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

                    // Publish damage event (assuming attacker is BaseEnemy or Boss)
                    if (attacker instanceof com.kelompok2.frontend.entities.BaseEnemy
                            || attacker instanceof com.kelompok2.frontend.entities.Boss) {
                        com.kelompok2.frontend.managers.GameEventManager.getInstance().publish(
                                new com.kelompok2.frontend.events.EnemyDamagedEvent(
                                        (com.kelompok2.frontend.entities.GameCharacter) attacker, reflectDamage, false) // Physical
                                                                                                                        // reflection?
                        );
                    }

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
