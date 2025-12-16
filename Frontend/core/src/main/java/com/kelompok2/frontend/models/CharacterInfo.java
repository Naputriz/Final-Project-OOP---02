package com.kelompok2.frontend.models;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.kelompok2.frontend.managers.AssetManager;

public class CharacterInfo {
    public String name;
    public String title;
    public float hp, atk, arts, def, speed;
    public String skillName;
    public String skillDescription;
    public String texturePath;
    public Texture spritesheet;
    public Animation<TextureRegion> animation;

    public CharacterInfo(String name, String title, float hp, float atk, float arts, float def, float speed,
                  String skillName, String skillDescription, String texturePath,
                  Texture spritesheet, int cols, int rows, int actualFrameCount, float frameDuration) {
        this.name = name;
        this.title = title;
        this.hp = hp;
        this.atk = atk;
        this.arts = arts;
        this.def = def;
        this.speed = speed;
        this.skillName = skillName;
        this.skillDescription = skillDescription;
        this.texturePath = texturePath;
        this.spritesheet = spritesheet;

        // Create animation if spritesheet provided
        if (spritesheet != null && cols > 1 && rows > 1) {
            TextureRegion[][] tmp = TextureRegion.split(
                spritesheet,
                spritesheet.getWidth() / cols,
                spritesheet.getHeight() / rows);

            // Use only actualFrameCount frames to avoid empty cells
            TextureRegion[] frames = new TextureRegion[actualFrameCount];
            int index = 0;
            for (int i = 0; i < rows && index < actualFrameCount; i++) {
                for (int j = 0; j < cols && index < actualFrameCount; j++) {
                    frames[index++] = tmp[i][j];
                }
            }

            animation = new Animation<>(frameDuration, frames);
            animation.setPlayMode(Animation.PlayMode.LOOP);
        }
    }

    public Texture getPortraitTexture() {
        if (spritesheet != null) {
            return spritesheet;
        }
        return AssetManager.getInstance().loadTexture(texturePath);
    }
}
