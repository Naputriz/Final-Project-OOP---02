package com.kelompok2.frontend.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.kelompok2.frontend.Main;
import com.kelompok2.frontend.managers.GameManager;
import java.util.HashSet;
import java.util.Set;

public class LoginWindow extends Window {
    private final Main game;
    private Skin skin;

    // UI Components
    private TextField usernameField, passwordField, confirmPasswordField;
    private Label messageLabel;
    private TextButton actionButton, switchModeLink, guestLink, cancelButton;

    private boolean isRegisterMode = false;
    private boolean canCancel;
    private static final String BASE_URL = "http://localhost:8080/api";

    private Runnable onSuccessCallback;

    public LoginWindow(String title, Skin skin, Main game, boolean canCancel, Runnable onSuccessCallback) {
        super(title, skin);
        this.skin = skin;
        this.game = game;
        this.canCancel = canCancel;
        this.onSuccessCallback = onSuccessCallback;

        setModal(true);
        setMovable(false);
        setSize(500, 450);
        setPosition(Gdx.graphics.getWidth() / 2f - 250, Gdx.graphics.getHeight() / 2f - 225);

        rebuildUI();
    }

    private void rebuildUI() {
        clearChildren();

        TextButton.TextButtonStyle linkStyle = new TextButton.TextButtonStyle();
        linkStyle.font = skin.getFont("default-font");
        linkStyle.fontColor = Color.GRAY;
        linkStyle.overFontColor = Color.WHITE;
        linkStyle.up = null;

        Label headerLabel = new Label(isRegisterMode ? "DAFTAR AKUN" : "LOGIN", skin);
        headerLabel.setFontScale(1.2f);
        headerLabel.setAlignment(Align.center);

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

        messageLabel = new Label("", skin);
        messageLabel.setColor(Color.SCARLET);
        messageLabel.setAlignment(Align.center);

        actionButton = new TextButton(isRegisterMode ? "DAFTAR" : "MASUK", skin);

        Table contentTable = new Table();
        contentTable.add(headerLabel).padBottom(20).row();
        contentTable.add(usernameField).width(280).height(40).padBottom(10).row();
        contentTable.add(passwordField).width(280).height(40).padBottom(10).row();

        if (isRegisterMode) {
            contentTable.add(confirmPasswordField).width(280).height(40).padBottom(10).row();
        }

        contentTable.add(messageLabel).padBottom(10).row();
        contentTable.add(actionButton).width(200).height(50).padBottom(20).row();

        if (canCancel) {
            cancelButton = new TextButton("Kembali (Batal)", linkStyle);
            cancelButton.setColor(Color.ORANGE);
            contentTable.add(cancelButton).padBottom(10).row();

            cancelButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    remove();
                }
            });
        }

        add(contentTable).expand().top().padTop(30);
        row();

        Table bottomTable = new Table();
        String switchText = isRegisterMode ? "Sudah punya akun? Login" : "Belum punya akun? Daftar";
        switchModeLink = new TextButton(switchText, linkStyle);
        guestLink = new TextButton("Masuk sebagai Tamu", linkStyle);

        bottomTable.add(switchModeLink).left().expandX().padLeft(20);
        bottomTable.add(guestLink).right().expandX().padRight(20);

        add(bottomTable).growX().bottom().padBottom(20);

        actionButton.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                if (isRegisterMode) handleRegister(); else handleLogin();
            }
        });

        switchModeLink.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                isRegisterMode = !isRegisterMode;
                messageLabel.setText("");
                rebuildUI();
            }
        });

        guestLink.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                finishLogin("Guest", null, null); // Guest has no key config
            }
        });
    }

    private void handleLogin() {
        String user = usernameField.getText().trim();
        String pass = passwordField.getText().trim();

        if (user.isEmpty() || pass.isEmpty()) {
            messageLabel.setText("Isi Username & Password!");
            return;
        }

        Net.HttpRequest request = new Net.HttpRequest(Net.HttpMethods.POST);
        request.setUrl(BASE_URL + "/login");
        request.setHeader("Content-Type", "application/json");
        request.setContent("{ \"username\": \"" + user + "\", \"password\": \"" + pass + "\" }");

        Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                if (httpResponse.getStatus().getStatusCode() == 200) {
                    // [UPDATE PENTING] Parse JSON User Lengkap
                    String responseString = httpResponse.getResultAsString();
                    JsonValue root = new JsonReader().parse(responseString);

                    // Ambil Key Config (String JSON di dalam JSON)
                    String keyConfig = root.getString("keyConfig", "");

                    // Ambil Unlocked Characters (Array) jika ada di response login langsung
                    // (Tergantung implementasi backend Anda apakah unlockedChars dikirim saat login)
                    Set<String> unlockedChars = new HashSet<>();
                    if (root.has("unlockedCharacters")) {
                        for (JsonValue val : root.get("unlockedCharacters")) {
                            unlockedChars.add(val.asString());
                        }
                    }

                    // Jika unlockedChars kosong dari login, coba fetch manual (opsional, tapi aman)
                    if (unlockedChars.isEmpty()) {
                        Gdx.app.postRunnable(() -> fetchUserUnlocksAndFinish(user, keyConfig));
                    } else {
                        Gdx.app.postRunnable(() -> finishLogin(user, unlockedChars, keyConfig));
                    }
                } else {
                    showError("Login Gagal!");
                }
            }

            @Override
            public void failed(Throwable t) { showError("Koneksi Error!"); }
            @Override
            public void cancelled() {}
        });
    }

    private void fetchUserUnlocksAndFinish(String user, String keyConfig) {
        Net.HttpRequest request = new Net.HttpRequest(Net.HttpMethods.GET);
        // Pastikan endpoint ini ada di backend Anda (UserController)
        request.setUrl(BASE_URL + "/user/" + user); // Menggunakan endpoint getUser yang return User object lengkap
        request.setHeader("Content-Type", "application/json");

        Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                Set<String> unlocked = new HashSet<>();
                if (httpResponse.getStatus().getStatusCode() == 200) {
                    try {
                        String result = httpResponse.getResultAsString();
                        JsonValue root = new JsonReader().parse(result);

                        // Parse Unlocked Characters
                        if (root.has("unlockedCharacters")) {
                            for (JsonValue val : root.get("unlockedCharacters")) {
                                unlocked.add(val.asString());
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("[LoginWindow] Error parsing user data: " + e.getMessage());
                    }
                }
                Gdx.app.postRunnable(() -> finishLogin(user, unlocked, keyConfig));
            }

            @Override
            public void failed(Throwable t) {
                // If fail, proceed with empty/default
                Gdx.app.postRunnable(() -> finishLogin(user, null, keyConfig));
            }
            @Override public void cancelled() {}
        });
    }

    private void handleRegister() {
        String user = usernameField.getText().trim();
        String pass = passwordField.getText().trim();
        String confirm = confirmPasswordField.getText().trim();

        if (!pass.equals(confirm)) { showError("Password beda!"); return; }

        Net.HttpRequest request = new Net.HttpRequest(Net.HttpMethods.POST);
        request.setUrl(BASE_URL + "/register");
        request.setHeader("Content-Type", "application/json");
        request.setContent("{ \"username\": \"" + user + "\", \"password\": \"" + pass + "\" }");

        Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                if (httpResponse.getStatus().getStatusCode() == 200) {
                    Gdx.app.postRunnable(() -> {
                        isRegisterMode = false;
                        usernameField.setText(user);
                        messageLabel.setText("Akun dibuat! Silakan Login.");
                        messageLabel.setColor(Color.GREEN);
                        rebuildUI();
                    });
                } else { showError("Username dipakai!"); }
            }
            @Override public void failed(Throwable t) { showError("Error!"); }
            @Override public void cancelled() {}
        });
    }

    private void showError(String msg) {
        Gdx.app.postRunnable(() -> {
            messageLabel.setColor(Color.RED);
            messageLabel.setText(msg);
        });
    }

    // [DIUPDATE] Menambahkan parameter keyConfig
    private void finishLogin(String name, Set<String> unlockedChars, String keyConfig) {
        game.setPlayerName(name);
        game.setLoggedIn(true);

        // 1. Sync Unlock Characters
        GameManager.getInstance().loginUser(name, unlockedChars);

        // 2. [BARU] Load Key Bindings
        game.loadKeyBindings(keyConfig);

        if (onSuccessCallback != null) onSuccessCallback.run();
        remove();
    }
}
