package com.kelompok2.frontend.events;

import com.kelompok2.frontend.entities.Boss;
import com.badlogic.gdx.math.Vector2;

public class BossSpawnedEvent implements GameEvent {
    private final long timestamp;
    private final Boss boss;
    private final String bossName;
    private final Vector2 spawnPosition;

    public BossSpawnedEvent(Boss boss, String bossName, Vector2 spawnPosition) {
        this.timestamp = System.currentTimeMillis();
        this.boss = boss;
        this.bossName = bossName;
        this.spawnPosition = new Vector2(spawnPosition);
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
    public Vector2 getSpawnPosition() {
        return spawnPosition;
    }
}
