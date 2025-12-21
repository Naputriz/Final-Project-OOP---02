package com.kelompok2.frontend.effects;

import com.kelompok2.frontend.entities.GameCharacter;
import com.kelompok2.frontend.skills.Skill;

public class NewSkillEffect implements LevelUpEffect {

    private Skill skillToAssign;

    public NewSkillEffect(Skill skill) {
        this.skillToAssign = skill;
    }

    @Override
    public void apply(GameCharacter character) {
        // Assign skill copy ke character (setiap character harus punya instance sendiri)
        Skill newSkill = skillToAssign.copy();

        // Trigger generic combo check inside the skill itself
        newSkill.onEquip(character);

        character.setSecondarySkill(newSkill);
        System.out.println("[NewSkillEffect] Learned new skill: " + newSkill.getName());
    }

    @Override
    public String getName() {
        // Show specific skill name sehingga player tau skill apa yang akan didapat
        return skillToAssign.getName();
    }

    @Override
    public String getDescription() {
        // Show specific skill description
        return skillToAssign.getDescription();
    }
}
