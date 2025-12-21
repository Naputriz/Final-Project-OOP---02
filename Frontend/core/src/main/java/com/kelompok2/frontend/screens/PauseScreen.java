package com.kelompok2.frontend.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.kelompok2.frontend.Main;
import com.kelompok2.frontend.managers.AudioManager;
import com.kelompok2.frontend.ui.SettingsWindow;

public class PauseScreen extends ScreenAdapter {
    private final Main game;
    private final GameScreen gameScreen;
    private Stage stage;
    private Skin skin;
    private ShapeRenderer shapeRenderer;
    private SettingsWindow settingsWindow;
    private boolean hasTransitioned = false;

    // Viewport 1920x1080
    private static final float WORLD_WIDTH = 1920;
    private static final float WORLD_HEIGHT = 1080;

    public PauseScreen(Main game, GameScreen gameScreen) {
        this.game = game;
        this.gameScreen = gameScreen;
    }

    @Override
    public void show() {
        stage = new Stage(new FitViewport(WORLD_WIDTH, WORLD_HEIGHT));
        Gdx.input.setInputProcessor(stage);
        skin = new Skin(Gdx.files.internal("uiskin.json"));
        shapeRenderer = new ShapeRenderer();

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        Label titleLabel = new Label("PAUSED", skin);
        titleLabel.setFontScale(3f);

        TextButton resumeButton = new TextButton("RESUME", skin);
        TextButton restartButton = new TextButton("RESTART", skin);
        TextButton settingsButton = new TextButton("SETTINGS", skin);
        TextButton charSelectButton = new TextButton("CHARACTER SELECT", skin);
        TextButton mainMenuButton = new TextButton("MAIN MENU", skin);

        table.add(titleLabel).padBottom(50).row();
        table.add(resumeButton).width(300).height(60).padBottom(15).row();
        table.add(restartButton).width(300).height(60).padBottom(15).row();
        table.add(settingsButton).width(300).height(60).padBottom(15).row();
        table.add(charSelectButton).width(300).height(60).padBottom(15).row();
        table.add(mainMenuButton).width(300).height(60).row();

        // Listeners
        resumeButton.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                if (hasTransitioned) return;
                hasTransitioned = true;
                gameScreen.resumeFromPause();
                game.setScreen(gameScreen);
            }
        });

        restartButton.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                if (hasTransitioned) return;
                hasTransitioned = true;
                gameScreen.dispose();
                game.setScreen(new GameScreen(game, gameScreen.getSelectedCharacter()));
            }
        });

        settingsButton.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                if (hasTransitioned) return;
                settingsWindow.show(stage);
            }
        });

        charSelectButton.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                if (hasTransitioned) return;
                hasTransitioned = true;
                gameScreen.dispose();
                AudioManager.getInstance().stopMusic();
                game.setScreen(new CharacterSelectionScreen(game));
            }
        });

        mainMenuButton.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                if (hasTransitioned) return;
                hasTransitioned = true;
                gameScreen.dispose();
                AudioManager.getInstance().stopMusic();
                game.setScreen(new MainMenuScreen(game));
            }
        });

        settingsWindow = new SettingsWindow(skin);
        settingsWindow.setVisible(false);
        stage.addActor(settingsWindow);
    }

    @Override
    public void render(float delta) {
        gameScreen.render(0);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.setProjectionMatrix(stage.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.7f);
        shapeRenderer.rect(0, 0, stage.getWidth(), stage.getHeight());
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        stage.act(delta);
        stage.draw();

        // Handle ESC to Resume
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (settingsWindow.isVisible()) {
                settingsWindow.setVisible(false);
            } else {
                if (hasTransitioned) return;
                hasTransitioned = true;
                gameScreen.resumeFromPause();
                game.setScreen(gameScreen);
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        gameScreen.resize(width, height);
        if (settingsWindow != null) settingsWindow.setPosition(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f, com.badlogic.gdx.utils.Align.center);
    }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
    }
}
