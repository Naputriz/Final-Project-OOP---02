package com.kelompok2.frontend.events;

import com.kelompok2.frontend.entities.Boss;
import com.kelompok2.frontend.skills.Skill;

public class BossDefeatedEvent implements GameEvent {
    private final long timestamp;
    private final Boss boss;
    private final String bossName;
    private final Skill ultimateSkill;

    public BossDefeatedEvent(Boss boss, String bossName, Skill ultimateSkill) {
        this.timestamp = System.currentTimeMillis();
        this.boss = boss;
        this.bossName = bossName;
        this.ultimateSkill = ultimateSkill;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
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
