package com.kelompok2.frontend.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kelompok2.frontend.Main;
import com.kelompok2.frontend.managers.AssetManager;
import com.kelompok2.frontend.managers.AudioManager;

public class MainMenuScreen extends ScreenAdapter {
    private final Main game;
    private Stage stage;
    private Skin skin;
    private Texture logoTexture;

    // Window Settings
    private Window settingsWindow;

    public MainMenuScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        skin = new Skin(Gdx.files.internal("uiskin.json"));

        // --- LOAD TEXTURES ---
        logoTexture = AssetManager.getInstance().loadTexture("maestra_trials_logo_pixel.png");
        logoTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        // Load Icon Textures (Pastikan file ini ada di assets!)
        Texture settingsIconTex = AssetManager.getInstance().loadTexture("settings_icon.png");
        Texture leaderboardIconTex = AssetManager.getInstance().loadTexture("leaderboard_icon.png");

        // Play main menu BGM
        AudioManager.getInstance().playMusic(
                "Audio/helmet_-_tales_of_the_helmets_knight_-_01_start_screen_theme_-_prelude (Start or main menu).wav",
                true);

        // --- 1. TABLE TENGAH (Logo, Start, Exit) ---
        Table centerTable = new Table();
        centerTable.setFillParent(true);
        stage.addActor(centerTable);

        Image logoImage = new Image(logoTexture);
        logoImage.setScaling(Scaling.fit);

        Label titleLabel = new Label("Roguelike Game", skin);
        titleLabel.setFontScale(0.5f);

        TextButton playButton = new TextButton("MULAI", skin);
        TextButton exitButton = new TextButton("KELUAR", skin);

        // Susun Center Table
        centerTable.add(logoImage).width(800).height(250).padTop(50).padBottom(20).row();
        centerTable.add(titleLabel).padBottom(50).row();
        centerTable.add(playButton).width(200).height(50).padBottom(20).row();
        centerTable.add(exitButton).width(200).height(50).row();

        // --- 2. TABLE POJOK KANAN ATAS (Leaderboard, Settings) ---
        Table topTable = new Table();
        topTable.setFillParent(true);
        topTable.top().right(); // Anchor ke Atas - Kanan
        stage.addActor(topTable);

        // Buat ImageButton dari Texture
        ImageButton leaderboardBtn = new ImageButton(new TextureRegionDrawable(new TextureRegion(leaderboardIconTex)));
        ImageButton settingsBtn = new ImageButton(new TextureRegionDrawable(new TextureRegion(settingsIconTex)));

        // Tambahkan ke Top Table dengan padding
        // Urutan add menentukan posisi (kiri ke kanan)
        topTable.add(leaderboardBtn).size(64, 64).padRight(20).padTop(20);
        topTable.add(settingsBtn).size(64, 64).padRight(20).padTop(20);

        // --- EVENT LISTENERS ---

        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Stop menu music before transitioning
                AudioManager.getInstance().stopMusic();
                game.setScreen(new CharacterSelectionScreen(game));
                dispose();
            }
        });

        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });

        // Logic Tombol Leaderboard
        leaderboardBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("Leaderboard clicked. Backend not connected yet.");
                // Nanti panggil fungsi fetch leaderboard di sini
            }
        });

        // Logic Tombol Settings
        settingsBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                settingsWindow.setVisible(true);
            }
        });

        createSettingsWindow();
    }

    private void createSettingsWindow() {
        settingsWindow = new Window("Pengaturan", skin);
        settingsWindow.setModal(true);
        settingsWindow.setMovable(true);
        settingsWindow.setVisible(false);
        settingsWindow.setSize(300, 200);
        settingsWindow.setPosition(Gdx.graphics.getWidth() / 2f - 150, Gdx.graphics.getHeight() / 2f - 100);

        Label volumeLabel = new Label("Volume Musik: 100%", skin);
        TextButton closeButton = new TextButton("Tutup", skin);

        settingsWindow.add(volumeLabel).pad(20).row();
        settingsWindow.add(closeButton).width(100).pad(10);

        closeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                settingsWindow.setVisible(false);
            }
        });

        stage.addActor(settingsWindow);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.15f, 0.15f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        if (settingsWindow != null) {
            settingsWindow.setPosition(width / 2f - 150, height / 2f - 100);
        }
    }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
    }
}
