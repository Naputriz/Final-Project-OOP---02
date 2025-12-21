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
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
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

    // Character data
    private Array<CharacterInfo> characters;
    private int hoveredIndex = 0; // Currently hovered character
    private int selectedIndex = -1; // Selected character (-1 = none)

    // --- LAYOUT CONSTANTS (1920x1080) ---

    // 1. LEFT PANEL (SCROLLABLE LIST)
    private static final float LIST_X = 100;
    private static final float LIST_Y = 100;
    private static final float LIST_WIDTH = 450;
    private static final float LIST_HEIGHT = 850;

    // Item list dimensions
    private static final float ITEM_HEIGHT = 140;
    private static final float ITEM_SPACING = 10;
    private static final float ICON_SIZE = 100;

    // 2. CENTER PANEL (PREVIEW)
    private static final float PREVIEW_CENTER_X = 960;
    private static final float PREVIEW_CENTER_Y = 540;
    private static final float PREVIEW_SIZE = 700;

    // 3. RIGHT PANELS (STATS & SKILL)
    private static final float RIGHT_PANEL_WIDTH = 450;
    private static final float RIGHT_PANEL_X = 1370; // 1920 - 100 - 450

    // Stats (Right Top)
    private static final float STATS_Y = 600;
    private static final float STATS_HEIGHT = 350;

    // Skill (Right Bottom)
    private static final float SKILL_Y = 100;
    private static final float SKILL_HEIGHT = 450;

    // Grid layout constants (Legacy/Unused but kept for compatibility if needed)
    private static final float GRID_X = 400;
    private static final float GRID_Y = 450;
    private static final float PORTRAIT_SPACING = 120;

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

        // Calculate total content height for scrolling
        float totalContentHeight = characters.size * (ITEM_HEIGHT + ITEM_SPACING);
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
        characters = new Array<>();
        JsonReader json = new JsonReader();

        try {
            JsonValue root = json.parse(Gdx.files.internal("data/characters.json"));

            for (JsonValue entry : root){
                String texturePath = entry.getString("texturePath");
                Texture texture = AssetManager.getInstance().loadTexture(texturePath);

                // [FIX] Replace literal "\n" characters from JSON with actual newlines
                String rawDesc = entry.getString("skillDesc");
                String cleanDesc = rawDesc.replace("\\n", "\n");

                CharacterInfo info = new CharacterInfo(
                    entry.getString("name"),
                    entry.getString("title"),
                    entry.getFloat("hp"),
                    entry.getFloat("atk"),
                    entry.getFloat("arts"),
                    entry.getFloat("def"),
                    entry.getFloat("speed"),
                    entry.getString("skillName"),
                    cleanDesc, // Use the cleaned description
                    texturePath,
                    texture,
                    entry.getInt("frameCols"),
                    entry.getInt("frameRows"),
                    entry.getInt("totalFrames"),
                    entry.getFloat("frameDuration")
                );

                characters.add(info);
            }
        } catch (Exception e) {
            Gdx.app.error("CharacterSelect", "Failed to load characters.json", e);
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

        // Preview Center
        shapeRenderer.setColor(0.15f, 0.15f, 0.15f, 1);
        float previewFrameSize = PREVIEW_SIZE + 40;
        shapeRenderer.rect(PREVIEW_CENTER_X - previewFrameSize/2, PREVIEW_CENTER_Y - previewFrameSize/2, previewFrameSize, previewFrameSize);

        // Right Panels
        shapeRenderer.setColor(0.1f, 0.1f, 0.1f, 1);
        shapeRenderer.rect(RIGHT_PANEL_X, STATS_Y, RIGHT_PANEL_WIDTH, STATS_HEIGHT);
        shapeRenderer.rect(RIGHT_PANEL_X, SKILL_Y, RIGHT_PANEL_WIDTH, SKILL_HEIGHT);
        shapeRenderer.end();

        // --- 2. RENDER BORDERS ---
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        Gdx.gl.glLineWidth(2);
        shapeRenderer.setColor(Color.WHITE);

        // List Left
        shapeRenderer.rect(LIST_X - 10, LIST_Y - 10, LIST_WIDTH + 20, LIST_HEIGHT + 20);
        // Panels Right
        shapeRenderer.rect(RIGHT_PANEL_X, STATS_Y, RIGHT_PANEL_WIDTH, STATS_HEIGHT);
        shapeRenderer.rect(RIGHT_PANEL_X, SKILL_Y, RIGHT_PANEL_WIDTH, SKILL_HEIGHT);
        shapeRenderer.end();

        // --- 3. RENDER TEXT & PREVIEW ---
        batch.begin();

        // Title
        titleFont.setColor(Color.WHITE);
        titleFont.draw(batch, "Character Select", LIST_X, 1050);

        // Character preview
        drawPreview(hoveredIndex);

        // Stats & Skill panels
        drawStatsText(hoveredIndex);
        drawSkillText(hoveredIndex);

        // Instructions
        font.setColor(Color.GRAY);
        font.draw(batch, "Scroll list to view more | Click to select | ESC to go back", LIST_X, 50);

        batch.end();

        // --- 4. RENDER SCROLLABLE LIST ---
        batch.begin();
        ScissorStack.calculateScissors(camera, batch.getTransformMatrix(), clipBounds, scissors);
        if (ScissorStack.pushScissors(scissors)) {

            // Calculate start Y for the top of the list content
            float startY = LIST_Y + LIST_HEIGHT - ITEM_HEIGHT + scrollY;

            // [FIX] Use characters.size for LibGDX Array
            for (int i = 0; i < characters.size; i++) {
                float y = startY - (i * (ITEM_HEIGHT + ITEM_SPACING));

                // Culling: only draw if visible
                if (y + ITEM_HEIGHT > LIST_Y && y < LIST_Y + LIST_HEIGHT) {
                    drawListItem(i, LIST_X, y);
                }
            }

            batch.flush();
            ScissorStack.popScissors();
        }
        batch.end();
    }

    // [FIX] Closed this method properly
    private void drawPortraitSprite(int index) {
        // Legacy grid method - mostly unused in list view but kept to prevent breakage if called
        // float x, y logic...
    }

    private void drawListItem(int index, float x, float y) {
        CharacterInfo character = characters.get(index);
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
        // [FIX] Use characters.size
        if (index < 0 || index >= characters.size) return;
        CharacterInfo character = characters.get(index);
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
        if (index < 0 || index >= characters.size) return;
        CharacterInfo character = characters.get(index);
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
        if (index < 0 || index >= characters.size) return;
        CharacterInfo character = characters.get(index);
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
        // The .split() will now work correctly because we fixed the string in initialization
        String[] lines = descriptionText.split("\n");
        float yPos = SKILL_Y + SKILL_HEIGHT - 110;
        for (String line : lines) {
            font.draw(batch, line, RIGHT_PANEL_X + 20, yPos);
            yPos -= 30;
        }
    }

    private void handleHover() {
        float mouseX = Gdx.input.getX();
        float mouseY = Gdx.input.getY();

        com.badlogic.gdx.math.Vector3 worldCoords = camera.unproject(
            new com.badlogic.gdx.math.Vector3(mouseX, mouseY, 0),
            viewport.getScreenX(), viewport.getScreenY(),
            viewport.getScreenWidth(), viewport.getScreenHeight()
        );

        if (worldCoords.x >= LIST_X && worldCoords.x <= LIST_X + LIST_WIDTH &&
            worldCoords.y >= LIST_Y && worldCoords.y <= LIST_Y + LIST_HEIGHT) {

            float startY = LIST_Y + LIST_HEIGHT - ITEM_HEIGHT + scrollY;
            float itemTotalHeight = ITEM_HEIGHT + ITEM_SPACING;

            float diff = startY + ITEM_HEIGHT - worldCoords.y;
            int idx = (int) (diff / itemTotalHeight);

            if (idx >= 0 && idx < characters.size) {
                float offset = diff % itemTotalHeight;
                if (offset <= ITEM_HEIGHT) {
                    hoveredIndex = idx;
                }
            }
        }
    }

    private void handleClick(int screenX, int screenY) {
        com.badlogic.gdx.math.Vector3 worldCoords = camera.unproject(
            new com.badlogic.gdx.math.Vector3(screenX, screenY, 0),
            viewport.getScreenX(), viewport.getScreenY(),
            viewport.getScreenWidth(), viewport.getScreenHeight()
        );

        if (worldCoords.x >= LIST_X && worldCoords.x <= LIST_X + LIST_WIDTH &&
            worldCoords.y >= LIST_Y && worldCoords.y <= LIST_Y + LIST_HEIGHT) {

            float startY = LIST_Y + LIST_HEIGHT - ITEM_HEIGHT + scrollY;
            float itemTotalHeight = ITEM_HEIGHT + ITEM_SPACING;

            float diff = startY + ITEM_HEIGHT - worldCoords.y;
            int idx = (int) (diff / itemTotalHeight);

            if (idx >= 0 && idx < characters.size) {
                float offset = diff % itemTotalHeight;
                if (offset <= ITEM_HEIGHT) {
                    selectedIndex = idx;
                    startGame();
                }
            }
        }
    }

    private void startGame() {
        if (selectedIndex >= 0 && selectedIndex < characters.size) {
            System.out.println("Selected: " + characters.get(selectedIndex).name);
            game.setScreen(new GameScreen(game, characters.get(selectedIndex).name));
        }
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
