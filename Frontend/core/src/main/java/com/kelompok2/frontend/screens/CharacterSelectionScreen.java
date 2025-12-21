package com.kelompok2.frontend.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.kelompok2.frontend.Main;
import com.kelompok2.frontend.managers.AssetManager;
import com.kelompok2.frontend.managers.GameManager;
import com.kelompok2.frontend.models.CharacterInfo;

public class CharacterSelectionScreen extends ScreenAdapter {
    private final Main game;
    private OrthographicCamera camera;
    private Viewport viewport;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;
    private BitmapFont titleFont;
    private Texture lockedTexture;

    private CharacterInfo[] characters;
    private int hoveredIndex = 0;
    private int selectedIndex = -1;

    // --- LAYOUT CONSTANTS (1920x1080) ---

    // 1. LEFT PANEL (SCROLLABLE LIST) - DIKECILKAN
    private static final float LIST_X = 100;
    private static final float LIST_Y = 100;
    private static final float LIST_WIDTH = 450; // [UBAH] Mengecil (sebelumnya 550)
    private static final float LIST_HEIGHT = 850;

    // Item di dalam list
    private static final float ITEM_HEIGHT = 140;
    private static final float ITEM_SPACING = 10;
    private static final float ICON_SIZE = 100;

    // 2. CENTER PANEL (PREVIEW) - DIPERBESAR
    private static final float PREVIEW_CENTER_X = 960;
    private static final float PREVIEW_CENTER_Y = 540;
    private static final float PREVIEW_SIZE = 700; // [UBAH] Membesar (sebelumnya 400)

    // 3. RIGHT PANELS (STATS & SKILL) - SIMETRIS
    private static final float RIGHT_PANEL_WIDTH = 450; // Sama dengan LIST_WIDTH
    // Posisi X agar jarak dari kanan layar (1920) sama dengan LIST_X (100)
    // 1920 - 100 - 450 = 1370
    private static final float RIGHT_PANEL_X = 1370;

    // Stats (Kanan Atas)
    private static final float STATS_Y = 600;
    private static final float STATS_HEIGHT = 350;

    // Skill (Kanan Bawah)
    private static final float SKILL_Y = 100;
    private static final float SKILL_HEIGHT = 450;

    // Scrolling Variables
    private float scrollY = 0;
    private float maxScrollY = 0;
    private Rectangle scissors = new Rectangle();
    private Rectangle clipBounds = new Rectangle(LIST_X, LIST_Y, LIST_WIDTH, LIST_HEIGHT);

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
        font.getData().setScale(1.3f);

        titleFont = new BitmapFont();
        titleFont.getData().setScale(3f);

        stateTime = 0;

        // Load Locked Texture
        if (AssetManager.getInstance().getTexture("question_mark.png") == null) {
            lockedTexture = AssetManager.getInstance().loadTexture("question_mark.png");
        } else {
            lockedTexture = AssetManager.getInstance().getTexture("question_mark.png");
        }

        initializeCharacters();

        // Hitung total tinggi konten untuk scrolling
        float totalContentHeight = characters.length * (ITEM_HEIGHT + ITEM_SPACING);
        maxScrollY = Math.max(0, totalContentHeight - LIST_HEIGHT);

        // Setup Input Processor
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean scrolled(float amountX, float amountY) {
                scrollY += amountY * 30f;
                scrollY = MathUtils.clamp(scrollY, 0, maxScrollY);
                return true;
            }

            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.ESCAPE) {
                    game.setScreen(new MainMenuScreen(game));
                    return true;
                }
                return false;
            }

            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                handleClick(screenX, screenY);
                return true;
            }
        });
    }

    private void initializeCharacters() {
        characters = new CharacterInfo[10];

        Texture ryzeSheet = AssetManager.getInstance().loadTexture("Ryze/pcgp-ryze-idle.png");
        characters[0] = new CharacterInfo("Ryze", "The Ghost of Insania", 100, 30, 10, 5, 200, "Spectral Body", "Invulnerability for 3s.\nCD: 15s", "Ryze/pcgp-ryze-idle.png", ryzeSheet, 3, 3, 8, 0.1f);

        Texture isoldeSheet = AssetManager.getInstance().loadTexture("FrostPlaceholderSprite.png");
        characters[1] = new CharacterInfo("Isolde", "The Frost Kaiser", 120, 15, 40, 10, 180, "Glacial Breath", "Cone freeze attack.\nCD: 10s", "FrostPlaceholderSprite.png", isoldeSheet, 10, 10, 100, 0.1f);

        Texture insaniaSheet = AssetManager.getInstance().loadTexture("Insania/pcgp-insania-idle.png");
        characters[2] = new CharacterInfo("Insania", "The Chaos Kaiser", 110, 35, 25, 5, 180, "Mind Fracture", "AoE Confusion.\nCD: 10s", "Insania/pcgp-insania-idle.png", insaniaSheet, 2, 2, 4, 0.2f);

        Texture blazeSheet = AssetManager.getInstance().loadTexture("BlazeCharacterPlaceholder.png");
        characters[3] = new CharacterInfo("Blaze", "The Flame Kaiser", 110, 25, 40, 5, 180, "Hellfire Pillar", "Summons fire pillar.\nCD: 5s", "BlazeCharacterPlaceholder.png", blazeSheet, 4, 23, 92, 0.1f);

        Texture whisperwindSprite = AssetManager.getInstance().loadTexture("WhisperwindPlaceholder.png");
        characters[4] = new CharacterInfo("Whisperwind", "The Silent Caster", 110, 15, 38, 12, 190, "Hurricane Bind", "Knockback wind ball.\nCD: 10s", "WhisperwindPlaceholder.png", whisperwindSprite, 1, 1, 1, 0.1f);

        Texture aelitaSprite = AssetManager.getInstance().loadTexture("AelitaPlaceholder.png");
        characters[5] = new CharacterInfo("Aelita", "The Evergreen Healer", 140, 15, 30, 20, 170, "Verdant Domain", "Healing Zone.\nCD: 15s", "AelitaPlaceholder.png", aelitaSprite, 1, 1, 1, 0.1f);

        Texture aegisSprite = AssetManager.getInstance().loadTexture("Aegis/pcgp-aegis.png");
        characters[6] = new CharacterInfo("Aegis", "The Impenetrable Shield", 150, 15, 10, 40, 170, "Here, I shall stand!", "Block frontal dmg.\nCD: 10s", "Aegis/pcgp-aegis.png", aegisSprite, 2, 2, 4, 0.15f);

        Texture lumiSprite = AssetManager.getInstance().loadTexture("LumiPlaceholder.png");
        characters[7] = new CharacterInfo("Lumi", "The Pale Renegade", 90, 45, 10, 15, 210, "Returnious Pull", "Pull marked enemy.\nCD: 12s", "LumiPlaceholder.png", lumiSprite, 1, 1, 1, 0.1f);

        Texture aliceSprite = AssetManager.getInstance().loadTexture("AlicePlaceholder.png");
        characters[8] = new CharacterInfo("Alice", "The Reckless Princess", 100, 40, 10, 10, 200, "Feral Rush", "Rapid dash attacks.\nCD: 5s", "AlicePlaceholder.png", aliceSprite, 1, 1, 1, 0.1f);

        Texture keiSheet = AssetManager.getInstance().loadTexture("FrostPlaceholderSprite.png");
        characters[9] = new CharacterInfo("Kei", "The Phantom Hunter", 95, 20, 45, 10, 190, "Phantom Haze", "Confuse enemies.\nCD: 12s", "FrostPlaceholderSprite.png", keiSheet, 10, 10, 100, 0.1f);
    }

    private void handleClick(int screenX, int screenY) {
        com.badlogic.gdx.math.Vector3 worldCoords = camera.unproject(new com.badlogic.gdx.math.Vector3(screenX, screenY, 0),
            viewport.getScreenX(), viewport.getScreenY(), viewport.getScreenWidth(), viewport.getScreenHeight());

        float worldX = worldCoords.x;
        float worldY = worldCoords.y;

        if (worldX >= LIST_X && worldX <= LIST_X + LIST_WIDTH &&
            worldY >= LIST_Y && worldY <= LIST_Y + LIST_HEIGHT) {

            float relativeY = (LIST_Y + LIST_HEIGHT) - worldY + scrollY;
            int index = (int) (relativeY / (ITEM_HEIGHT + ITEM_SPACING));

            if (index >= 0 && index < characters.length) {
                if (GameManager.getInstance().isCharacterUnlocked(characters[index].name)) {
                    selectedIndex = index;
                    startGame();
                } else {
                    System.out.println("Character Locked!");
                }
            }
        }
    }

    private void handleHover() {
        int mouseX = Gdx.input.getX();
        int mouseY = Gdx.input.getY();

        com.badlogic.gdx.math.Vector3 worldCoords = camera.unproject(new com.badlogic.gdx.math.Vector3(mouseX, mouseY, 0),
            viewport.getScreenX(), viewport.getScreenY(), viewport.getScreenWidth(), viewport.getScreenHeight());

        if (worldCoords.x >= LIST_X && worldCoords.x <= LIST_X + LIST_WIDTH &&
            worldCoords.y >= LIST_Y && worldCoords.y <= LIST_Y + LIST_HEIGHT) {

            float relativeY = (LIST_Y + LIST_HEIGHT) - worldCoords.y + scrollY;
            int index = (int) (relativeY / (ITEM_HEIGHT + ITEM_SPACING));

            if (index >= 0 && index < characters.length) {
                hoveredIndex = index;
            }
        }
    }

    @Override
    public void render(float delta) {
        stateTime += delta;
        handleHover();

        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);

        // --- 1. RENDER BACKGROUNDS ---
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Preview Center (Diperbesar)
        shapeRenderer.setColor(0.15f, 0.15f, 0.15f, 1);
        float previewFrameSize = PREVIEW_SIZE + 40;
        shapeRenderer.rect(PREVIEW_CENTER_X - previewFrameSize/2, PREVIEW_CENTER_Y - previewFrameSize/2, previewFrameSize, previewFrameSize);

        // Stats Right Top
        shapeRenderer.setColor(0.1f, 0.1f, 0.1f, 1);
        shapeRenderer.rect(RIGHT_PANEL_X, STATS_Y, RIGHT_PANEL_WIDTH, STATS_HEIGHT);

        // Skill Right Bottom
        shapeRenderer.rect(RIGHT_PANEL_X, SKILL_Y, RIGHT_PANEL_WIDTH, SKILL_HEIGHT);
        shapeRenderer.end();

        // --- 2. RENDER BORDERS ---
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        Gdx.gl.glLineWidth(2);
        shapeRenderer.setColor(Color.WHITE);

        // List Kiri
        shapeRenderer.rect(LIST_X - 10, LIST_Y - 10, LIST_WIDTH + 20, LIST_HEIGHT + 20);
        // Stats
        shapeRenderer.rect(RIGHT_PANEL_X, STATS_Y, RIGHT_PANEL_WIDTH, STATS_HEIGHT);
        // Skill
        shapeRenderer.rect(RIGHT_PANEL_X, SKILL_Y, RIGHT_PANEL_WIDTH, SKILL_HEIGHT);
        shapeRenderer.end();

        // --- 3. RENDER TEXT & PREVIEW ---
        batch.begin();

        titleFont.setColor(Color.WHITE);
        titleFont.draw(batch, "Character Select", LIST_X, 1050);

        drawPreview(hoveredIndex);
        drawStatsText(hoveredIndex);
        drawSkillText(hoveredIndex);

        // [UBAH] Update instruksi di bawah
        font.setColor(Color.GRAY);
        font.draw(batch, "Scroll list to view more | Click to select | ESC to go back", LIST_X, 50);

        batch.end();

        // --- 4. RENDER SCROLLABLE LIST ---
        batch.begin();
        ScissorStack.calculateScissors(camera, batch.getTransformMatrix(), clipBounds, scissors);
        if (ScissorStack.pushScissors(scissors)) {

            float startY = LIST_Y + LIST_HEIGHT - ITEM_HEIGHT + scrollY;

            for (int i = 0; i < characters.length; i++) {
                float y = startY - (i * (ITEM_HEIGHT + ITEM_SPACING));

                if (y + ITEM_HEIGHT > LIST_Y && y < LIST_Y + LIST_HEIGHT) {
                    drawListItem(i, LIST_X, y);
                }
            }

            batch.flush();
            ScissorStack.popScissors();
        }
        batch.end();
    }

    private void drawListItem(int index, float x, float y) {
        CharacterInfo character = characters[index];
        boolean isUnlocked = GameManager.getInstance().isCharacterUnlocked(character.name);

        if (index == hoveredIndex) {
            font.setColor(Color.CYAN);
        } else {
            font.setColor(Color.WHITE);
        }

        float iconX = x + 20;
        float iconY = y + (ITEM_HEIGHT - ICON_SIZE) / 2;

        if (isUnlocked) {
            if (character.animation != null) {
                batch.draw(character.animation.getKeyFrame(0), iconX, iconY, ICON_SIZE, ICON_SIZE);
            } else {
                Texture p = character.getPortraitTexture();
                if(p!=null) batch.draw(p, iconX, iconY, ICON_SIZE, ICON_SIZE);
            }
        } else {
            if (lockedTexture != null) batch.draw(lockedTexture, iconX, iconY, ICON_SIZE, ICON_SIZE);
        }

        float textX = iconX + ICON_SIZE + 30;
        float textY = y + ITEM_HEIGHT - 40;

        if (isUnlocked) {
            font.getData().setScale(1.5f);
            font.draw(batch, character.name, textX, textY);

            font.setColor(Color.LIGHT_GRAY);
            font.getData().setScale(1.0f);
            font.draw(batch, character.title, textX, textY - 40);
        } else {
            font.getData().setScale(1.5f);
            font.draw(batch, "???", textX, textY);

            font.setColor(Color.GRAY);
            font.getData().setScale(1.0f);
            font.draw(batch, "Locked Character", textX, textY - 40);
        }
        font.getData().setScale(1.3f);
    }

    private void drawPreview(int index) {
        if (index < 0 || index >= characters.length) return;
        CharacterInfo character = characters[index];
        boolean isUnlocked = GameManager.getInstance().isCharacterUnlocked(character.name);

        float x = PREVIEW_CENTER_X - PREVIEW_SIZE / 2;
        float y = PREVIEW_CENTER_Y - PREVIEW_SIZE / 2;

        if (isUnlocked) {
            if (character.animation != null) {
                batch.draw(character.animation.getKeyFrame(stateTime), x, y, PREVIEW_SIZE, PREVIEW_SIZE);
            } else {
                Texture p = character.getPortraitTexture();
                if(p!=null) batch.draw(p, x, y, PREVIEW_SIZE, PREVIEW_SIZE);
            }
        } else {
            if (lockedTexture != null) batch.draw(lockedTexture, x, y, PREVIEW_SIZE, PREVIEW_SIZE);
        }
    }

    private void drawStatsText(int index) {
        if (index < 0 || index >= characters.length) return;
        CharacterInfo character = characters[index];
        boolean isUnlocked = GameManager.getInstance().isCharacterUnlocked(character.name);

        font.setColor(Color.YELLOW);
        font.draw(batch, "Stats", RIGHT_PANEL_X + 20, STATS_Y + STATS_HEIGHT - 20);

        font.setColor(Color.WHITE);
        float startY = STATS_Y + STATS_HEIGHT - 70;
        float gap = 40;

        if (isUnlocked) {
            font.draw(batch, "HP:    " + (int) character.hp, RIGHT_PANEL_X + 20, startY);
            font.draw(batch, "ATK:   " + (int) character.atk, RIGHT_PANEL_X + 20, startY - gap);
            font.draw(batch, "Arts:  " + (int) character.arts, RIGHT_PANEL_X + 20, startY - gap * 2);
            font.draw(batch, "DEF:   " + (int) character.def, RIGHT_PANEL_X + 20, startY - gap * 3);
            font.draw(batch, "Speed: " + (int) character.speed, RIGHT_PANEL_X + 20, startY - gap * 4);
        } else {
            for(int i=0; i<5; i++) font.draw(batch, "???", RIGHT_PANEL_X + 20, startY - gap*i);
        }
    }

    private void drawSkillText(int index) {
        if (index < 0 || index >= characters.length) return;
        CharacterInfo character = characters[index];
        boolean isUnlocked = GameManager.getInstance().isCharacterUnlocked(character.name);

        font.setColor(Color.YELLOW);
        font.draw(batch, "Innate Skill", RIGHT_PANEL_X + 20, SKILL_Y + SKILL_HEIGHT - 20);

        font.setColor(Color.CYAN);
        font.getData().setScale(1.4f);

        if (isUnlocked) {
            font.draw(batch, character.skillName, RIGHT_PANEL_X + 20, SKILL_Y + SKILL_HEIGHT - 60);
        } else {
            font.draw(batch, "???", RIGHT_PANEL_X + 20, SKILL_Y + SKILL_HEIGHT - 60);
        }

        font.setColor(Color.WHITE);
        font.getData().setScale(1.2f);

        String descriptionText = isUnlocked ? character.skillDescription : "???";
        String[] lines = descriptionText.split("\n");
        float yPos = SKILL_Y + SKILL_HEIGHT - 110;
        for (String line : lines) {
            font.draw(batch, line, RIGHT_PANEL_X + 20, yPos);
            yPos -= 30;
        }
    }

    private void startGame() {
        System.out.println("Selected: " + characters[selectedIndex].name);
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
