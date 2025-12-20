package com.kelompok2.frontend.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.kelompok2.frontend.Main;
import com.kelompok2.frontend.entities.GameCharacter;
import com.kelompok2.frontend.factories.CharacterFactory;
import com.kelompok2.frontend.utils.InputHandler;
import com.kelompok2.frontend.managers.AssetManager;
import com.kelompok2.frontend.managers.GameManager;
import com.kelompok2.frontend.managers.AudioManager;
import com.kelompok2.frontend.pools.ProjectilePool;
import com.kelompok2.frontend.pools.EnemyPool;
import com.kelompok2.frontend.systems.GameFacade;

public class GameScreen extends ScreenAdapter {
    // Core resources
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camera;
    private Texture background;

    // Game entities
    private GameCharacter player;
    private ProjectilePool projectilePool;
    private EnemyPool enemyPool;

    // Input & Game Control
    private InputHandler inputHandler;
    private Main game;
    private String selectedCharacter;
    private boolean isPaused = false;
    private boolean isDisposed = false;

    // ✨ FACADE PATTERN - Coordinates all subsystems
    private GameFacade gameFacade;
    private boolean firstFrame = true;

    public GameScreen(Main game, String selectedCharacter) {
        this.game = game;
        this.selectedCharacter = selectedCharacter;

        // Initialize core resources
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 1280, 720);
        background = AssetManager.getInstance().loadTexture("FireflyPlaceholder.jpg");

        // Initialize pools
        projectilePool = new ProjectilePool(50);

        float mapW = com.kelompok2.frontend.systems.MapBoundarySystem.getMapWidth();
        float mapH = com.kelompok2.frontend.systems.MapBoundarySystem.getMapHeight();
        float padding = 700f;

        float startX = com.badlogic.gdx.math.MathUtils.random(padding, mapW - padding);
        float startY = com.badlogic.gdx.math.MathUtils.random(padding, mapH - padding);

        player = CharacterFactory.createCharacter(selectedCharacter, startX, startY);

        // Initialize EnemyPool
        enemyPool = new EnemyPool(player, 30);

        // Initialize GameManager
        GameManager.getInstance().startNewGame(this.selectedCharacter);

        // ✨ Initialize GameFacade - coordinates all subsystems
        gameFacade = new GameFacade(batch, shapeRenderer, background);
        gameFacade.initialize(player, enemyPool, projectilePool);

        // Inject dependencies into player (e.g. Lumi needs access to pools)
        player.injectDependencies(gameFacade, enemyPool);

        // Setup Input Handler (uses facade's attack arrays)
        inputHandler = new InputHandler(player, camera, projectilePool, gameFacade.getPlayerMeleeAttacks());

        // Play BGM
        AudioManager.getInstance().playMusic("Audio/battleThemeA.mp3", true);

        System.out.println("[GameScreen] Initialized with Facade Pattern");
        System.out.println("[GameScreen] Player: " + selectedCharacter);
    }

    public String getSelectedCharacter() {
        return selectedCharacter;
    }

    @Override
    public void render(float delta) {
        // Handle pause toggle
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            togglePause();
            return;
        }

        // Skip updates if paused
        if (isPaused) {
            return;
        }

        // Game over check
        if (player.isDead()) {
            handleGameOver();
            return;
        }

        if (player.isLevelUpPending()) {
            isPaused = true;
            game.setScreen(new LevelUpScreen(game, this, player));
            return;
        }

        // Clear screen
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Update camera to follow player
        updateCamera(delta);

        com.kelompok2.frontend.managers.GameManager.getInstance().updateGameTime(delta);

        // Handle input (movement, attacks, skills)
        inputHandler.update(delta);

        // Update player
        player.update(delta);

        if (!gameFacade.getBossCinematicSystem().isCinematicActive()) {
            projectilePool.update(delta);
        }

        // NOTE: Enemy updates moved to GameFacade to prevent double-updating

        for (int i = enemyPool.getActiveEnemies().size - 1; i >= 0; i--) {
            com.kelompok2.frontend.entities.BaseEnemy enemy = enemyPool.getActiveEnemies().get(i);
            if (enemy.isDead()) {
                enemyPool.free(enemy);
                System.out.println("[GameScreen] Removed dead enemy from pool");
            }
        }

        // ✨ Update all game systems via Facade
        gameFacade.update(delta, camera);

        // ✨ Render all game systems via Facade
        gameFacade.render(camera);
    }

    private void updateCamera(float delta) {
        float targetX = Math.round(player.getPosition().x + player.getVisualWidth() / 2);
        float targetY = Math.round(player.getPosition().y + player.getVisualHeight() / 2);

        if (firstFrame) {
            camera.position.x = targetX;
            camera.position.y = targetY;
            firstFrame = false;
        } else {
            // Smooth camera follow (Lerp)
            float lerpSpeed = 5f;
            camera.position.x += (targetX - camera.position.x) * lerpSpeed * delta;
            camera.position.y += (targetY - camera.position.y) * lerpSpeed * delta;

            // Snap to target if very close (prevents micro-jitter when stopped)
            if (Math.abs(camera.position.x - targetX) < 0.5f)
                camera.position.x = targetX;
            if (Math.abs(camera.position.y - targetY) < 0.5f)
                camera.position.y = targetY;
        }

        camera.update();
    }

    private void togglePause() {
        isPaused = true;
        game.setScreen(new PauseScreen(game, this));
    }

    public void resumeGame() {
        isPaused = false;
    }

    public void resumeFromPause() {
        isPaused = false;
        System.out.println("[GameScreen] Resumed from pause");
    }

    public void resumeFromLevelUp() {
        isPaused = false;
        // Snap camera to player to prevent "shift/swoop" from potential resize resets
        firstFrame = true;
    }

    private void handleGameOver() {
        System.out.println("[GameScreen] Game Over!");
        // Get final stats from GameManager
        int finalLevel = com.kelompok2.frontend.managers.GameManager.getInstance().getCurrentLevel();
        float finalTime = com.kelompok2.frontend.managers.GameManager.getInstance().getGameTime();
        game.setScreen(new GameOverScreen(game, selectedCharacter, finalLevel, finalTime));
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, width, height);
    }

    @Override
    public void dispose() {
        if (isDisposed) {
            return;
        }
        isDisposed = true;

        System.out.println("[GameScreen] Disposing resources...");

        // Dispose rendering resources
        if (batch != null) {
            batch.dispose();
        }
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }

        // Dispose facade (which disposes UISystem)
        if (gameFacade != null) {
            gameFacade.dispose();
        }

        // Dispose pools
        if (projectilePool != null) {
            projectilePool.dispose();
        }
        if (enemyPool != null) {
            enemyPool.dispose();
        }

        System.out.println("[GameScreen] Disposed successfully");
    }
}
