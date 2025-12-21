package com.kelompok2.frontend.managers;

import com.badlogic.gdx.graphics.Texture;
import java.util.HashMap;

public class AssetManager {
    private static AssetManager instance;

    // Cache untuk menyimpan texture yang sudah di-load
    // Key: path file, Value: Texture object
    private HashMap<String, Texture> textureCache;

    // Flag untuk tracking apakah sudah diinisialisasi
    private boolean initialized;

    // Skill Texture Path
    public static final String HELLFIRE_WARNING = "skills/HPWarn.png";
    public static final String HELLFIRE_PILLAR = "skills/HP.png";

    // Private constructor untuk mencegah instantiasi langsung
    private AssetManager() {
        textureCache = new HashMap<>();
        initialized = false;
        System.out.println("[AssetManager] Instance created (Singleton)");
    }

    public static synchronized AssetManager getInstance() {
        if (instance == null) {
            instance = new AssetManager();
        }
        return instance;
    }

    public Texture loadTexture(String path) {
        // Cek apakah texture sudah ada di cache
        if (textureCache.containsKey(path)) {
            System.out.println("[AssetManager] Returning cached texture: " + path);
            return textureCache.get(path);
        }

        // Jika belum ada, load texture baru
        System.out.println("[AssetManager] Loading new texture: " + path);
        try {
            Texture texture = new Texture(path);
            textureCache.put(path, texture);
            return texture;
        } catch (Exception e) {
            System.err.println("[AssetManager] ERROR: Failed to load texture: " + path);
            System.err.println("[AssetManager] " + e.getMessage());
            return null;
        }
    }


    public Texture getTexture(String path) {
        return textureCache.get(path);
    }

    public void preloadTextures() {
        System.out.println("[AssetManager] Preloading common textures...");

        // Skill Texture
        loadTexture(HELLFIRE_WARNING);
        loadTexture(HELLFIRE_PILLAR);

        initialized = true;
    }

    public boolean isLoaded(String path) {
        return textureCache.containsKey(path);
    }

    public void disposeTexture(String path) {
        if (textureCache.containsKey(path)) {
            Texture texture = textureCache.get(path);
            texture.dispose();
            textureCache.remove(path);
            System.out.println("[AssetManager] Disposed texture: " + path);
        }
    }

    public void dispose() {
        System.out.println("[AssetManager] Disposing all textures (" + textureCache.size() + " items)");
        for (Texture texture : textureCache.values()) {
            texture.dispose();
        }
        textureCache.clear();
        initialized = false;
    }

    public int getCacheSize() {
        return textureCache.size();
    }
}
