package com.kelompok2.frontend.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kelompok2.frontend.Main;
import com.kelompok2.frontend.managers.AudioManager;

public class LoginScreen extends ScreenAdapter {
    private final Main game;
    private Stage stage;
    private Skin skin;

    // UI Components
    private TextField usernameField;
    private TextField passwordField;
    private TextField confirmPasswordField; // Hanya muncul saat register
    private Label messageLabel; // Untuk menampilkan error/sukses
    private TextButton actionButton; // Tombol "Masuk" atau "Daftar"
    private TextButton switchModeButton; // Tombol ganti mode

    // State
    private boolean isRegisterMode = false;

    // API Config (Sesuaikan dengan backend kamu)
    private static final String BASE_URL = "http://localhost:8080/api";

    public LoginScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        skin = new Skin(Gdx.files.internal("uiskin.json"));

        // Putar musik background
        AudioManager.getInstance().playMusic(
            "Audio/helmet_-_tales_of_the_helmets_knight_-_01_start_screen_theme_-_prelude (Start or main menu).wav",
            true);

        rebuildUI();
    }

    private void rebuildUI() {
        stage.clear(); // Bersihkan stage sebelum membangun ulang (berguna saat switch mode)

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        // --- TITLE ---
        Label titleLabel = new Label(isRegisterMode ? "DAFTAR AKUN BARU" : "LOGIN PENGGUNA", skin);
        titleLabel.setFontScale(1.2f);
        titleLabel.setAlignment(Align.center);

        // --- INPUT FIELDS ---
        usernameField = new TextField("", skin);
        usernameField.setMessageText("Username");
        usernameField.setAlignment(Align.center);

        passwordField = new TextField("", skin);
        passwordField.setMessageText("Password");
        passwordField.setPasswordMode(true);
        passwordField.setPasswordCharacter('*');
        passwordField.setAlignment(Align.center);

        confirmPasswordField = new TextField("", skin);
        confirmPasswordField.setMessageText("Konfirmasi Password");
        confirmPasswordField.setPasswordMode(true);
        confirmPasswordField.setPasswordCharacter('*');
        confirmPasswordField.setAlignment(Align.center);

        // --- MESSAGE LABEL ---
        messageLabel = new Label("", skin);
        messageLabel.setColor(1, 0.3f, 0.3f, 1); // Warna merah muda
        messageLabel.setAlignment(Align.center);

        // --- BUTTONS ---
        actionButton = new TextButton(isRegisterMode ? "DAFTAR" : "MASUK", skin);
        switchModeButton = new TextButton(isRegisterMode ? "Sudah punya akun? Login" : "Belum punya akun? Daftar", skin);

        TextButton guestButton = new TextButton("Masuk sebagai Tamu", skin);

        // --- LAYOUT ---
        table.add(titleLabel).padBottom(30).row();
        table.add(usernameField).width(300).height(40).padBottom(10).row();
        table.add(passwordField).width(300).height(40).padBottom(10).row();

        if (isRegisterMode) {
            table.add(confirmPasswordField).width(300).height(40).padBottom(10).row();
        }

        table.add(messageLabel).padBottom(10).row();
        table.add(actionButton).width(200).height(50).padBottom(10).row();
        table.add(switchModeButton).padBottom(20).row();
        table.add(guestButton).width(200).height(40).row();

        // --- LISTENERS ---

        // 1. Action Button (Login / Register)
        actionButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (isRegisterMode) {
                    handleRegister();
                } else {
                    handleLogin();
                }
            }
        });

        // 2. Switch Mode Button
        switchModeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                isRegisterMode = !isRegisterMode; // Toggle mode
                messageLabel.setText(""); // Reset pesan error
                rebuildUI(); // Render ulang tampilan
            }
        });

        // 3. Guest Button
        guestButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                proceedToMainMenu("Guest");
            }
        });
    }

    private void handleLogin() {
        String user = usernameField.getText().trim();
        String pass = passwordField.getText().trim();

        if (user.isEmpty() || pass.isEmpty()) {
            messageLabel.setText("Username dan Password harus diisi!");
            return;
        }

        messageLabel.setText("Sedang memproses...");
        messageLabel.setColor(1, 1, 1, 1);

        // Buat JSON Body
        String jsonBody = "{ \"username\": \"" + user + "\", \"password\": \"" + pass + "\" }";

        Net.HttpRequest request = new Net.HttpRequest(Net.HttpMethods.POST);
        request.setUrl(BASE_URL + "/login");
        request.setHeader("Content-Type", "application/json");
        request.setContent(jsonBody);

        Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                String result = httpResponse.getResultAsString();
                int statusCode = httpResponse.getStatus().getStatusCode();

                Gdx.app.postRunnable(() -> {
                    if (statusCode == 200) {
                        // Asumsi server mengembalikan JSON user atau pesan sukses
                        proceedToMainMenu(user);
                    } else {
                        messageLabel.setColor(1, 0.3f, 0.3f, 1);
                        messageLabel.setText("Login Gagal: Username/Password salah");
                    }
                });
            }

            @Override
            public void failed(Throwable t) {
                Gdx.app.postRunnable(() -> {
                    messageLabel.setColor(1, 0, 0, 1);
                    messageLabel.setText("Koneksi Error: Cek Server!");
                });
            }

            @Override
            public void cancelled() {}
        });
    }

    private void handleRegister() {
        String user = usernameField.getText().trim();
        String pass = passwordField.getText().trim();
        String confirm = confirmPasswordField.getText().trim();

        if (user.isEmpty() || pass.isEmpty()) {
            messageLabel.setText("Semua kolom harus diisi!");
            return;
        }

        if (!pass.equals(confirm)) {
            messageLabel.setText("Password tidak sama!");
            return;
        }

        messageLabel.setText("Mendaftarkan akun...");
        messageLabel.setColor(1, 1, 1, 1);

        String jsonBody = "{ \"username\": \"" + user + "\", \"password\": \"" + pass + "\" }";

        Net.HttpRequest request = new Net.HttpRequest(Net.HttpMethods.POST);
        request.setUrl(BASE_URL + "/register");
        request.setHeader("Content-Type", "application/json");
        request.setContent(jsonBody);

        Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                int statusCode = httpResponse.getStatus().getStatusCode();

                Gdx.app.postRunnable(() -> {
                    if (statusCode == 200 || statusCode == 201) {
                        // Registrasi sukses, otomatis login atau suruh login
                        messageLabel.setColor(0.3f, 1, 0.3f, 1);
                        messageLabel.setText("Akun dibuat! Silakan Login.");
                        isRegisterMode = false; // Kembali ke mode login
                        usernameField.setText(user);
                        passwordField.setText("");
                        rebuildUI();
                    } else {
                        messageLabel.setColor(1, 0.3f, 0.3f, 1);
                        messageLabel.setText("Gagal: Username mungkin sudah ada.");
                    }
                });
            }

            @Override
            public void failed(Throwable t) {
                Gdx.app.postRunnable(() -> {
                    messageLabel.setColor(1, 0, 0, 1);
                    messageLabel.setText("Koneksi Error!");
                });
            }

            @Override
            public void cancelled() {}
        });
    }

    private void proceedToMainMenu(String playerName) {
        game.setPlayerName(playerName);
        game.setScreen(new MainMenuScreen(game));
        dispose();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.2f, 1);
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
