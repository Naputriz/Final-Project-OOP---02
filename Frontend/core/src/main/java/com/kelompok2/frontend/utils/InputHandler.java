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

public class InputHandler {
    private GameCharacter character;
    private Camera camera; // Referensi kamera untuk konversi koordinat mouse
    private Array<Projectile> projectiles; // Referensi ke list peluru di GameScreen
    private Array<MeleeAttack> meleeAttacks; // Referensi ke list melee attacks di GameScreen

    public InputHandler(GameCharacter character, Camera camera,
            Array<Projectile> projectiles, Array<MeleeAttack> meleeAttacks) {
        this.character = character;
        this.camera = camera;
        this.projectiles = projectiles;
        this.meleeAttacks = meleeAttacks;
    }

    public void update(float delta) {
        handleMovement(delta);
        handleDirection();
        handleShooting();
        handleSkills();
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

            character.move(moveX * delta, moveY * delta);
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

            // Call character attack dengan melee attacks support
            character.attack(target, projectiles, meleeAttacks);

            // Reset cooldown
            character.resetAttackTimer();
        }
    }

    private void handleSkills() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            character.performInnateSkill();
        }
    }
}
