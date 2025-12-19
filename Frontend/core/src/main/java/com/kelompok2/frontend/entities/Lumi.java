package com.kelompok2.frontend.entities;

import com.kelompok2.frontend.managers.AssetManager;
import com.kelompok2.frontend.pools.EnemyPool;
import com.kelompok2.frontend.skills.ReturniousPullSkill;
import com.kelompok2.frontend.strategies.MarkingMeleeAttackStrategy;

import com.kelompok2.frontend.systems.GameFacade;

public class Lumi extends GameCharacter {

    private ReturniousPullSkill innateSkill;

    public Lumi(float x, float y) {
        super(x, y, 210f, 90f);

        this.atk = 45f;
        this.arts = 10f;
        this.def = 15f;

        // Load asset placeholder
        this.texture = AssetManager.getInstance().loadTexture("LumiPlaceholder.png");

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
    }

    public void setEnemyPool(EnemyPool enemyPool) {
        this.innateSkill.setEnemyPool(enemyPool);
    }

    public void setGameFacade(GameFacade gameFacade) {
        this.innateSkill.setGameFacade(gameFacade);
    }

    @Override
    public void performInnateSkill() {
        System.out.println("Lumi needs a target context? No, it finds nearest marked.");
        innateSkill.activate(this, null, null, null);
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        innateSkill.update(delta);
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
