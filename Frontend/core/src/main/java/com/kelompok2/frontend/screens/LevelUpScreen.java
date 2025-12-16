package com.kelompok2.frontend.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
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

    // Array semua effect yang tersedia (pool)
    private Array<LevelUpEffect> allEffects;

    // 3 effect yang dipilih secara random untuk ditampilkan
    private Array<LevelUpEffect> selectedEffects;

    // Rectangles untuk clickable areas (effect cards)
    private Array<Rectangle> effectCards;

    // Index card yang sedang di-hover (-1 jika tidak ada)
    private int hoveredCardIndex = -1;

    // Screen dimensions
    private static final int SCREEN_WIDTH = 1920;
    private static final int SCREEN_HEIGHT = 1080;

    // Card dimensions dan layout
    private static final float CARD_WIDTH = 300;
    private static final float CARD_HEIGHT = 180;
    private static final float CARD_SPACING = 50;

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

        // Inisialisasi effect pool
        allEffects = new Array<>();
        allEffects.add(new RecoverHPEffect());
        allEffects.add(new IncreaseAtkEffect());
        allEffects.add(new IncreaseArtsEffect());
        allEffects.add(new IncreaseMaxHPEffect());
        allEffects.add(new IncreaseDefenseEffect());

        // Add ONE random skill sebagai NewSkillEffect
        com.kelompok2.frontend.skills.Skill randomSkill = com.kelompok2.frontend.factories.SkillFactory.getRandomSkill();
        allEffects.add(new NewSkillEffect(randomSkill));

        // Pilih 3 effect secara random
        selectedEffects = new Array<>();
        effectCards = new Array<>();
        selectRandomEffects();

        // Setup card positions
        setupCardPositions();
    }

    private void selectRandomEffects() {
        selectedEffects.clear();
        Array<LevelUpEffect> tempPool = new Array<>(allEffects);
        Random random = new Random();

        // Pilih 3 effect tanpa duplikasi
        for (int i = 0; i < 3 && tempPool.size > 0; i++) {
            int randomIndex = random.nextInt(tempPool.size);
            selectedEffects.add(tempPool.get(randomIndex));
            tempPool.removeIndex(randomIndex);
        }
    }

    private void setupCardPositions() {
        effectCards.clear();

        // Posisi center screen
        float centerX = SCREEN_WIDTH / 2f;
        float centerY = SCREEN_HEIGHT / 2f;

        // Total width untuk 3 cards + spacing
        float totalWidth = (CARD_WIDTH * 3) + (CARD_SPACING * 2);
        float startX = centerX - totalWidth / 2;

        // Card 1 (left)
        effectCards.add(new Rectangle(startX, centerY - CARD_HEIGHT / 2, CARD_WIDTH, CARD_HEIGHT));

        // Card 2 (center)
        effectCards.add(
                new Rectangle(startX + CARD_WIDTH + CARD_SPACING, centerY - CARD_HEIGHT / 2, CARD_WIDTH, CARD_HEIGHT));

        // Card 3 (right)
        effectCards.add(new Rectangle(startX + (CARD_WIDTH + CARD_SPACING) * 2, centerY - CARD_HEIGHT / 2, CARD_WIDTH,
                CARD_HEIGHT));
    }

    @Override
    public void render(float delta) {
        // Clear screen dengan dark overlay
        Gdx.gl.glClearColor(0, 0, 0, 0.8f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Update hover state
        updateHover();

        // Handle click
        handleClick();

        // Draw UI
        drawOverlay();
        drawCards();
        drawText();
    }

    private void updateHover() {
        int mouseX = Gdx.input.getX();
        int mouseY = SCREEN_HEIGHT - Gdx.input.getY(); // Flip Y coordinate

        hoveredCardIndex = -1;

        for (int i = 0; i < effectCards.size; i++) {
            Rectangle card = effectCards.get(i);
            if (card.contains(mouseX, mouseY)) {
                hoveredCardIndex = i;
                break;
            }
        }
    }

    private void handleClick() {
        if (Gdx.input.justTouched() && hoveredCardIndex >= 0) {
            // Apply effect yang dipilih
            LevelUpEffect selectedEffect = selectedEffects.get(hoveredCardIndex);
            selectedEffect.apply(player);

            // Clear level-up pending flag
            player.setLevelUpPending(false);

            // Resume game
            gameScreen.resumeFromLevelUp();
            game.setScreen(gameScreen);

            System.out.println("[LevelUpScreen] Effect selected: " + selectedEffect.getName());
        }
    }

    private void drawOverlay() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.7f);
        shapeRenderer.rect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
        shapeRenderer.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void drawCards() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        for (int i = 0; i < effectCards.size; i++) {
            Rectangle card = effectCards.get(i);

            // Card background (highlight on hover)
            if (i == hoveredCardIndex) {
                shapeRenderer.setColor(0.4f, 0.4f, 0.6f, 1f); // Light purple on hover
            } else {
                shapeRenderer.setColor(0.2f, 0.2f, 0.3f, 1f); // Dark gray
            }
            shapeRenderer.rect(card.x, card.y, card.width, card.height);

            // Card border
            shapeRenderer.end();
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            Gdx.gl.glLineWidth(3);

            if (i == hoveredCardIndex) {
                shapeRenderer.setColor(Color.CYAN);
            } else {
                shapeRenderer.setColor(Color.LIGHT_GRAY);
            }
            shapeRenderer.rect(card.x, card.y, card.width, card.height);
            Gdx.gl.glLineWidth(1);

            shapeRenderer.end();
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        }

        shapeRenderer.end();
    }

    private void drawText() {
        batch.begin();

        // Title: "LEVEL UP!"
        titleFont.setColor(Color.GOLD);
        String titleText = "LEVEL UP!";

        // Gunakan GlyphLayout untuk mendapatkan width yang akurat
        com.badlogic.gdx.graphics.g2d.GlyphLayout titleLayout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(titleFont,
                titleText);
        float titleX = (SCREEN_WIDTH - titleLayout.width) / 2f;
        titleFont.draw(batch, titleText, titleX, SCREEN_HEIGHT - 150);

        // Current level
        font.setColor(Color.WHITE);
        String levelText = "Level " + player.getLevel();
        com.badlogic.gdx.graphics.g2d.GlyphLayout levelLayout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(font,
                levelText);
        float levelX = (SCREEN_WIDTH - levelLayout.width) / 2f;
        font.draw(batch, levelText, levelX, SCREEN_HEIGHT - 230);

        // Effect names and descriptions
        for (int i = 0; i < selectedEffects.size; i++) {
            LevelUpEffect effect = selectedEffects.get(i);
            Rectangle card = effectCards.get(i);

            // Effect name (centered in card, top)
            font.setColor(Color.YELLOW);
            String name = effect.getName();
            com.badlogic.gdx.graphics.g2d.GlyphLayout nameLayout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(font,
                    name);
            font.draw(batch, name,
                    card.x + (card.width - nameLayout.width) / 2,
                    card.y + card.height - 30);

            // Effect description (centered in card, middle)
            font.setColor(Color.LIGHT_GRAY);
            String desc = effect.getDescription();
            com.badlogic.gdx.graphics.g2d.GlyphLayout descLayout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(font,
                    desc);
            font.draw(batch, desc,
                    card.x + (card.width - descLayout.width) / 2,
                    card.y + card.height / 2 + 10);

            // Hover hint
            if (i == hoveredCardIndex) {
                font.setColor(Color.CYAN);
                String hint = "Klik untuk pilih";
                com.badlogic.gdx.graphics.g2d.GlyphLayout hintLayout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(
                        font, hint);
                font.draw(batch, hint,
                        card.x + (card.width - hintLayout.width) / 2,
                        card.y + 30);
            }
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
