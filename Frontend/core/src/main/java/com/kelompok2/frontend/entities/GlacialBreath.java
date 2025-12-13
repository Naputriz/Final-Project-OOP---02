package com.kelompok2.frontend.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class GlacialBreath {
    private Polygon cone;
    private float damage;
    private float duration;
    private float timeAlive;
    private boolean active;
    private GameCharacter owner;

    // Track enemies yang sudah terkena cone ini (untuk prevent multi-hit)
    private Array<GameCharacter> hitEnemies;

    public GlacialBreath(GameCharacter owner, Vector2 targetDirection, float damage, float duration) {
        this.owner = owner;
        this.damage = damage;
        this.duration = duration;
        this.timeAlive = 0;
        this.active = true;
        this.hitEnemies = new Array<>();

        // Buat cone polygon
        createCone(owner.getPosition(), targetDirection);
    }

    private void createCone(Vector2 origin, Vector2 direction) {
        // Normalize direction
        Vector2 dir = direction.cpy().nor();

        // Cone parameters - Balanced size
        float coneLength = 400f; // Range cone (doubled from 200)
        float coneAngle = 60f; // Sudut cone (reverted to 60 degrees for precision)

        // Posisi awal (tengah karakter)
        float startX = origin.x + owner.getVisualWidth() / 2;
        float startY = origin.y + owner.getVisualHeight() / 2;

        // Hitung endpoint cone
        float endX = startX + dir.x * coneLength;
        float endY = startY + dir.y * coneLength;

        // Hitung perpendicular vector untuk cone width
        Vector2 perpendicular = new Vector2(-dir.y, dir.x);
        float coneWidth = coneLength * (float) Math.tan(Math.toRadians(coneAngle / 2));

        // Titik-titik cone (triangle shape)
        float[] vertices = new float[] {
                startX, startY, // Origin
                endX + perpendicular.x * coneWidth, endY + perpendicular.y * coneWidth, // Left
                endX - perpendicular.x * coneWidth, endY - perpendicular.y * coneWidth // Right
        };

        cone = new Polygon(vertices);
    }

    public void update(float delta) {
        timeAlive += delta;
        if (timeAlive >= duration) {
            active = false;
        }
    }

    public boolean canHit(GameCharacter enemy) {
        if (!active)
            return false;
        if (hitEnemies.contains(enemy, true))
            return false;

        // Check collision dengan cone polygon
        Rectangle enemyBounds = enemy.getBounds();

        // Check 4 corners + center enemy bounds
        float[] points = {
                enemyBounds.x, enemyBounds.y,
                enemyBounds.x + enemyBounds.width, enemyBounds.y,
                enemyBounds.x, enemyBounds.y + enemyBounds.height,
                enemyBounds.x + enemyBounds.width, enemyBounds.y + enemyBounds.height,
                enemyBounds.x + enemyBounds.width / 2, enemyBounds.y + enemyBounds.height / 2
        };

        for (int i = 0; i < points.length; i += 2) {
            if (cone.contains(points[i], points[i + 1])) {
                return true;
            }
        }

        return false;
    }

    public void markAsHit(GameCharacter enemy) {
        hitEnemies.add(enemy);
    }

    public float getDamage() {
        return damage;
    }

    public boolean isActive() {
        return active;
    }

    public void render(ShapeRenderer shapeRenderer) {
        if (!active)
            return;

        shapeRenderer.setColor(new Color(0.5f, 0.8f, 1f, 0.3f)); // Light blue semi-transparent

        // Render cone as triangle
        float[] vertices = cone.getTransformedVertices();
        shapeRenderer.triangle(
                vertices[0], vertices[1],
                vertices[2], vertices[3],
                vertices[4], vertices[5]);
    }

    public Polygon getCone() {
        return cone;
    }
}
