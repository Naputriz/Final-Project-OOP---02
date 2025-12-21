package com.kelompok2.frontend.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.Align; // Added Import
import com.kelompok2.frontend.Main;
import com.kelompok2.frontend.managers.AudioManager;
import com.kelompok2.frontend.ui.SettingsWindow; // Added Import

public class PauseScreen extends ScreenAdapter {
    private final Main game;
    private final GameScreen gameScreen; // Referensi ke layar game yang sedang jalan
    private Stage stage;
    private Skin skin;
    private ShapeRenderer shapeRenderer; // Class-level untuk menghindari memory leak
    private SettingsWindow settingsWindow; // Replaced Window with SettingsWindow
    private boolean hasTransitioned = false; // Guard against multiple transitions

    public PauseScreen(Main game, GameScreen gameScreen) {
        this.game = game;
        this.gameScreen = gameScreen;
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        skin = new Skin(Gdx.files.internal("uiskin.json")); // Pastikan punya uiskin
        shapeRenderer = new ShapeRenderer(); // Initialize once

        // Setup UI Table
        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        // Judul PAUSED
        Label titleLabel = new Label("PAUSED", skin);
        titleLabel.setFontScale(3f);

        // Buttons
        TextButton resumeButton = new TextButton("RESUME", skin);
        TextButton restartButton = new TextButton("RESTART", skin);
        TextButton settingsButton = new TextButton("SETTINGS", skin);
        TextButton charSelectButton = new TextButton("CHARACTER SELECT", skin);
        TextButton mainMenuButton = new TextButton("MAIN MENU", skin);

        // Layout
        table.add(titleLabel).padBottom(50).row();
        table.add(resumeButton).width(300).height(60).padBottom(15).row();
        table.add(restartButton).width(300).height(60).padBottom(15).row();
        table.add(settingsButton).width(300).height(60).padBottom(15).row();
        table.add(charSelectButton).width(300).height(60).padBottom(15).row();
        table.add(mainMenuButton).width(300).height(60).row();

        // Listeners

        // 1. Resume: Kembali ke instance GameScreen yang lama
        resumeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (hasTransitioned)
                    return;
                hasTransitioned = true;
                gameScreen.resumeFromPause();
                game.setScreen(gameScreen);
            }
        });

        // 2. Restart: Buat instance GameScreen baru dengan karakter yang sama
        restartButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (hasTransitioned)
                    return; // Prevent multiple transitions
                hasTransitioned = true;
                gameScreen.dispose(); // Bersihkan yang lama
                game.setScreen(new GameScreen(game, gameScreen.getSelectedCharacter()));
            }
        });

        // 3. Settings: menampilkan window game settings (saat ini hanya untuk audio)
        settingsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (hasTransitioned)
                    return; // Prevent multiple transitions
                settingsWindow.setVisible(true);
            }
        });

        // 4. Character Select
        charSelectButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (hasTransitioned)
                    return; // Prevent multiple transitions
                hasTransitioned = true;
                gameScreen.dispose();
                AudioManager.getInstance().stopMusic(); // Stop lagu battle
                game.setScreen(new CharacterSelectionScreen(game));
            }
        });

        // 5. Main Menu
        mainMenuButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (hasTransitioned)
                    return; // Prevent multiple transitions
                hasTransitioned = true;
                gameScreen.dispose();
                AudioManager.getInstance().stopMusic();
                game.setScreen(new MainMenuScreen(game));
            }
        });

        createSettingsWindow();
    }

    // window untuk settings
    private void createSettingsWindow() {
        // Use the shared SettingsWindow class instead of manually building one
        settingsWindow = new SettingsWindow(game, skin);
        stage.addActor(settingsWindow);
    }

    @Override
    public void render(float delta) {
        // 1. Render Game World di Background (Frozen)
        // Kirim delta 0 agar animasi tidak jalan
        gameScreen.render(0);

        // 2. Gambar Overlay Hitam Transparan
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.setProjectionMatrix(stage.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.7f);
        shapeRenderer.rect(0, 0, stage.getWidth(), stage.getHeight());
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        // 3. Render UI Pause Menu
        stage.act(delta);
        stage.draw();

        // Fitur Unpause pakai ESC
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (settingsWindow.isVisible()) {
                settingsWindow.setVisible(false);
            } else {
                if (hasTransitioned)
                    return; // Prevent multiple transitions
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
        if (settingsWindow != null) {
            settingsWindow.setPosition(width / 2f, height / 2f, Align.center);
        }
    }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }
    }
}
