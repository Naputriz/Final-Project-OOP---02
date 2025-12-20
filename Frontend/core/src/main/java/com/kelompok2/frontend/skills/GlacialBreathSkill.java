package com.kelompok2.frontend.skills;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.kelompok2.frontend.entities.GameCharacter;
import com.kelompok2.frontend.entities.GlacialBreath;
import com.kelompok2.frontend.entities.MeleeAttack;
import com.kelompok2.frontend.entities.Projectile;

public class GlacialBreathSkill extends BaseSkill {
    private static final float COOLDOWN = 10f;
    private Array<GlacialBreath> activeBreaths;

    public GlacialBreathSkill() {
        super("Glacial Breath", "Cone freeze attack.", COOLDOWN);
        this.activeBreaths = new Array<>();
    }

    @Override
    protected boolean executeSkill(GameCharacter user, Vector2 targetPos, Array<Projectile> projectiles,
            Array<MeleeAttack> meleeAttacks) {

        float userCenterX = user.getPosition().x + user.getVisualWidth() / 2;
        float userCenterY = user.getPosition().y + user.getVisualHeight() / 2;

        Vector2 direction = new Vector2(
                targetPos.x - userCenterX,
                targetPos.y - userCenterY).nor();

        // Damage calculation (Arts * 0.3f)
        float damage = user.getArts() * 0.3f;

        GlacialBreath breath = new GlacialBreath(user, direction, damage, 0.5f);
        activeBreaths.add(breath);

        System.out.println("[GlacialBreathSkill] Activated!");
        return true;
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        // Update active breaths
        for (int i = activeBreaths.size - 1; i >= 0; i--) {
            GlacialBreath gb = activeBreaths.get(i);
            gb.update(delta);
            if (!gb.isActive()) {
                activeBreaths.removeIndex(i);
            }
        }
    }

    public Array<GlacialBreath> getActiveBreaths() {
        return activeBreaths;
    }

    @Override
    public Skill copy() {
        return new GlacialBreathSkill();
    }
}
