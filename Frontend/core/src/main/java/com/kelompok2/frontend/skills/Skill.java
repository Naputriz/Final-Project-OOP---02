package com.kelompok2.frontend.skills;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.kelompok2.frontend.entities.GameCharacter;
import com.kelompok2.frontend.entities.MeleeAttack;
import com.kelompok2.frontend.entities.Projectile;

public interface Skill {
    void activate(GameCharacter user, Vector2 targetPos,
            Array<Projectile> projectiles,
            Array<MeleeAttack> meleeAttacks);

    boolean canUse();

    void update(float delta);

    String getName();

    String getDescription();

    float getCooldown();

    float getRemainingCooldown();

    Skill copy();

    default float onOwnerTakeDamage(GameCharacter owner, float amount) {
        return amount;
    }

    // New methods for Visual Hitbox Indicators
    default float getRadius() {
        return 0; // Default radius (0 means no visual indicator or point target)
    }

    default float getRange() {
        return 0; // Default range
    }

    // New method for Character-Skill Combos
    default void onEquip(GameCharacter owner) {
        // Default implementation does nothing
    }
}
