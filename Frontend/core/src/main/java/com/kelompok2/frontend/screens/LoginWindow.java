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
    private boolean canCancel; // Menentukan apakah tombol Kembali muncul
    private static final String BASE_URL = "http://localhost:8080/api";

    // Callback untuk memberitahu MainMenuScreen kalau login sukses
    private Runnable onSuccessCallback;

    public LoginWindow(String title, Skin skin, Main game, boolean canCancel, Runnable onSuccessCallback) {
        super(title, skin);
        this.skin = skin;
        this.game = game;
        this.canCancel = canCancel;
        this.onSuccessCallback = onSuccessCallback;

        setModal(true); // Agar background tidak bisa diklik
        setMovable(false); // Agar tidak bisa digeser-geser
        setSize(500, 450); // Ukuran Pop-up
        setPosition(Gdx.graphics.getWidth() / 2f - 250, Gdx.graphics.getHeight() / 2f - 225); // Tengah Layar

        rebuildUI();
    }

    private void rebuildUI() {
        clearChildren();

        // Style untuk Link Text
        TextButton.TextButtonStyle linkStyle = new TextButton.TextButtonStyle();
        linkStyle.font = skin.getFont("default-font");
        linkStyle.fontColor = Color.GRAY;
        linkStyle.overFontColor = Color.WHITE;
        linkStyle.up = null;

        // Title diganti Label di dalam window agar lebih fleksibel
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

        // --- Layout Table ---
        Table contentTable = new Table();
        contentTable.add(headerLabel).padBottom(20).row();
        contentTable.add(usernameField).width(280).height(40).padBottom(10).row();
        contentTable.add(passwordField).width(280).height(40).padBottom(10).row();

        if (isRegisterMode) {
            contentTable.add(confirmPasswordField).width(280).height(40).padBottom(10).row();
        }

        contentTable.add(messageLabel).padBottom(10).row();
        contentTable.add(actionButton).width(200).height(50).padBottom(20).row();

        // Tombol Kembali (Hanya muncul jika boleh cancel)
        if (canCancel) {
            cancelButton = new TextButton("Kembali (Batal)", linkStyle);
            cancelButton.setColor(Color.ORANGE);
            contentTable.add(cancelButton).padBottom(10).row();

            cancelButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    remove(); // Tutup window tanpa ubah login
                }
            });
        }

        add(contentTable).expand().top().padTop(30);
        row();

        // --- Bottom Navigation ---
        Table bottomTable = new Table();
        // bottomTable.setDebug(true); // Hapus komentar ini jika ingin melihat garis
        // bantu

        String switchText = isRegisterMode ? "Sudah punya akun? Login" : "Belum punya akun? Daftar";
        switchModeLink = new TextButton(switchText, linkStyle);
        guestLink = new TextButton("Masuk sebagai Tamu", linkStyle);

        // Logic Layout:
        // 1. Tambahkan Link Kiri -> Align Left -> expandX() agar mendorong ke kiri
        // 2. Tambahkan Link Kanan -> Align Right -> expandX() agar mendorong ke kanan
        // 3. Beri padLeft dan padRight yang sama agar jarak ke dinding window seimbang

        bottomTable.add(switchModeLink).left().expandX().padLeft(20);
        bottomTable.add(guestLink).right().expandX().padRight(20);

        // Masukkan bottomTable ke Window
        // growX() agar tabel bawah melebar memenuhi lebar window
        add(bottomTable).growX().bottom().padBottom(20);

        // --- Listeners ---
        actionButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (isRegisterMode)
                    handleRegister();
                else
                    handleLogin();
            }
        });

        switchModeLink.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                isRegisterMode = !isRegisterMode;
                messageLabel.setText("");
                rebuildUI();
            }
        });

        guestLink.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                finishLogin("Guest");
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
                    // Login Success -> Now Fetch Unlocks
                    Gdx.app.postRunnable(() -> fetchUserUnlocks(user));
                } else {
                    showError("Login Gagal!");
                }
            }

            @Override
            public void failed(Throwable t) {
                showError("Koneksi Error!");
            }

            @Override
            public void cancelled() {
            }
        });
    }

    private void fetchUserUnlocks(String user) {
        Net.HttpRequest request = new Net.HttpRequest(Net.HttpMethods.GET);
        // Assuming endpoint exists. If not, this might 404, which we handle.
        request.setUrl(BASE_URL + "/user/unlocks?username=" + user);
        request.setHeader("Content-Type", "application/json");

        Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                Set<String> unlocked = new HashSet<>();

                if (httpResponse.getStatus().getStatusCode() == 200) {
                    String result = httpResponse.getResultAsString();
                    if (result != null && !result.isEmpty()) {
                        try {
                            JsonValue root = new JsonReader().parse(result);
                            // Assuming response is JSON Array of strings: ["Ryze", "Insania", ...]
                            for (JsonValue val : root) {
                                unlocked.add(val.asString());
                            }
                        } catch (Exception e) {
                            System.err.println("[LoginWindow] Error parsing unlocks: " + e.getMessage());
                        }
                    }
                }

                // Proceed to finish login (even if fetch fails, unlocked will be empty/null,
                // triggering local load)
                Gdx.app.postRunnable(() -> finishLogin(user, unlocked));
            }

            @Override
            public void failed(Throwable t) {
                // Ignore error, proceed with local load
                Gdx.app.postRunnable(() -> finishLogin(user, null));
            }

            @Override
            public void cancelled() {
            }
        });
    }

    private void handleRegister() {
        // ... (Logika Register sama seperti sebelumnya, dipersingkat) ...
        String user = usernameField.getText().trim();
        String pass = passwordField.getText().trim();
        String confirm = confirmPasswordField.getText().trim();

        if (!pass.equals(confirm)) {
            showError("Password beda!");
            return;
        }

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
                } else {
                    showError("Username dipakai!");
                }
            }

            @Override
            public void failed(Throwable t) {
                showError("Error!");
            }

            @Override
            public void cancelled() {
            }
        });
    }

    private void showError(String msg) {
        Gdx.app.postRunnable(() -> {
            messageLabel.setColor(Color.RED);
            messageLabel.setText(msg);
        });
    }

    private void finishLogin(String name) {
        finishLogin(name, null);
    }

    private void finishLogin(String name, Set<String> unlockedChars) {
        game.setPlayerName(name);
        game.setLoggedIn(true);

        // Push to GameManager to handle sync and persistence
        GameManager.getInstance().loginUser(name, unlockedChars);

        if (onSuccessCallback != null)
            onSuccessCallback.run();
        remove(); // Tutup Pop-up
    }
}
