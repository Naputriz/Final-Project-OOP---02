package com.kelompok2.frontend.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kelompok2.frontend.Main;
import com.kelompok2.frontend.managers.AssetManager;
import com.kelompok2.frontend.managers.AudioManager;

public class NameInputScreen extends ScreenAdapter {
    private final Main game;
    private Stage stage;
    private Skin skin;
    private TextField nameField;
    private Label errorLabel;

    public NameInputScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        skin = new Skin(Gdx.files.internal("uiskin.json"));

        // Putar musik jika belum berputar
        AudioManager.getInstance().playMusic(
            "Audio/helmet_-_tales_of_the_helmets_knight_-_01_start_screen_theme_-_prelude (Start or main menu).wav",
            true);

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        // Judul
        Label titleLabel = new Label("MASUKKAN NAMA", skin);
        titleLabel.setFontScale(1.5f);
        titleLabel.setAlignment(Align.center);

        // Input Field
        nameField = new TextField("", skin);
        nameField.setMessageText("Username..."); // Placeholder text
        nameField.setAlignment(Align.center);

        // Tombol Lanjut
        TextButton confirmButton = new TextButton("LANJUT", skin);

        // Label Error (Hidden by default)
        errorLabel = new Label("Nama tidak boleh kosong!", skin);
        errorLabel.setColor(1, 0, 0, 1); // Merah
        errorLabel.setVisible(false);

        // Layout Table
        table.add(titleLabel).padBottom(30).row();
        table.add(nameField).width(300).height(50).padBottom(10).row();
        table.add(errorLabel).padBottom(10).row();
        table.add(confirmButton).width(200).height(50).row();

        // Logic Tombol
        confirmButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                submitName();
            }
        });

        // Opsional: Tekan Enter untuk submit
        nameField.setTextFieldListener((textField, c) -> {
            if (c == '\r' || c == '\n') {
                submitName();
            }
        });
    }

    private void submitName() {
        String inputName = nameField.getText().trim();

        if (inputName.isEmpty()) {
            errorLabel.setVisible(true);
        } else {
            // Simpan nama ke class Main
            game.setPlayerName(inputName);

            // Pindah ke Main Menu
            game.setScreen(new MainMenuScreen(game));
            dispose();
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1); // Warna background gelap
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
