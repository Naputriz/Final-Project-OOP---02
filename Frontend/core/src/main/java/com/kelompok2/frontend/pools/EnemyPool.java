package com.kelompok2.frontend.pools;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.kelompok2.frontend.entities.DummyEnemy;
import com.kelompok2.frontend.entities.GameCharacter;

public class EnemyPool {
    // Pool untuk enemy yang tersedia (tidak sedang digunakan)
    private Array<DummyEnemy> availableEnemies;

    // Pool untuk enemy yang sedang aktif (hidup di game)
    private Array<DummyEnemy> activeEnemies;

    // Reference ke player (target untuk semua musuh)
    private GameCharacter target;

    // Ukuran awal pool
    private int initialPoolSize;

    public EnemyPool(GameCharacter target, int initialSize) {
        this.target = target;
        this.initialPoolSize = initialSize;
        this.availableEnemies = new Array<>();
        this.activeEnemies = new Array<>();

        // Pra-alokasi enemies untuk performa
        for (int i = 0; i < initialSize; i++) {
            availableEnemies.add(new DummyEnemy(0, 0, target));
        }

        System.out.println("[EnemyPool] Pool created with " + initialSize + " enemies");
    }

    public DummyEnemy obtain(float x, float y) {
        DummyEnemy enemy;

        if (availableEnemies.size > 0) {
            // Ambil dari pool yang tersedia (reuse)
            enemy = availableEnemies.pop();
            System.out.println("[EnemyPool] Reusing enemy from pool (available: " +
                    availableEnemies.size + ")");
        } else {
            // Pool kosong, buat enemy baru
            enemy = new DummyEnemy(x, y, target);
            System.out.println("[EnemyPool] Pool empty, creating new enemy");
        }

        // Reset state enemy dengan posisi baru
        enemy.reset(x, y, target);

        // Pindahkan ke active pool
        activeEnemies.add(enemy);

        return enemy;
    }

    public void free(DummyEnemy enemy) {
        if (activeEnemies.removeValue(enemy, true)) {
            availableEnemies.add(enemy);
        }
    }

    public void update(float delta) {
        // Gunakan iterator untuk safe removal
        for (int i = activeEnemies.size - 1; i >= 0; i--) {
            DummyEnemy e = activeEnemies.get(i);
            e.update(delta);

            // Jika enemy mati, kembalikan ke pool
            if (e.isDead()) {
                free(e);
                System.out.println("[EnemyPool] Enemy died, returned to pool");
            }
        }
    }

    public void render(SpriteBatch batch) {
        for (DummyEnemy e : activeEnemies) {
            if (!e.isDead()) { // Only render alive enemies
                e.render(batch);
            }
        }
    }

    public Array<DummyEnemy> getActiveEnemies() {
        return activeEnemies;
    }

    public void dispose() {
        for (DummyEnemy e : availableEnemies) {
            e.dispose();
        }
        for (DummyEnemy e : activeEnemies) {
            e.dispose();
        }
        availableEnemies.clear();
        activeEnemies.clear();
        System.out.println("[EnemyPool] Pool disposed");
    }

    public String getPoolStats() {
        return String.format("EnemyPool - Active: %d, Available: %d, Total: %d",
                activeEnemies.size,
                availableEnemies.size,
                activeEnemies.size + availableEnemies.size);
    }
}
