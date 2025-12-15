package com.kelompok2.frontend.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kelompok2.frontend.Main;
import com.kelompok2.frontend.managers.AudioManager;

public class GameOverScreen extends ScreenAdapter {
    private final Main game;
    private Stage stage;
    private Skin skin;

    // Data untuk restart / display
    private String lastCharacter;
    private int finalLevel;
    private float finalTime;

    public GameOverScreen(Main game, String lastCharacter, int finalLevel, float finalTime) {
        this.game = game;
        this.lastCharacter = lastCharacter;
        this.finalLevel = finalLevel;
        this.finalTime = finalTime;
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        skin = new Skin(Gdx.files.internal("uiskin.json"));

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        // Stop any music and play game over sound
        AudioManager.getInstance().stopMusic();
        AudioManager.getInstance().playSound("Audio/game-over-voice.mp3");

        // --- UI COMPONENTS ---
        Label titleLabel = new Label("GAME OVER", skin);
        titleLabel.setFontScale(3f);
        titleLabel.setColor(Color.RED);

        Label scoreLabel = new Label(String.format("Character: %s\nLevel Reached: %d\nTime Survived: %.1fs",
                lastCharacter, finalLevel, finalTime), skin);
        scoreLabel.setAlignment(com.badlogic.gdx.utils.Align.center);

        TextButton restartButton = new TextButton("RESTART", skin);
        TextButton charSelectButton = new TextButton("PILIH KARAKTER", skin);
        TextButton homeButton = new TextButton("MENU UTAMA", skin);

        // --- LAYOUT ---
        table.add(titleLabel).padBottom(30).row();
        table.add(scoreLabel).padBottom(50).row();
        table.add(restartButton).width(250).height(60).padBottom(15).row();
        table.add(charSelectButton).width(250).height(60).padBottom(15).row();
        table.add(homeButton).width(250).height(60).row();

        // --- LOGIC ---

        // 1. Restart: Mulai GameScreen baru dengan karakter yang sama
        restartButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new GameScreen(game, lastCharacter));
                dispose();
            }
        });

        // 2. Character Select: Balik ke layar pilih karakter
        charSelectButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new CharacterSelectionScreen(game));
                dispose();
            }
        });

        // 3. Home: Balik ke Main Menu
        homeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MainMenuScreen(game));
                dispose();
            }
        });
    }

    @Override
    public void render(float delta) {
        // Background merah gelap transparan biar dramatis
        Gdx.gl.glClearColor(0.1f, 0.0f, 0.0f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
    }
}
