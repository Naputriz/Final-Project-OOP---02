package com.kelompok2.frontend.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.kelompok2.frontend.entities.GameCharacter;
import com.kelompok2.frontend.entities.Projectile;

public class InputHandler {
    private GameCharacter character;
    private Camera camera; // Referensi kamera untuk konversi koordinat mouse
    private Array<Projectile> projectiles; // Referensi ke list peluru di GameScreen


    public InputHandler(GameCharacter character, Camera camera, Array<Projectile> projectiles) {
        this.character = character;
        this.camera = camera;
        this.projectiles = projectiles;
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

        if (Gdx.input.isKeyPressed(Input.Keys.W)) moveY = 1;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) moveY = -1;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) moveX = -1;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) moveX = 1;

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
        // unproject mengubah (0,0) dari pojok kiri-atas (layar) ke sistem koordinat game (biasanya kiri-bawah)
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
        // Klik Kiri untuk menembak
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            // Ambil posisi mouse
            Vector3 mousePos = camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));

            // Titik awal peluru (dari tengah badan karakter)
            float startX = character.getPosition().x + character.getWidth() / 2;
            float startY = character.getPosition().y + character.getHeight() / 2;

            // Spawn peluru baru dan masukkan ke list
            Projectile p = new Projectile(startX, startY, mousePos.x, mousePos.y);
            projectiles.add(p);
        }
    }

    private void handleSkills() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            character.performInnateSkill();
        }
    }
}
