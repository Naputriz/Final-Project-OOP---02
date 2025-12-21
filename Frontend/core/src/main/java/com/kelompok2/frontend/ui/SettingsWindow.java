package com.kelompok2.frontend.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.graphics.Color;
import com.kelompok2.frontend.Main;
import com.kelompok2.frontend.managers.AudioManager;
import com.kelompok2.frontend.utils.KeyBindings;

public class SettingsWindow extends Window {
    private Main game;
    private boolean isListening = false; // Mode menangkap tombol
    private TextButton activeBtn = null; // Tombol UI yang sedang diedit
    private String activeKeyField = "";  // Nama field di KeyBindings (misal "moveUp")

    public SettingsWindow(Main game, Skin skin) {
        super("Pengaturan", skin);
        this.game = game;
        initialize(skin);
    }

    private void initialize(Skin skin) {
        this.setModal(true);
        this.setMovable(true);
        this.setVisible(false);
        this.setSize(500, 600); // Ukuran lebih tinggi untuk muat list keybind
        this.setPosition(Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() / 2f, Align.center);

        // --- 1. DISPLAY & AUDIO SECTION ---
        Table topTable = new Table();

        CheckBox fullscreenCheckbox = new CheckBox(" Fullscreen", skin);
        fullscreenCheckbox.setChecked(Gdx.graphics.isFullscreen());
        fullscreenCheckbox.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                if (fullscreenCheckbox.isChecked()) Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
                else Gdx.graphics.setWindowedMode(1280, 720);
            }
        });

        Label musicLabel = new Label("Musik", skin);
        Slider musicSlider = new Slider(0f, 1f, 0.01f, false, skin);
        musicSlider.setValue(AudioManager.getInstance().getMusicVolume());
        musicSlider.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                AudioManager.getInstance().setMusicVolume(musicSlider.getValue());
            }
        });

        Label soundLabel = new Label("SFX", skin);
        Slider soundSlider = new Slider(0f, 1f, 0.01f, false, skin);
        soundSlider.setValue(AudioManager.getInstance().getSoundVolume());
        soundSlider.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                AudioManager.getInstance().setSoundVolume(soundSlider.getValue());
            }
        });

        topTable.add(fullscreenCheckbox).colspan(2).left().padBottom(10).row();
        topTable.add(musicLabel).left().padRight(10);
        topTable.add(musicSlider).width(200).row();
        topTable.add(soundLabel).left().padRight(10);
        topTable.add(soundSlider).width(200).row();

        // --- 2. KEYBINDINGS SECTION (SCROLLABLE) ---
        Table keyTable = new Table();
        keyTable.pad(10);

        keyTable.add(new Label("- KONTROL -", skin)).colspan(2).padBottom(10).row();

        // Daftar tombol yang bisa diubah
        addKeyRow(keyTable, skin, "Maju", "moveUp");
        addKeyRow(keyTable, skin, "Mundur", "moveDown");
        addKeyRow(keyTable, skin, "Kiri", "moveLeft");
        addKeyRow(keyTable, skin, "Kanan", "moveRight");
        addKeyRow(keyTable, skin, "Innate Skill", "innateSkill");
        addKeyRow(keyTable, skin, "Loot Skill", "secondarySkill");
        addKeyRow(keyTable, skin, "Ultimate", "ultimateSkill");

        ScrollPane scrollPane = new ScrollPane(keyTable, skin);
        scrollPane.setFadeScrollBars(false);

        // --- 3. BUTTONS BAWAH ---
        TextButton resetButton = new TextButton("Reset Default", skin);
        resetButton.setColor(Color.ORANGE);
        resetButton.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                game.getKeys().resetToDefault();
                // Refresh UI dengan menutup dan membuka (cara paling mudah)
                setVisible(false);
                if(!game.getPlayerName().equals("Guest")) game.saveKeyBindingsToServer();
            }
        });

        TextButton closeButton = new TextButton("Simpan & Tutup", skin);
        closeButton.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                // Simpan otomatis ke server saat tutup (jika bukan guest)
                if(!game.getPlayerName().equals("Guest")) game.saveKeyBindingsToServer();
                setVisible(false);
            }
        });

        // --- SUSUN SEMUA KE WINDOW ---
        this.add(topTable).pad(10).row();
        this.add(scrollPane).width(400).height(250).pad(10).row(); // Area scroll keybind

        Table btnTable = new Table();
        btnTable.add(resetButton).width(150).padRight(10);
        btnTable.add(closeButton).width(150);
        this.add(btnTable).pad(10);
    }

    private void addKeyRow(Table table, Skin skin, String label, String keyField) {
        table.add(new Label(label, skin)).left().padRight(20);

        int keyCode = getKeyCode(keyField);
        String keyName = Input.Keys.toString(keyCode);

        TextButton btn = new TextButton(keyName, skin);
        btn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                if (!isListening) {
                    startListening(btn, keyField);
                }
            }
        });

        table.add(btn).width(120).height(30).padBottom(5).row();
    }

    private void startListening(TextButton btn, String keyField) {
        isListening = true;
        activeBtn = btn;
        activeKeyField = keyField;

        btn.setText("Press Key...");
        btn.setColor(Color.YELLOW);

        // Tambahkan listener sementara ke Stage untuk menangkap keyboard
        getStage().setKeyboardFocus(this);
        getStage().addCaptureListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (isListening) {
                    // Update data di Main
                    setKeyCode(activeKeyField, keycode);

                    // Update tampilan tombol
                    activeBtn.setText(Input.Keys.toString(keycode));
                    activeBtn.setColor(Color.WHITE);

                    // Selesai
                    isListening = false;
                    getStage().removeCaptureListener(this); // Hapus listener diri sendiri
                    return true; // Consume event (agar tidak tembus ke game)
                }
                return false;
            }
        });
    }

    // Helper: Reflection manual (Get Value)
    private int getKeyCode(String field) {
        KeyBindings k = game.getKeys();
        switch(field) {
            case "moveUp": return k.moveUp;
            case "moveDown": return k.moveDown;
            case "moveLeft": return k.moveLeft;
            case "moveRight": return k.moveRight;
            case "innateSkill": return k.innateSkill;
            case "secondarySkill": return k.secondarySkill;
            case "ultimateSkill": return k.ultimateSkill;
            default: return 0;
        }
    }

    // Helper: Reflection manual (Set Value)
    private void setKeyCode(String field, int code) {
        KeyBindings k = game.getKeys();
        switch(field) {
            case "moveUp": k.moveUp = code; break;
            case "moveDown": k.moveDown = code; break;
            case "moveLeft": k.moveLeft = code; break;
            case "moveRight": k.moveRight = code; break;
            case "innateSkill": k.innateSkill = code; break;
            case "secondarySkill": k.secondarySkill = code; break;
            case "ultimateSkill": k.ultimateSkill = code; break;
        }
    }

    public void show(Stage stage) {
        this.setPosition(stage.getWidth() / 2f, stage.getHeight() / 2f, Align.center);
        this.setVisible(true);
        if (!this.hasParent()) {
            stage.addActor(this);
        }
    }
}
