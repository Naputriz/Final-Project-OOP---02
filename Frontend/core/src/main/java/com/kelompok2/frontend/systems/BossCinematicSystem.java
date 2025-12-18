package com.kelompok2.frontend.systems;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.kelompok2.frontend.entities.DummyEnemy;
import com.kelompok2.frontend.entities.GameCharacter;
import com.kelompok2.frontend.managers.AudioManager;
import com.kelompok2.frontend.pools.EnemyPool;

public class BossCinematicSystem {
    // Camera panning state
    private boolean cameraPanningToBoss = false;
    private float cameraPanTimer = 0f;

    // Camera pan timing
    private static final float CAMERA_PAN_TO_BOSS_DURATION = 1.0f; // Pan to boss
    private static final float CAMERA_HOLD_DURATION = 3.0f; // Hold on boss
    private static final float CAMERA_PAN_BACK_DURATION = 1.0f; // Pan back to player
    private static final float CAMERA_PAN_DURATION = CAMERA_PAN_TO_BOSS_DURATION + CAMERA_HOLD_DURATION
            + CAMERA_PAN_BACK_DURATION; // Total: 5 seconds

    // Camera state
    private Vector2 originalCameraPos = new Vector2();
    private Vector2 targetCameraPos = new Vector2();
    private float originalCameraZoom = 1f;
    private static final float BOSS_ZOOM_LEVEL = 0.5f; // Zoom in MORE (smaller = closer)

    // References (injected)
    private GameCharacter player;
    private EnemyPool enemyPool;

    public BossCinematicSystem() {
    }

    public void initialize(GameCharacter player, EnemyPool enemyPool) {
        this.player = player;
        this.enemyPool = enemyPool;
    }

    public void startBossPanSequence(Vector2 bossPosition, OrthographicCamera camera) {
        cameraPanningToBoss = true;
        cameraPanTimer = 0f;

        // Save original camera state
        originalCameraZoom = camera.zoom;

        // Store original camera position (player position)
        originalCameraPos.set(
                player.getPosition().x + player.getVisualWidth() / 2,
                player.getPosition().y + player.getVisualHeight() / 2);

        // Set target to boss position
        targetCameraPos.set(bossPosition);

        // Freeze player during camera pan (same duration as enemies)
        player.freeze(CAMERA_PAN_DURATION);

        // Freeze all enemies during camera pan (for full duration)
        for (DummyEnemy enemy : enemyPool.getActiveEnemies()) {
            enemy.freeze(CAMERA_PAN_DURATION);
        }

        System.out.println("[Camera] Starting boss pan sequence");
        System.out.println(
                "[Camera] From: (" + originalCameraPos.x + ", " + originalCameraPos.y + ") zoom=" + originalCameraZoom);
        System.out
                .println("[Camera] To: (" + targetCameraPos.x + ", " + targetCameraPos.y + ") zoom=" + BOSS_ZOOM_LEVEL);
    }

    public void updateCameraPan(float delta, OrthographicCamera camera) {
        if (!cameraPanningToBoss) {
            return;
        }

        // Cap delta to prevent instant completion (max 33ms per frame = 30 FPS)
        float cappedDelta = Math.min(delta, 0.033f);

        cameraPanTimer += cappedDelta;

        // Phase 1: Pan to boss (0.0 - 1.0 seconds)
        if (cameraPanTimer < CAMERA_PAN_TO_BOSS_DURATION) {
            float phaseProgress = cameraPanTimer / CAMERA_PAN_TO_BOSS_DURATION;
            // Smooth interpolation (ease-in-out)
            float smoothProgress = phaseProgress * phaseProgress * (3f - 2f * phaseProgress);

            camera.position.x = originalCameraPos.x + (targetCameraPos.x - originalCameraPos.x) * smoothProgress;
            camera.position.y = originalCameraPos.y + (targetCameraPos.y - originalCameraPos.y) * smoothProgress;
            camera.zoom = originalCameraZoom + (BOSS_ZOOM_LEVEL - originalCameraZoom) * smoothProgress;
        }
        // Phase 2: Hold on boss (1.0 - 4.0 seconds)
        else if (cameraPanTimer < CAMERA_PAN_TO_BOSS_DURATION + CAMERA_HOLD_DURATION) {
            // Keep camera focused on boss
            camera.position.x = targetCameraPos.x;
            camera.position.y = targetCameraPos.y;
            camera.zoom = BOSS_ZOOM_LEVEL;
        }
        // Phase 3: Pan back to player (4.0 - 5.0 seconds)
        else if (cameraPanTimer < CAMERA_PAN_DURATION) {
            float phaseProgress = (cameraPanTimer - CAMERA_PAN_TO_BOSS_DURATION - CAMERA_HOLD_DURATION)
                    / CAMERA_PAN_BACK_DURATION;
            // Smooth interpolation (ease-in-out)
            float smoothProgress = phaseProgress * phaseProgress * (3f - 2f * phaseProgress);

            camera.position.x = targetCameraPos.x + (originalCameraPos.x - targetCameraPos.x) * smoothProgress;
            camera.position.y = targetCameraPos.y + (originalCameraPos.y - targetCameraPos.y) * smoothProgress;
            camera.zoom = BOSS_ZOOM_LEVEL + (originalCameraZoom - BOSS_ZOOM_LEVEL) * smoothProgress;
        }
        // Phase 4: Complete
        else {
            cameraPanningToBoss = false;
            camera.position.x = originalCameraPos.x;
            camera.position.y = originalCameraPos.y;
            camera.zoom = originalCameraZoom; // Ensure exact restoration

            // Explicitly unfreeze all entities to prevent lingering freeze effects
            player.clearFreeze();
            for (DummyEnemy enemy : enemyPool.getActiveEnemies()) {
                enemy.setFrozen(false); // Force unfreeze
            }

            System.out.println("[Camera] Boss pan sequence complete - all entities unfrozen");
        }

        camera.update();
    }

    public void playBossMusic(String bossName) {
        String musicPath;

        switch (bossName) {
            case "Insania":
                musicPath = "Audio/BossThemes/InsaniaBossTheme.mp3";
                break;
            case "Blaze":
                musicPath = "Audio/BossThemes/BlazeBossTheme.mp3";
                break;
            case "Isolde":
                musicPath = "Audio/BossThemes/IsoldeBossTheme.mp3";
                break;
            default:
                musicPath = "Audio/battleThemeA.mp3"; // Fallback
                break;
        }

        AudioManager.getInstance().stopMusic();
        AudioManager.getInstance().playMusic(musicPath, true);
        System.out.println("[Audio] Playing boss music: " + musicPath);
    }

    public boolean isCinematicActive() {
        return cameraPanningToBoss;
    }
}
