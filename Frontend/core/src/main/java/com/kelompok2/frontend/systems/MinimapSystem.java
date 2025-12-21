package com.kelompok2.frontend.systems;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.kelompok2.frontend.entities.BaseEnemy;
import com.kelompok2.frontend.entities.Boss;
import com.kelompok2.frontend.entities.GameCharacter;
import com.kelompok2.frontend.pools.EnemyPool;

public class MinimapSystem {
    private ShapeRenderer shapeRenderer;
    private GameCharacter player;
    private EnemyPool enemyPool;
    private SpawningSystem spawningSystem;

    // Map Dimensions (Must match MapBoundarySystem)
    private static final float MAP_WIDTH = 4000f;
    private static final float MAP_HEIGHT = 4000f;

    // Minimap UI Settings
    private static final float MINIMAP_SIZE = 200f; // 200x200 px
    private static final float PADDING = 40f; // Padding from screen edge

    private OrthographicCamera uiCamera;

    public MinimapSystem(ShapeRenderer shapeRenderer) {
        this.shapeRenderer = shapeRenderer;
    }

    public void initialize(GameCharacter player, EnemyPool enemyPool, SpawningSystem spawningSystem) {
        this.player = player;
        this.enemyPool = enemyPool;
        this.spawningSystem = spawningSystem;
    }

    public void render(OrthographicCamera gameCamera) {
        // Setup UI Camera logic (similar to UISystem)
        if (uiCamera == null) {
            uiCamera = new OrthographicCamera();
        }

        // Sync UI camera
        if (uiCamera.viewportWidth != gameCamera.viewportWidth
                || uiCamera.viewportHeight != gameCamera.viewportHeight) {
            uiCamera.setToOrtho(false, gameCamera.viewportWidth, gameCamera.viewportHeight);
        }
        uiCamera.update();

        float screenW = uiCamera.viewportWidth;
        float screenH = uiCamera.viewportHeight;

        // Safety check
        if (screenW <= 1 || screenH <= 1)
            return;

        // Calculate UI Scale
        float uiScale = screenH / 1080f;
        float scaledSize = MINIMAP_SIZE * uiScale;
        float scaledPadding = PADDING * uiScale;

        // Position: Top Right
        float minimapX = screenW - scaledSize - scaledPadding;
        float minimapY = screenH - scaledSize - scaledPadding;

        // Begin Rendering
        shapeRenderer.setProjectionMatrix(uiCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // 1. Background (Semi-transparent black)
        shapeRenderer.setColor(0f, 0f, 0f, 0.5f);
        shapeRenderer.rect(minimapX, minimapY, scaledSize, scaledSize);

        // 2. Border (White)
        // Draw lines manually or use Line type?
        // Using rect for border (filled behind or lines after)
        // Let's do background first, then entities, then border outline at the end.

        // --- Calculate Entity Positions ---
        // Map Coordinate (0,0) is center of map? Or bottom-left?
        // MapBoundarySystem: "4000x4000". Usually centered at 0,0 or 0->4000.
        // Checking MapBoundarySystem would be ideal, but assuming standard 0-centered
        // or 0-offset.
        // If map is 4000x4000, boundaries are likely -2000 to +2000 OR 0 to 4000.
        // Let's assume 0 to 4000 based on "Player spawn randomized ... 700 units
        // padding".

        // Let's normalize coordinates to [0,1]
        // If map is centered at 0,0: Range is [-2000, 2000].
        // Normalized = (val + 2000) / 4000.

        // Let's check player position to guess.
        // If player starts at 0,0 and map is 0-4000, they are at corner.
        // If player starts at 0,0 and map is -2000 to 2000, they are at center.
        // Most LibGDX games use 0,0 as center if using a camera moving around,
        // BUT MapBoundarySystem usually clamps.
        // I will assume 0,0 is CENTER [-2000, 2000] to start.
        // ADJUSTMENT: If map is 0-4000, I'll need to offset.
        // Safer bet: Normalized X = (worldX + HalfMap) / MapSize

        float halfMapW = MAP_WIDTH / 2f;
        float halfMapH = MAP_HEIGHT / 2f;

        // 3. Render Enemies (Red Dots)
        shapeRenderer.setColor(Color.RED);
        float enemyDotSize = 3f * uiScale;
        for (BaseEnemy enemy : enemyPool.getActiveEnemies()) {
            if (enemy.isDead())
                continue;

            drawEntity(enemy.getPosition().x, enemy.getPosition().y,
                    minimapX, minimapY, scaledSize, halfMapW, halfMapH, enemyDotSize);
        }

        // 4. Render Boss (Purple/Large Dot)
        Boss boss = spawningSystem.getCurrentBoss();
        if (boss != null && !boss.isDead()) {
            shapeRenderer.setColor(Color.PURPLE);
            float bossDotSize = 8f * uiScale;
            drawEntity(boss.getPosition().x, boss.getPosition().y,
                    minimapX, minimapY, scaledSize, halfMapW, halfMapH, bossDotSize);
        }

        // 5. Render Player (Green Dot)
        shapeRenderer.setColor(Color.GREEN);
        float playerDotSize = 5f * uiScale;
        drawEntity(player.getPosition().x, player.getPosition().y,
                minimapX, minimapY, scaledSize, halfMapW, halfMapH, playerDotSize);

        shapeRenderer.end();

        // 6. Border Outline
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(minimapX, minimapY, scaledSize, scaledSize);
        shapeRenderer.end();
    }

    private void drawEntity(float worldX, float worldY, float miniX, float miniY, float miniSize, float halfMapW,
            float halfMapH, float dotSize) {
        // Fix: Map is 0-4000, not -2000 to 2000.
        // Normalize 0-4000 to 0-1
        float normX = worldX / (halfMapW * 2);
        float normY = worldY / (halfMapH * 2);

        // Clamp to 0-1 to prevent drawing outside minimap
        normX = Math.max(0, Math.min(1, normX));
        normY = Math.max(0, Math.min(1, normY));

        float drawX = miniX + (normX * miniSize);
        float drawY = miniY + (normY * miniSize);

        shapeRenderer.circle(drawX, drawY, dotSize);
    }
}
