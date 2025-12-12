package com.kelompok2.frontend.pools;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.kelompok2.frontend.entities.Projectile;

public class ProjectilePool {
    // Pool untuk projectile yang tersedia (tidak sedang digunakan)
    private Array<Projectile> availableProjectiles;

    // Pool untuk projectile yang sedang aktif (sedang terbang)
    private Array<Projectile> activeProjectiles;

    // Ukuran awal pool
    private int initialPoolSize;

    public ProjectilePool(int initialSize) {
        this.initialPoolSize = initialSize;
        this.availableProjectiles = new Array<>();
        this.activeProjectiles = new Array<>();

        // Pra-alokasi projectiles untuk performance
        for (int i = 0; i < initialSize; i++) {
            availableProjectiles.add(new Projectile(0, 0, new Vector2(0, 0), 0));
        }

        System.out.println("[ProjectilePool] Pool created with " + initialSize + " projectiles");
    }

    public Projectile obtain(float x, float y, Vector2 direction, float damage) {
        Projectile projectile;

        if (availableProjectiles.size > 0) {
            // Ambil dari pool yang tersedia (reuse)
            projectile = availableProjectiles.pop();
            System.out.println("[ProjectilePool] Reusing projectile from pool (available: " +
                    availableProjectiles.size + ")");
        } else {
            // Pool kosong, buat projectile baru
            projectile = new Projectile(x, y, direction, damage);
            System.out.println("[ProjectilePool] Pool empty, creating new projectile");
        }

        // Reset state projectile dengan nilai baru
        projectile.reset(x, y, direction, damage);

        // Pindahkan ke active pool
        activeProjectiles.add(projectile);

        return projectile;
    }

    public void free(Projectile projectile) {
        if (activeProjectiles.removeValue(projectile, true)) {
            availableProjectiles.add(projectile);
        }
    }

    public void update(float delta) {
        // Gunakan iterator untuk safe removal
        for (int i = activeProjectiles.size - 1; i >= 0; i--) {
            Projectile p = activeProjectiles.get(i);
            p.update(delta);

            // Jika projectile tidak aktif, kembalikan ke pool
            if (!p.active) {
                free(p);
            }
        }
    }

    public void render(SpriteBatch batch) {
        for (Projectile p : activeProjectiles) {
            p.render(batch);
        }
    }

    public Array<Projectile> getActiveProjectiles() {
        return activeProjectiles;
    }

    public void dispose() {
        for (Projectile p : availableProjectiles) {
            p.dispose();
        }
        for (Projectile p : activeProjectiles) {
            p.dispose();
        }
        availableProjectiles.clear();
        activeProjectiles.clear();
        System.out.println("[ProjectilePool] Pool disposed");
    }

    public String getPoolStats() {
        return String.format("ProjectilePool - Active: %d, Available: %d, Total: %d",
                activeProjectiles.size,
                availableProjectiles.size,
                activeProjectiles.size + availableProjectiles.size);
    }
}
