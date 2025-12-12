package com.kelompok2.frontend.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kelompok2.frontend.Main;
import com.kelompok2.frontend.managers.AssetManager;
import com.kelompok2.frontend.managers.GameManager;

public class MainMenuScreen extends ScreenAdapter {
    private final Main game;
    private Stage stage; // Panggung untuk meletakkan UI
    private Skin skin; // Gaya visual tombol/font
    private Texture logoTexture;

    // Variabel untuk Window Settings (Pop-up)
    private Window settingsWindow;

    public MainMenuScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        // 1. Setup Stage & Input
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage); // PENTING: Agar tombol bisa diklik!

        // 2. Load Skin (Pastikan file uiskin ada di folder assets)
        skin = new Skin(Gdx.files.internal("uiskin.json"));

        // 3. Layout Utama (Table)
        Table mainTable = new Table();
        mainTable.setFillParent(true); // Memenuhi layar
        // mainTable.setDebug(true); // Uncomment baris ini untuk melihat garis layout
        stage.addActor(mainTable);
        // Load logo melalui AssetManager (Singleton Pattern)
        logoTexture = AssetManager.getInstance().loadTexture("maestra_trials_logo_pixel.png");

        // --- KOMPONEN UI ---
        logoTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        Image logoImage = new Image(logoTexture);
        logoImage.setScaling(Scaling.fit);
        // Judul Game
        Label titleLabel = new Label("Roguelike Game", skin);
        titleLabel.setFontScale(0.5f); // Perbesar font

        // Tombol-tombol
        TextButton playButton = new TextButton("MULAI", skin);
        TextButton settingsButton = new TextButton("PENGATURAN", skin);
        TextButton exitButton = new TextButton("KELUAR", skin);

        // --- MENYUSUN LAYOUT ---
        mainTable.add(logoImage)
                .width(1000).height(200) // Atur UKURAN LOGO di sini (sesuaikan pixelnya)
                .padTop(50) // Jarak dari atas layar
                .padBottom(50) // Jarak antara logo dan tombol
                .row(); // Pindah baris ke bawah
        mainTable.add(titleLabel).padBottom(50).row();
        mainTable.add(playButton).width(200).height(50).padBottom(20).row();
        mainTable.add(settingsButton).width(200).height(50).padBottom(20).row();
        mainTable.add(exitButton).width(200).height(50).row();

        // --- LOGIKA TOMBOL (Event Listeners) ---

        // 1. Tombol MULAI -> Pindah ke GameScreen
        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Reset GameManager untuk game baru
                GameManager.getInstance().reset();

                // Pindah screen
                game.setScreen(new GameScreen());
                // Dispose menu saat ini agar hemat memori
                dispose();
            }
        });

        // 2. Tombol PENGATURAN -> Buka Pop-up
        settingsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                settingsWindow.setVisible(true); // Tampilkan window
            }
        });

        // 3. Tombol KELUAR -> Tutup Aplikasi
        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });

        // --- SETUP POP-UP SETTINGS ---
        createSettingsWindow();
    }

    private void createSettingsWindow() {
        // Membuat Jendela kecil di tengah layar
        settingsWindow = new Window("Pengaturan Suara", skin);
        settingsWindow.setModal(true); // Mencegah klik di luar window
        settingsWindow.setMovable(true);
        settingsWindow.setVisible(false); // Default tersembunyi
        settingsWindow.setSize(300, 200);
        settingsWindow.setPosition(
                Gdx.graphics.getWidth() / 2f - 150,
                Gdx.graphics.getHeight() / 2f - 100);

        // Isi window setting
        Label volumeLabel = new Label("Volume Musik: 100%", skin);
        TextButton closeButton = new TextButton("Tutup / Kembali", skin);

        settingsWindow.add(volumeLabel).pad(20).row();
        settingsWindow.add(closeButton).width(150).pad(10);

        // Logic tombol Tutup di dalam Settings
        closeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                settingsWindow.setVisible(false); // Sembunyikan window
            }
        });

        stage.addActor(settingsWindow);
    }

    @Override
    public void render(float delta) {
        // Bersihkan layar dengan warna abu-abu gelap
        Gdx.gl.glClearColor(0.15f, 0.15f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Update logika UI dan Gambar UI
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        // Update viewport saat window di-resize agar UI tidak gepeng
        stage.getViewport().update(width, height, true);

        // Update posisi pop-up settings agar tetap di tengah
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
