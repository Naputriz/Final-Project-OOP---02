package com.kelompok2.frontend.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.kelompok2.frontend.Main;
import com.kelompok2.frontend.entities.Boss;
import com.kelompok2.frontend.entities.GameCharacter;
import com.kelompok2.frontend.events.BossDefeatedEvent;
import com.kelompok2.frontend.events.UltimateActivatedEvent;
import com.kelompok2.frontend.managers.GameEventManager;
import com.kelompok2.frontend.skills.Skill;
import com.kelompok2.frontend.utils.KeyBindings;

public class GameHUD {
    private Main game;
    private Viewport viewport;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;

    // Fonts
    private BitmapFont font;       // Angka Level
    private BitmapFont fontSmall;  // Deskripsi kecil
    private BitmapFont fontTitle;  // Judul Stats
    private BitmapFont fontBoss;   // Nama Boss
    private BitmapFont fontMessage; // Notifikasi Besar
    private BitmapFont fontTimer;   // Timer

    // --- LAYOUT CONSTANTS (Resolusi 1920x1080) ---
    private static final float SCREEN_W = 1920;
    private static final float SCREEN_H = 1080;
    private static final float MARGIN = 40; // Jarak standar dari pinggir layar

    // 1. PLAYER BARS (Kiri Atas)
    private static final float BAR_X = MARGIN;
    private static final float BAR_Y = SCREEN_H - MARGIN - 35; // Y = 1005
    private static final float BAR_WIDTH = 450;
    private static final float BAR_HEIGHT = 35;

    // 2. BOSS BAR (Tengah Atas)
    private static final float BOSS_BAR_WIDTH = 800;
    private static final float BOSS_BAR_HEIGHT = 35;
    private static final float BOSS_BAR_X = (SCREEN_W - BOSS_BAR_WIDTH) / 2;
    private static final float BOSS_BAR_Y = 950; // Agak turun biar nama muat di atasnya

    // 3. SKILL SLOTS (Tengah Bawah)
    private static final float SLOT_SIZE = 80;
    private static final float SLOT_SPACING = 20;
    private static final float SLOT_Y = MARGIN; // 40 pixel dari bawah

    // 4. STATS PANEL (Kiri Bawah)
    private static final float STATS_X = MARGIN;
    // Box stats tinggi ~180px. Kita set agar margin bawahnya pas 40px
    private static final float STATS_Y = MARGIN + 140;

    // 5. TIMER (Kanan Atas - Di Bawah Minimap)
    // Asumsi Minimap size 200px + Margin 40px = 240px dari atas.
    // Timer ditaruh di bawahnya dengan spasi tambahan.
    private static final float MINIMAP_SIZE = 200;
    private static final float TIMER_Y = SCREEN_H - MARGIN - MINIMAP_SIZE - 30;

    // State Variables
    private float gameTimer = 0f;
    private String notificationMessage = "";
    private float messageTimer = 0f;
    private static final float MESSAGE_DURATION = 3.0f;

    public GameHUD(Main game, SpriteBatch batch) {
        this.game = game;
        this.batch = batch;
        this.shapeRenderer = new ShapeRenderer();
        this.viewport = new FitViewport(SCREEN_W, SCREEN_H);

        // Init Fonts
        this.font = new BitmapFont();
        this.font.getData().setScale(2.0f);

        this.fontSmall = new BitmapFont();
        this.fontSmall.getData().setScale(1.2f);

        this.fontTitle = new BitmapFont();
        this.fontTitle.getData().setScale(1.5f);
        this.fontTitle.setColor(Color.GOLD);

        this.fontBoss = new BitmapFont();
        this.fontBoss.getData().setScale(2.2f);
        this.fontBoss.setColor(Color.SCARLET);

        this.fontMessage = new BitmapFont();
        this.fontMessage.getData().setScale(3.0f);
        this.fontMessage.setColor(Color.GOLD);

        this.fontTimer = new BitmapFont();
        this.fontTimer.getData().setScale(2.5f); // Timer agak besar
        this.fontTimer.setColor(Color.WHITE);

        subscribeToEvents();
    }

    private void subscribeToEvents() {
        GameEventManager em = GameEventManager.getInstance();
        em.subscribe(BossDefeatedEvent.class, event -> {
            showNotification(event.getBossName().toUpperCase() + " DEFEATED!\nULTIMATE OBTAINED");
        });
        em.subscribe(UltimateActivatedEvent.class, event -> {
            showNotification(event.getUltimateName().toUpperCase() + " ACTIVATED!");
        });
    }

    public void showNotification(String msg) {
        this.notificationMessage = msg;
        this.messageTimer = MESSAGE_DURATION;
    }

    public void update(float delta) {
        gameTimer += delta;
        if (messageTimer > 0) {
            messageTimer -= delta;
            if (messageTimer <= 0) notificationMessage = "";
        }
    }

    public void render(GameCharacter player, Boss currentBoss) {
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        // --- PHASE 1: SHAPES (KOTAK & BAR) ---
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // A. Player Bars
        drawProgressBar(BAR_X, BAR_Y, BAR_WIDTH, BAR_HEIGHT, player.getHp(), player.getMaxHp(), Color.FIREBRICK, Color.LIME);
        drawProgressBar(BAR_X, BAR_Y - 30, BAR_WIDTH * 0.8f, 15, player.getCurrentXp(), player.getXpToNextLevel(), Color.NAVY, Color.CYAN);

        // B. Boss Bar (Transparan)
        if (currentBoss != null && !currentBoss.isDead()) {
            shapeRenderer.setColor(0, 0, 0, 0.3f); // Transparan 0.3 alpha
            shapeRenderer.rect(BOSS_BAR_X - 5, BOSS_BAR_Y - 5, BOSS_BAR_WIDTH + 10, BOSS_BAR_HEIGHT + 10);

            // Bar merah tetap solid
            drawProgressBar(BOSS_BAR_X, BOSS_BAR_Y, BOSS_BAR_WIDTH, BOSS_BAR_HEIGHT, currentBoss.getHp(), currentBoss.getMaxHp(), new Color(0,0,0,0), Color.RED);
        }

        // C. Skill Slots
        float centerX = SCREEN_W / 2f;
        float startX = centerX - ((SLOT_SIZE * 3) + (SLOT_SPACING * 2)) / 2;

        drawSkillSlot(startX, SLOT_Y, Color.ORANGE, player.getInnateSkillTimer(), player.getInnateSkillCooldown());

        float secTimer = 0, secMax = 1;
        if (player.hasSecondarySkill()) {
            Skill s = player.getSecondarySkill();
            secTimer = s.getRemainingCooldown(); secMax = s.getCooldown();
        }
        drawSkillSlot(startX + SLOT_SIZE + SLOT_SPACING, SLOT_Y, player.hasSecondarySkill() ? Color.ROYAL : Color.DARK_GRAY, secTimer, secMax);

        Color ultColor = Color.DARK_GRAY; float ultTimer = 0;
        if (player.hasUltimateSkill()) ultColor = Color.PURPLE;
        else if (player.isUltimateUsed()) { ultColor = Color.GRAY; ultTimer = 1; }
        drawSkillSlot(startX + (SLOT_SIZE + SLOT_SPACING) * 2, SLOT_Y, ultColor, ultTimer, 1);

        // D. Stats Panel Background
        shapeRenderer.setColor(0, 0, 0, 0.6f);
        shapeRenderer.rect(STATS_X - 10, STATS_Y - 140, 220, 180);

        shapeRenderer.end();

        // --- PHASE 2: BORDERS (GARIS PUTIH) ---
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        Gdx.gl.glLineWidth(3);
        shapeRenderer.setColor(Color.WHITE);

        shapeRenderer.rect(BAR_X, BAR_Y, BAR_WIDTH, BAR_HEIGHT);
        shapeRenderer.rect(BAR_X, BAR_Y - 30, BAR_WIDTH * 0.8f, 15);
        shapeRenderer.rect(startX, SLOT_Y, SLOT_SIZE, SLOT_SIZE);
        shapeRenderer.rect(startX + SLOT_SIZE + SLOT_SPACING, SLOT_Y, SLOT_SIZE, SLOT_SIZE);
        shapeRenderer.rect(startX + (SLOT_SIZE + SLOT_SPACING) * 2, SLOT_Y, SLOT_SIZE, SLOT_SIZE);
        shapeRenderer.rect(STATS_X - 10, STATS_Y - 140, 220, 180);

        if (currentBoss != null && !currentBoss.isDead()) {
            shapeRenderer.setColor(Color.RED);
            shapeRenderer.rect(BOSS_BAR_X, BOSS_BAR_Y, BOSS_BAR_WIDTH, BOSS_BAR_HEIGHT);
        }
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        // --- PHASE 3: TEXT ---
        batch.begin();

        // 1. Timer (Align Kanan - Di Bawah Minimap)
        int minutes = (int) (gameTimer / 60);
        int seconds = (int) (gameTimer % 60);
        String timeStr = String.format("%02d:%02d", minutes, seconds);
        GlyphLayout timerLayout = new GlyphLayout(fontTimer, timeStr);
        // X = Layar Kanan - Margin - Lebar Teks
        fontTimer.draw(batch, timeStr, SCREEN_W - MARGIN - timerLayout.width, TIMER_Y);

        // 2. Notification
        if (messageTimer > 0 && !notificationMessage.isEmpty()) {
            fontMessage.setColor(1, 0.8f, 0, 1);
            drawCenteredText(batch, fontMessage, notificationMessage, 0, 0, SCREEN_W, SCREEN_H);
        }

        // 3. Player Info
        String hpText = (int)player.getHp() + " / " + (int)player.getMaxHp();
        fontSmall.setColor(Color.WHITE);
        fontSmall.draw(batch, hpText, BAR_X + 10, BAR_Y + 24);
        font.setColor(Color.GOLD);
        font.draw(batch, "LV. " + player.getLevel(), BAR_X + BAR_WIDTH + 20, BAR_Y + 30);

        // 4. Skills Keys
        KeyBindings keys = game.getKeys();
        String keyE = Input.Keys.toString(keys.innateSkill);
        String keyQ = Input.Keys.toString(keys.secondarySkill);
        String keyR = Input.Keys.toString(keys.ultimateSkill);

        font.setColor(Color.WHITE);
        drawCenteredText(batch, font, keyE, startX, SLOT_Y, SLOT_SIZE, SLOT_SIZE);
        drawCenteredText(batch, font, keyQ, startX + SLOT_SIZE + SLOT_SPACING, SLOT_Y, SLOT_SIZE, SLOT_SIZE);
        drawCenteredText(batch, font, keyR, startX + (SLOT_SIZE + SLOT_SPACING) * 2, SLOT_Y, SLOT_SIZE, SLOT_SIZE);

        fontSmall.setColor(Color.LIGHT_GRAY);
        fontSmall.draw(batch, "Innate", startX, SLOT_Y + SLOT_SIZE + 25);
        fontSmall.draw(batch, "Loot", startX + SLOT_SIZE + SLOT_SPACING, SLOT_Y + SLOT_SIZE + 25);
        fontSmall.draw(batch, "Ult", startX + (SLOT_SIZE + SLOT_SPACING) * 2, SLOT_Y + SLOT_SIZE + 25);

        // 5. Stats
        fontTitle.draw(batch, "STATS", STATS_X, STATS_Y + 25);
        fontSmall.setColor(Color.WHITE);
        float gap = 30;
        fontSmall.draw(batch, "ATK: " + (int)player.getAtk(), STATS_X, STATS_Y - 10);
        fontSmall.draw(batch, "ARTS: " + (int)player.getArts(), STATS_X, STATS_Y - 10 - gap);
        fontSmall.draw(batch, "DEF: " + (int)player.getDef(), STATS_X, STATS_Y - 10 - gap*2);
        fontSmall.draw(batch, "SPD: " + (int)player.getSpeed(), STATS_X, STATS_Y - 10 - gap*3);

        // 6. Boss Info
        if (currentBoss != null && !currentBoss.isDead()) {
            String bossInfo = currentBoss.getBossName() + " - " + currentBoss.getBossTitle();
            String bossHp = (int)currentBoss.getHp() + " / " + (int)currentBoss.getMaxHp();

            // Nama Boss di ATAS bar (+75)
            fontBoss.setColor(Color.GOLD);
            drawCenteredText(batch, fontBoss, bossInfo, BOSS_BAR_X, BOSS_BAR_Y + 75, BOSS_BAR_WIDTH, 0);

            // HP Boss di DALAM bar
            fontSmall.setColor(Color.WHITE);
            drawCenteredText(batch, fontSmall, bossHp, BOSS_BAR_X, BOSS_BAR_Y, BOSS_BAR_WIDTH, BOSS_BAR_HEIGHT);
        }

        batch.end();
    }

    private void drawProgressBar(float x, float y, float w, float h, float current, float max, Color bg, Color fill) {
        float percent = (max > 0) ? Math.max(0, Math.min(1, current / max)) : 0;
        shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 0.8f);
        shapeRenderer.rect(x, y, w, h);
        shapeRenderer.setColor(fill);
        shapeRenderer.rect(x, y, w * percent, h);
    }

    private void drawSkillSlot(float x, float y, Color color, float timer, float maxTime) {
        shapeRenderer.setColor(0.1f, 0.1f, 0.1f, 0.8f);
        shapeRenderer.rect(x, y, SLOT_SIZE, SLOT_SIZE);
        shapeRenderer.setColor(color);
        shapeRenderer.rect(x + 5, y + 5, SLOT_SIZE - 10, SLOT_SIZE - 10);
        if (timer > 0 && maxTime > 0) {
            float ratio = timer / maxTime;
            shapeRenderer.setColor(0, 0, 0, 0.7f);
            shapeRenderer.rect(x + 5, y + 5, SLOT_SIZE - 10, (SLOT_SIZE - 10) * ratio);
        }
    }

    private void drawCenteredText(SpriteBatch batch, BitmapFont font, String text, float x, float y, float w, float h) {
        GlyphLayout layout = new GlyphLayout(font, text);
        float textX = x + (w - layout.width) / 2;
        float textY = y + (h + layout.height) / 2;
        font.draw(batch, text, textX, textY);
    }

    public void resize(int width, int height) { viewport.update(width, height, true); }
    public void dispose() { shapeRenderer.dispose(); font.dispose(); fontSmall.dispose(); fontTitle.dispose(); fontBoss.dispose(); fontMessage.dispose(); fontTimer.dispose(); }
}
