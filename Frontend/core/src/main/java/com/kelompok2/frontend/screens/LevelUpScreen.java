package com.kelompok2.frontend.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.kelompok2.frontend.Main;
import com.kelompok2.frontend.effects.*;
import com.kelompok2.frontend.entities.GameCharacter;

import java.util.Random;

public class LevelUpScreen extends ScreenAdapter {
    private Main game;
    private GameScreen gameScreen;
    private GameCharacter player;

    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;
    private BitmapFont titleFont;

    private Array<LevelUpEffect> allEffects;
    private Array<LevelUpEffect> selectedEffects;
    private Array<Rectangle> effectCards;

    private int hoveredCardIndex = -1;
    private int selectedCardIndex = -1; // -1 artinya belum ada yang dipilih

    // Screen dimensions
    private static final int SCREEN_WIDTH = 1920;
    private static final int SCREEN_HEIGHT = 1080;

    // Layout
    private static final float CARD_WIDTH = 300;
    private static final float CARD_HEIGHT = 400; // Agak lebih tinggi biar muat deskripsi
    private static final float CARD_SPACING = 50;

    // Safety Features
    private float inputDelayTimer = 0f;
    private final float MIN_DELAY = 0.5f; // Setengah detik delay sebelum bisa interaksi
    private Rectangle confirmButtonBounds;

    public LevelUpScreen(Main game, GameScreen gameScreen, GameCharacter player) {
        this.game = game;
        this.gameScreen = gameScreen;
        this.player = player;

        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();
        font.getData().setScale(1.5f);
        titleFont = new BitmapFont();
        titleFont.getData().setScale(3.5f);

        // Setup Pools & Selection (Tetap sama)
        allEffects = new Array<>();
        allEffects.add(new RecoverHPEffect());
        allEffects.add(new IncreaseAtkEffect());
        allEffects.add(new IncreaseArtsEffect());
        allEffects.add(new IncreaseMaxHPEffect());
        allEffects.add(new IncreaseDefenseEffect());
        allEffects.add(new IncreaseSpeedEffect());

        // Add ONE random skill sebagai NewSkillEffect
        com.kelompok2.frontend.skills.Skill randomSkill = com.kelompok2.frontend.factories.SkillFactory
                .getRandomSkill();
        allEffects.add(new NewSkillEffect(randomSkill));

        selectedEffects = new Array<>();
        effectCards = new Array<>();
        selectRandomEffects();
        setupCardPositions();

        // Setup tombol confirm di tengah bawah
        float btnWidth = 300;
        float btnHeight = 80;
        confirmButtonBounds = new Rectangle(
                (SCREEN_WIDTH - btnWidth) / 2,
                100,
                btnWidth,
                btnHeight);
    }

    private void selectRandomEffects() {
        selectedEffects.clear();
        Array<LevelUpEffect> tempPool = new Array<>(allEffects);
        Random random = new Random();
        for (int i = 0; i < 3 && tempPool.size > 0; i++) {
            int randomIndex = random.nextInt(tempPool.size);
            selectedEffects.add(tempPool.get(randomIndex));
            tempPool.removeIndex(randomIndex);
        }
    }

    private void setupCardPositions() {
        effectCards.clear();
        float centerX = SCREEN_WIDTH / 2f;
        float centerY = SCREEN_HEIGHT / 2f;
        float totalWidth = (CARD_WIDTH * 3) + (CARD_SPACING * 2);
        float startX = centerX - totalWidth / 2;

        for (int i = 0; i < 3; i++) {
            effectCards.add(new Rectangle(
                    startX + (CARD_WIDTH + CARD_SPACING) * i,
                    centerY - CARD_HEIGHT / 2,
                    CARD_WIDTH,
                    CARD_HEIGHT));
        }
    }

    @Override
    public void render(float delta) {
        // 1. RENDER BACKGROUND GAME (Frozen)
        gameScreen.render(0);

        // 2. RENDER OVERLAY GELAP
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.85f); // Sedikit lebih gelap dari pause
        shapeRenderer.rect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        // Update Timer Delay
        inputDelayTimer += Gdx.graphics.getDeltaTime();

        // Update Logic
        updateHoverAndClick();

        // Draw UI Elements
        drawCards();
        drawText();
        drawConfirmButton();
    }

    private void updateHoverAndClick() {
        // Jika delay belum selesai, jangan proses input apapun
        if (inputDelayTimer < MIN_DELAY)
            return;

        int mouseX = Gdx.input.getX();
        int mouseY = SCREEN_HEIGHT - Gdx.input.getY();

        // Cek Hover Cards
        hoveredCardIndex = -1;
        for (int i = 0; i < effectCards.size; i++) {
            if (effectCards.get(i).contains(mouseX, mouseY)) {
                hoveredCardIndex = i;
                break;
            }
        }

        // Handle click events
        handleClick();
    }

    private void handleClick() {
        int mouseX = Gdx.input.getX();
        int mouseY = SCREEN_HEIGHT - Gdx.input.getY();

        // Handle Click
        if (Gdx.input.justTouched()) {
            // Logic Pilih Kartu
            if (hoveredCardIndex != -1) {
                selectedCardIndex = hoveredCardIndex; // Set kartu yang dipilih
            }

            // Logic Klik Confirm
            if (selectedCardIndex != -1 && confirmButtonBounds.contains(mouseX, mouseY)) {
                applyAndClose();
            }
        }
    }

    private void applyAndClose() {
        LevelUpEffect effect = selectedEffects.get(selectedCardIndex);
        effect.apply(player);
        player.setLevelUpPending(false);
        gameScreen.resumeFromLevelUp();
        game.setScreen(gameScreen);
    }

    private void drawCards() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        for (int i = 0; i < effectCards.size; i++) {
            Rectangle card = effectCards.get(i);

            // Warna Dasar Kartu
            if (i == selectedCardIndex) {
                shapeRenderer.setColor(0.1f, 0.6f, 0.1f, 1f); // Hijau (Terpilih)
            } else if (i == hoveredCardIndex && inputDelayTimer >= MIN_DELAY) {
                shapeRenderer.setColor(0.4f, 0.4f, 0.6f, 1f); // Ungu Muda (Hover)
            } else {
                shapeRenderer.setColor(0.2f, 0.2f, 0.3f, 1f); // Abu Gelap (Normal)
            }
            shapeRenderer.rect(card.x, card.y, card.width, card.height);
        }
        shapeRenderer.end();

        // Border
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        Gdx.gl.glLineWidth(3);
        for (int i = 0; i < effectCards.size; i++) {
            Rectangle card = effectCards.get(i);
            if (i == selectedCardIndex) {
                shapeRenderer.setColor(Color.GOLD); // Border Emas jika dipilih
            } else {
                shapeRenderer.setColor(Color.LIGHT_GRAY);
            }
            shapeRenderer.rect(card.x, card.y, card.width, card.height);
        }
        shapeRenderer.end();
        Gdx.gl.glLineWidth(1);
    }

    private void drawConfirmButton() {
        // Tombol hanya muncul jika ada kartu yang dipilih
        if (selectedCardIndex == -1)
            return;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Cek Hover Button
        int mouseX = Gdx.input.getX();
        int mouseY = SCREEN_HEIGHT - Gdx.input.getY();
        boolean isHoveringBtn = confirmButtonBounds.contains(mouseX, mouseY);

        if (isHoveringBtn)
            shapeRenderer.setColor(Color.GOLD);
        else
            shapeRenderer.setColor(Color.ORANGE);

        shapeRenderer.rect(confirmButtonBounds.x, confirmButtonBounds.y, confirmButtonBounds.width,
                confirmButtonBounds.height);
        shapeRenderer.end();

        // Text Button
        batch.begin();
        String text = "CONFIRM";
        GlyphLayout layout = new GlyphLayout(font, text);
        font.setColor(Color.BLACK);
        font.draw(batch, text,
                confirmButtonBounds.x + (confirmButtonBounds.width - layout.width) / 2,
                confirmButtonBounds.y + (confirmButtonBounds.height + layout.height) / 2);
        batch.end();
    }

    private void drawText() {
        batch.begin();

        // Title
        titleFont.setColor(Color.GOLD);
        String title = "LEVEL UP!";
        GlyphLayout layout = new GlyphLayout(titleFont, title);
        titleFont.draw(batch, title, (SCREEN_WIDTH - layout.width) / 2, SCREEN_HEIGHT - 100);

        // Instruction
        font.setColor(Color.LIGHT_GRAY);
        String sub = (selectedCardIndex == -1) ? "Pilih satu kartu" : "Tekan CONFIRM untuk melanjutkan";
        if (inputDelayTimer < MIN_DELAY)
            sub = "..."; // Loading

        layout.setText(font, sub);
        font.draw(batch, sub, (SCREEN_WIDTH - layout.width) / 2, SCREEN_HEIGHT - 180);

        // Card Content
        for (int i = 0; i < selectedEffects.size; i++) {
            LevelUpEffect effect = selectedEffects.get(i);
            Rectangle card = effectCards.get(i);

            // Nama Effect
            font.setColor(Color.YELLOW);
            layout.setText(font, effect.getName());
            font.draw(batch, effect.getName(),
                    card.x + (card.width - layout.width) / 2,
                    card.y + card.height - 40);

            // Deskripsi (Simple Word Wrap manual untuk contoh ini)
            // Sebaiknya pakai Label Scene2D untuk wrapping otomatis, tapi ini pakai font
            // biasa:
            font.setColor(Color.WHITE);
            font.getData().setScale(1.0f); // Kecilkan font deskripsi
            String desc = effect.getDescription();
            // Ganti newline manual agar pas di kartu (quick fix)
            desc = desc.replace(", ", ",\n");

            layout.setText(font, desc);
            font.draw(batch, desc,
                    card.x + 20, // Padding kiri
                    card.y + card.height / 2 + 20);

            font.getData().setScale(1.5f); // Balikin scale
        }

        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        font.dispose();
        titleFont.dispose();
    }
}
