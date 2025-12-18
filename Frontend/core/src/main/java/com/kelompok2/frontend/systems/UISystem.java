package com.kelompok2.frontend.systems;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.kelompok2.frontend.entities.Boss;
import com.kelompok2.frontend.entities.GameCharacter;
import com.kelompok2.frontend.events.*;
import com.kelompok2.frontend.managers.GameEventManager;
import com.kelompok2.frontend.pools.EnemyPool;

public class UISystem {
    // Rendering resources
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;
    private BitmapFont bossFont;

    // Entity references (injected)
    private GameCharacter player;
    private EnemyPool enemyPool;

    // UI state (updated via events)
    private String ultimateMessage = "";
    private float ultimateMessageTimer = 0f;
    private static final float ULTIMATE_MESSAGE_DURATION = 3.0f;
    private float gameTimer = 0f;

    public UISystem(SpriteBatch batch, ShapeRenderer shapeRenderer) {
        this.batch = batch;
        this.shapeRenderer = shapeRenderer;
        this.font = new BitmapFont();
        this.bossFont = new BitmapFont();
        this.bossFont.getData().setScale(2.0f);
        this.bossFont.setColor(Color.WHITE);
    }

    public void initialize(GameCharacter player, EnemyPool enemyPool, GameEventManager eventManager) {
        this.player = player;
        this.enemyPool = enemyPool;

        // Subscribe to events (Observer Pattern)
        subscribeToEvents(eventManager);
    }

    private void subscribeToEvents(GameEventManager eventManager) {
        // Player damaged -> update HP bar (passive, bar auto-reads player.getHp())
        eventManager.subscribe(PlayerDamagedEvent.class, this::onPlayerDamaged);

        // Enemy killed -> update XP bar (passive, bar auto-reads player.getCurrentXp())
        eventManager.subscribe(EnemyKilledEvent.class, this::onEnemyKilled);

        // Boss spawned -> show boss UI
        eventManager.subscribe(BossSpawnedEvent.class, this::onBossSpawned);

        // Boss defeated -> hide boss UI, show ultimate message
        eventManager.subscribe(BossDefeatedEvent.class, this::onBossDefeated);

        // Ultimate activated -> show activation message
        eventManager.subscribe(UltimateActivatedEvent.class, this::onUltimateActivated);
    }

    // Event handlers
    private void onPlayerDamaged(PlayerDamagedEvent event) {
        // UI auto-updates by reading player.getHp() in render
        System.out.println("[UISystem] Player damaged: " + event.getDamage());
    }

    private void onEnemyKilled(EnemyKilledEvent event) {
        // UI auto-updates by reading player.getCurrentXp() in render
        System.out.println("[UISystem] Enemy killed, XP gained: " + event.getXpGained());
    }

    private void onBossSpawned(BossSpawnedEvent event) {
        System.out.println("[UISystem] Boss spawned: " + event.getBossName());
        // Boss UI will be shown in render if boss != null
    }

    private void onBossDefeated(BossDefeatedEvent event) {
        System.out.println("[UISystem] Boss defeated: " + event.getBossName());
        ultimateMessage = "*** " + event.getUltimateSkill().getName().toUpperCase() + " OBTAINED! Press R to use. ***";
        ultimateMessageTimer = ULTIMATE_MESSAGE_DURATION;
    }

    private void onUltimateActivated(UltimateActivatedEvent event) {
        System.out.println("[UISystem] Ultimate activated: " + event.getUltimateName());
        ultimateMessage = "*** " + event.getUltimateName().toUpperCase() + " ACTIVATED! ***";
        ultimateMessageTimer = ULTIMATE_MESSAGE_DURATION;
    }

    public void update(float delta) {
        gameTimer += delta;

        // Update ultimate message timer
        if (ultimateMessageTimer > 0) {
            ultimateMessageTimer -= delta;
            if (ultimateMessageTimer < 0) {
                ultimateMessage = "";
            }
        }
    }

    public void render(OrthographicCamera camera, Boss currentBoss) {
        drawHealthBars(currentBoss);
        drawUltimateMessage(camera);
        drawGameTimer(camera);
    }

    // TODO: Extract these methods from GameScreen gradually
    private void drawHealthBars(Boss currentBoss) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Render HP Player
        drawBar(player);
        // Render XP player
        drawXpBar(player);
        // Render E Skill Cooldown player
        drawSkillCooldownBar(player);
        // Render Q Secondary Skill Cooldown player
        drawSecondarySkillBar(player);
        // ✨ NEW: Render R Ultimate Skill Indicator
        drawUltimateSkillIndicator(player);

        // Render Boss Health Bar if active
        if (currentBoss != null && !currentBoss.isDead()) {
            drawBossHealthBar(currentBoss);
        }

        // Render enemy HP bars
        // TODO: Extract

        shapeRenderer.end();

        // Render boss title
        if (currentBoss != null && !currentBoss.isDead()) {
            drawBossTitle(currentBoss);
        }
    }

    // Extracted from GameScreen lines 533-553
    private void drawBar(GameCharacter character) {
        float x = character.getPosition().x;

        // HP bar at top (furthest from character)
        // Use Template Method pattern - check if has innate skill
        boolean isPlayer = (character.getInnateSkillCooldown() > 0);
        float offset = isPlayer ? 25 : 5;
        float y = character.getPosition().y + character.getVisualHeight() + offset;

        float width = character.getVisualWidth();
        float height = 5;

        float hpPercent = character.getHp() / character.getMaxHp();

        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(x, y, width, height);

        shapeRenderer.setColor(Color.GREEN);
        shapeRenderer.rect(x, y, width * hpPercent, height);
    }

    // Extracted from GameScreen lines 517-531
    private void drawXpBar(GameCharacter character) {
        float x = character.getPosition().x;
        float y = character.getPosition().y + character.getVisualHeight() + 13;
        float width = character.getVisualWidth();
        float height = 4;

        float maxXp = character.getXpToNextLevel();
        float xpPercent = (maxXp > 0) ? character.getCurrentXp() / maxXp : 0;

        shapeRenderer.setColor(Color.DARK_GRAY);
        shapeRenderer.rect(x, y, width, height);

        shapeRenderer.setColor(Color.CYAN);
        shapeRenderer.rect(x, y, width * xpPercent, height);
    }

    // Extracted from GameScreen lines 555-587
    private void drawSkillCooldownBar(GameCharacter character) {
        // Use Template Method pattern - check if character has innate skill
        // Characters with no innate skill return 0 for cooldown
        if (character.getInnateSkillCooldown() == 0) {
            return;
        }

        float x = character.getPosition().x;
        float y = character.getPosition().y + character.getVisualHeight() + 19;
        float width = character.getVisualWidth();
        float height = 4;

        float skillTimer = 0f;
        float skillCooldown = 1f; // Default to avoid division by zero

        // Use Template Method pattern - polymorphic call instead of instanceof
        skillTimer = character.getInnateSkillTimer();
        skillCooldown = character.getInnateSkillCooldown();

        // Safety check to avoid division by zero
        if (skillCooldown == 0) {
            skillCooldown = 1f;
        }

        float cooldownPercent = (skillCooldown > 0) ? (skillCooldown - skillTimer) / skillCooldown : 1f;

        // Background (Dark gray)
        shapeRenderer.setColor(Color.DARK_GRAY);
        shapeRenderer.rect(x, y, width, height);

        shapeRenderer.setColor(new Color(1f, 0.8f, 0f, 1f));
        shapeRenderer.rect(x, y, width * cooldownPercent, height);
    }

    // Extracted from GameScreen lines 589-620
    private void drawSecondarySkillBar(GameCharacter character) {
        // Only draw if character has secondary skill
        if (!character.hasSecondarySkill()) {
            return;
        }

        float x = character.getPosition().x;
        // Q Skill Bar below E Skill, above XP bar
        // HP at +25 (height 5)
        // E Skill at +19 (height 4)
        // Q Skill at +16 (height 3)
        // XP at +13 (height 4)
        float y = character.getPosition().y + character.getVisualHeight() + 16;

        float width = character.getVisualWidth();
        float height = 3; // Slightly smaller untuk distinguish

        // Get Q skill cooldown data
        float skillTimer = character.getSecondarySkill().getRemainingCooldown();
        float skillCooldown = character.getSecondarySkill().getCooldown();

        // Calculate percentage remaining (skill is ready when timer = 0)
        float cooldownPercent = (skillCooldown > 0) ? (skillCooldown - skillTimer) / skillCooldown : 1f;

        // Background (Dark gray)
        shapeRenderer.setColor(Color.DARK_GRAY);
        shapeRenderer.rect(x, y, width, height);

        // Foreground (Blue for Q skill cooldown - different from E skill's orange)
        shapeRenderer.setColor(new Color(0.2f, 0.6f, 1f, 1f)); // Light blue
        shapeRenderer.rect(x, y, width * cooldownPercent, height);
    }

    // ✨ NEW FEATURE - Ultimate Skill Indicator
    private void drawUltimateSkillIndicator(GameCharacter character) {
        // Only draw for player (check if has innate skill cooldown > 0)
        if (character.getInnateSkillCooldown() == 0) {
            return;
        }

        float x = character.getPosition().x;
        // R Skill Indicator below Q Skill (or E Skill if no Q skill), above XP bar
        // HP at +25 (height 5)
        // E Skill at +19 (height 4)
        // Q Skill at +16 (height 3) - if has secondary
        // R Indicator at +10 (height 2)
        // XP at +13 (height 4)
        float y = character.getPosition().y + character.getVisualHeight() + 10;

        float width = character.getVisualWidth();
        float height = 2; // Thin bar

        // Background
        shapeRenderer.setColor(Color.DARK_GRAY);
        shapeRenderer.rect(x, y, width, height);

        // If player has ultimate, show as ready (gold/purple)
        if (character.hasUltimateSkill()) {
            shapeRenderer.setColor(new Color(1f, 0.84f, 0f, 1f)); // Gold
            shapeRenderer.rect(x, y, width, height);
        } else {
            // No ultimate yet - show as empty/grayed out
            shapeRenderer.setColor(new Color(0.2f, 0.2f, 0.2f, 1f)); // Dark gray
            shapeRenderer.rect(x, y, width, height);
        }
    }

    // Extracted from GameScreen lines 1704-1734
    private void drawBossHealthBar(Boss boss) {
        if (boss == null || boss.isDead())
            return;

        // Position health bar above the boss
        float x = boss.getPosition().x;
        float y = boss.getPosition().y + boss.getVisualHeight() + 10f; // Above boss
        float barWidth = boss.getVisualWidth() * 2f; // Wider than boss for visibility
        float barHeight = 8f; // Thicker than regular enemy bars

        // Background (dark red)
        shapeRenderer.setColor(new Color(0.3f, 0, 0, 1f));
        shapeRenderer.rect(x, y, barWidth, barHeight);

        // Health fill
        float healthPercent = boss.getHp() / boss.getMaxHp();
        Color healthColor;
        if (healthPercent > 0.5f) {
            healthColor = Color.GOLD; // Gold for bosses
        } else if (healthPercent > 0.25f) {
            healthColor = Color.ORANGE;
        } else {
            healthColor = Color.RED;
        }
        shapeRenderer.setColor(healthColor);
        shapeRenderer.rect(x, y, barWidth * healthPercent, barHeight);
    }

    // Extracted from GameScreen (boss title rendering)
    private void drawBossTitle(Boss boss) {
        if (boss == null || boss.isDead())
            return;

        batch.begin();

        // Position above boss
        float bossX = boss.getPosition().x;
        float bossY = boss.getPosition().y + boss.getVisualHeight();
        float bossWidth = boss.getVisualWidth() * 2f;

        // Get boss name and title
        String bossName = boss.getBossName();
        String bossTitle = boss.getBossTitle();
        String fullTitle = bossName + " - " + bossTitle; // e.g., "Insania - The Chaos Kaiser"

        // Calculate text position (centered above boss)
        GlyphLayout titleLayout = new GlyphLayout(bossFont, fullTitle);
        float titleX = bossX + (bossWidth - titleLayout.width) / 2;
        float titleY = bossY + 50f; // Above health bar

        // Draw title with shadow for visibility
        bossFont.setColor(0f, 0f, 0f, 0.7f); // Shadow
        bossFont.draw(batch, fullTitle, titleX + 2, titleY - 2);
        bossFont.setColor(1f, 0.84f, 0f, 1f); // Gold
        bossFont.draw(batch, fullTitle, titleX, titleY);
        bossFont.setColor(Color.WHITE); // Reset

        batch.end();
    }

    private void drawUltimateMessage(OrthographicCamera camera) {
        if (ultimateMessageTimer > 0 && !ultimateMessage.isEmpty()) {
            batch.begin();
            bossFont.setColor(Color.GOLD);
            float messageX = (camera.viewportWidth - ultimateMessage.length() * 15f) / 2f;
            float messageY = camera.viewportHeight - 150f;
            bossFont.draw(batch, ultimateMessage, messageX, messageY);
            batch.end();
        }
    }

    private void drawGameTimer(OrthographicCamera camera) {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        int minutes = (int) (gameTimer / 60);
        int seconds = (int) (gameTimer % 60);
        String timerText = String.format("%02d:%02d", minutes, seconds);
        GlyphLayout timerLayout = new GlyphLayout(bossFont, timerText);
        float timerX = camera.position.x - (timerLayout.width / 2);
        float timerY = camera.position.y + (camera.viewportHeight / 2) - 30;
        bossFont.setColor(0f, 0f, 0f, 0.8f);
        bossFont.draw(batch, timerText, timerX + 2, timerY - 2);
        bossFont.setColor(Color.WHITE);
        bossFont.draw(batch, timerText, timerX, timerY);
        batch.end();
    }

    public void dispose() {
        font.dispose();
        bossFont.dispose();
    }
}
