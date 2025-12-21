package com.kelompok2.frontend.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.kelompok2.frontend.Main;
import com.kelompok2.frontend.entities.GameCharacter;
import com.kelompok2.frontend.entities.MeleeAttack;
import com.kelompok2.frontend.pools.ProjectilePool;

public class InputHandler {
    private Main game; // [BARU]
    private GameCharacter character;
    private Camera camera;
    private ProjectilePool projectilePool;
    private Array<MeleeAttack> meleeAttacks;

    private float insanityAttackTimer = 0f;
    private static final float INSANITY_ATTACK_INTERVAL = 0.5f;
    private java.util.Random random = new java.util.Random();

    public InputHandler(Main game, GameCharacter character, Camera camera,
                        ProjectilePool projectilePool, Array<MeleeAttack> meleeAttacks) {
        this.game = game;
        this.character = character;
        this.camera = camera;
        this.projectilePool = projectilePool;
        this.meleeAttacks = meleeAttacks;
    }

    public void update(float delta) {
        if (character.isInsane()) {
            handleInsaneBehavior(delta);
            return;
        }
        if (character instanceof com.kelompok2.frontend.entities.Aegis) {
            if (((com.kelompok2.frontend.entities.Aegis) character).isImmobilized()) return;
        }

        handleMovement(delta);
        handleDirection();
        handleShooting();
        handleSkills();
    }

    private void handleMovement(float delta) {
        float moveX = 0; float moveY = 0;

        // [UBAH] Gunakan variabel dari Main
        KeyBindings keys = game.getKeys();

        if (Gdx.input.isKeyPressed(keys.moveUp)) moveY = 1;
        if (Gdx.input.isKeyPressed(keys.moveDown)) moveY = -1;
        if (Gdx.input.isKeyPressed(keys.moveLeft)) moveX = -1;
        if (Gdx.input.isKeyPressed(keys.moveRight)) moveX = 1;

        if (moveX != 0 || moveY != 0) {
            float length = (float) Math.sqrt(moveX * moveX + moveY * moveY);
            moveX /= length;
            moveY /= length;
            character.move(new Vector2(moveX, moveY), delta);
        }
    }

    private void handleDirection() {
        float mouseX = Gdx.input.getX();
        float mouseY = Gdx.input.getY();
        Vector3 mousePos = camera.unproject(new Vector3(mouseX, mouseY, 0));
        float charCenterX = character.getPosition().x + character.getWidth() / 2;
        character.setFacingRight(mousePos.x > charCenterX);
    }

    private void handleShooting() {
        boolean intentToShoot = character.isAutoAttack() ? Gdx.input.isButtonPressed(Input.Buttons.LEFT) : Gdx.input.isButtonJustPressed(Input.Buttons.LEFT);
        if (intentToShoot && character.canAttack()) {
            Vector3 mousePos3 = camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
            character.attack(new Vector2(mousePos3.x, mousePos3.y), projectilePool.getActiveProjectiles(), meleeAttacks);
            character.resetAttackTimer();
        }
    }

    private void handleSkills() {
        KeyBindings keys = game.getKeys(); // [UBAH]

        // Innate
        if (Gdx.input.isKeyJustPressed(keys.innateSkill)) {
            Vector3 m = camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
            character.performInnateSkill(new Vector2(m.x, m.y));
        }

        // Secondary
        if (Gdx.input.isKeyJustPressed(keys.secondarySkill) && character.hasSecondarySkill()) {
            Vector3 m = camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
            character.performSecondarySkill(new Vector2(m.x, m.y), projectilePool.getActiveProjectiles(), meleeAttacks);
        }

        // Ultimate
        if (character.hasUltimateSkill()) {
            if (Gdx.input.isKeyJustPressed(keys.ultimateSkill)) {
                character.setAimingUltimate(true);
            } else if (character.isAimingUltimate()) {
                if (Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT)) {
                    character.setAimingUltimate(false);
                    return;
                }
                // Check released (not pressed anymore)
                if (!Gdx.input.isKeyPressed(keys.ultimateSkill)) {
                    character.setAimingUltimate(false);
                    Vector3 m = camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
                    character.performUltimateSkill(new Vector2(m.x, m.y), projectilePool.getActiveProjectiles(), meleeAttacks);
                }
            }
        }
    }

    private void handleInsaneBehavior(float delta) {
        float rx = (random.nextFloat() - 0.5f) * 2f;
        float ry = (random.nextFloat() - 0.5f) * 2f;
        float len = (float) Math.sqrt(rx * rx + ry * ry);
        if (len > 0) { rx /= len; ry /= len; }
        character.move(new Vector2(rx, ry), delta);
        character.setFacingRight(random.nextBoolean());

        insanityAttackTimer -= delta;
        if (insanityAttackTimer <= 0 && character.canAttack()) {
            float angle = random.nextFloat() * 360f;
            float ax = character.getPosition().x + (float) (Math.cos(Math.toRadians(angle)) * 200);
            float ay = character.getPosition().y + (float) (Math.sin(Math.toRadians(angle)) * 200);
            character.attack(new Vector2(ax, ay), projectilePool.getActiveProjectiles(), meleeAttacks);
            character.resetAttackTimer();
            insanityAttackTimer = INSANITY_ATTACK_INTERVAL;
        }
    }
}
