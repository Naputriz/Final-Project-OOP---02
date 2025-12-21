package com.kelompok2.frontend.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.kelompok2.frontend.Main;
import com.kelompok2.frontend.entities.Boss; // [PENTING] Import Boss
import com.kelompok2.frontend.entities.GameCharacter;
import com.kelompok2.frontend.factories.CharacterFactory;
import com.kelompok2.frontend.utils.InputHandler;
import com.kelompok2.frontend.managers.AssetManager;
import com.kelompok2.frontend.managers.GameManager;
import com.kelompok2.frontend.managers.AudioManager;
import com.kelompok2.frontend.pools.ProjectilePool;
import com.kelompok2.frontend.pools.EnemyPool;
import com.kelompok2.frontend.systems.GameFacade;
import com.kelompok2.frontend.systems.MapBoundarySystem;
import com.kelompok2.frontend.ui.GameHUD; // [PENTING] Import GameHUD

public class GameScreen extends ScreenAdapter {
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camera;
    private Viewport viewport;
    private Texture background;

    private GameCharacter player;
    private ProjectilePool projectilePool;
    private EnemyPool enemyPool;
    private InputHandler inputHandler;
    private Main game;
    private String selectedCharacter;
    private boolean isPaused = false;
    private boolean isDisposed = false;
    private GameFacade gameFacade;
    private boolean firstFrame = true;

    // [BARU] HUD UI Manager
    private GameHUD gameHUD;

    // Resolusi Virtual 1920x1080
    private static final float VIRTUAL_WIDTH = 1920;
    private static final float VIRTUAL_HEIGHT = 1080;

    public GameScreen(Main game, String selectedCharacter) {
        this.game = game;
        this.selectedCharacter = selectedCharacter;

        // Reset Input Processor agar UI menu sebelumnya tidak mengganggu
        Gdx.input.setInputProcessor(null);

        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        camera = new OrthographicCamera();

        // Setup FitViewport
        viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
        camera.position.set(VIRTUAL_WIDTH / 2, VIRTUAL_HEIGHT / 2, 0);

        // Load Background Dungeon
        try {
            background = AssetManager.getInstance().loadTexture("dungeon_floor.png");
            background.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        } catch (Exception e) {
            // Fallback jika file belum ada
            background = AssetManager.getInstance().loadTexture("FireflyPlaceholder.jpg");
        }

        projectilePool = new ProjectilePool(50);

        float mapW = MapBoundarySystem.getMapWidth();
        float mapH = MapBoundarySystem.getMapHeight();
        float padding = 700f;

        float startX = com.badlogic.gdx.math.MathUtils.random(padding, mapW - padding);
        float startY = com.badlogic.gdx.math.MathUtils.random(padding, mapH - padding);

        player = CharacterFactory.createCharacter(selectedCharacter, startX, startY);
        enemyPool = new EnemyPool(player, 30, projectilePool);
        GameManager.getInstance().startNewGame(this.selectedCharacter);

        // Init Game Systems (Facade)
        gameFacade = new GameFacade(batch, shapeRenderer, background);
        gameFacade.initialize(player, enemyPool, projectilePool);
        player.injectDependencies(gameFacade, enemyPool);

        inputHandler = new InputHandler(player, camera, projectilePool, gameFacade.getPlayerMeleeAttacks());
        AudioManager.getInstance().playMusic("Audio/battleThemeA.mp3", true);

        // [BARU] Init HUD
        gameHUD = new GameHUD(batch);

        System.out.println("[GameScreen] Initialized.");
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(null);
    }

    public String getSelectedCharacter() { return selectedCharacter; }

    @Override
    public void render(float delta) {
        delta = Math.min(delta, 0.1f);

        // Handle Input Pause
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            togglePause();
        }

        if (player.isDead()) {
            handleGameOver();
            return;
        }

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (!isPaused) {
            // Handle Level Up
            if (player.canLevelUp()) {
                isPaused = true;
                player.stop();
                game.setScreen(new LevelUpScreen(game, this, player));
                return;
            }

            // Update Game Logic
            updateCamera(delta);
            GameManager.getInstance().updateGameTime(delta);
            inputHandler.update(delta);
            player.update(delta);

            if (!gameFacade.getBossCinematicSystem().isCinematicActive()) {
                projectilePool.update(delta);
            }

            for (int i = enemyPool.getActiveEnemies().size - 1; i >= 0; i--) {
                com.kelompok2.frontend.entities.BaseEnemy enemy = enemyPool.getActiveEnemies().get(i);
                if (enemy.isDead()) enemyPool.free(enemy);
            }

            gameFacade.update(delta, camera);

            // [BARU] Update HUD Timer & Messages
            if (gameHUD != null) {
                gameHUD.update(delta);
            }
        }

        // 1. Render World (Background, Entities, etc via Facade)
        gameFacade.render(camera);

        // 2. Render UI Overlay (Hanya jika tidak sedang cutscene boss)
        if (!gameFacade.getBossCinematicSystem().isCinematicActive()) {

            // [BARU] Ambil Boss aktif dari sistem spawning untuk ditampilkan bar darahnya
            Boss currentBoss = gameFacade.getSpawningSystem().getCurrentBoss();

            // Render HUD (Pass Player & Boss)
            if (gameHUD != null) {
                gameHUD.render(player, currentBoss);
            }
        }
    }

    private void updateCamera(float delta) {
        float targetX = Math.round(player.getPosition().x + player.getVisualWidth() / 2);
        float targetY = Math.round(player.getPosition().y + player.getVisualHeight() / 2);

        if (firstFrame) {
            camera.position.x = targetX;
            camera.position.y = targetY;
            firstFrame = false;
        } else {
            float lerpSpeed = 5f;
            camera.position.x += (targetX - camera.position.x) * lerpSpeed * delta;
            camera.position.y += (targetY - camera.position.y) * lerpSpeed * delta;

            if (Math.abs(camera.position.x - targetX) < 0.5f) camera.position.x = targetX;
            if (Math.abs(camera.position.y - targetY) < 0.5f) camera.position.y = targetY;
        }
        camera.update();
    }

    private void togglePause() {
        isPaused = true;
        player.stop();
        game.setScreen(new PauseScreen(game, this));
    }

    public void resumeGame() { isPaused = false; }
    public void resumeFromPause() { isPaused = false; firstFrame = true; }
    public void resumeFromLevelUp() { isPaused = false; firstFrame = true; }

    private void handleGameOver() {
        int finalLevel = GameManager.getInstance().getCurrentLevel();
        float finalTime = GameManager.getInstance().getGameTime();
        game.setScreen(new GameOverScreen(game, selectedCharacter, finalLevel, finalTime));
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);

        // [BARU] Resize HUD agar UI tetap proporsional
        if(gameHUD != null) gameHUD.resize(width, height);

        if (player != null) {
            float targetX = Math.round(player.getPosition().x + player.getVisualWidth() / 2);
            float targetY = Math.round(player.getPosition().y + player.getVisualHeight() / 2);
            camera.position.set(targetX, targetY, 0);
            camera.update();
        }
    }

    @Override
    public void pause() {
        if (!isPaused && !player.isDead()) togglePause();
    }

    @Override
    public void dispose() {
        if (isDisposed) return;
        isDisposed = true;

        if (batch != null) batch.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (gameFacade != null) gameFacade.dispose();
        if (projectilePool != null) projectilePool.dispose();
        if (enemyPool != null) enemyPool.dispose();

        // [BARU] Dispose HUD
        if (gameHUD != null) gameHUD.dispose();
    }
}
