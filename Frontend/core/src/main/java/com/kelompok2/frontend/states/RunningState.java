package com.kelompok2.frontend.states;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.kelompok2.frontend.entities.GameCharacter;
import com.kelompok2.frontend.managers.AssetManager;

public class RunningState implements AnimationState {
    private Animation<TextureRegion> runAnimation;
    private String spritesheetPath;
    private int frameColumns;
    private int frameRows;
    private int actualFrameCount; // Actual number of frames to use
    private float frameDuration;

    public RunningState(String spritesheetPath, int frameColumns, int frameRows, int actualFrameCount,
            float frameDuration) {
        this.spritesheetPath = spritesheetPath;
        this.frameColumns = frameColumns;
        this.frameRows = frameRows;
        this.actualFrameCount = actualFrameCount;
        this.frameDuration = frameDuration;

        loadAnimation();
    }

    private void loadAnimation() {
        // Load spritesheet
        Texture spritesheet = AssetManager.getInstance().loadTexture(spritesheetPath);

        // Split spritesheet menjadi individual frames
        TextureRegion[][] tmp = TextureRegion.split(
                spritesheet,
                spritesheet.getWidth() / frameColumns,
                spritesheet.getHeight() / frameRows);

        // Convert 2D array ke 1D array, USE ONLY actualFrameCount frames
        // Ini untuk handle spritesheet yang punya empty cells di akhir
        TextureRegion[] frames = new TextureRegion[actualFrameCount];
        int index = 0;
        for (int i = 0; i < frameRows && index < actualFrameCount; i++) {
            for (int j = 0; j < frameColumns && index < actualFrameCount; j++) {
                frames[index++] = tmp[i][j];
            }
        }

        // Buat animation
        runAnimation = new Animation<>(frameDuration, frames);
        runAnimation.setPlayMode(Animation.PlayMode.LOOP);
    }

    @Override
    public void enter(GameCharacter character) {
        // Called when entering running state
        System.out.println("[RunningState] Entered running state for " + character.getClass().getSimpleName());
    }

    @Override
    public void update(GameCharacter character, float delta) {
        // No special update logic for running state
    }

    @Override
    public TextureRegion getCurrentFrame(float stateTime) {
        return runAnimation.getKeyFrame(stateTime);
    }

    @Override
    public void exit(GameCharacter character) {
        // Called when leaving running state
    }
}
