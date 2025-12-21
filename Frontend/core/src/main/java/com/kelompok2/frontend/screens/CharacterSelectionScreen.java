package com.kelompok2.frontend.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.kelompok2.frontend.Main;
import com.kelompok2.frontend.managers.AssetManager;
import com.kelompok2.frontend.models.CharacterInfo;
import com.kelompok2.frontend.managers.GameManager;

public class CharacterSelectionScreen extends ScreenAdapter {
    private final Main game;
    private OrthographicCamera camera;
    private Viewport viewport;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;
    private BitmapFont titleFont;
    private Texture lockedTexture; // [FITUR TEMAN]

    // Character data
    private CharacterInfo[] characters;
    private int hoveredIndex = 0;
    private int selectedIndex = -1;

    // --- [PERBAIKAN LAYOUT] ---
    // Menggunakan koordinat yang luas agar tidak menabrak
    private static final float GRID_START_X = 150;
    private static final float GRID_START_Y = 650; // Tinggi agar tidak kena panel bawah

    private static final float PORTRAIT_SIZE = 100;

    // Spacing Lebar agar teks nama panjang muat
    private static final float COL_WIDTH = 420;
    private static final float ROW_HEIGHT = 140;

    // Preview settings (Paling Kanan)
    private static final float PREVIEW_X = 1450;
    private static final float PREVIEW_Y = 500;
    private static final float PREVIEW_SIZE = 300;

    // Bottom Panels (Di Bawah)
    private static final float STATS_PANEL_X = 150;
    private static final float STATS_PANEL_Y = 50;
    private static final float STATS_PANEL_WIDTH = 350;
    private static final float STATS_PANEL_HEIGHT = 220;

    private static final float SKILL_PANEL_X = 550;
    private static final float SKILL_PANEL_Y = 50;
    private static final float SKILL_PANEL_WIDTH = 800;
    private static final float SKILL_PANEL_HEIGHT = 220;

    // Animation
    private float stateTime;

    public CharacterSelectionScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(1920, 1080, camera);
        camera.position.set(viewport.getWorldWidth() / 2, viewport.getWorldHeight() / 2, 0);

        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        font = new BitmapFont();
        font.getData().setScale(1.3f); // Font size disesuaikan layout

        titleFont = new BitmapFont();
        titleFont.getData().setScale(3f);

        stateTime = 0;

        // Load Locked Texture (Fitur Teman)
        // Pastikan file "question_mark.png" ada di assets, kalau tidak ganti placeholder lain
        if (AssetManager.getInstance().getTexture("question_mark.png") == null) {
            // Fallback load jika belum diload sebelumnya
            lockedTexture = AssetManager.getInstance().loadTexture("question_mark.png");
        } else {
            lockedTexture = AssetManager.getInstance().getTexture("question_mark.png");
        }

        initializeCharacters();
    }

    private void initializeCharacters() {
        // Todo: bikin ini lebioh scalable, jika mungkin, biar ga nambah2 per karakter
        characters = new CharacterInfo[10]; // Updated to 10 characters

        // Ryze
        Texture ryzeSheet = AssetManager.getInstance().loadTexture("Ryze/pcgp-ryze-idle.png");
        characters[0] = new CharacterInfo(
            "Ryze", "The Ghost of Insania",
            100, 30, 10, 5, 200,
            "Spectral Body", "Invulnerability for 3 seconds.\nCooldown: 15s",
            "Ryze/pcgp-ryze-idle.png", ryzeSheet, 3, 3, 8, 0.1f);

        // Isolde
        Texture isoldeSheet = AssetManager.getInstance().loadTexture("FrostPlaceholderSprite.png");
        characters[1] = new CharacterInfo(
            "Isolde", "The Frost Kaiser",
            120, 15, 40, 10, 180,
            "Glacial Breath", "Cone attack that freezes enemies.\nDmg: Arts x1.0, CD: 10s",
            "FrostPlaceholderSprite.png", isoldeSheet, 10, 10, 100, 0.1f);

        // Insania
        Texture insaniaSheet = AssetManager.getInstance().loadTexture("Insania/pcgp-insania-idle.png");
        characters[2] = new CharacterInfo(
            "Insania", "The Chaos Kaiser",
            110, 35, 25, 5, 180,
            "Mind Fracture", "AoE Insanity debuff. Enemies move\nrandomly & attack each other.\nDur: 5s, CD: 10s",
            "Insania/pcgp-insania-idle.png", insaniaSheet, 2, 2, 4, 0.2f);

        // Blaze
        Texture blazeSheet = AssetManager.getInstance().loadTexture("BlazeCharacterPlaceholder.png");
        characters[3] = new CharacterInfo(
            "Blaze", "The Flame Kaiser",
            110, 25, 40, 5, 180,
            "Hellfire Pillar", "Summons a damage pillar at cursor.\nDamage: High, CD: 5s",
            "BlazeCharacterPlaceholder.png", blazeSheet, 4, 23, 92, 0.1f);

        // Whisperwind
        Texture whisperwindSprite = AssetManager.getInstance().loadTexture("WhisperwindPlaceholder.png");
        characters[4] = new CharacterInfo(
            "Whisperwind", "The Silent Caster",
            110, 15, 38, 12, 190,
            "Hurricane Bind", "Wind ball with knockback & stun.\nDmg: Arts x2.0, CD: 10s",
            "WhisperwindPlaceholder.png", whisperwindSprite, 1, 1, 1, 0.1f);

        // Aelita
        Texture aelitaSprite = AssetManager.getInstance().loadTexture("AelitaPlaceholder.png");
        characters[5] = new CharacterInfo(
            "Aelita", "The Evergreen Healer",
            140, 15, 30, 20, 170,
            "Verdant Domain", "Consume 25% HP to create healing zone.\nHeals 50% HP over 5s.\nBoosts ATK/Arts +25%. CD: 15s",
            "AelitaPlaceholder.png", aelitaSprite, 1, 1, 1, 0.1f);

        // Aegis
        Texture aegisSprite = AssetManager.getInstance().loadTexture("Aegis/pcgp-aegis.png");
        characters[6] = new CharacterInfo(
            "Aegis", "The Impenetrable Shield",
            150, 15, 10, 40, 170,
            "Here, I shall stand!", "Immobilized for 2s, blocks frontal\ndamage & reflects 50% back.\nCD: 10s",
            "Aegis/pcgp-aegis.png", aegisSprite, 2, 2, 4, 0.15f);

        // Lumi
        Texture lumiSprite = AssetManager.getInstance().loadTexture("LumiPlaceholder.png");
        characters[7] = new CharacterInfo(
            "Lumi", "The Pale Renegade",
            90, 45, 10, 15, 210,
            "Returnious Pull", "Marks enemies with attacks.\nSkill pulls marked enemy + Stun.\nCD: 12s",
            "LumiPlaceholder.png", lumiSprite, 1, 1, 1, 0.1f);

        // Alice
        Texture aliceSprite = AssetManager.getInstance().loadTexture("AlicePlaceholder.png");
        characters[8] = new CharacterInfo(
                "Alice",
                "The Reckless Princess",
                100, 40, 10, 10, 200,
                "Feral Rush",
                "Dashes forward rapidly and unleashes\n5x scratch attacks.\nCooldown: 5s",
                "AlicePlaceholder.png",
                aliceSprite,
                1, 1, 1, 0.1f); // 1x1 grid (no animation)

        // Kei - The Phantom Hunter
        Texture keiSheet = AssetManager.getInstance().loadTexture("FrostPlaceholderSprite.png");
        characters[9] = new CharacterInfo(
                "Kei",
                "The Phantom Hunter",
                95, 20, 45, 10, 190,
                "Phantom Haze",
                "Releases a hallucinogenic mist.\nConfuses enemies (move away).\nCooldown: 12s",
                "FrostPlaceholderSprite.png", // Using placeholder
                keiSheet,
                10, 10, 100, 0.1f);
    }

    @Override
    public void render(float delta) {
        stateTime += delta;

        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);

        handleInput();

        // PHASE 1: Render Background Shapes
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // 1. Grid Backgrounds (Menggunakan Method Layout Baru)
        for (int i = 0; i < characters.length; i++) {
            float x = getColX(i);
            float y = getRowY(i);

            if (i == hoveredIndex) {
                shapeRenderer.setColor(0.3f, 0.6f, 1f, 0.5f); // Highlight Blue
            } else {
                shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 0.8f); // Dark Gray
            }
            shapeRenderer.rect(x, y, PORTRAIT_SIZE, PORTRAIT_SIZE);
        }

        // 2. Preview Background
        shapeRenderer.setColor(0.15f, 0.15f, 0.15f, 0.9f);
        shapeRenderer.rect(PREVIEW_X - 20, PREVIEW_Y - 20, PREVIEW_SIZE + 40, PREVIEW_SIZE + 40);

        // 3. Stats Panel Background
        shapeRenderer.setColor(0.1f, 0.1f, 0.1f, 0.9f);
        shapeRenderer.rect(STATS_PANEL_X, STATS_PANEL_Y, STATS_PANEL_WIDTH, STATS_PANEL_HEIGHT);

        // 4. Skill Panel Background
        shapeRenderer.rect(SKILL_PANEL_X, SKILL_PANEL_Y, SKILL_PANEL_WIDTH, SKILL_PANEL_HEIGHT);

        shapeRenderer.end();

        // PHASE 2: Render Borders
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        Gdx.gl.glLineWidth(2);
        shapeRenderer.setColor(Color.WHITE);

        for (int i = 0; i < characters.length; i++) {
            shapeRenderer.rect(getColX(i), getRowY(i), PORTRAIT_SIZE, PORTRAIT_SIZE);
        }

        shapeRenderer.rect(STATS_PANEL_X, STATS_PANEL_Y, STATS_PANEL_WIDTH, STATS_PANEL_HEIGHT);
        shapeRenderer.rect(SKILL_PANEL_X, SKILL_PANEL_Y, SKILL_PANEL_WIDTH, SKILL_PANEL_HEIGHT);

        Gdx.gl.glLineWidth(1);
        shapeRenderer.end();

        // PHASE 3: Render Sprites & Text
        batch.begin();

        titleFont.setColor(Color.WHITE);
        titleFont.draw(batch, "Character Select", 150, 1000); // Judul di Kiri Atas

        // Render Grid Items
        for (int i = 0; i < characters.length; i++) {
            drawPortraitSprite(i);
        }

        // Render Details
        drawPreview(hoveredIndex);
        drawStatsText(hoveredIndex);
        drawSkillText(hoveredIndex);

        font.setColor(Color.LIGHT_GRAY);
        font.draw(batch, "Hover to preview | Click to select | ESC to go back", 1500, 30);

        batch.end();
    }

    // --- HELPER UNTUK LAYOUT GRID ---
    private float getColX(int index) {
        int col = index % 3; // 3 kolom
        return GRID_START_X + (col * COL_WIDTH);
    }

    private float getRowY(int index) {
        int row = index / 3;
        return GRID_START_Y - (row * ROW_HEIGHT);
    }

    private void drawPortraitSprite(int index) {
        CharacterInfo character = characters[index];
        float x = getColX(index);
        float y = getRowY(index);

        boolean isUnlocked = GameManager.getInstance().isCharacterUnlocked(character.name);

        // GAMBAR (Icon / Question Mark)
        if (isUnlocked) {
            if (character.animation != null) {
                TextureRegion firstFrame = character.animation.getKeyFrame(0);
                batch.draw(firstFrame, x, y, PORTRAIT_SIZE, PORTRAIT_SIZE);
            } else {
                Texture portrait = character.getPortraitTexture();
                if (portrait != null) {
                    batch.draw(portrait, x, y, PORTRAIT_SIZE, PORTRAIT_SIZE);
                }
            }
        } else {
            // Jika terkunci: Render gambar "?" (Menggunakan FrostPlaceholderSprite.png
            // sesuai request)
            // Pastikan string path ini sama persis dengan yang di-load di AssetManager
            Texture lockedTexture = AssetManager.getInstance().getTexture("question_mark.png");
            if (lockedTexture != null) {
                batch.draw(lockedTexture, x, y, PORTRAIT_SIZE, PORTRAIT_SIZE);
            }
        }

        // TEXT (Nama / ???)
        float textX = x + PORTRAIT_SIZE + 20;
        float textY = y + PORTRAIT_SIZE - 20;

        font.setColor(Color.WHITE);
        if (isUnlocked) {
            font.getData().setScale(1.5f);
            font.draw(batch, character.name, textX, textY);
            font.setColor(Color.LIGHT_GRAY);
            font.getData().setScale(1.1f);
            font.draw(batch, character.title, textX, textY - 35);
        } else {
            font.getData().setScale(1.5f);
            font.draw(batch, "???", textX, textY);
            font.setColor(Color.LIGHT_GRAY);
            font.getData().setScale(1.1f);
            font.draw(batch, "Locked Character", textX, textY - 35);
        }
    }

    private void drawPreview(int index) {
        CharacterInfo character = characters[index];
        boolean isUnlocked = GameManager.getInstance().isCharacterUnlocked(character.name);

        if (isUnlocked) {
            if (character.animation != null) {
                TextureRegion currentFrame = character.animation.getKeyFrame(stateTime);
                batch.draw(currentFrame, PREVIEW_X, PREVIEW_Y, PREVIEW_SIZE, PREVIEW_SIZE);
            } else {
                Texture preview = character.getPortraitTexture();
                if (preview != null) {
                    batch.draw(preview, PREVIEW_X, PREVIEW_Y, PREVIEW_SIZE, PREVIEW_SIZE);
                }
            }
        } else {
            if (lockedTexture != null) {
                batch.draw(lockedTexture, PREVIEW_X, PREVIEW_Y, PREVIEW_SIZE, PREVIEW_SIZE);
            }
        }
    }

    private void drawStatsText(int index) {
        CharacterInfo character = characters[index];
        boolean isUnlocked = GameManager.getInstance().isCharacterUnlocked(character.name);

        font.getData().setScale(1.3f);
        font.setColor(Color.YELLOW);
        font.draw(batch, "Stats", STATS_PANEL_X + 20, STATS_PANEL_Y + STATS_PANEL_HEIGHT - 20);

        font.setColor(Color.WHITE);
        float startY = STATS_PANEL_Y + STATS_PANEL_HEIGHT - 60;
        float gap = 30;

        if (isUnlocked) {
            font.draw(batch, "HP:    " + (int) character.hp, STATS_PANEL_X + 20, startY);
            font.draw(batch, "ATK:   " + (int) character.atk, STATS_PANEL_X + 20, startY - gap);
            font.draw(batch, "Arts:  " + (int) character.arts, STATS_PANEL_X + 20, startY - gap * 2);
            font.draw(batch, "DEF:   " + (int) character.def, STATS_PANEL_X + 20, startY - gap * 3);
            font.draw(batch, "Speed: " + (int) character.speed, STATS_PANEL_X + 20, startY - gap * 4);
        } else {
            font.draw(batch, "HP:    ???", STATS_PANEL_X + 20, startY);
            font.draw(batch, "ATK:   ???", STATS_PANEL_X + 20, startY - gap);
            font.draw(batch, "Arts:  ???", STATS_PANEL_X + 20, startY - gap * 2);
            font.draw(batch, "DEF:   ???", STATS_PANEL_X + 20, startY - gap * 3);
            font.draw(batch, "Speed: ???", STATS_PANEL_X + 20, startY - gap * 4);
        }
    }

    private void drawSkillText(int index) {
        CharacterInfo character = characters[index];
        boolean isUnlocked = GameManager.getInstance().isCharacterUnlocked(character.name);

        font.getData().setScale(1.3f);
        font.setColor(Color.YELLOW);
        font.draw(batch, "Innate Skill", SKILL_PANEL_X + 20, SKILL_PANEL_Y + SKILL_PANEL_HEIGHT - 20);

        font.setColor(Color.CYAN);
        font.getData().setScale(1.4f);

        if (isUnlocked) {
            font.draw(batch, character.skillName, SKILL_PANEL_X + 20, SKILL_PANEL_Y + SKILL_PANEL_HEIGHT - 60);
        } else {
            font.draw(batch, "???", SKILL_PANEL_X + 20, SKILL_PANEL_Y + SKILL_PANEL_HEIGHT - 60);
        }

        font.setColor(Color.WHITE);
        font.getData().setScale(1.2f);

        String descriptionText = isUnlocked ? character.skillDescription : "???";
        String[] lines = descriptionText.split("\n");
        float yPos = SKILL_PANEL_Y + SKILL_PANEL_HEIGHT - 100;
        for (String line : lines) {
            font.draw(batch, line, SKILL_PANEL_X + 20, yPos);
            yPos -= 30;
        }
    }

    private void handleInput() {
        int mouseX = Gdx.input.getX();
        int mouseY = Gdx.input.getY();

        com.badlogic.gdx.math.Vector3 screenCoords = new com.badlogic.gdx.math.Vector3(mouseX, mouseY, 0);
        com.badlogic.gdx.math.Vector3 worldCoords = camera.unproject(screenCoords, viewport.getScreenX(),
            viewport.getScreenY(), viewport.getScreenWidth(), viewport.getScreenHeight());
        float worldX = worldCoords.x;
        float worldY = worldCoords.y;

        for (int i = 0; i < characters.length; i++) {
            float x = getColX(i);
            float y = getRowY(i);

            // Hitbox diperlebar agar mencakup teks nama
            Rectangle bounds = new Rectangle(x, y, COL_WIDTH - 20, PORTRAIT_SIZE);

            if (bounds.contains(worldX, worldY)) {
                hoveredIndex = i; // Selalu update hover index

                if (Gdx.input.justTouched()) {
                    if (GameManager.getInstance().isCharacterUnlocked(characters[i].name)) {
                        selectedIndex = i;
                        startGame();
                    } else {
                        // Play sound locked effect (optional)
                        System.out.println("Character Locked: " + characters[i].name);
                    }
                }
                break;
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new MainMenuScreen(game));
        }
    }

    private void startGame() {
        System.out.println("Selected character: " + characters[selectedIndex].name);
        game.setScreen(new GameScreen(game, characters[selectedIndex].name));
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
        camera.position.set(viewport.getWorldWidth() / 2, viewport.getWorldHeight() / 2, 0);
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        font.dispose();
        titleFont.dispose();
    }
}
