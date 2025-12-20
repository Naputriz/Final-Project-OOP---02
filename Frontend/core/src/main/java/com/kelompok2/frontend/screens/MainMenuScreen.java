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
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kelompok2.frontend.Main;
import com.kelompok2.frontend.managers.AssetManager;
import com.kelompok2.frontend.managers.AudioManager;
import com.kelompok2.frontend.ui.SettingsWindow;

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

    private SelectBox<String> charFilterBox;
    private SelectBox<String> sortFilterBox;
    private Table leaderboardContentTable;
    private Table filterTable;

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
        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });
        leaderboardBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                leaderboardWindow.getTitleLabel().setText("Peringkat Global (Top 20)"); // Ubah Judul
                filterTable.setVisible(true); // Tampilkan Filter
                leaderboardWindow.setVisible(true);
                leaderboardWindow.setPosition(stage.getWidth() / 2f, stage.getHeight() / 2f, Align.center);
                fetchLeaderboardData(); // Ambil data Global
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

        settingsWindow = new SettingsWindow(skin);
        stage.addActor(settingsWindow);

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
                if (accountWindow != null)
                    accountWindow.setVisible(false);
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
        accountWindow.setSize(300, 260); // Perbesar sedikit tingginya
        accountWindow.setPosition(20, Gdx.graphics.getHeight() - 300);

        Label nameLabel = new Label("User: ...", skin);
        nameLabel.setName("lblUsername");
        nameLabel.setAlignment(Align.center);

        // Tombol-tombol
        TextButton historyBtn = new TextButton("Riwayat Skor", skin); // [BARU]
        TextButton switchAccountBtn = new TextButton("Ganti Akun", skin);
        TextButton closeBtn = new TextButton("Tutup", skin);

        accountWindow.add(nameLabel).pad(20).row();
        accountWindow.add(historyBtn).padBottom(10).width(150).row(); // [BARU]
        accountWindow.add(switchAccountBtn).padBottom(10).width(150).row();
        accountWindow.add(closeBtn).width(100);

        // [BARU] Logic Tombol Riwayat
        historyBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                accountWindow.setVisible(false); // Tutup info akun

                // Siapkan Window Leaderboard untuk Mode Personal
                leaderboardWindow.getTitleLabel().setText("Riwayat: " + game.getPlayerName());
                filterTable.setVisible(false); // Sembunyikan Filter (agar bersih)
                leaderboardWindow.setVisible(true);
                leaderboardWindow.setPosition(stage.getWidth() / 2f, stage.getHeight() / 2f, Align.center);

                fetchPersonalHistory(); // Panggil data personal
            }
        });

        switchAccountBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                accountWindow.setVisible(false);
                showLoginWindow(true);
            }
        });

        closeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
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
        leaderboardWindow.setPosition(Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() / 2f, Align.center);

        // --- 1. BAGIAN FILTER (Disimpan ke variabel filterTable) ---
        filterTable = new Table(); // <--- INI PENTING

        filterTable.add(new Label("Filter:", skin)).padRight(10);
        charFilterBox = new SelectBox<>(skin);
        charFilterBox.setItems("All", "Isolde", "Ryze", "Insania", "Blaze", "Whisperwind", "Aelita", "Aegis", "Lumi");
        filterTable.add(charFilterBox).width(120).padRight(20);

        filterTable.add(new Label("Urutkan:", skin)).padRight(10);
        sortFilterBox = new SelectBox<>(skin);
        sortFilterBox.setItems("Waktu (Terlama)", "Level (Tertinggi)");
        filterTable.add(sortFilterBox).width(150);

        ChangeListener refreshListener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // Hanya fetch data global jika window sedang dalam mode global (filter visible)
                if (filterTable.isVisible()) {
                    fetchLeaderboardData();
                }
            }
        };
        charFilterBox.addListener(refreshListener);
        sortFilterBox.addListener(refreshListener);

        // --- 2. HEADER TABEL (Sama seperti sebelumnya) ---
        Table headerTable = new Table();
        headerTable.setBackground(skin.newDrawable("white", 0.2f, 0.2f, 0.2f, 1f));

        float colRank = 40;
        float colName = 140;
        float colChar = 130;
        float colLvl = 60;
        float colTime = 120;

        headerTable.add(new Label("#", skin)).width(colRank).center();
        headerTable.add(new Label("Nama", skin)).width(colName).left().padLeft(10);
        headerTable.add(new Label("Char", skin)).width(colChar).center();
        headerTable.add(new Label("Lvl", skin)).width(colLvl).center();
        headerTable.add(new Label("Waktu", skin)).width(colTime).right().padRight(10);

        // --- 3. ISI DATA ---
        leaderboardContentTable = new Table();
        leaderboardContentTable.top();
        ScrollPane scrollPane = new ScrollPane(leaderboardContentTable, skin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);

        // --- 4. TOMBOL TUTUP ---
        TextButton closeButton = new TextButton("Tutup", skin);
        closeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                leaderboardWindow.setVisible(false);
            }
        });

        // Susun ke Window
        leaderboardWindow.add(filterTable).pad(10).fillX().row(); // Filter disimpan di sini
        leaderboardWindow.add(headerTable).height(30).fillX().row();
        leaderboardWindow.add(scrollPane).expand().fill().row();
        leaderboardWindow.add(closeButton).width(100).pad(10);

        stage.addActor(leaderboardWindow);
    }

    private void fetchLeaderboardData() {
        leaderboardContentTable.clear();
        leaderboardContentTable.add(new Label("Memuat data...", skin)).pad(20);

        // Ambil nilai dari Dropdown
        String selectedChar = charFilterBox.getSelected();
        String selectedSortRaw = sortFilterBox.getSelected();

        // Konversi text dropdown ke parameter API
        String sortParam = selectedSortRaw.equals("Level (Tertinggi)") ? "level" : "time";

        Net.HttpRequest request = new Net.HttpRequest(Net.HttpMethods.GET);
        // Susun URL: /api/leaderboard?character=Ryze&sortBy=level
        String url = "http://localhost:8080/api/leaderboard?character=" + selectedChar + "&sortBy=" + sortParam;

        request.setUrl(url);
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
                    leaderboardContentTable.add(new Label("Gagal koneksi!", skin)).row();
                });
            }

            @Override
            public void cancelled() {
            }
        });
    }

    private void fetchPersonalHistory() {
        leaderboardContentTable.clear();
        leaderboardContentTable.add(new Label("Memuat riwayatmu...", skin)).pad(20);

        Net.HttpRequest request = new Net.HttpRequest(Net.HttpMethods.GET);
        // Panggil endpoint baru: /api/leaderboard/player?name=NAMAPLAYER
        request.setUrl("http://localhost:8080/api/leaderboard/player?name=" + game.getPlayerName());
        request.setHeader("Content-Type", "application/json");

        Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                String result = httpResponse.getResultAsString();
                Gdx.app.postRunnable(() -> updateLeaderboardUI(result)); // Reuse UI logic yang sama!
            }

            @Override
            public void failed(Throwable t) {
                Gdx.app.postRunnable(() -> {
                    leaderboardContentTable.clear();
                    leaderboardContentTable.add(new Label("Gagal koneksi!", skin));
                });
            }

            @Override
            public void cancelled() {
            }
        });
    }

    private String formatTime(int totalSeconds) {
        if (totalSeconds < 0)
            return "0 detik";
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

            // Ukuran Kolom (HARUS SAMA dengan Header di createLeaderboardWindow)
            float colRank = 40;
            float colName = 140;
            float colChar = 130;
            float colLvl = 60;
            float colTime = 120;

            int rank = 1;
            String myName = game.getPlayerName(); // Untuk highlight nama sendiri

            for (JsonValue score : root) {
                String name = score.getString("playerName");
                String charName = score.getString("character", "-");
                int level = score.getInt("level", 1);
                int value = score.getInt("value");
                String timeString = formatTime(value);

                Color textColor = name.equals(myName) ? Color.GOLD : Color.WHITE;

                // --- BARIS DATA ---
                // Peringkat
                Label lblRank = new Label(String.valueOf(rank), skin);
                lblRank.setColor(textColor);
                leaderboardContentTable.add(lblRank).width(colRank).center();

                // Nama (Align Kiri)
                Label lblName = new Label(name, skin);
                lblName.setColor(textColor);
                lblName.setEllipsis(true); // ... jika kepanjangan
                leaderboardContentTable.add(lblName).width(colName).left().padLeft(10);

                // Karakter (Align Tengah)
                Label lblChar = new Label(charName, skin);
                lblChar.setColor(textColor);
                leaderboardContentTable.add(lblChar).width(colChar).center();

                // Level (Align Tengah)
                Label lblLvl = new Label(String.valueOf(level), skin);
                lblLvl.setColor(textColor);
                leaderboardContentTable.add(lblLvl).width(colLvl).center();

                // Waktu (Align Kanan)
                Label lblTime = new Label(timeString, skin);
                lblTime.setColor(textColor);
                leaderboardContentTable.add(lblTime).width(colTime).right().padRight(10).row();

                // Garis tipis antar baris (Opsional, biar rapi)
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
