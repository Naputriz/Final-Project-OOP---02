package com.kelompok2.frontend.systems;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.kelompok2.frontend.entities.Boss;
import com.kelompok2.frontend.entities.GameCharacter;
import com.kelompok2.frontend.entities.MeleeAttack;
import com.kelompok2.frontend.entities.Projectile;
import com.kelompok2.frontend.pools.EnemyPool;
import com.kelompok2.frontend.pools.ProjectilePool;

public class RenderingSystem {
    // Rendering resources
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private Texture background;

    // Entities to render (injected)
    private GameCharacter player;
    private EnemyPool enemyPool;
    private ProjectilePool projectilePool;
    private Array<MeleeAttack> playerMeleeAttacks;
    private Array<MeleeAttack> bossMeleeAttacks;
    private Array<Projectile> bossProjectiles;

    // Damage Indicators
    private Array<com.kelompok2.frontend.entities.DamageIndicator> damageIndicators;
    private com.badlogic.gdx.graphics.g2d.BitmapFont damageFont;
    private com.kelompok2.frontend.managers.GameEventManager eventManager;

    public RenderingSystem(SpriteBatch batch, ShapeRenderer shapeRenderer, Texture background) {
        this.batch = batch;
        this.shapeRenderer = shapeRenderer;
        this.background = background;
        this.damageIndicators = new Array<>();
        this.damageFont = new com.badlogic.gdx.graphics.g2d.BitmapFont();
        this.damageFont.getData().setScale(1.5f); // Make damage numbers visible
    }

    public void initialize(GameCharacter player, EnemyPool enemyPool, ProjectilePool projectilePool,
            Array<MeleeAttack> playerMeleeAttacks, Array<MeleeAttack> bossMeleeAttacks,
            Array<Projectile> bossProjectiles, com.kelompok2.frontend.managers.GameEventManager eventManager) {
        this.player = player;
        this.enemyPool = enemyPool;
        this.projectilePool = projectilePool;
        this.playerMeleeAttacks = playerMeleeAttacks;
        this.bossMeleeAttacks = bossMeleeAttacks;
        this.bossProjectiles = bossProjectiles;
        this.eventManager = eventManager;

        subscribeToEvents();
    }

    private void subscribeToEvents() {
        eventManager.subscribe(com.kelompok2.frontend.events.EnemyDamagedEvent.class, this::onEnemyDamaged);
        eventManager.subscribe(com.kelompok2.frontend.events.PlayerDamagedEvent.class, this::onPlayerDamaged);
    }

    private void onEnemyDamaged(com.kelompok2.frontend.events.EnemyDamagedEvent event) {
        float x = event.getEnemy().getPosition().x + event.getEnemy().getVisualWidth() / 2;
        float y = event.getEnemy().getPosition().y + event.getEnemy().getVisualHeight() + 20f; // +20f padding
        com.badlogic.gdx.graphics.Color color;

        if (event.isArts()) {
            color = new com.badlogic.gdx.graphics.Color(0.2f, 0.6f, 1f, 1f); // Blue for Arts
        } else {
            color = com.badlogic.gdx.graphics.Color.WHITE; // White for Physical
        }

        damageIndicators.add(new com.kelompok2.frontend.entities.DamageIndicator(x, y, event.getDamage(), color));
    }

    private void onPlayerDamaged(com.kelompok2.frontend.events.PlayerDamagedEvent event) {
        float x = event.getPlayer().getPosition().x + event.getPlayer().getVisualWidth() / 2;
        float y = event.getPlayer().getPosition().y + event.getPlayer().getVisualHeight() + 20f; // +20f padding
        // Red for Player Damage
        damageIndicators.add(new com.kelompok2.frontend.entities.DamageIndicator(x, y, event.getDamage(),
                com.badlogic.gdx.graphics.Color.RED));
    }

    public void render(OrthographicCamera camera, Boss currentBoss) {
        // Set projection matrices
        batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);

        // Render background
        renderBackground(camera);

        // Render Verdant Domain (underneath sprites)
        renderVerdantDomain();

        // Render entities
        batch.begin();
        player.render(batch);
        projectilePool.render(batch);
        enemyPool.render(batch);

        // Render boss if active
        if (currentBoss != null && !currentBoss.isDead()) {
            currentBoss.render(batch);
        }
        batch.end();

        // ✅ FIX: Render enemy HP bars
        renderEnemyHPBars();

        // Render effects & attacks
        renderMeleeAttacks();
        renderGlacialBreaths();
        renderHurricaneBinds();
        renderMindFracture();
        renderPhantomHaze();
        // renderHellfirePillar();

        renderGroundSlam();
        renderIceShield();
        renderShieldStance();
        renderShieldStance();
        renderBossSkills(currentBoss);

        // Render Damage Indicators (Last, on top)
        renderDamageIndicators();

        renderUltimateAiming(camera);
    }

    private void renderDamageIndicators() {
        float delta = com.badlogic.gdx.Gdx.graphics.getDeltaTime();
        batch.begin();
        for (int i = damageIndicators.size - 1; i >= 0; i--) {
            com.kelompok2.frontend.entities.DamageIndicator indicator = damageIndicators.get(i);
            indicator.update(delta);
            indicator.render(batch, damageFont);

            if (!indicator.isActive()) {
                damageIndicators.removeIndex(i);
            }
        }
        batch.end();
    }

    private void renderShieldStance() {
        if (!(player instanceof com.kelompok2.frontend.entities.Aegis))
            return;

        com.kelompok2.frontend.entities.Aegis aegis = (com.kelompok2.frontend.entities.Aegis) player;
        com.kelompok2.frontend.skills.ShieldStanceSkill shieldStance = aegis.getShieldStanceSkill();

        if (shieldStance != null && shieldStance.isActive()) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

            // Setup drawing parameters
            float centerX = player.getPosition().x + player.getVisualWidth() / 2;
            float centerY = player.getPosition().y + player.getVisualHeight() / 2;
            float radius = player.getVisualWidth() / 2 + 25f; // Stick out a bit
            Vector2 facing = shieldStance.getFacingDirection();

            // Calculate angle from facing vector
            float angle = (float) Math.toDegrees(Math.atan2(facing.y, facing.x));

            // Draw thick red arc (Border)
            com.badlogic.gdx.Gdx.gl.glLineWidth(5);
            shapeRenderer.setColor(1f, 0f, 0f, 0.9f); // Solid Red

            // Arc logic: x, y, radius, start, degrees
            // We want 200 degrees centered on 'angle'.
            // So start = angle - 100.
            shapeRenderer.arc(centerX, centerY, radius, angle - 100, 200);

            // Draw inner glow arc (Thin)
            com.badlogic.gdx.Gdx.gl.glLineWidth(2);
            shapeRenderer.setColor(1f, 0.4f, 0.4f, 0.6f);
            shapeRenderer.arc(centerX, centerY, radius - 4, angle - 100, 200);

            com.badlogic.gdx.Gdx.gl.glLineWidth(1);
            shapeRenderer.end();
        }
    }

    private void renderBackground(OrthographicCamera camera) {
        batch.begin();
        // Draw Tiled Background
        float bgSize = 500;
        int startX = (int) (camera.position.x - camera.viewportWidth / 2) / (int) bgSize - 1;
        int startY = (int) (camera.position.y - camera.viewportHeight / 2) / (int) bgSize - 1;
        int endX = (int) (camera.position.x + camera.viewportWidth / 2) / (int) bgSize + 1;
        int endY = (int) (camera.position.y + camera.viewportHeight / 2) / (int) bgSize + 1;

        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {
                batch.draw(background, x * bgSize, y * bgSize, bgSize, bgSize);
            }
        }
        batch.end();
    }

    // Extracted from GameScreen lines 677-699
    private void renderVerdantDomain() {
        if (!(player instanceof com.kelompok2.frontend.entities.Aelita)) {
            return;
        }

        com.kelompok2.frontend.entities.Aelita aelita = (com.kelompok2.frontend.entities.Aelita) player;
        com.kelompok2.frontend.skills.VerdantDomainSkill vd = aelita.getVerdantDomain();

        if (!vd.isZoneActive()) {
            return;
        }

        // Render green healing zone
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        Vector2 zonePos = vd.getZonePosition();
        float radius = vd.getZoneRadius();

        // Semi-transparent green for healing zone (low alpha so player stays visible)
        shapeRenderer.setColor(0.2f, 0.8f, 0.3f, 0.15f);
        shapeRenderer.circle(zonePos.x, zonePos.y, radius, 50);

        shapeRenderer.end();
    }

    // Extracted from GameScreen lines 645-672
    private void renderMeleeAttacks() {
        batch.begin();

        // Render Player Attacks
        for (MeleeAttack attack : playerMeleeAttacks) {
            if (attack.isActive()) {
                attack.render(batch);
            }
        }

        // Render Boss Attacks
        for (MeleeAttack attack : bossMeleeAttacks) {
            if (attack.isActive()) {
                attack.render(batch);
            }
        }

        // Render projectiles (must be inside batch block)
        for (Projectile projectile : projectilePool.getActiveProjectiles()) {
            projectile.render(batch);
        }

        for (Projectile proj : bossProjectiles) {
            proj.render(batch);
        }

        batch.end();
    }

    // Extracted from GameScreen lines 701-712
    private void renderGlacialBreaths() {
        if (!(player instanceof com.kelompok2.frontend.entities.Isolde))
            return;
        com.kelompok2.frontend.entities.Isolde isolde = (com.kelompok2.frontend.entities.Isolde) player;
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (com.kelompok2.frontend.entities.GlacialBreath gb : isolde.getGlacialBreaths()) {
            if (gb.isActive()) {
                gb.render(shapeRenderer);
            }
        }
        shapeRenderer.end();
    }

    // Extracted from GameScreen lines 714-727
    private void renderHurricaneBinds() {
        if (!(player instanceof com.kelompok2.frontend.entities.Whisperwind))
            return;

        com.kelompok2.frontend.entities.Whisperwind whisperwind = (com.kelompok2.frontend.entities.Whisperwind) player;
        batch.begin();
        for (Projectile hurricaneProjectile : whisperwind.getHurricaneProjectiles()) {
            if (hurricaneProjectile.active) {
                hurricaneProjectile.render(batch);
            }
        }
        batch.end();
    }

    // Extracted from GameScreen lines 729-744
    private void renderMindFracture() {
        if (!(player instanceof com.kelompok2.frontend.entities.Insania))
            return;
        com.kelompok2.frontend.entities.Insania insania = (com.kelompok2.frontend.entities.Insania) player;
        if (insania.shouldShowMindFractureCircle()) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            float playerCenterX = player.getPosition().x + player.getVisualWidth() / 2;
            float playerCenterY = player.getPosition().y + player.getVisualHeight() / 2;
            com.badlogic.gdx.Gdx.gl.glLineWidth(3);
            shapeRenderer.setColor(0.8f, 0.3f, 0.8f, 0.7f);
            shapeRenderer.circle(playerCenterX, playerCenterY, insania.getSkillRadius(), 50);
            com.badlogic.gdx.Gdx.gl.glLineWidth(1);
            shapeRenderer.end();
        }
    }

    // Kei
    private void renderPhantomHaze() {
        if (!(player instanceof com.kelompok2.frontend.entities.Kei))
            return;
        com.kelompok2.frontend.entities.Kei kei = (com.kelompok2.frontend.entities.Kei) player;
        if (kei.shouldShowPhantomHazeCircle()) {
            com.badlogic.gdx.Gdx.gl.glEnable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled); // Filled for semi-transparent overlay
            float playerCenterX = player.getPosition().x + player.getVisualWidth() / 2;
            float playerCenterY = player.getPosition().y + player.getVisualHeight() / 2;

            // Magenta semi-transparent
            shapeRenderer.setColor(1f, 0.4f, 1f, 0.4f);
            shapeRenderer.circle(playerCenterX, playerCenterY, kei.getSkillRadius(), 50);
            shapeRenderer.end();

            // Border
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            com.badlogic.gdx.Gdx.gl.glLineWidth(2);
            shapeRenderer.setColor(1f, 0.4f, 1f, 0.8f);
            shapeRenderer.circle(playerCenterX, playerCenterY, kei.getSkillRadius(), 50);
            com.badlogic.gdx.Gdx.gl.glLineWidth(1);
            shapeRenderer.end();
            com.badlogic.gdx.Gdx.gl.glDisable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
        }
    }

    /*
    private void renderHellfirePillar() {
        if (!(player instanceof com.kelompok2.frontend.entities.Blaze))
            return;
        com.kelompok2.frontend.entities.Blaze blaze = (com.kelompok2.frontend.entities.Blaze) player;
        if (blaze.shouldShowPillarVisual()) { // Show during warning AND active
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(1f, 0.5f, 0f, 0.4f);
            shapeRenderer.circle(blaze.getPillarPosition().x, blaze.getPillarPosition().y, blaze.getPillarRadius(), 50);
            shapeRenderer.end();
        }
    }
    */

    // Extracted from GameScreen lines 814-834
    private void renderGroundSlam() {
        if (!player.hasSecondarySkill())
            return;

        if (player.getSecondarySkill() instanceof com.kelompok2.frontend.skills.GroundSlamSkill) {
            com.kelompok2.frontend.skills.GroundSlamSkill groundSlam = (com.kelompok2.frontend.skills.GroundSlamSkill) player
                    .getSecondarySkill();

            if (groundSlam.isShockwaveActive()) {
                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

                // Draw expanding shockwave circle
                com.badlogic.gdx.math.Vector2 slamPos = groundSlam.getShockwavePosition();
                shapeRenderer.setColor(0.8f, 0.6f, 0.3f, 0.3f); // Brown/orange semi-transparent
                shapeRenderer.circle(slamPos.x, slamPos.y, groundSlam.getRadius(), 50);

                shapeRenderer.end();
            }
        }
    }

    // Extracted from GameScreen lines 836-861
    private void renderIceShield() {
        if (!player.hasSecondarySkill())
            return;

        if (player.getSecondarySkill() instanceof com.kelompok2.frontend.skills.IceShieldSkill) {
            com.kelompok2.frontend.skills.IceShieldSkill iceShield = (com.kelompok2.frontend.skills.IceShieldSkill) player
                    .getSecondarySkill();

            if (iceShield.isShieldActive()) {
                shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

                // Draw blue shield circle around player
                float playerCenterX = player.getPosition().x + player.getVisualWidth() / 2;
                float playerCenterY = player.getPosition().y + player.getVisualHeight() / 2;
                float shieldRadius = player.getVisualWidth() / 2 + 10; // Slightly larger than character

                com.badlogic.gdx.Gdx.gl.glLineWidth(2);
                shapeRenderer.setColor(0.3f, 0.7f, 1f, 0.7f); // Light blue
                shapeRenderer.circle(playerCenterX, playerCenterY, shieldRadius, 50);
                com.badlogic.gdx.Gdx.gl.glLineWidth(1);

                shapeRenderer.end();
            }
        }
    }

    private void renderEnemyHPBars() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        for (com.kelompok2.frontend.entities.BaseEnemy enemy : enemyPool.getActiveEnemies()) {
            if (enemy.isDead())
                continue;

            float enemyX = enemy.getPosition().x;
            float enemyY = enemy.getPosition().y;
            float enemyWidth = enemy.getVisualWidth();

            // HP bar position (above enemy)
            float barX = enemyX;
            float barY = enemyY + enemy.getVisualHeight() + 5;
            float barWidth = enemyWidth;
            float barHeight = 4f;

            // Background (red)
            shapeRenderer.setColor(0.5f, 0f, 0f, 0.8f);
            shapeRenderer.rect(barX, barY, barWidth, barHeight);

            // Foreground (green, proportional to HP)
            float hpPercent = enemy.getHp() / enemy.getMaxHp();
            shapeRenderer.setColor(0f, 0.8f, 0f, 0.9f);
            shapeRenderer.rect(barX, barY, barWidth * hpPercent, barHeight);
        }

        shapeRenderer.end();
    }

    // Extracted from GameScreen lines 863-906
    private void renderBossSkills(Boss boss) {
        if (boss == null || boss.isDead())
            return;

        // Render Mind Fracture for BossInsania
        if (boss instanceof com.kelompok2.frontend.entities.BossInsania) {
            com.kelompok2.frontend.entities.BossInsania bossInsania = (com.kelompok2.frontend.entities.BossInsania) boss;
            if (bossInsania.shouldShowMindFractureCircle()) {
                shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
                float bossCenterX = boss.getPosition().x + boss.getVisualWidth() / 2;
                float bossCenterY = boss.getPosition().y + boss.getVisualHeight() / 2;
                com.badlogic.gdx.Gdx.gl.glLineWidth(3);
                shapeRenderer.setColor(0.8f, 0.3f, 0.8f, 0.7f);
                shapeRenderer.circle(bossCenterX, bossCenterY, bossInsania.getSkillRadius(), 50);
                com.badlogic.gdx.Gdx.gl.glLineWidth(1);
                shapeRenderer.end();
            }
        }

        // Render Hellfire Pillar for BossBlaze
        if (boss instanceof com.kelompok2.frontend.entities.BossBlaze) {
            com.kelompok2.frontend.entities.BossBlaze bossBlaze = (com.kelompok2.frontend.entities.BossBlaze) boss;
            if (bossBlaze.shouldShowPillarVisual()) { // Show during warning AND active
                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                shapeRenderer.setColor(1f, 0.5f, 0f, 0.4f);
                shapeRenderer.circle(bossBlaze.getPillarPosition().x, bossBlaze.getPillarPosition().y,
                        bossBlaze.getPillarRadius(), 50);
                shapeRenderer.end();
            }
        }

        // Render Glacial Breath for BossIsolde
        if (boss instanceof com.kelompok2.frontend.entities.BossIsolde) {
            com.kelompok2.frontend.entities.BossIsolde bossIsolde = (com.kelompok2.frontend.entities.BossIsolde) boss;
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            for (com.kelompok2.frontend.entities.GlacialBreath gb : bossIsolde.getGlacialBreaths()) {
                if (gb.isActive()) {
                    gb.render(shapeRenderer);
                }
            }
            shapeRenderer.end();
        }
    }

    private void renderUltimateAiming(OrthographicCamera camera) {
        if (!player.isAimingUltimate() || !player.hasUltimateSkill()) {
            return;
        }

        com.kelompok2.frontend.skills.Skill ultimate = player.getUltimateSkill();
        if (ultimate == null)
            return;

        // Visual is centered on PLAYER, not MOUSE, because these skills are
        // self-centered AoE
        float centerX = player.getPosition().x + player.getVisualWidth() / 2;
        float centerY = player.getPosition().y + player.getVisualHeight() / 2;

        float radius = ultimate.getRadius();

        // 1. Draw Range/Radius Indicator
        com.badlogic.gdx.Gdx.gl.glEnable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
        com.badlogic.gdx.Gdx.gl.glBlendFunc(com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA,
                com.badlogic.gdx.graphics.GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Color coding based on skill type
        if (ultimate instanceof com.kelompok2.frontend.skills.InfernoNovaSkill) {
            shapeRenderer.setColor(1f, 0.4f, 0f, 0.3f); // Orange semi-transparent
        } else if (ultimate instanceof com.kelompok2.frontend.skills.InsanityBurstSkill) {
            shapeRenderer.setColor(0.8f, 0f, 0.8f, 0.3f); // Purple semi-transparent
        } else if (ultimate instanceof com.kelompok2.frontend.skills.FrozenApocalypseSkill) {
            shapeRenderer.setColor(0f, 0.8f, 1f, 0.3f); // Cyan semi-transparent
        } else {
            shapeRenderer.setColor(1f, 1f, 1f, 0.3f); // White default
        }

        if (radius > 1500) {
            // Full screen effect (Frozen Apocalypse)
            shapeRenderer.rect(camera.position.x - camera.viewportWidth / 2,
                    camera.position.y - camera.viewportHeight / 2,
                    camera.viewportWidth, camera.viewportHeight);
        } else {
            // Circle Area Effect around PLAYER
            // Use high segment count for smooth circle
            shapeRenderer.circle(centerX, centerY, radius, 64);
        }

        shapeRenderer.end();

        // 2. Draw Border Line
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        com.badlogic.gdx.Gdx.gl.glLineWidth(2);
        shapeRenderer.setColor(1f, 1f, 1f, 0.7f); // White border

        if (radius <= 1500) {
            shapeRenderer.circle(centerX, centerY, radius, 64);
        }

        com.badlogic.gdx.Gdx.gl.glLineWidth(1);
        shapeRenderer.end();
        com.badlogic.gdx.Gdx.gl.glDisable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
    }

    public SpriteBatch getBatch() {
        return batch;
    }

    public ShapeRenderer getShapeRenderer() {
        return shapeRenderer;
    }
}
