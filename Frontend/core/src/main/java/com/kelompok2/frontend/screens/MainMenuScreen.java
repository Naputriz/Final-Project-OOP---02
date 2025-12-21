package com.kelompok2.frontend.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
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
import com.badlogic.gdx.utils.Array; // [PENTING]
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.kelompok2.frontend.Main;
import com.kelompok2.frontend.managers.AssetManager;
import com.kelompok2.frontend.managers.AudioManager;
import com.kelompok2.frontend.models.ScoreData; // [PENTING]
import com.kelompok2.frontend.ui.SettingsWindow;

public class MainMenuScreen extends ScreenAdapter {
    private final Main game;
    private Stage stage;
    private Skin skin;
    private Texture logoTexture;

    // Window Settings
    private SettingsWindow settingsWindow;
    private Window leaderboardWindow;
    private Window accountWindow;
    private LoginWindow activeLoginWindow;

    private SelectBox<String> charFilterBox;
    private SelectBox<String> sortFilterBox;
    private Table leaderboardContentTable;
    private Table filterTable;

    // Resolusi Virtual 1920x1080
    private static final float WORLD_WIDTH = 1920;
    private static final float WORLD_HEIGHT = 1080;

    public MainMenuScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage = new Stage(new FitViewport(WORLD_WIDTH, WORLD_HEIGHT));
        Gdx.input.setInputProcessor(stage);
        skin = new Skin(Gdx.files.internal("uiskin.json"));

        // Load Textures
        logoTexture = AssetManager.getInstance().loadTexture("maestra_trials_logo_pixel.png");
        logoTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        Texture settingsIconTex = AssetManager.getInstance().loadTexture("settings_icon.png");
        Texture leaderboardIconTex = AssetManager.getInstance().loadTexture("leaderboard_icon.png");
        Texture accountIconTex = AssetManager.getInstance().loadTexture("settings_akun.png");

        AudioManager.getInstance().playMusic(
            "Audio/helmet_-_tales_of_the_helmets_knight_-_01_start_screen_theme_-_prelude (Start or main menu).wav",
            true);

        // --- CENTER TABLE ---
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

        // --- TOP RIGHT ---
        Table topTable = new Table();
        topTable.setFillParent(true);
        topTable.top().right();
        stage.addActor(topTable);
        ImageButton leaderboardBtn = new ImageButton(new TextureRegionDrawable(new TextureRegion(leaderboardIconTex)));
        ImageButton settingsBtn = new ImageButton(new TextureRegionDrawable(new TextureRegion(settingsIconTex)));
        topTable.add(leaderboardBtn).size(64, 64).padRight(20).padTop(20);
        topTable.add(settingsBtn).size(64, 64).padRight(20).padTop(20);

        // --- TOP LEFT ---
        Table topLeftTable = new Table();
        topLeftTable.setFillParent(true);
        topLeftTable.top().left();
        stage.addActor(topLeftTable);
        ImageButton accountBtn = new ImageButton(new TextureRegionDrawable(new TextureRegion(accountIconTex)));
        topLeftTable.add(accountBtn).size(64, 64).padLeft(20).padTop(20);

        // --- LISTENERS ---
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
        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });

        leaderboardBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                leaderboardWindow.getTitleLabel().setText("Peringkat Global (Top 20)");
                filterTable.setVisible(true);
                leaderboardWindow.setVisible(true);
                leaderboardWindow.setPosition(stage.getWidth() / 2f, stage.getHeight() / 2f, Align.center);
                fetchLeaderboardData();
            }
        });

        settingsBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                settingsWindow.show(stage);
            }
        });

        accountBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                updateAccountInfo();
                accountWindow.setVisible(true);
            }
        });

        // Initialize Windows
        settingsWindow = new SettingsWindow(game, skin);
        createLeaderboardWindow();
        createAccountWindow();

        if (!game.isLoggedIn()) {
            showLoginWindow(false);
        }
    }

    private void showLoginWindow(boolean canCancel) {
        if (activeLoginWindow != null) activeLoginWindow.remove();
        activeLoginWindow = new LoginWindow("Login", skin, game, canCancel, () -> {
            System.out.println("User Logged In: " + game.getPlayerName());
            if (accountWindow != null) accountWindow.setVisible(false);
            activeLoginWindow = null;
        });
        activeLoginWindow.setPosition(stage.getWidth() / 2f, stage.getHeight() / 2f, Align.center);
        stage.addActor(activeLoginWindow);
    }

    private void createAccountWindow() {
        accountWindow = new Window("Info Akun", skin);
        accountWindow.setModal(true);
        accountWindow.setVisible(false);
        accountWindow.setSize(300, 260);
        accountWindow.setPosition(20, WORLD_HEIGHT - 300);

        Label nameLabel = new Label("User: ...", skin);
        nameLabel.setName("lblUsername");
        nameLabel.setAlignment(Align.center);
        TextButton historyBtn = new TextButton("Riwayat Skor", skin);
        TextButton switchAccountBtn = new TextButton("Ganti Akun", skin);
        TextButton closeBtn = new TextButton("Tutup", skin);

        accountWindow.add(nameLabel).pad(20).row();
        accountWindow.add(historyBtn).padBottom(10).width(150).row();
        accountWindow.add(switchAccountBtn).padBottom(10).width(150).row();
        accountWindow.add(closeBtn).width(100);

        historyBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                accountWindow.setVisible(false);
                leaderboardWindow.getTitleLabel().setText("Riwayat: " + game.getPlayerName());
                filterTable.setVisible(false); // Sembunyikan Filter untuk history personal
                leaderboardWindow.setVisible(true);
                leaderboardWindow.setPosition(stage.getWidth() / 2f, stage.getHeight() / 2f, Align.center);

                // [PENTING] Panggil fetchPersonalHistory
                fetchPersonalHistory();
            }
        });
        switchAccountBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                accountWindow.setVisible(false);
                showLoginWindow(true);
            }
        });
        closeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                accountWindow.setVisible(false);
            }
        });
        stage.addActor(accountWindow);
    }

    private void updateAccountInfo() {
        Label l = accountWindow.findActor("lblUsername");
        if (l != null)
            l.setText("User: " + game.getPlayerName());
    }

    private void createLeaderboardWindow() {
        leaderboardWindow = new Window("Peringkat Global", skin);
        leaderboardWindow.setModal(true);
        leaderboardWindow.setMovable(true);
        leaderboardWindow.setVisible(false);
        leaderboardWindow.setSize(600, 600);
        leaderboardWindow.setPosition(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f, Align.center);

        filterTable = new Table();
        filterTable.add(new Label("Filter:", skin)).padRight(10);
        charFilterBox = new SelectBox<>(skin);
        charFilterBox.setItems("All", "Isolde", "Ryze", "Insania", "Blaze", "Whisperwind", "Aelita", "Aegis", "Lumi", "Alice");
        filterTable.add(charFilterBox).width(120).padRight(20);
        filterTable.add(new Label("Urutkan:", skin)).padRight(10);
        sortFilterBox = new SelectBox<>(skin);
        sortFilterBox.setItems("Waktu (Terlama)", "Level (Tertinggi)");
        filterTable.add(sortFilterBox).width(150);

        ChangeListener refreshListener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (filterTable.isVisible()) fetchLeaderboardData();
            }
        };
        charFilterBox.addListener(refreshListener);
        sortFilterBox.addListener(refreshListener);

        Table headerTable = new Table();
        headerTable.setBackground(skin.newDrawable("white", 0.2f, 0.2f, 0.2f, 1f));
        headerTable.add(new Label("#", skin)).width(40).center();
        headerTable.add(new Label("Nama", skin)).width(140).left().padLeft(10);
        headerTable.add(new Label("Char", skin)).width(130).center();
        headerTable.add(new Label("Lvl", skin)).width(60).center();
        headerTable.add(new Label("Waktu", skin)).width(120).right().padRight(10);

        leaderboardContentTable = new Table();
        leaderboardContentTable.top();
        ScrollPane scrollPane = new ScrollPane(leaderboardContentTable, skin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);

        TextButton closeButton = new TextButton("Tutup", skin);
        closeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                leaderboardWindow.setVisible(false);
            }
        });

        leaderboardWindow.add(filterTable).pad(10).fillX().row();
        leaderboardWindow.add(headerTable).height(30).fillX().row();
        leaderboardWindow.add(scrollPane).expand().fill().row();
        leaderboardWindow.add(closeButton).width(100).pad(10);
        stage.addActor(leaderboardWindow);
    }

    private void fetchLeaderboardData() {
        leaderboardContentTable.clear();
        leaderboardContentTable.add(new Label("Memuat data...", skin)).pad(20);
        String selectedChar = charFilterBox.getSelected();
        String selectedSortRaw = sortFilterBox.getSelected();
        String sortParam = selectedSortRaw.equals("Level (Tertinggi)") ? "level" : "time";

        Net.HttpRequest request = new Net.HttpRequest(Net.HttpMethods.GET);
        request.setUrl("http://localhost:8080/api/leaderboard?character=" + selectedChar + "&sortBy=" + sortParam);
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
                    leaderboardContentTable.clear();
                    leaderboardContentTable.add(new Label("Gagal koneksi!", skin));
                });
            }
            @Override
            public void cancelled() {}
        });
    }

    // --- [LOGIKA PENTING: HISTORY GUEST] ---
    private void fetchPersonalHistory() {
        leaderboardContentTable.clear();

        // 1. CEK JIKA USER ADALAH GUEST
        if (game.getPlayerName().equals("Guest")) {
            leaderboardContentTable.add(new Label("Riwayat Lokal (Tamu)", skin)).pad(20);

            // Ambil data dari RAM (Main.java)
            Array<ScoreData> history = game.getGuestHistory();

            if (history.size == 0) {
                updateLeaderboardUI("[]"); // Kosong
            } else {
                // Konversi data Java ke format JSON String Manual
                // Agar bisa dibaca oleh method updateLeaderboardUI yang sudah ada
                StringBuilder sb = new StringBuilder();
                sb.append("[");

                for (int i = 0; i < history.size; i++) {
                    ScoreData data = history.get(i);
                    sb.append("{");
                    sb.append("\"playerName\":\"Guest\",");
                    sb.append("\"character\":\"").append(data.character).append("\",");
                    sb.append("\"level\":").append(data.level).append(",");
                    sb.append("\"value\":").append(data.value);
                    sb.append("}");

                    if (i < history.size - 1) sb.append(",");
                }
                sb.append("]");

                // Update UI langsung tanpa internet
                updateLeaderboardUI(sb.toString());
            }
            return; // STOP! Jangan request ke server
        }

        // 2. JIKA MEMBER (Logic Lama - Request ke Server)
        leaderboardContentTable.add(new Label("Memuat riwayatmu...", skin)).pad(20);
        Net.HttpRequest request = new Net.HttpRequest(Net.HttpMethods.GET);
        request.setUrl("http://localhost:8080/api/leaderboard/player?name=" + game.getPlayerName());
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
                    leaderboardContentTable.clear();
                    leaderboardContentTable.add(new Label("Gagal koneksi!", skin));
                });
            }
            @Override
            public void cancelled() {}
        });
    }

    private String formatTime(int totalSeconds) {
        if (totalSeconds < 0) return "0 detik";
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;
        StringBuilder sb = new StringBuilder();
        if (hours > 0) {
            sb.append(hours).append(" h ");
            sb.append(minutes).append(" m ");
            sb.append(seconds).append(" s");
        } else if (minutes > 0) {
            sb.append(minutes).append(" m ");
            sb.append(seconds).append(" s");
        } else {
            sb.append(seconds).append(" s");
        }
        return sb.toString();
    }

    private void updateLeaderboardUI(String jsonString) {
        leaderboardContentTable.clear();
        try {
            JsonValue root = new JsonReader().parse(jsonString);
            float colRank = 40; float colName = 140; float colChar = 130; float colLvl = 60; float colTime = 120;
            int rank = 1;
            String myName = game.getPlayerName();

            for (JsonValue score : root) {
                String name = score.getString("playerName");
                String charName = score.getString("character", "-");
                int level = score.getInt("level", 1);
                int value = score.getInt("value");
                String timeString = formatTime(value);

                Color textColor = name.equals(myName) ? Color.GOLD : Color.WHITE;

                Label lblRank = new Label(String.valueOf(rank), skin);
                lblRank.setColor(textColor);
                leaderboardContentTable.add(lblRank).width(colRank).center();

                Label lblName = new Label(name, skin);
                lblName.setColor(textColor);
                lblName.setEllipsis(true);
                leaderboardContentTable.add(lblName).width(colName).left().padLeft(10);

                Label lblChar = new Label(charName, skin);
                lblChar.setColor(textColor);
                leaderboardContentTable.add(lblChar).width(colChar).center();

                Label lblLvl = new Label(String.valueOf(level), skin);
                lblLvl.setColor(textColor);
                leaderboardContentTable.add(lblLvl).width(colLvl).center();

                Label lblTime = new Label(timeString, skin);
                lblTime.setColor(textColor);
                leaderboardContentTable.add(lblTime).width(colTime).right().padRight(10).row();

                Image sep = new Image(skin.newDrawable("white", 1, 1, 1, 0.1f));
                leaderboardContentTable.add(sep).height(1).colspan(5).fillX().padBottom(2).row();

                rank++;
            }

            if (root.size == 0) {
                leaderboardContentTable.add(new Label("Belum ada data.", skin)).colspan(5).pad(20);
            }

        } catch (Exception e) {
            leaderboardContentTable.add(new Label("Error parsing data.", skin));
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

        if (settingsWindow != null) settingsWindow.setPosition(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f, Align.center);
        if (leaderboardWindow != null) leaderboardWindow.setPosition(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f, Align.center);
        if (accountWindow != null) accountWindow.setPosition(20, WORLD_HEIGHT - 300);
        if (activeLoginWindow != null) activeLoginWindow.setPosition(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f, Align.center);
    }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
    }
}
