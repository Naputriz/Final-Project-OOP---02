package com.kelompok2.frontend.systems;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Texture;
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

    // Cached Player State (Observer Pattern)
    private float playerHp;
    private float playerMaxHp;
    private float playerXp;
    private float playerMaxXp;
    private int playerLevel;
    private float innateSkillTimer;
    private float innateSkillCooldown;
    private float secondarySkillTimer;
    private float secondarySkillCooldown;
    private boolean ultimateReady;

    // Cached Boss State
    private float bossHp;
    private float bossMaxHp;
    private String bossNameCached; // Rename to avoid confusion if needed, but bossName is fine

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

        // Initialize cached values from player
        this.playerHp = player.getHp();
        this.playerMaxHp = player.getMaxHp();
        this.playerXp = player.getCurrentXp();
        this.playerMaxXp = player.getXpToNextLevel();
        this.playerLevel = player.getLevel();
        this.innateSkillTimer = 0;
        this.innateSkillCooldown = 0; // Will be updated by event or we can poll once? Better to wait for event or
                                      // poll once here?
        // Poll once for initial state if static, but cooldowns change.
        // Assuming cooldowns start at 0 (ready).

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

        // NEW: State Change Events
        eventManager.subscribe(HealthChangedEvent.class, this::onHealthChanged);
        eventManager.subscribe(XpChangedEvent.class, this::onXpChanged);
        eventManager.subscribe(CooldownChangedEvent.class, this::onCooldownChanged);
    }

    // Event handlers
    private void onPlayerDamaged(PlayerDamagedEvent event) {
        System.out.println("[UISystem] Player damaged: " + event.getDamage());
    }

    private void onEnemyKilled(EnemyKilledEvent event) {
        System.out.println("[UISystem] Enemy killed, XP gained: " + event.getXpGained());
    }

    private void onBossSpawned(BossSpawnedEvent event) {
        System.out.println("[UISystem] Boss spawned: " + event.getBossName());
        this.bossHp = event.getBoss().getHp();
        this.bossMaxHp = event.getBoss().getMaxHp();
        this.bossNameCached = event.getBossName();
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

    private void onHealthChanged(HealthChangedEvent event) {
        if (event.getCharacter() == this.player) {
            this.playerHp = event.getCurrentHp();
            this.playerMaxHp = event.getMaxHp();
        } else if (event.getCharacter() instanceof Boss) {
            this.bossHp = event.getCurrentHp();
            this.bossMaxHp = event.getMaxHp();
        }
    }

    private void onXpChanged(XpChangedEvent event) {
        if (event.getCharacter() == this.player) {
            this.playerXp = event.getCurrentXp();
            this.playerMaxXp = event.getMaxXp();
            this.playerLevel = event.getLevel();
        }
    }

    private void onCooldownChanged(CooldownChangedEvent event) {
        if (event.getCharacter() == this.player) {
            switch (event.getSkillType()) {
                case INNATE:
                    this.innateSkillTimer = event.getRemainingTime();
                    this.innateSkillCooldown = event.getTotalCooldown();
                    break;
                case SECONDARY:
                    this.secondarySkillTimer = event.getRemainingTime();
                    this.secondarySkillCooldown = event.getTotalCooldown();
                    break;
                case ULTIMATE:
                    // If remaining is 0, it means ready.
                    this.ultimateReady = (event.getRemainingTime() <= 0);
                    break;
            }
        }
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
        // Prevent crash when window is minimized
        if (camera.viewportWidth <= 1 || camera.viewportHeight <= 1) {
            return;
        }

        // Draw HUD (Fixed Screen Coordinates)
        drawHUD(camera, currentBoss);

        drawUltimateMessage();
        drawGameTimer();
    }

    private OrthographicCamera uiCamera;

    private void drawHUD(OrthographicCamera gameCamera, Boss currentBoss) {
        // --- UI CAMERA SETUP (Static Screen Space) ---
        if (uiCamera == null) {
            uiCamera = new OrthographicCamera();
        }

        // Sync UI camera with current viewport size, but keep position static
        // (centered)
        if (uiCamera.viewportWidth != gameCamera.viewportWidth
                || uiCamera.viewportHeight != gameCamera.viewportHeight) {
            uiCamera.setToOrtho(false, gameCamera.viewportWidth, gameCamera.viewportHeight);
        }
        uiCamera.update();

        // Screen Coordinates (0,0 is bottom-left)
        float screenW = uiCamera.viewportWidth;
        float screenH = uiCamera.viewportHeight;

        // CRITICAL FIX: Prevent crash when window is minimized (size = 0)
        if (screenW <= 1 || screenH <= 1) {
            return;
        }

        float uiScale = screenH / 1080f;

        // HUD Anchors
        float hudLeft = 0; // Left edge
        float hudTop = screenH; // Top edge
        float hudCenterX = screenW / 2;

        // --- 0. XP Bar (Top Screen Edge, Full Width) ---
        float xpBarHeight = 15 * uiScale;
        float xpY = hudTop - xpBarHeight;

        // Use UI Camera for rendering
        shapeRenderer.setProjectionMatrix(uiCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Background
        shapeRenderer.setColor(Color.DARK_GRAY);
        shapeRenderer.rect(hudLeft, xpY, screenW, xpBarHeight);

        // Fill (Cyan)
        if (playerMaxXp > 0) {
            float xpPercent = Math.max(0, playerXp / playerMaxXp);
            shapeRenderer.setColor(Color.CYAN);
            shapeRenderer.rect(hudLeft, xpY, screenW * xpPercent, xpBarHeight);
        }

        // 1. Portrait (Top Left, below XP Bar)
        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        Texture portrait = player.getTexture();
        float portraitSize = 90 * uiScale;
        float portraitX = hudLeft + (20 * uiScale);
        float portraitY = xpY - (15 * uiScale) - portraitSize;

        if (portrait != null) {
            batch.draw(portrait, portraitX, portraitY, portraitSize, portraitSize);
        }
        batch.end();

        // 2. Bars (Right of Portrait)
        shapeRenderer.setProjectionMatrix(uiCamera.combined);
        shapeRenderer.end();

        // Now Bars and Skills (Shapes)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        float barsX = portraitX + portraitSize + (15 * uiScale);
        float barWidth = 400 * uiScale;
        float hpBarHeight = 30 * uiScale;

        float hpY = portraitY + portraitSize - hpBarHeight;

        // --- HP Bar ---
        // Background
        shapeRenderer.setColor(Color.DARK_GRAY);
        shapeRenderer.rect(barsX, hpY, barWidth, hpBarHeight);

        // Fill (Green)
        if (playerMaxHp > 0) {
            float hpPercent = Math.max(0, playerHp / playerMaxHp);
            shapeRenderer.setColor(Color.GREEN);
            shapeRenderer.rect(barsX, hpY, barWidth * hpPercent, hpBarHeight);
        }

        // --- Skills (Under Portrait) ---
        float skillY = portraitY - (50 * uiScale);
        float skillSize = 50 * uiScale;
        float skillGap = 10 * uiScale;
        float skillX = portraitX;

        // Innate
        shapeRenderer.setColor(Color.ORANGE);
        shapeRenderer.rect(skillX, skillY, skillSize, skillSize);
        if (innateSkillCooldown > 0 && innateSkillTimer > 0) {
            shapeRenderer.setColor(0, 0, 0, 0.5f);
            float alpha = innateSkillTimer / innateSkillCooldown;
            shapeRenderer.rect(skillX, skillY, skillSize, skillSize * alpha);
        }

        // Secondary
        if (secondarySkillCooldown > 0) {
            skillX += skillSize + skillGap;
            shapeRenderer.setColor(new Color(0.2f, 0.6f, 1f, 1f));
            shapeRenderer.rect(skillX, skillY, skillSize, skillSize);
            if (secondarySkillCooldown > 0 && secondarySkillTimer > 0) {
                shapeRenderer.setColor(0, 0, 0, 0.5f);
                float alpha = secondarySkillTimer / secondarySkillCooldown;
                shapeRenderer.rect(skillX, skillY, skillSize, skillSize * alpha);
            }
        }

        // Ultimate Indicator (Gold Box)
        if (ultimateReady) {
            skillX += skillSize + skillGap;
            shapeRenderer.setColor(Color.GOLD);
            shapeRenderer.rect(skillX, skillY, skillSize, skillSize);
        }

        // --- Boss Health Bar (Top Center, Lowered) ---
        float bossBarWidth = 600 * uiScale;
        float bossBarHeight = 30 * uiScale;

        float bossX = hudCenterX - (bossBarWidth / 2);
        float bossY = hudTop - (60 * uiScale) - xpBarHeight;

        if (currentBoss != null && !currentBoss.isDead() && bossMaxHp > 0) {
            shapeRenderer.setColor(new Color(0.3f, 0, 0, 1f));
            shapeRenderer.rect(bossX, bossY, bossBarWidth, bossBarHeight);

            float bossHpPercent = Math.max(0, bossHp / bossMaxHp);
            shapeRenderer.setColor(Color.RED);
            shapeRenderer.rect(bossX, bossY, bossBarWidth * bossHpPercent, bossBarHeight);
        }

        shapeRenderer.end();

        // 3. Text Overlay
        batch.begin();

        // HP Text
        String hpText = (int) playerHp + " / " + (int) playerMaxHp;
        font.setColor(Color.WHITE);
        font.getData().setScale(1.2f * uiScale);
        font.draw(batch, hpText, barsX + (15 * uiScale), hpY + (22 * uiScale));
        font.getData().setScale(1.0f * uiScale);

        // Lv Text
        String lvText = "Lv." + playerLevel;
        font.setColor(Color.WHITE);
        font.draw(batch, lvText, hudLeft + (10 * uiScale), xpY + xpBarHeight + (15 * uiScale));

        // Boss Title & HP Text
        if (currentBoss != null && !currentBoss.isDead() && bossMaxHp > 0) {
            String bossName = (bossNameCached != null) ? bossNameCached : currentBoss.getBossName();
            String bossTitle = currentBoss.getBossTitle();
            String fullTitle = bossName + " - " + bossTitle;

            float bossBarY = hudTop - (60 * uiScale) - xpBarHeight;

            // Title
            bossFont.getData().setScale(2.0f * uiScale);
            GlyphLayout layout = new GlyphLayout(bossFont, fullTitle);
            float titleX = hudCenterX - (layout.width / 2);
            float titleY = bossBarY + (50 * uiScale);

            bossFont.setColor(0, 0, 0, 0.7f);
            bossFont.draw(batch, fullTitle, titleX + (2 * uiScale), titleY - (2 * uiScale));
            bossFont.setColor(Color.GOLD);
            bossFont.draw(batch, fullTitle, titleX, titleY);
            bossFont.setColor(Color.WHITE);

            // Boss HP Text
            String bossHpString = (int) bossHp + " / " + (int) bossMaxHp;
            GlyphLayout hpLayout = new GlyphLayout(font, bossHpString);
            float hpX = hudCenterX - (hpLayout.width / 2);
            float hpYText = bossBarY + (20 * uiScale);

            font.setColor(Color.WHITE);
            font.draw(batch, bossHpString, hpX, hpYText);
        }
        batch.end();
    }

    private void drawUltimateMessage() {
        if (uiCamera == null)
            return;

        float screenH = uiCamera.viewportHeight;
        float uiScale = screenH / 1080f;

        if (ultimateMessageTimer > 0 && !ultimateMessage.isEmpty()) {
            batch.setProjectionMatrix(uiCamera.combined);
            batch.begin();
            bossFont.setColor(Color.GOLD);
            bossFont.getData().setScale(2.0f * uiScale);

            GlyphLayout layout = new GlyphLayout(bossFont, ultimateMessage);
            float messageX = (uiCamera.viewportWidth - layout.width) / 2f;
            float messageY = uiCamera.viewportHeight / 1.5f; // Middle-ish top

            bossFont.draw(batch, ultimateMessage, messageX, messageY);
            batch.end();
            bossFont.setColor(Color.WHITE);
            bossFont.getData().setScale(1.0f); // Reset
        }
    }

    private void drawGameTimer() {
        if (uiCamera == null)
            return;

        float screenH = uiCamera.viewportHeight;
        float uiScale = screenH / 1080f;

        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        int minutes = (int) (gameTimer / 60);
        int seconds = (int) (gameTimer % 60);
        String timerText = String.format("%02d:%02d", minutes, seconds);

        bossFont.getData().setScale(2.0f * uiScale);
        GlyphLayout timerLayout = new GlyphLayout(bossFont, timerText);

        float timerX = (uiCamera.viewportWidth - timerLayout.width) / 2f;

        // Base Timer Y (Top Screen)
        float hudTop = uiCamera.viewportHeight;

        // Default (without boss): slightly below top
        float timerY = hudTop - (40 * uiScale);

        // If Boss is Active:
        // Boss Bar Y is approx hudTop - 50 - 12 (XP) ~= hudTop - 62
        // Boss Bar Height is 25. Bottom is hudTop - 87.
        // So Timer should be at hudTop - 100 or so.
        if (bossMaxHp > 0 && bossHp > 0) {
            timerY = hudTop - (105 * uiScale);
        }

        bossFont.setColor(0f, 0f, 0f, 0.8f);
        bossFont.draw(batch, timerText, timerX + (2 * uiScale), timerY - (2 * uiScale));
        bossFont.setColor(Color.WHITE);
        bossFont.draw(batch, timerText, timerX, timerY);
        batch.end();
        bossFont.getData().setScale(1.0f); // Reset
    }

    public void dispose() {
        font.dispose();
        bossFont.dispose();
    }
}
