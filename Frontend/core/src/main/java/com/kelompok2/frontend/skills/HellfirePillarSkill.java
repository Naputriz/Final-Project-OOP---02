package com.kelompok2.frontend.skills;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.kelompok2.frontend.entities.GameCharacter;
import com.kelompok2.frontend.entities.MeleeAttack;
import com.kelompok2.frontend.entities.Projectile;
import com.kelompok2.frontend.managers.AssetManager;

public class HellfirePillarSkill extends BaseSkill {
    private static final float COOLDOWN = 5f;
    private static final float DURATION = 2f;
    private static final float RADIUS = 40f;
    private static final float WARNING_DURATION = 0.5f; // Balance Fix: Warning before damage

    private boolean isActive = false;
    private boolean isWarning = false; // Warning phase (no damage yet)
    private float activeTimer = 0f;
    private float warningTimer = 0f;
    private Vector2 pillarPosition;

    private Texture warningTexture;
    private Texture activeTexture;

    public HellfirePillarSkill() {
        super("Hellfire Pillar", "Summons a pillar of fire.", COOLDOWN);
        this.pillarPosition = new Vector2();

        this.warningTexture = AssetManager.getInstance().loadTexture(AssetManager.HELLFIRE_WARNING);
        this.activeTexture = AssetManager.getInstance().loadTexture(AssetManager.HELLFIRE_PILLAR);
    }

    @Override
    public void render(SpriteBatch batch) {
        // Check 1: Is the render method even called?
        System.out.println("Render called on HellfireSkill");

        if (!shouldShowVisual()) {
            return;
        }

        float drawX = pillarPosition.x - RADIUS;
        float drawY = pillarPosition.y - RADIUS;
        float size = RADIUS * 2;

        // Check 2: Where is it trying to draw?
        // If these numbers are huge (e.g., 2000, 2000) or 0,0, checking against your player position helps.
        System.out.println("Drawing Hellfire at: " + drawX + ", " + drawY + " | Warning: " + isWarning + " | Tex: " + (warningTexture != null));

        batch.setColor(Color.WHITE);

        if (isWarning && warningTexture != null) {
            batch.draw(warningTexture, drawX, drawY, size, size);
        }
        else if (isActive && activeTexture != null) {
            batch.draw(activeTexture, drawX, drawY, size, size);
        }
    }
    @Override
    protected boolean executeSkill(GameCharacter user, Vector2 targetPos, Array<Projectile> projectiles,
            Array<MeleeAttack> meleeAttacks) {

        pillarPosition.set(targetPos);
        isWarning = true; // Start with warning phase
        isActive = false; // Not dealing damage yet
        warningTimer = WARNING_DURATION;
        // Don't set activeTimer here - it will be set when warning ends

        System.out.println("[HellfirePillarSkill] Warning phase started at " + targetPos);
        return true;
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        // Warning phase
        if (isWarning) {
            warningTimer -= delta;
            if (warningTimer <= 0) {
                isWarning = false;
                isActive = true; // Now start dealing damage
                activeTimer = DURATION; // Set timer when entering active phase
                System.out.println("[HellfirePillarSkill] Warning ended, pillar now active!");
            }
        }

        // Active damage phase
        if (isActive) {
            activeTimer -= delta;
            if (activeTimer <= 0) {
                isActive = false;
            }
        }
    }

    public boolean isPillarActive() {
        return isActive;
    }

    public Vector2 getPillarPosition() {
        return pillarPosition;
    }

    public float getPillarRadius() {
        return RADIUS;
    }

    public boolean isInWarningPhase() {
        return isWarning;
    }

    // Balance Fix: Show visual during both warning and active phases
    public boolean shouldShowVisual() {
        return isWarning || isActive;
    }

    @Override
    public Skill copy() {
        return new HellfirePillarSkill();
    }
}
