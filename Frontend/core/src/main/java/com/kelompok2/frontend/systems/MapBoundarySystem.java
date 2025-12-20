package com.kelompok2.frontend.systems;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.kelompok2.frontend.entities.GameCharacter;

public class MapBoundarySystem {

    // Map Dimensions (0,0 to 4000,4000)
    // Expanded based on user feedback to prevent camera clamping too often
    private static final float MAP_WIDTH = 4000f;
    private static final float MAP_HEIGHT = 4000f;

    // Visual boundary padding (keep player slightly inside)
    private static final float PADDING = 64f;

    public void update(GameCharacter player, OrthographicCamera camera) {
        // 1. Clamp Player Position
        clampPlayerPosition(player);
a
        // 2. Clamp Camera Position
        clampCameraPosition(camera);
    }

    private void clampPlayerPosition(GameCharacter player) {
        Vector2 pos = player.getPosition();
        float x = pos.x;
        float y = pos.y;
        float width = player.getWidth();
        float height = player.getHeight();

        // Clamp X
        if (x < PADDING) {
            x = PADDING;
        } else if (x + width > MAP_WIDTH - PADDING) {
            x = MAP_WIDTH - PADDING - width;
        }

        // Clamp Y
        if (y < PADDING) {
            y = PADDING;
        } else if (y + height > MAP_HEIGHT - PADDING) {
            y = MAP_HEIGHT - PADDING - height;
        }

        player.setPosition(x, y);
    }

    private void clampCameraPosition(OrthographicCamera camera) {
        float viewportHalfWidth = camera.viewportWidth * camera.zoom * 0.5f;
        float viewportHalfHeight = camera.viewportHeight * camera.zoom * 0.5f;

        // Clamp X
        // Min X = viewportHalfWidth (left edge at 0)
        // Max X = MAP_WIDTH - viewportHalfWidth (right edge at MAP_WIDTH)
        float minX = viewportHalfWidth;
        float maxX = MAP_WIDTH - viewportHalfWidth;

        // If map is smaller than viewport, center it
        if (minX > maxX) {
            camera.position.x = MAP_WIDTH / 2f;
        } else {
            camera.position.x = MathUtils.clamp(camera.position.x, minX, maxX);
        }

        // Clamp Y
        float minY = viewportHalfHeight;
        float maxY = MAP_HEIGHT - viewportHalfHeight;

        if (minY > maxY) {
            camera.position.y = MAP_HEIGHT / 2f;
        } else {
            camera.position.y = MathUtils.clamp(camera.position.y, minY, maxY);
        }

        camera.update();
    }

    public static float getMapWidth() {
        return MAP_WIDTH;
    }

    public static float getMapHeight() {
        return MAP_HEIGHT;
    }
}
