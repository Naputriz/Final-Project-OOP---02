package com.kelompok2.frontend.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
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

    private Window leaderboardWindow;
    private Table leaderboardDataTx;

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
            // Stop menu music before transitioning
            @Override
            public void clicked(InputEvent event, float x, float y) {
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
                leaderboardWindow.setVisible(true);
                fetchLeaderboardData();
            }
        });

        settingsBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                settingsWindow.setVisible(true);
            }
        });

        createSettingsWindow();
        createLeaderboardWindow();
    }

    private void createSettingsWindow() {
        settingsWindow = new Window("Pengaturan", skin);
        settingsWindow.setModal(true);
        settingsWindow.setMovable(true);
        settingsWindow.setVisible(false);
        settingsWindow.setSize(450, 250);
        settingsWindow.setPosition(Gdx.graphics.getWidth() / 2f - 225, Gdx.graphics.getHeight() / 2f - 125);

        // music
        final Label musicLabel = new Label("Musik: " + (int)(AudioManager.getInstance().getMusicVolume() * 100) + "%", skin);
        final Slider musicSlider = new Slider(0f, 1f, 0.01f, false, skin); // music slider
        musicSlider.setValue(AudioManager.getInstance().getMusicVolume());

        // sfx (belum ada)
        final Label soundLabel = new Label("SFX: " + (int)(AudioManager.getInstance().getSoundVolume() * 100) + "%", skin);
        final Slider soundSlider = new Slider(0f, 1f, 0.01f, false, skin);
        soundSlider.setValue(AudioManager.getInstance().getSoundVolume());

        TextButton closeButton = new TextButton("Tutup", skin);

        musicSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float vol = musicSlider.getValue();
                AudioManager.getInstance().setMusicVolume(vol);
                musicLabel.setText("Musik: " + (int)(vol * 100) + "%");
            }
        });

        soundSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float vol = soundSlider.getValue();
                AudioManager.getInstance().setSoundVolume(vol);
                soundLabel.setText("SFX: " + (int)(vol * 100) + "%");
            }
        });

        closeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                settingsWindow.setVisible(false);
            }
        });

        settingsWindow.add(musicLabel).width(120).pad(10);
        settingsWindow.add(musicSlider).width(250).pad(10).row();
        settingsWindow.add(soundLabel).width(120).pad(10);
        settingsWindow.add(soundSlider).width(250).pad(10).row();
        settingsWindow.add(closeButton).colspan(2).width(100).padTop(20);

        stage.addActor(settingsWindow);
    }

    private void createLeaderboardWindow() {
        leaderboardWindow = new Window("Peringkat", skin);
        leaderboardWindow.setModal(true);
        leaderboardWindow.setMovable(true);
        leaderboardWindow.setVisible(false);
        leaderboardWindow.setSize(400, 500);
        leaderboardWindow.setPosition(Gdx.graphics.getWidth() / 2f - 200, Gdx.graphics.getHeight() / 2f - 250);

        leaderboardDataTx = new Table();
        ScrollPane scrollPane = new ScrollPane(leaderboardDataTx, skin);
        scrollPane.setFadeScrollBars(false);

        TextButton closeButton = new TextButton("Tutup", skin);
        closeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                leaderboardWindow.setVisible(false);
            }
        });

        leaderboardWindow.add(scrollPane).width(350).height(380).pad(10).row();
        leaderboardWindow.add(closeButton).width(100).pad(10);

        stage.addActor(leaderboardWindow);
    }

    private void fetchLeaderboardData() {
        leaderboardDataTx.clear();
        leaderboardDataTx.add(new Label("Memuat data...", skin)).pad(10);

        Net.HttpRequest request = new Net.HttpRequest(Net.HttpMethods.GET);
        request.setUrl("http://localhost:8080/api/leaderboard");
        request.setHeader("Content-Type", "application/json");

        Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                String result = httpResponse.getResultAsString();
                Gdx.app.postRunnable(() -> updateLeaderboardUI(result));
            }

            @Override
            public void failed(Throwable t) {
                Gdx.app.postRunnable(() -> {
                    leaderboardDataTx.clear();
                    leaderboardDataTx.add(new Label("Gagal koneksi!", skin)).row();
                    Label errorLabel = new Label("Pastikan server nyala.", skin);
                    errorLabel.setFontScale(0.8f);
                    leaderboardDataTx.add(errorLabel).row();
                });
            }

            @Override
            public void cancelled() {
                Gdx.app.postRunnable(() -> {
                    leaderboardDataTx.clear();
                    leaderboardDataTx.add(new Label("Dibatalkan.", skin));
                });
            }
        });
    }

    // Method pembantu untuk mengubah detik menjadi format Jam/Menit/Detik
    private String formatTime(int totalSeconds) {
        if (totalSeconds < 0) return "0 detik";

        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        StringBuilder sb = new StringBuilder();

        // Jika ada jam (misal: 1 jam ...)
        if (hours > 0) {
            sb.append(hours).append(" jam ");
            sb.append(minutes).append(" menit ");
            sb.append(seconds).append(" detik");
        }
        // Jika tidak ada jam, tapi ada menit (misal: 39 menit ...)
        else if (minutes > 0) {
            sb.append(minutes).append(" menit ");
            sb.append(seconds).append(" detik");
        }
        // Jika hanya detik (misal: 55 detik)
        else {
            sb.append(seconds).append(" detik");
        }

        return sb.toString();
    }

    private void updateLeaderboardUI(String jsonString) {
        leaderboardDataTx.clear();
        try {
            JsonValue root = new JsonReader().parse(jsonString);

            // --- HEADER TABEL (3 Kolom) ---
            // Kolom 1: Nama (Lebar)
            leaderboardDataTx.add(new Label("Nama", skin)).expandX().left().pad(5);

            // Kolom 2: Level (Tengah, agak kecil)
            leaderboardDataTx.add(new Label("Lvl", skin)).width(50).center().pad(5);

            // Kolom 3: Waktu (Kanan)
            leaderboardDataTx.add(new Label("Waktu", skin)).right().pad(5).row();

            // Garis Pemisah
            leaderboardDataTx.add(new Label("--------------------------------------------------------------------", skin)).colspan(0).row();

            // --- ISI DATA ---
            for (JsonValue score : root) {
                String name = score.getString("playerName");

                // Ambil level (default 1 jika data lama belum punya level)
                int level = score.getInt("level", 1);

                // Ambil waktu (detik)
                int value = score.getInt("value");

                // Format waktu menggunakan method baru kita
                String timeString = formatTime(value);

                // Masukkan ke Tabel
                leaderboardDataTx.add(new Label(name, skin)).left().pad(5);
                leaderboardDataTx.add(new Label(String.valueOf(level), skin)).center().pad(5);
                leaderboardDataTx.add(new Label(timeString, skin)).right().pad(5).row();
            }
        } catch (Exception e) {
            leaderboardDataTx.add(new Label("Error parsing data.", skin)).colspan(3);
            Gdx.app.error("Leaderboard", "JSON Parse Error", e);
        }
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
            settingsWindow.setPosition(width / 2f - 225, height / 2f - 125);
        }
        if (leaderboardWindow != null) {
            leaderboardWindow.setPosition(width / 2f - 200, height / 2f - 250);
        }
    }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
    }
}
