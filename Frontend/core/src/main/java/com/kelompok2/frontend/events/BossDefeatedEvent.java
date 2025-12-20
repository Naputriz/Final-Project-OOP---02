package com.kelompok2.frontend.events;

import com.kelompok2.frontend.entities.Boss;
import com.kelompok2.frontend.skills.Skill;

public class BossDefeatedEvent extends GameEvent {
    private final Boss boss;
    private final String bossName;
    private final Skill ultimateSkill;

    public BossDefeatedEvent(Boss boss, String bossName, Skill ultimateSkill) {
        super();
        this.boss = boss;
        this.bossName = bossName;
        this.ultimateSkill = ultimateSkill;
    }

    public Boss getBoss() {
        return boss;
    }

    public String getBossName() {
        return bossName;
    }

    public Skill getUltimateSkill() {
        return ultimateSkill;
    }
}
