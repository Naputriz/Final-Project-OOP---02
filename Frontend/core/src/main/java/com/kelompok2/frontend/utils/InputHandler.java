package com.kelompok2.frontend.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.kelompok2.frontend.entities.GameCharacter;
import com.kelompok2.frontend.entities.MeleeAttack;
import com.kelompok2.frontend.entities.Projectile;
import com.kelompok2.frontend.pools.ProjectilePool;

public class InputHandler {
    private GameCharacter character;
    private Camera camera; // Referensi kamera untuk konversi koordinat mouse
    private ProjectilePool projectilePool; // Pool projectiles (Object Pool Pattern)
    private Array<MeleeAttack> meleeAttacks; // Referensi ke list melee attacks di GameScreen

    // Insanity effect tracking
    private float insanityAttackTimer = 0f;
    private static final float INSANITY_ATTACK_INTERVAL = 0.5f; // Attack every 0.5 seconds when insane
    private java.util.Random random = new java.util.Random();

    public InputHandler(GameCharacter character, Camera camera,
            ProjectilePool projectilePool, Array<MeleeAttack> meleeAttacks) {
        this.character = character;
        this.camera = camera;
        this.projectilePool = projectilePool;
        this.meleeAttacks = meleeAttacks;
    }

    public void update(float delta) {
        // Check if player is insane - modify behavior
        if (character.isInsane()) {
            handleInsaneBehavior(delta);
        } else {
            // Normal behavior
            handleMovement(delta);
            handleDirection();
            handleShooting();
            handleSkills();
        }
    }

    private void handleMovement(float delta) {
        float moveX = 0;
        float moveY = 0;

        if (Gdx.input.isKeyPressed(Input.Keys.W))
            moveY = 1;
        if (Gdx.input.isKeyPressed(Input.Keys.S))
            moveY = -1;
        if (Gdx.input.isKeyPressed(Input.Keys.A))
            moveX = -1;
        if (Gdx.input.isKeyPressed(Input.Keys.D))
            moveX = 1;

        if (moveX != 0 || moveY != 0) {
            // Normalisasi vektor agar jalan diagonal tidak lebih cepat
            float length = (float) Math.sqrt(moveX * moveX + moveY * moveY);
            moveX /= length;
            moveY /= length;

            Vector2 direction = new Vector2(moveX, moveY);
            character.move(direction, delta);
        }
    }

    private void handleDirection() {
        // Ambil posisi mouse di layar
        float mouseX = Gdx.input.getX();
        float mouseY = Gdx.input.getY();

        // Konversi posisi mouse ke koordinat dunia game
        // unproject mengubah (0,0) dari pojok kiri-atas (layar) ke sistem koordinat
        // game (biasanya kiri-bawah)
        Vector3 mousePos = camera.unproject(new Vector3(mouseX, mouseY, 0));

        // 3. Ambil posisi tengah karakter
        float charCenterX = character.getPosition().x + character.getWidth() / 2;

        // 4. Cek posisi mouse relatif terhadap karakter
        if (mousePos.x > charCenterX) {
            character.setFacingRight(true); // Mouse di kanan -> hadap kanan
        } else {
            character.setFacingRight(false); // Mouse di kiri -> hadap kiri
        }
    }

    private void handleShooting() {
        boolean intentToShoot;

        if (character.isAutoAttack()) {
            // Auto (True): button is HELD
            intentToShoot = Gdx.input.isButtonPressed(Input.Buttons.LEFT);
        } else {
            // Manual (False): button is CLICKED
            intentToShoot = Gdx.input.isButtonJustPressed(Input.Buttons.LEFT);
        }

        if (intentToShoot && character.canAttack()) {
            // Mouse position
            Vector3 mousePos3 = camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
            Vector2 target = new Vector2(mousePos3.x, mousePos3.y);

            // Call character attack dengan pool support
            character.attack(target, projectilePool.getActiveProjectiles(), meleeAttacks);

            // Reset cooldown
            character.resetAttackTimer();
        }
    }

    private void handleSkills() {
        // E key - Innate Skill
        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            // Get mouse position in world coordinates
            Vector3 mousePos3 = camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
            Vector2 mousePos = new Vector2(mousePos3.x, mousePos3.y);

            // Call skill with mouse position untuk aiming
            character.performInnateSkill(mousePos);
        }

        // Q key - Secondary Skill
        if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
            if (character.hasSecondarySkill()) {
                // Get mouse position in world coordinates
                Vector3 mousePos3 = camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
                Vector2 mousePos = new Vector2(mousePos3.x, mousePos3.y);

                // Activate secondary skill via Command Pattern
                character.performSecondarySkill(mousePos, projectilePool.getActiveProjectiles(), meleeAttacks);
            }
        }
    }

    private void handleInsaneBehavior(float delta) {
        // Random erratic movement when insane
        float randomX = (random.nextFloat() - 0.5f) * 2f; // -1 to 1
        float randomY = (random.nextFloat() - 0.5f) * 2f; // -1 to 1

        // Normalize
        float length = (float) Math.sqrt(randomX * randomX + randomY * randomY);
        if (length > 0) {
            randomX /= length;
            randomY /= length;
        }

        Vector2 direction = new Vector2(randomX, randomY);
        character.move(direction, delta);

        // Random facing direction
        character.setFacingRight(random.nextBoolean());

        // Auto-attack in random directions periodically
        insanityAttackTimer -= delta;
        if (insanityAttackTimer <= 0 && character.canAttack()) {
            // Attack in random direction
            float angle = random.nextFloat() * 360f;
            float attackX = character.getPosition().x + (float) (Math.cos(Math.toRadians(angle)) * 200);
            float attackY = character.getPosition().y + (float) (Math.sin(Math.toRadians(angle)) * 200);
            Vector2 randomTarget = new Vector2(attackX, attackY);

            character.attack(randomTarget, projectilePool.getActiveProjectiles(), meleeAttacks);
            character.resetAttackTimer();
            insanityAttackTimer = INSANITY_ATTACK_INTERVAL;
        }
    }
}
