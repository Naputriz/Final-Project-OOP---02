package com.kelompok2.frontend.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.kelompok2.frontend.skills.HellfirePillarSkill;
import com.kelompok2.frontend.strategies.MeleeAttackStrategy;
import com.kelompok2.frontend.managers.AssetManager;
import com.kelompok2.frontend.states.AnimationState;
import com.kelompok2.frontend.states.IdleState;
import com.kelompok2.frontend.states.RunningState;

public class Blaze extends GameCharacter {

    // State Pattern for animations
    private AnimationState currentState;
    private AnimationState idleState;
    private AnimationState runningState;
    private float stateTime = 0f;

    // Movement tracking for state transitions
    private Vector2 lastPosition;

    public Blaze(float x, float y) {
        super(x, y, 180f, 110f); // Moderate speed, Moderate HP

        // Set stats sesuai role Arts Attacker
        this.atk = 25f; // Moderate ATK
        this.arts = 40f; // High Arts - primary damage source
        this.def = 5f; // Low Defence
        this.title = "The Flame Kaiser";
        this.description = "The reawakened Kaiser, rising from the ashes of banishment. Once a supreme ruler imprisoned for his dangerous ambition, he has shattered his chains to ignite a new revolution. Driven by a fury that survived in the dark, he has returned with a singular ultimatum: the world will bow to his control, or he will use his one shot to burn it all down.";
        this.skillName = "Hellfire Pillar";
        this.skillDescription = "Summons a damage pillar at cursor. Damage: High, Cooldown: 5s";

        // Initialize Skill
        this.setInnateSkill(new com.kelompok2.frontend.skills.HellfirePillarSkill());

        // Initialize animation states
        // 4 columns × 23 rows = 92 frames total
        idleState = new IdleState("BlazeCharacterPlaceholder.png", 4, 23, 92, 0.1f);
        runningState = new RunningState("BlazeCharacterPlaceholder.png", 4, 23, 92, 0.08f); // Slightly faster for run

        currentState = idleState;
        currentState.enter(this);

        // Placeholder texture (will use animation frames instead)
        Texture placeholder = AssetManager.getInstance().loadTexture("BlazeCharacterPlaceholder.png");
        this.texture = placeholder;

        // Setup visual dan hitbox
        float visualSize = 128f;
        this.renderWidth = visualSize;
        this.renderHeight = visualSize;

        float hitboxWidth = visualSize / 3f;
        float hitboxHeight = visualSize * (2f / 3f);
        this.bounds.setSize(hitboxWidth, hitboxHeight);
        this.boundsOffsetX = (visualSize - hitboxWidth) / 2f;
        this.boundsOffsetY = 0;

        setPosition(x, y);

        // Flame Punch - Hybrid damage melee attack
        // Flame Punch - Hybrid damage melee attack
        // Increased range to 120f
        this.attackStrategy = new MeleeAttackStrategy(120f, 90f, 1.0f, 0.25f);
        this.autoAttack = true;
        this.attackCooldown = 0.4f;

        lastPosition = new Vector2(x, y);
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        // Update state time
        stateTime += delta;
        currentState.update(this, delta);
        innateSkill.update(delta);

        // Check for movement to transition states
        boolean isMoving = !position.epsilonEquals(lastPosition, 0.1f);

        if (isMoving && currentState != runningState) {
            // Transition to running
            currentState.exit(this);
            currentState = runningState;
            currentState.enter(this);
            stateTime = 0;
        } else if (!isMoving && currentState != idleState) {
            // Transition to idle
            currentState.exit(this);
            currentState = idleState;
            currentState.enter(this);
            stateTime = 0;
        }

        lastPosition.set(position);
    }

    @Override
    public void render(SpriteBatch batch) {
        // Get current frame from state
        TextureRegion currentFrame = currentState.getCurrentFrame(stateTime);

        // Set Color explicitly based on status
        batch.setColor(getRenderColor());

        // Flip sprite based on facing direction
        // Sprites default to facing LEFT, flip when facing RIGHT
        boolean needsFlip = (isFacingRight && !currentFrame.isFlipX()) || (!isFacingRight && currentFrame.isFlipX());
        if (needsFlip) {
            currentFrame.flip(true, false);
        }

        // Draw character sprite
        batch.draw(currentFrame, position.x, position.y, renderWidth, renderHeight);

        // Reset color explicitly
        batch.setColor(com.badlogic.gdx.graphics.Color.WHITE);

        if (innateSkill != null && innateSkill instanceof com.kelompok2.frontend.skills.BaseSkill) {
            ((com.kelompok2.frontend.skills.BaseSkill) innateSkill).render(batch);
        }
    }

    @Override
    public void performInnateSkill() {
        // Default behavior jika tidak ada target position
        Vector2 dir = new Vector2(isFacingRight ? 1 : -1, 0);
        Vector2 targetPos = position.cpy().add(dir.scl(150));
        performInnateSkill(targetPos);
    }

    @Override
    public void performInnateSkill(Vector2 mousePos) {
        if (innateSkill != null) {
            innateSkill.activate(this, mousePos, null, null);
        }
    }

    public boolean isPillarActive() {
        return (innateSkill instanceof HellfirePillarSkill) && ((HellfirePillarSkill) innateSkill).isPillarActive();
    }

    public Vector2 getPillarPosition() {
        if (innateSkill instanceof HellfirePillarSkill) {
            return ((HellfirePillarSkill) innateSkill).getPillarPosition();
        }
        return new Vector2(); // Fallback
    }

    public float getPillarRadius() {
        if (innateSkill instanceof HellfirePillarSkill) {
            return ((HellfirePillarSkill) innateSkill).getPillarRadius();
        }
        return 0f;
    }

    public boolean shouldShowPillarVisual() {
        return (innateSkill instanceof HellfirePillarSkill) && ((HellfirePillarSkill) innateSkill).shouldShowVisual();
    }

    // Getter untuk skill cooldown bar
    public float getSkillTimer() {
        return (innateSkill != null) ? innateSkill.getRemainingCooldown() : 0f;
    }

    public float getSkillCooldown() {
        return (innateSkill != null) ? innateSkill.getCooldown() : 0f;
    }

    @Override
    public float getInnateSkillTimer() {
        return (innateSkill != null) ? innateSkill.getRemainingCooldown() : 0f;
    }

    @Override
    public float getInnateSkillCooldown() {
        return (innateSkill != null) ? innateSkill.getCooldown() : 0f;
    }

    @Override
    public String getAttackAnimationType() {
        return "slash"; // Blaze uses slash animations
    }

    @Override
    public float getAtk() {
        // For Flame Punch, return hybrid damage
        return (this.atk * 0.7f) + (this.arts * 0.3f);
    }
}
