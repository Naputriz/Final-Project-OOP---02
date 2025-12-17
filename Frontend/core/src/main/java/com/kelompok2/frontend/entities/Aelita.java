package com.kelompok2.frontend.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.kelompok2.frontend.managers.AssetManager;
import com.kelompok2.frontend.strategies.RangedAttackStrategy;
import com.kelompok2.frontend.skills.VerdantDomainSkill;

public class Aelita extends GameCharacter {

    private VerdantDomainSkill verdantDomain;

    float atkBuffMultiplier = 1.0f;
    float artsBuffMultiplier = 1.0f;

    public Aelita(float x, float y) {
        super(x, y, 170f, 140f); // Speed: 170, HP: 140
        this.atk = 15f;
        this.arts = 30f;
        this.def = 20f;

        // Load texture (placeholder)
        this.texture = AssetManager.getInstance().loadTexture("AelitaPlaceholder.png");

        // Ukuran visual dan hitbox
        float visualSize = 128f;
        this.renderWidth = visualSize;
        this.renderHeight = visualSize;

        float hitboxWidth = visualSize / 3f;
        float hitboxHeight = visualSize * (2f / 3f);
        this.bounds.setSize(hitboxWidth, hitboxHeight);

        this.boundsOffsetX = (visualSize - hitboxWidth) / 2f;
        this.boundsOffsetY = 0;

        setPosition(x, y);

        // Ranged attack strategy with GREEN projectiles
        this.attackStrategy = new RangedAttackStrategy(
                1.0f, // damage multiplier (Arts × 1.0)
                Color.GREEN // ✨ Green projectiles for healer aesthetic
        );
        this.attackCooldown = 0.7f;
        this.autoAttack = true; // Click-to-attack

        // Create innate skill
        this.verdantDomain = new VerdantDomainSkill();

        System.out.println("[Aelita] Healer initialized - HP: " + this.maxHp +
                ", ATK: " + this.atk + ", Arts: " + this.arts);
    }

    @Override
    public void performInnateSkill() {
        // Verdant Domain activates at player's center position
        Vector2 playerCenter = new Vector2(
                position.x + getVisualWidth() / 2,
                position.y + getVisualHeight() / 2);
        performInnateSkill(playerCenter);
    }

    @Override
    public void performInnateSkill(Vector2 targetPos) {
        verdantDomain.activate(this, targetPos, new Array<>(), new Array<>());
    }

    @Override
    public float getInnateSkillTimer() {
        return verdantDomain.getRemainingCooldown();
    }

    @Override
    public float getInnateSkillCooldown() {
        return verdantDomain.getCooldown();
    }

    @Override
    public String getAttackAnimationType() {
        return "slash"; // Default animation type
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        verdantDomain.update(delta);
    }

    // Override getAtk to apply Verdant Domain buff
    @Override
    public float getAtk() {
        return super.getAtk() * atkBuffMultiplier;
    }

    // Override getArts to apply Verdant Domain buff
    @Override
    public float getArts() {
        return super.getArts() * artsBuffMultiplier;
    }

    // Apply Verdant Domain buff (called by GameScreen when player is in zone)
    public void applyVerdantBuff(float atkMult, float artsMult) {
        this.atkBuffMultiplier = atkMult;
        this.artsBuffMultiplier = artsMult;
    }

    // Clear Verdant Domain buff (called when player leaves zone or zone expires)
    public void clearVerdantBuff() {
        this.atkBuffMultiplier = 1.0f;
        this.artsBuffMultiplier = 1.0f;
    }

    // Getter for Verdant Domain skill (used by GameScreen for zone rendering and
    // logic)
    public VerdantDomainSkill getVerdantDomain() {
        return verdantDomain;
    }

    // Getters for buff state (used for debug logging)
    public float getAtkBuffMultiplier() {
        return atkBuffMultiplier;
    }

    public float getArtsBuffMultiplier() {
        return artsBuffMultiplier;
    }
}
