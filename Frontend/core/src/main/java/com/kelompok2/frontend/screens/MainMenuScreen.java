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
import com.badlogic.gdx.utils.Align;
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
    private Window accountWindow;

    // [BARU] Variabel untuk melacak Login Window
    private LoginWindow activeLoginWindow;

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
        Texture settingsIconTex = AssetManager.getInstance().loadTexture("settings_icon.png");
        Texture leaderboardIconTex = AssetManager.getInstance().loadTexture("leaderboard_icon.png");
        Texture accountIconTex = AssetManager.getInstance().loadTexture("settings_akun.png");

        AudioManager.getInstance().playMusic(
            "Audio/helmet_-_tales_of_the_helmets_knight_-_01_start_screen_theme_-_prelude (Start or main menu).wav",
            true);

        // --- TABLE TENGAH ---
        Table centerTable = new Table();
        centerTable.setFillParent(true);
        stage.addActor(centerTable);

        Image logoImage = new Image(logoTexture);
        logoImage.setScaling(Scaling.fit);
        Label titleLabel = new Label("Roguelike Game", skin);
        titleLabel.setFontScale(0.5f);
        TextButton playButton = new TextButton("MULAI", skin);
        TextButton exitButton = new TextButton("KELUAR", skin);

        centerTable.add(logoImage).width(800).height(250).padTop(50).padBottom(20).row();
        centerTable.add(titleLabel).padBottom(50).row();
        centerTable.add(playButton).width(200).height(50).padBottom(20).row();
        centerTable.add(exitButton).width(200).height(50).row();

        // --- TABLE KANAN ATAS ---
        Table topTable = new Table();
        topTable.setFillParent(true);
        topTable.top().right();
        stage.addActor(topTable);

        ImageButton leaderboardBtn = new ImageButton(new TextureRegionDrawable(new TextureRegion(leaderboardIconTex)));
        ImageButton settingsBtn = new ImageButton(new TextureRegionDrawable(new TextureRegion(settingsIconTex)));
        topTable.add(leaderboardBtn).size(64, 64).padRight(20).padTop(20);
        topTable.add(settingsBtn).size(64, 64).padRight(20).padTop(20);

        // --- TABLE KIRI ATAS ---
        Table topLeftTable = new Table();
        topLeftTable.setFillParent(true);
        topLeftTable.top().left();
        stage.addActor(topLeftTable);

        ImageButton accountBtn = new ImageButton(new TextureRegionDrawable(new TextureRegion(accountIconTex)));
        topLeftTable.add(accountBtn).size(64, 64).padLeft(20).padTop(20);

        // --- EVENT LISTENERS ---
        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (game.isLoggedIn()) {
                    AudioManager.getInstance().stopMusic();
                    game.setScreen(new CharacterSelectionScreen(game));
                    dispose();
                }
            }
        });
        exitButton.addListener(new ClickListener() { @Override public void clicked(InputEvent event, float x, float y) { Gdx.app.exit(); }});
        leaderboardBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                leaderboardWindow.setVisible(true);
                // Center window saat dibuka
                leaderboardWindow.setPosition(stage.getWidth() / 2f, stage.getHeight() / 2f, Align.center);
                fetchLeaderboardData();
            }
        });
        settingsBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                settingsWindow.setVisible(true);
                // Center window saat dibuka
                settingsWindow.setPosition(stage.getWidth() / 2f, stage.getHeight() / 2f, Align.center);
            }
        });
        accountBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                updateAccountInfo();
                accountWindow.setVisible(true);
            }
        });

        createSettingsWindow();
        createLeaderboardWindow();
        createAccountWindow();

        // --- LOGIKA AWAL ---
        if (!game.isLoggedIn()) {
            showLoginWindow(false);
        }
    }

    // --- METHOD DIPERBAIKI: SHOW LOGIN WINDOW ---
    private void showLoginWindow(boolean canCancel) {
        // Hapus yang lama jika ada (safety)
        if (activeLoginWindow != null) {
            activeLoginWindow.remove();
        }

        activeLoginWindow = new LoginWindow("Login", skin, game, canCancel, new Runnable() {
            @Override
            public void run() {
                System.out.println("User Logged In: " + game.getPlayerName());
                if(accountWindow != null) accountWindow.setVisible(false);
                activeLoginWindow = null; // Clear referensi saat login sukses/window tutup
            }
        });

        // Posisikan di tengah menggunakan Align.center
        activeLoginWindow.setPosition(stage.getWidth() / 2f, stage.getHeight() / 2f, Align.center);
        stage.addActor(activeLoginWindow);
    }

    private void createAccountWindow() {
        accountWindow = new Window("Info Akun", skin);
        accountWindow.setModal(true);
        accountWindow.setVisible(false);
        accountWindow.setSize(300, 200);
        accountWindow.setPosition(20, Gdx.graphics.getHeight() - 300);

        Label nameLabel = new Label("User: ...", skin);
        nameLabel.setName("lblUsername");
        nameLabel.setAlignment(Align.center);

        TextButton switchAccountBtn = new TextButton("Ganti Akun", skin);
        TextButton closeBtn = new TextButton("Tutup", skin);

        accountWindow.add(nameLabel).pad(20).row();
        accountWindow.add(switchAccountBtn).padBottom(10).width(150).row();
        accountWindow.add(closeBtn).width(100);

        switchAccountBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                accountWindow.setVisible(false);
                showLoginWindow(true);
            }
        });
        closeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) { accountWindow.setVisible(false); }
        });
        stage.addActor(accountWindow);
    }

    private void updateAccountInfo() {
        Label l = accountWindow.findActor("lblUsername");
        if (l != null) l.setText("User: " + game.getPlayerName());
    }

    // --- METHOD DIPERBAIKI: SETTINGS WINDOW (Menggunakan Align.center) ---
    private void createSettingsWindow() {
        settingsWindow = new Window("Pengaturan", skin);
        settingsWindow.setModal(true);
        settingsWindow.setMovable(true);
        settingsWindow.setVisible(false);
        settingsWindow.setSize(450, 250);
        // Posisi awal (nanti dihandle resize juga)
        settingsWindow.setPosition(Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() / 2f, Align.center);

        // music
        final Label musicLabel = new Label("Musik: " + (int)(AudioManager.getInstance().getMusicVolume() * 100) + "%", skin);
        final Slider musicSlider = new Slider(0f, 1f, 0.01f, false, skin);
        musicSlider.setValue(AudioManager.getInstance().getMusicVolume());

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
            public void clicked(InputEvent event, float x, float y) { settingsWindow.setVisible(false); }
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
        leaderboardWindow.setPosition(Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() / 2f, Align.center);

        leaderboardDataTx = new Table();
        ScrollPane scrollPane = new ScrollPane(leaderboardDataTx, skin);
        scrollPane.setFadeScrollBars(false);

        TextButton closeButton = new TextButton("Tutup", skin);
        closeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) { leaderboardWindow.setVisible(false); }
        });

        leaderboardWindow.add(scrollPane).width(350).height(380).pad(10).row();
        leaderboardWindow.add(closeButton).width(100).pad(10);
        stage.addActor(leaderboardWindow);
    }

    // ... (fetchLeaderboardData, formatTime, updateLeaderboardUI SAMA SAJA, TIDAK PERLU DIUBAH) ...
    // Copy paste bagian fetch, formatTime, updateUI dari kode Anda sebelumnya di sini
    private void fetchLeaderboardData() {
        leaderboardDataTx.clear();
        leaderboardDataTx.add(new Label("Memuat data...", skin)).pad(10);
        Net.HttpRequest request = new Net.HttpRequest(Net.HttpMethods.GET);
        request.setUrl("http://localhost:8080/api/leaderboard");
        request.setHeader("Content-Type", "application/json");
        Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {
            @Override public void handleHttpResponse(Net.HttpResponse httpResponse) {
                String result = httpResponse.getResultAsString();
                Gdx.app.postRunnable(() -> updateLeaderboardUI(result));
            }
            @Override public void failed(Throwable t) {
                Gdx.app.postRunnable(() -> {
                    leaderboardDataTx.clear();
                    leaderboardDataTx.add(new Label("Gagal koneksi!", skin)).row();
                });
            }
            @Override public void cancelled() {}
        });
    }

    private String formatTime(int totalSeconds) {
        if (totalSeconds < 0) return "0 detik";
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;
        StringBuilder sb = new StringBuilder();
        if (hours > 0) { sb.append(hours).append(" jam "); sb.append(minutes).append(" menit "); sb.append(seconds).append(" detik"); }
        else if (minutes > 0) { sb.append(minutes).append(" menit "); sb.append(seconds).append(" detik"); }
        else { sb.append(seconds).append(" detik"); }
        return sb.toString();
    }

    private void updateLeaderboardUI(String jsonString) {
        leaderboardDataTx.clear();
        try {
            JsonValue root = new JsonReader().parse(jsonString);
            leaderboardDataTx.add(new Label("Nama", skin)).expandX().left().pad(5);
            leaderboardDataTx.add(new Label("Char", skin)).width(80).center().pad(5);
            leaderboardDataTx.add(new Label("Lvl", skin)).width(80).center().pad(5);
            leaderboardDataTx.add(new Label("Waktu", skin)).right().pad(5).row();
            leaderboardDataTx.add(new Label("---------------------------------------------------------------------", skin)).colspan(4).row();
            for (JsonValue score : root) {
                String name = score.getString("playerName");
                String charName = score.getString("character", "-");
                int level = score.getInt("level", 1);
                int value = score.getInt("value");
                String timeString = formatTime(value);
                leaderboardDataTx.add(new Label(name, skin)).left().pad(5);
                if(charName.length() > 8) charName = charName.substring(0, 8) + ".";
                leaderboardDataTx.add(new Label(charName, skin)).center().pad(5);
                leaderboardDataTx.add(new Label(String.valueOf(level), skin)).center().pad(5);
                leaderboardDataTx.add(new Label(timeString, skin)).right().pad(5).row();
            }
        } catch (Exception e) {
            leaderboardDataTx.add(new Label("Error parsing data.", skin)).colspan(4);
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.15f, 0.15f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    // --- METHOD DIPERBAIKI: RESIZE (Memaksa semua window ke tengah) ---
    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);

        // Paksa window Settings ke tengah
        if (settingsWindow != null) {
            settingsWindow.setPosition(stage.getWidth() / 2f, stage.getHeight() / 2f, Align.center);
        }
        // Paksa window Leaderboard ke tengah
        if (leaderboardWindow != null) {
            leaderboardWindow.setPosition(stage.getWidth() / 2f, stage.getHeight() / 2f, Align.center);
        }
        // Window Akun tetap di kiri bawah
        if (accountWindow != null) {
            accountWindow.setPosition(20, height - 300);
        }

        // [PENTING] Paksa Login Window ke tengah jika sedang aktif
        if (activeLoginWindow != null) {
            activeLoginWindow.setPosition(stage.getWidth() / 2f, stage.getHeight() / 2f, Align.center);
        }
    }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
    }
}
