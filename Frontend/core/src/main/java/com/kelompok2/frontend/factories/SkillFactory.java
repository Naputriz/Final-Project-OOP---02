package com.kelompok2.frontend.factories;

import com.badlogic.gdx.utils.Array;
import com.kelompok2.frontend.skills.*;

public class SkillFactory {

    public enum SkillType {
        FIREBALL, // Arts - ranged projectile
        BLADE_FURY, // Physical - melee AoE spinning
        GROUND_SLAM, // Physical - shockwave with stun
        ICE_SHIELD, // Utility - defense buff
        HEALING_WAVE, // Utility - heal
        WIND_DASH // Utility - mobility dash
    }

    public static Skill createSkill(SkillType type) {
        switch (type) {
            case FIREBALL:
                return new FireballSkill();

            case BLADE_FURY:
                return new BladeFurySkill();

            case GROUND_SLAM:
                return new GroundSlamSkill();

            case ICE_SHIELD:
                return new IceShieldSkill();

            case HEALING_WAVE:
                return new HealingWaveSkill();

            case WIND_DASH:
                return new WindDashSkill();

            default:
                System.err.println("[SkillFactory] Unknown skill type: " + type);
                return null;
        }
    }

    public static Array<Skill> getAllSkills() {
        Array<Skill> skills = new Array<>();

        // Add semua skill types
        for (SkillType type : SkillType.values()) {
            Skill skill = createSkill(type);
            if (skill != null) {
                skills.add(skill);
            }
        }

        return skills;
    }

    public static Skill getRandomSkill() {
        SkillType[] types = SkillType.values();
        int randomIndex = (int) (Math.random() * types.length);
        return createSkill(types[randomIndex]);
    }
}
