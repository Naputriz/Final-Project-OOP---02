package com.kelompok2.frontend.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import com.kelompok2.frontend.pools.EnemyPool;
import com.kelompok2.frontend.skills.ReturniousPullSkill;
import com.kelompok2.frontend.states.AnimationState;
import com.kelompok2.frontend.states.IdleState;
import com.kelompok2.frontend.states.RunningState;
import com.kelompok2.frontend.strategies.MarkingMeleeAttackStrategy;

import com.kelompok2.frontend.systems.GameFacade;

public class Lumi extends GameCharacter {

    private ReturniousPullSkill innateSkill;

    // Animation state system
    private AnimationState currentState;
    private AnimationState idleState;
    private AnimationState runState;
    private float stateTime;

    // Movement tracking
    private Vector2 previousPosition;
    private boolean isMoving;

    public Lumi(float x, float y) {
        super(x, y, 210f, 90f);

        this.atk = 45f;
        this.arts = 10f;
        this.def = 15f;
        this.title = "The Pale Renegade";
        this.description = "The rogue operator known as 'White Scarf.', now more known as 'The Pale Renegade'. Once a loyal weapon of the Maestra Hunters. In a bid for vengeance against her brother, Ryze, she sacrificed her own arms to wield the Maestra 'Returnious,` a weapon allowing her to pull anything back towards her. But his death brought only a hollow silence, revealing the lies she had been fed. Now, she wanders the world in solitude, hunted by the very organization she once served. Burdened by Maestron poisoning and crushing guilt, she fights not for a cause, but to survive the consequences of her own hatred.";
        this.skillName = "Returnious Pull";
        this.skillDescription = "Marks enemies with attacks. Skill pulls marked enemy + Dmg + Stun. Cooldown: 12s";

        // Initialize Animation States
        // Idle: 2x2 grid, 4 frames
        idleState = new IdleState("Lumi/pcgp-lumi_1.png", 2, 2, 4, 0.15f);

        // Run: 3x4 grid, 10 frames
        runState = new RunningState("Lumi/pcgp-lumi-run.png", 3, 4, 10, 0.1f);

        // Start with idle state
        currentState = idleState;
        currentState.enter(this);
        stateTime = 0f;

        // Initialize movement tracking
        previousPosition = new Vector2(x, y);
        isMoving = false;

        // Setup visual dan hitbox (matching Ryze/Blaze/Aegis pattern)
        float visualSize = 128f;
        this.renderWidth = visualSize;
        this.renderHeight = visualSize;

        // Hitbox lebih kecil dari visual untuk collision yang akurat
        float hitboxWidth = visualSize / 3f;
        float hitboxHeight = visualSize * (2f / 3f);
        this.bounds.setSize(hitboxWidth, hitboxHeight);

        // Offset agar hitbox centered di visual sprite
        this.boundsOffsetX = (visualSize - hitboxWidth) / 2f;
        this.boundsOffsetY = 0;

        setPosition(x, y);

        // Attack Strategy - MarkingMeleeAttackStrategy (Strategy Pattern Extension)
        // Extends MeleeAttackStrategy to add mark application
        // Range 120f and width 100f to match Ryze's attack visual size
        this.attackStrategy = new MarkingMeleeAttackStrategy(120f, 100f, 1.0f, 0.4f);
        this.autoAttack = false; // Manual click for precise marking
        this.attackCooldown = 0.5f;

        // Innate Skill
        this.innateSkill = new ReturniousPullSkill();
        super.innateSkill = this.innateSkill; // Fix shadowing: Set parent field so generic activation works
    }

    @Override
    public void injectDependencies(GameFacade facade, EnemyPool enemyPool) {
        this.innateSkill.setGameFacade(facade);
        this.innateSkill.setEnemyPool(enemyPool);
        System.out.println("[Lumi] Dependencies injected for Returnious Pull");
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        stateTime += delta;

        // Check movement for state transition
        checkMovementState();

        // Update current state (animation)
        currentState.update(this, delta);

        innateSkill.update(delta);
    }

    private void checkMovementState() {
        // Bandingkan posisi sekarang dengan posisi sebelumnya
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

    @Override
    public void render(SpriteBatch batch) {
        // Get current animation frame from state
        TextureRegion currentFrame = currentState.getCurrentFrame(stateTime);

        // Flip logic
        boolean needsFlip = (isFacingRight && !currentFrame.isFlipX()) || (!isFacingRight && currentFrame.isFlipX());
        if (needsFlip) {
            currentFrame.flip(true, false);
        }

        // Set Color explicitly based on status
        batch.setColor(getRenderColor());

        // Draw current frame
        batch.draw(currentFrame, position.x, position.y, renderWidth, renderHeight);

        // Reset color
        batch.setColor(Color.WHITE);
    }

    @Override
    public void performInnateSkill() {
        System.out.println("Lumi needs a target context? No, it finds nearest marked.");
        innateSkill.activate(this, null, null, null);
    }

    @Override
    public float getInnateSkillTimer() {
        return innateSkill.getRemainingCooldown();
    }

    @Override
    public float getInnateSkillCooldown() {
        return innateSkill.getCooldown();
    }

    @Override
    public String getAttackAnimationType() {
        return "slash";
    }
}
