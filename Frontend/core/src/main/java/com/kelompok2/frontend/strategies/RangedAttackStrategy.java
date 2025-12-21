package com.kelompok2.frontend.strategies;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.kelompok2.frontend.entities.GameCharacter;
import com.kelompok2.frontend.entities.MeleeAttack;
import com.kelompok2.frontend.entities.Projectile;

public class RangedAttackStrategy implements AttackStrategy {
    private float damageMultiplier; // Multiplier untuk damage (akan dikalikan dengan Arts)
    private Color projectileColor; // Warna projectile (optional)
    private float projectileSpeed; // ✅ FIX: Customizable projectile speed
    private Texture projectileTexture; // Custom Projectile Texture

    // Constructor: Custom Texture (For characters with sprite projectiles)
    public RangedAttackStrategy(float damageMultiplier, float speed, Texture texture) {
        this.damageMultiplier = damageMultiplier;
        this.projectileSpeed = speed;
        this.projectileTexture = texture;
        this.projectileColor = Color.WHITE; // Default white so texture isn't tinted weirdly
    }

    // Constructor: Custom Speed & Color (For Bosses/Specific Mobs)
    public RangedAttackStrategy(float damageMultiplier, Color color, float speed) {
        this.damageMultiplier = damageMultiplier;
        this.projectileColor = color;
        this.projectileSpeed = speed;
        this.projectileTexture = null;
    }

    // Constructor: Default Speed
    public RangedAttackStrategy(float damageMultiplier, Color color) {
        this(damageMultiplier, color, 400f);
    }

    // Constructor: Default Color
    public RangedAttackStrategy(float damageMultiplier) {
        this(damageMultiplier, Color.YELLOW);
    }

    // Constructor: Default All (Multiplier 1.0)
    public RangedAttackStrategy() {
        this(1.0f);
    }

    @Override
    public void execute(GameCharacter attacker, Vector2 targetPos,
            Array<Projectile> projectiles, Array<MeleeAttack> meleeAttacks) {

        // Hitung posisi spawn projectile (tengah karakter)
        float startX = attacker.getPosition().x + attacker.getVisualWidth() / 2;
        float startY = attacker.getPosition().y + attacker.getVisualHeight() / 2;

        // Hitung damage berdasarkan Arts karakter
        float finalDamage = attacker.getArts() * damageMultiplier;

        // ✅ FIX: Buat Projectile baru dengan damage, warna, DAN speed custom
        Projectile p = new Projectile(startX, startY, targetPos.x, targetPos.y, finalDamage, projectileColor,
                projectileSpeed);

        if (projectileTexture != null) {
            p.setTexture(projectileTexture); // Ensure Projectile.java has setTexture()
        }

        // Set ownership
        p.setEnemyProjectile(!attacker.isPlayerCharacter());

        // Make ALL player ranged attacks pierce (User Request)
        if (attacker.isPlayerCharacter()) {
            p.setPiercing(true);
        }

        projectiles.add(p);
    }
}
