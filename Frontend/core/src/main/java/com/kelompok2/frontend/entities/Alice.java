package com.kelompok2.frontend.entities;

import com.badlogic.gdx.math.Vector2;
import com.kelompok2.frontend.skills.FeralRushSkill;
import com.kelompok2.frontend.strategies.MeleeAttackStrategy;
import com.kelompok2.frontend.systems.GameFacade;
import com.kelompok2.frontend.pools.EnemyPool;

public class Alice extends GameCharacter {

    // Animation state system
    private com.kelompok2.frontend.states.AnimationState currentState;
    private com.kelompok2.frontend.states.AnimationState idleState;
    private com.kelompok2.frontend.states.AnimationState runState;
    private float stateTime;

    // Movement tracking
    private Vector2 previousPosition;
    private boolean isMoving;

    // Innate Skill
    private FeralRushSkill feralRushSkill;

    // Dependencies
    private GameFacade gameFacade;

    public Alice(float x, float y) {
        super(x, y, 200f, 100f); // Speed 200 (High), HP 100 (Moderate)

        // Stats according to GDD - Physical Attacker
        this.atk = 40f; // High ATK
        this.arts = 10f; // Low Arts
        this.def = 10f; // Low Defence
        this.title = "The Reckless Princess";
        this.description = "The 'Reckless Princess' of Lumina. Once defined solely by her impulsive spirit and refusal to adhere to royal tradition, her life was shattered by the ambush that took her mother and brother. Now, she channels her grief into a singular, aggressive purpose: hunting down the 'White Scarf' for revenge";
        this.skillName = "Feral Rush";
        this.skillDescription = "Dashes forward rapidly and unleashes 5x scratch attacks. Cooldown: 5s";

        // Initialize Animation States
        // Idle: 2x2 grid, 4 frames
        idleState = new com.kelompok2.frontend.states.IdleState("Alice/pcgp-alice.png", 2, 2, 4, 0.1f);

        // Run: 2x3 grid, 6 frames
        runState = new com.kelompok2.frontend.states.RunningState("Alice/pcgp-alice_run.png", 2, 3, 6, 0.1f);

        // Start with idle state
        currentState = idleState;
        currentState.enter(this);
        stateTime = 0f;

        // Initialize movement tracking
        previousPosition = new Vector2(x, y);
        isMoving = false;

        // Visual and Hitbox setup
        float visualSize = 128f;
        this.renderWidth = visualSize;
        this.renderHeight = visualSize;

        // Hitbox smaller than visual
        float hitboxWidth = visualSize / 3f;
        float hitboxHeight = visualSize * (2f / 3f);
        this.bounds.setSize(hitboxWidth, hitboxHeight);

        this.boundsOffsetX = (visualSize - hitboxWidth) / 2f;
        this.boundsOffsetY = 0;

        setPosition(x, y);

        // Attack Strategy: Scratch (Melee)
        this.attackStrategy = new MeleeAttackStrategy(120f, 100f, 1.0f, 0.4f);
        this.autoAttack = true; // Auto attack enabled
        this.attackCooldown = 0.4f;

        // Initialize Skill
        feralRushSkill = new FeralRushSkill();

        System.out.println("[Alice] Created - HP: " + maxHp + ", ATK: " + atk +
                ", DEF: " + def + ", Speed: " + speed);
    }

    @Override
    public void injectDependencies(GameFacade facade, EnemyPool enemyPool) {
        this.gameFacade = facade;
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        stateTime += delta;

        // Check movement for state transition
        checkMovementState();

        // Update animation state
        currentState.update(this, delta);

        // Update skill
        if (feralRushSkill != null) {
            feralRushSkill.update(delta);
        }
    }

    private void checkMovementState() {
        // Compare current position with previous position
        isMoving = !position.epsilonEquals(previousPosition, 0.1f);

        // Transition states
        if (isMoving && currentState == idleState) {
            // Idle -> Run
            currentState.exit(this);
            currentState = runState;
            currentState.enter(this);
            stateTime = 0f;
        } else if (!isMoving && currentState == runState) {
            // Run -> Idle
            currentState.exit(this);
            currentState = idleState;
            currentState.enter(this);
            stateTime = 0f;
        }

        // Update previous position
        previousPosition.set(position);
    }

    // Override move to block control during basic dash skill
    @Override
    public void move(Vector2 direction, float delta) {
        if (feralRushSkill != null && feralRushSkill.isActive()) {
            return; // Block input movement during dash
        }
        super.move(direction, delta);
    }

    @Override
    public void render(com.badlogic.gdx.graphics.g2d.SpriteBatch batch) {
        // Get current animation frame
        com.badlogic.gdx.graphics.g2d.TextureRegion currentFrame = currentState.getCurrentFrame(stateTime);

        // Set Color explicitly based on status
        batch.setColor(getRenderColor());

        // Flip sprite based on facing direction
        // Assuming sprite faces LEFT by default (like others), flip if facing RIGHT
        boolean needsFlip = (isFacingRight && !currentFrame.isFlipX()) || (!isFacingRight && currentFrame.isFlipX());
        if (needsFlip) {
            currentFrame.flip(true, false);
        }

        // Draw character
        batch.draw(currentFrame, position.x, position.y, renderWidth, renderHeight);

        // Reset color
        batch.setColor(com.badlogic.gdx.graphics.Color.WHITE);
    }

    @Override
    public void performInnateSkill() {
        // Default forward
        Vector2 facing = new Vector2(isFacingRight ? 1 : -1, 0);
        Vector2 targetPos = position.cpy().add(facing.scl(200));
        performInnateSkill(targetPos);
    }

    @Override
    public void performInnateSkill(Vector2 targetPos) {
        if (feralRushSkill.getRemainingCooldown() > 0) {
            System.out.println("[Alice] Feral Rush on cooldown");
            return;
        }

        if (gameFacade != null) {
            // Activate skill passing meleeAttacks from facade
            feralRushSkill.activate(this, targetPos, null, gameFacade.getPlayerMeleeAttacks());
        } else {
            System.err.println("[Alice] GameFacade not injected! Cannot activate skill.");
        }
    }

    @Override
    public float getInnateSkillTimer() {
        return feralRushSkill.getRemainingCooldown();
    }

    @Override
    public float getInnateSkillCooldown() {
        return feralRushSkill.getCooldown();
    }

    @Override
    public String getAttackAnimationType() {
        return "scratch"; // Alice uses scratch
    }

    @Override
    public void takeDamage(float damage, GameCharacter attacker) {
        if (feralRushSkill != null && feralRushSkill.isActive()) {
            System.out.println("[Alice] Dodged damage during Feral Rush!");
            return;
        }
        super.takeDamage(damage, attacker);
    }
}
