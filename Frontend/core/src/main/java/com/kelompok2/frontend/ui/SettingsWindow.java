package com.kelompok2.frontend.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.kelompok2.frontend.managers.AudioManager;

public class SettingsWindow extends Window {

    public SettingsWindow(Skin skin) {
        super("Pengaturan", skin);
        initialize(skin);
    }

    private void initialize(Skin skin) {
        this.setModal(true);
        this.setMovable(true);
        this.setVisible(false);
        this.setSize(450, 250);
        // Center position will be handled by the screen resize/show,
        // but we can set a default center here relative to current Gdx graphics
        this.setPosition(Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() / 2f, Align.center);

        // Music Controls
        final Label musicLabel = new Label("Musik: " + (int) (AudioManager.getInstance().getMusicVolume() * 100) + "%",
                skin);
        final Slider musicSlider = new Slider(0f, 1f, 0.01f, false, skin);
        musicSlider.setValue(AudioManager.getInstance().getMusicVolume());

        // SFX Controls
        final Label soundLabel = new Label("SFX: " + (int) (AudioManager.getInstance().getSoundVolume() * 100) + "%",
                skin);
        final Slider soundSlider = new Slider(0f, 1f, 0.01f, false, skin);
        soundSlider.setValue(AudioManager.getInstance().getSoundVolume());

        // Close Button
        TextButton closeButton = new TextButton("Tutup", skin);

        // Listeners
        musicSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float vol = musicSlider.getValue();
                AudioManager.getInstance().setMusicVolume(vol);
                musicLabel.setText("Musik: " + (int) (vol * 100) + "%");
            }
        });

        soundSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float vol = soundSlider.getValue();
                AudioManager.getInstance().setSoundVolume(vol);
                soundLabel.setText("SFX: " + (int) (vol * 100) + "%");
            }
        });

        closeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                setVisible(false);
            }
        });

        // Layout
        this.add(musicLabel).width(120).pad(10);
        this.add(musicSlider).width(250).pad(10).row();
        this.add(soundLabel).width(120).pad(10);
        this.add(soundSlider).width(250).pad(10).row();
        this.add(closeButton).colspan(2).width(100).padTop(20);
    }
}
