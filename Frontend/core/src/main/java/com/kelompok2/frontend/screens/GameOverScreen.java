package com.kelompok2.frontend.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
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
import com.badlogic.gdx.utils.Align; // Penting untuk alignment
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

    // Label tambahan untuk status upload
    private Label statusLabel;

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
        titleLabel.setAlignment(Align.center);

        Label scoreLabel = new Label(String.format("Character: %s\nLevel Reached: %d\nTime Survived: %.1fs",
            lastCharacter, finalLevel, finalTime), skin);
        scoreLabel.setAlignment(Align.center);

        // Label Status Upload (Fitur Baru)
        statusLabel = new Label("Mengirim skor ke leaderboard...", skin);
        statusLabel.setColor(Color.YELLOW);
        statusLabel.setAlignment(Align.center);
        statusLabel.setFontScale(0.8f);

        TextButton restartButton = new TextButton("RESTART", skin);
        TextButton charSelectButton = new TextButton("PILIH KARAKTER", skin);
        TextButton homeButton = new TextButton("MENU UTAMA", skin);

        // --- LAYOUT ---
        table.add(titleLabel).padBottom(20).row();
        table.add(scoreLabel).padBottom(10).row();
        table.add(statusLabel).padBottom(30).row(); // Menambahkan status label di sini
        table.add(restartButton).width(250).height(60).padBottom(15).row();
        table.add(charSelectButton).width(250).height(60).padBottom(15).row();
        table.add(homeButton).width(250).height(60).row();

        // --- LOGIC ---

        // 1. Restart
        restartButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new GameScreen(game, lastCharacter));
                dispose();
            }
        });

        // 2. Character Select
        charSelectButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new CharacterSelectionScreen(game));
                dispose();
            }
        });

        // 3. Home
        homeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MainMenuScreen(game));
                dispose();
            }
        });

        // --- BACKEND INTEGRATION ---
        // Kirim skor otomatis saat layar dibuka
        // Kita cast finalTime (float) ke (int) karena backend biasanya menyimpan integer
        sendScoreToBackend(game.getPlayerName(), (int) finalTime);
    }

    private void sendScoreToBackend(String name, int time) {
        Net.HttpRequest request = new Net.HttpRequest(Net.HttpMethods.POST);
        request.setUrl("http://localhost:8080/api/leaderboard");
        request.setHeader("Content-Type", "application/json");

        // Membuat JSON payload manual
        String jsonPayload = "{\"playerName\":\"" + name + "\", \"value\":" + time + "}";
        request.setContent(jsonPayload);

        Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                int statusCode = httpResponse.getStatus().getStatusCode();

                Gdx.app.postRunnable(() -> {
                    if (statusCode == 200 || statusCode == 201) {
                        statusLabel.setText("Skor berhasil disimpan di Leaderboard!");
                        statusLabel.setColor(Color.GREEN);
                    } else {
                        statusLabel.setText("Gagal menyimpan skor (Error " + statusCode + ")");
                        statusLabel.setColor(Color.RED);
                    }
                });
            }

            @Override
            public void failed(Throwable t) {
                Gdx.app.postRunnable(() -> {
                    statusLabel.setText("Gagal koneksi ke server Leaderboard.");
                    statusLabel.setColor(Color.RED);
                });
            }

            @Override
            public void cancelled() {
                // Do nothing
            }
        });
    }

    @Override
    public void render(float delta) {
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
