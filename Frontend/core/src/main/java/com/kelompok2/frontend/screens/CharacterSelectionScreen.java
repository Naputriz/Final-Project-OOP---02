package com.kelompok2.frontend.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
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
    private Texture lockedTexture;

    // Character data
    private CharacterInfo[] characters;
    private int hoveredIndex = 0; // Currently hovered character
    private int selectedIndex = -1; // Selected character (-1 = none)

    // UI layout constants (centered for 1920x1080) (Belum gw cek buat dimensi lain,
    // kalo ga centered nanti ubah)
    private static final float GRID_X = 400; // Centered left side
    private static final float GRID_Y = 400; // Centered vertically
    private static final float PORTRAIT_SIZE = 100;
    private static final float PORTRAIT_SPACING = 120;

    private static final float PREVIEW_X = 1300; // Right side (moved to avoid overlap)
    private static final float PREVIEW_Y = 400;
    private static final float PREVIEW_SIZE = 256;

    private static final float STATS_PANEL_X = 400;
    private static final float STATS_PANEL_Y = 100;
    private static final float STATS_PANEL_WIDTH = 300;
    private static final float STATS_PANEL_HEIGHT = 200;

    private static final float SKILL_PANEL_X = 750;
    private static final float SKILL_PANEL_Y = 100;
    private static final float SKILL_PANEL_WIDTH = 600;
    private static final float SKILL_PANEL_HEIGHT = 200;

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
        font.getData().setScale(1.5f);

        titleFont = new BitmapFont();
        titleFont.getData().setScale(3f);

        stateTime = 0;
        lockedTexture = AssetManager.getInstance().loadTexture("question_mark.png");
        // Initialize character data
        initializeCharacters();
    }

    private void initializeCharacters() {
        // Todo: bikin ini lebioh scalable, jika mungkin, biar ga nambah2 per karakter
        characters = new CharacterInfo[10]; // Updated to 10 characters

        // Ryze - The Ghost of Insania
        Texture ryzeSheet = AssetManager.getInstance().loadTexture("Ryze/pcgp-ryze-idle.png");
        characters[0] = new CharacterInfo(
                "Ryze",
                "The Ghost of Insania",
                100, 30, 10, 5, 200,
                "Spectral Body",
                "Invulnerability for 3 seconds.\nCooldown: 15s",
                "Ryze/pcgp-ryze-idle.png",
                ryzeSheet,
                3, 3, 8, 0.1f); // 8 frames in 3x3 grid

        // Isolde - The Frost Kaiser
        Texture isoldeSheet = AssetManager.getInstance().loadTexture("FrostPlaceholderSprite.png");
        characters[1] = new CharacterInfo(
                "Isolde",
                "The Frost Kaiser",
                120, 15, 40, 10, 180,
                "Glacial Breath",
                "Cone attack that freezes enemies.\nDamage: Arts x1.0, Cooldown: 10s",
                "FrostPlaceholderSprite.png",
                isoldeSheet,
                10, 10, 100, 0.1f); // 100 frames in 10x10 grid

        // Insania - The Chaos Kaiser
        Texture insaniaSheet = AssetManager.getInstance().loadTexture("Insania/pcgp-insania-idle.png");
        characters[2] = new CharacterInfo(
                "Insania",
                "The Chaos Kaiser",
                110, 35, 25, 5, 180,
                "Mind Fracture",
                "AoE Insanity debuff. Enemies move\nrandomly and attack each other.\nDuration: 5s, Cooldown: 10s",
                "Insania/pcgp-insania-idle.png",
                insaniaSheet,
                2, 2, 4, 0.2f); // 4 frames in 2x2 grid, slower animation (0.2s per frame)

        // Blaze - The Flame Kaiser
        Texture blazeSheet = AssetManager.getInstance().loadTexture("BlazeCharacterPlaceholder.png");
        characters[3] = new CharacterInfo(
                "Blaze",
                "The Flame Kaiser",
                110, 25, 40, 5, 180,
                "Hellfire Pillar",
                "Summons a damage pillar at cursor.\\nDamage: High, Cooldown: 5s",
                "BlazeCharacterPlaceholder.png",
                blazeSheet,
                4, 23, 92, 0.1f); // 4 columns × 23 rows = 92 frames

        // Whisperwind - The Silent Caster
        Texture whisperwindSprite = AssetManager.getInstance().loadTexture("WhisperwindPlaceholder.png");
        characters[4] = new CharacterInfo(
                "Whisperwind",
                "The Silent Caster",
                110, 15, 38, 12, 190,
                "Hurricane Bind",
                "Wind ball with knockback and stun.\\nDamage: Arts x2.0, Cooldown: 10s",
                "WhisperwindPlaceholder.png",
                whisperwindSprite,
                1, 1, 1, 0.1f); // Stationary sprite

        // Aelita - The Evergreen Healer
        Texture aelitaSprite = AssetManager.getInstance().loadTexture("AelitaPlaceholder.png");
        characters[5] = new CharacterInfo(
                "Aelita",
                "The Evergreen Healer",
                140, 15, 30, 20, 170,
                "Verdant Domain",
                "Consume 25% HP to create a healing\\nzone. Heals 50% HP over 5s and boosts\\nATK/Arts +25%. Cooldown: 15s",
                "AelitaPlaceholder.png",
                aelitaSprite,
                1, 1, 1, 0.1f); // Stationary sprite (placeholder)

        // Aegis - The Impenetrable Shield
        Texture aegisSprite = AssetManager.getInstance().loadTexture("Aegis/pcgp-aegis.png");
        characters[6] = new CharacterInfo(
                "Aegis",
                "The Impenetrable Shield",
                150, 15, 10, 40, 170,
                "Here, I shall stand!",
                "Immobilized for 2s, blocks frontal damage\\nand reflects 50% damage back.\\nCooldown: 10s",
                "Aegis/pcgp-aegis.png",
                aegisSprite,
                2, 2, 4, 0.15f);

        // Lumi - The Pale Renegade
        Texture lumiSprite = AssetManager.getInstance().loadTexture("LumiPlaceholder.png");
        characters[7] = new CharacterInfo(
                "Lumi",
                "The Pale Renegade",
                90, 45, 10, 15, 210,
                "Returnious Pull",
                "Marks enemies with attacks.\nSkill pulls marked enemy + Dmg + Stun.\nCooldown: 12s",
                "LumiPlaceholder.png",
                lumiSprite,
                1, 1, 1, 0.1f);

        // Alice - The Reckless Princess
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

        // Clear screen
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);

        handleInput();

        // PHASE 1: Render all shapes (backgrounds and borders)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Portrait backgrounds - 3 column layout (3 per column now)
        for (int i = 0; i < characters.length; i++) {
            float x, y;
            // Calculate column (0, 1, 2) and row (0, 1, 2) for 3x3 grid
            int col = i / 3;
            int row = i % 3;

            x = GRID_X + col * PORTRAIT_SPACING * 2;
            y = GRID_Y + (1 - row) * PORTRAIT_SPACING; // 1-row to invert (top first)

            if (i == hoveredIndex) {
                shapeRenderer.setColor(0.3f, 0.6f, 1f, 0.5f); // Blue highlight
            } else {
                shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 0.8f); // Dark gray
            }
            shapeRenderer.rect(x, y, PORTRAIT_SIZE, PORTRAIT_SIZE);
        }

        // Preview frame background
        shapeRenderer.setColor(0.15f, 0.15f, 0.15f, 0.9f);
        shapeRenderer.rect(PREVIEW_X - 20, PREVIEW_Y - 20, PREVIEW_SIZE + 40, PREVIEW_SIZE + 40);

        // Stats panel background
        shapeRenderer.rect(STATS_PANEL_X, STATS_PANEL_Y, STATS_PANEL_WIDTH, STATS_PANEL_HEIGHT);

        // Skill panel background
        shapeRenderer.rect(SKILL_PANEL_X, SKILL_PANEL_Y, SKILL_PANEL_WIDTH, SKILL_PANEL_HEIGHT);

        shapeRenderer.end();

        // PHASE 2: Render borders
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        Gdx.gl.glLineWidth(2);
        shapeRenderer.setColor(Color.WHITE);

        // Portrait borders - 3 column layout
        for (int i = 0; i < characters.length; i++) {
            float x, y;
            int col = i / 3;
            int row = i % 3;
            x = GRID_X + col * PORTRAIT_SPACING * 2;
            y = GRID_Y + (1 - row) * PORTRAIT_SPACING;
            shapeRenderer.rect(x, y, PORTRAIT_SIZE, PORTRAIT_SIZE);
        }

        // Stats panel border
        shapeRenderer.rect(STATS_PANEL_X, STATS_PANEL_Y, STATS_PANEL_WIDTH, STATS_PANEL_HEIGHT);

        // Skill panel border
        shapeRenderer.rect(SKILL_PANEL_X, SKILL_PANEL_Y, SKILL_PANEL_WIDTH, SKILL_PANEL_HEIGHT);

        Gdx.gl.glLineWidth(1);
        shapeRenderer.end();

        // PHASE 3: Render all sprites and text
        batch.begin();

        // Title
        titleFont.setColor(Color.WHITE);
        titleFont.draw(batch, "Character Select", 760, 950); // Centered

        // Character grid portraits
        for (int i = 0; i < characters.length; i++) {
            drawPortraitSprite(i);
        }

        // Character preview (large, with animation)
        drawPreview(hoveredIndex);

        // Stats panel
        drawStatsText(hoveredIndex);

        // Skill panel
        drawSkillText(hoveredIndex);

        // Instructions
        font.setColor(Color.LIGHT_GRAY);
        font.draw(batch, "Hover to preview | Click to select | ESC to go back", 250, 30);

        batch.end();
    }

    private void drawPortraitSprite(int index) {
        CharacterInfo character = characters[index];
        float x, y;

        // Calculate position based on 3x3 grid layout
        int col = index / 3;
        int row = index % 3;
        x = GRID_X + col * PORTRAIT_SPACING * 2;
        y = GRID_Y + (1 - row) * PORTRAIT_SPACING;

        // CEK STATUS UNLOCK
        boolean isUnlocked = GameManager.getInstance().isCharacterUnlocked(character.name);

        // LOGIKA GAMBAR
        if (isUnlocked) {
            // Jika terbuka: Render animasi/gambar asli
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

        // LOGIKA TEXT (NAMA & JULUKAN)
        font.setColor(Color.WHITE);
        if (isUnlocked) {
            font.draw(batch, character.name, x + PORTRAIT_SIZE + 10, y + PORTRAIT_SIZE / 2 + 10);
            font.setColor(Color.LIGHT_GRAY);
            font.getData().setScale(1f);
            font.draw(batch, character.title, x + PORTRAIT_SIZE + 10, y + PORTRAIT_SIZE / 2 - 10);
        } else {
            // Jika terkunci
            font.draw(batch, "???", x + PORTRAIT_SIZE + 10, y + PORTRAIT_SIZE / 2 + 10);
            font.setColor(Color.LIGHT_GRAY);
            font.getData().setScale(1f);
            font.draw(batch, "???", x + PORTRAIT_SIZE + 10, y + PORTRAIT_SIZE / 2 - 10);
        }
        font.getData().setScale(1.5f); // Kembalikan scale
    }

    private void drawPreview(int index) {
        CharacterInfo character = characters[index];
        boolean isUnlocked = GameManager.getInstance().isCharacterUnlocked(character.name);

        if (isUnlocked) {
            // Draw animated character or static sprite (Normal)
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
            // Draw Locked Preview
            Texture lockedTexture = AssetManager.getInstance().getTexture("question_mark.png");
            if (lockedTexture != null) {
                batch.draw(lockedTexture, PREVIEW_X, PREVIEW_Y, PREVIEW_SIZE, PREVIEW_SIZE);
            }
        }
    }

    private void drawStatsText(int index) {
        CharacterInfo character = characters[index];
        boolean isUnlocked = GameManager.getInstance().isCharacterUnlocked(character.name);

        font.setColor(Color.YELLOW);
        font.draw(batch, "Stats", STATS_PANEL_X + 10, STATS_PANEL_Y + STATS_PANEL_HEIGHT - 10);

        font.setColor(Color.WHITE);
        float yPos = STATS_PANEL_Y + STATS_PANEL_HEIGHT - 50;

        if (isUnlocked) {
            font.draw(batch, "HP:    " + (int) character.hp, STATS_PANEL_X + 10, yPos);
            yPos -= 30;
            font.draw(batch, "ATK:   " + (int) character.atk, STATS_PANEL_X + 10, yPos);
            yPos -= 30;
            font.draw(batch, "Arts:  " + (int) character.arts, STATS_PANEL_X + 10, yPos);
            yPos -= 30;
            font.draw(batch, "DEF:   " + (int) character.def, STATS_PANEL_X + 10, yPos);
            yPos -= 30;
            font.draw(batch, "Speed: " + (int) character.speed, STATS_PANEL_X + 10, yPos);
        } else {
            font.draw(batch, "HP:    ???", STATS_PANEL_X + 10, yPos);
            yPos -= 30;
            font.draw(batch, "ATK:   ???", STATS_PANEL_X + 10, yPos);
            yPos -= 30;
            font.draw(batch, "Arts:  ???", STATS_PANEL_X + 10, yPos);
            yPos -= 30;
            font.draw(batch, "DEF:   ???", STATS_PANEL_X + 10, yPos);
            yPos -= 30;
            font.draw(batch, "Speed: ???", STATS_PANEL_X + 10, yPos);
        }
    }

    private void drawSkillText(int index) {
        CharacterInfo character = characters[index];
        boolean isUnlocked = GameManager.getInstance().isCharacterUnlocked(character.name);

        font.setColor(Color.YELLOW);
        font.draw(batch, "Innate Skill", SKILL_PANEL_X + 10, SKILL_PANEL_Y + SKILL_PANEL_HEIGHT - 10);

        font.setColor(Color.CYAN);
        if (isUnlocked) {
            font.draw(batch, character.skillName, SKILL_PANEL_X + 10, SKILL_PANEL_Y + SKILL_PANEL_HEIGHT - 50);
        } else {
            font.draw(batch, "???", SKILL_PANEL_X + 10, SKILL_PANEL_Y + SKILL_PANEL_HEIGHT - 50);
        }

        font.setColor(Color.WHITE);
        font.getData().setScale(1.2f);

        String descriptionText;
        if (isUnlocked) {
            descriptionText = character.skillDescription;
        } else {
            descriptionText = "???";
        }

        // Draw multi-line description
        String[] lines = descriptionText.split("\n");
        float yPos = SKILL_PANEL_Y + SKILL_PANEL_HEIGHT - 90;
        for (String line : lines) {
            font.draw(batch, line, SKILL_PANEL_X + 10, yPos);
            yPos -= 25;
        }
        font.getData().setScale(1.5f);
    }

    private void handleInput() {
        // Mouse hover detection
        int mouseX = Gdx.input.getX();
        int mouseY = Gdx.input.getY();

        // Convert screen coordinates to world coordinates using viewport unprojection
        com.badlogic.gdx.math.Vector3 screenCoords = new com.badlogic.gdx.math.Vector3(mouseX, mouseY, 0);
        com.badlogic.gdx.math.Vector3 worldCoords = camera.unproject(screenCoords, viewport.getScreenX(),
                viewport.getScreenY(), viewport.getScreenWidth(), viewport.getScreenHeight());
        float worldX = worldCoords.x;
        float worldY = worldCoords.y;

        // Check hover over portraits - 3x3 grid layout
        for (int i = 0; i < characters.length; i++) {
            float x, y;
            int col = i / 3;
            int row = i % 3;
            x = GRID_X + col * PORTRAIT_SPACING * 2;
            y = GRID_Y + (1 - row) * PORTRAIT_SPACING;

            Rectangle portraitBounds = new Rectangle(x, y, PORTRAIT_SIZE, PORTRAIT_SIZE);

            if (portraitBounds.contains(worldX, worldY)) {
                hoveredIndex = i; // Hover tetap jalan (biar player bisa liat info "???")

                // Click to select
                if (Gdx.input.justTouched()) {
                    // CEK DULU APAKAH UNLOCKED
                    if (GameManager.getInstance().isCharacterUnlocked(characters[i].name)) {
                        selectedIndex = i;
                        startGame();
                    } else {
                        System.out.println("Character Locked: " + characters[i].name);
                        // Opsional: Play sound effect "Access Denied" disini
                    }
                }
                break;
            }
        }

        // ESC to go back
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new MainMenuScreen(game));
        }
    }

    private void startGame() {
        System.out.println("Selected character: " + characters[selectedIndex].name);
        // Note: Don't dispose while screen is still active - causes crash
        // LibGDX will garbage collect this screen after transition
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
