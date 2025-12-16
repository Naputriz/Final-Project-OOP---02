package com.kelompok2.frontend.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import java.util.HashMap;

public class AudioManager {
    private static AudioManager instance;

    // Cache untuk Music dan Sound objects
    private HashMap<String, Music> musicCache;
    private HashMap<String, Sound> soundCache;

    // Current playing music
    private Music currentMusic;
    private String currentMusicPath;

    // Volume settings (0.0 - 1.0)
    private float musicVolume = 0.7f;
    private float soundVolume = 0.7f;

    // Private constructor untuk Singleton
    private AudioManager() {
        musicCache = new HashMap<>();
        soundCache = new HashMap<>();
        System.out.println("[AudioManager] Instance created (Singleton)");
    }

    public static synchronized AudioManager getInstance() {
        if (instance == null) {
            instance = new AudioManager();
        }
        return instance;
    }

    public void playMusic(String path, boolean loop) {
        // Jika music yang sama sedang playing, skip
        if (currentMusicPath != null && currentMusicPath.equals(path) && currentMusic != null
                && currentMusic.isPlaying()) {
            return;
        }

        // Stop current music jika ada
        stopMusic();

        // Load atau ambil dari cache
        Music music = musicCache.get(path);
        if (music == null) {
            try {
                music = Gdx.audio.newMusic(Gdx.files.internal(path));
                musicCache.put(path, music);
                System.out.println("[AudioManager] Loaded new music: " + path);
            } catch (Exception e) {
                System.err.println("[AudioManager] ERROR: Failed to load music: " + path);
                System.err.println("[AudioManager] " + e.getMessage());
                return;
            }
        }

        // Play music
        currentMusic = music;
        currentMusicPath = path;
        currentMusic.setLooping(loop);
        currentMusic.setVolume(musicVolume);
        currentMusic.play();

        System.out.println("[AudioManager] Playing music: " + path + " (loop=" + loop + ")");
    }

    public void stopMusic() {
        if (currentMusic != null && currentMusic.isPlaying()) {
            currentMusic.stop();
            System.out.println("[AudioManager] Stopped music: " + currentMusicPath);
        }
        currentMusic = null;
        currentMusicPath = null;
    }

    public void pauseMusic() {
        if (currentMusic != null && currentMusic.isPlaying()) {
            currentMusic.pause();
            System.out.println("[AudioManager] Paused music");
        }
    }

    public void resumeMusic() {
        if (currentMusic != null && !currentMusic.isPlaying()) {
            currentMusic.play();
            System.out.println("[AudioManager] Resumed music");
        }
    }

    public void playSound(String path) {
        playSound(path, soundVolume);
    }

    public void playSound(String path, float volume) {
        // Load atau ambil dari cache
        Sound sound = soundCache.get(path);
        if (sound == null) {
            try {
                sound = Gdx.audio.newSound(Gdx.files.internal(path));
                soundCache.put(path, sound);
                System.out.println("[AudioManager] Loaded new sound: " + path);
            } catch (Exception e) {
                System.err.println("[AudioManager] ERROR: Failed to load sound: " + path);
                System.err.println("[AudioManager] " + e.getMessage());
                return;
            }
        }

        // Play sound
        sound.play(volume);
    }

    public void setMusicVolume(float volume) {
        this.musicVolume = Math.max(0f, Math.min(1f, volume));
        if (currentMusic != null) {
            currentMusic.setVolume(this.musicVolume);
        }
        System.out.println("[AudioManager] Music volume set to: " + this.musicVolume);
    }

    public void setSoundVolume(float volume) {
        this.soundVolume = Math.max(0f, Math.min(1f, volume));
        System.out.println("[AudioManager] Sound volume set to: " + this.soundVolume);
    }

    public float getMusicVolume() {
        return musicVolume;
    }

    public float getSoundVolume() {
        return soundVolume;
    }

    public void dispose() {
        System.out.println("[AudioManager] Disposing all audio resources...");

        // Stop current music
        stopMusic();

        // Dispose all music
        for (Music music : musicCache.values()) {
            music.dispose();
        }
        musicCache.clear();

        // Dispose all sounds
        for (Sound sound : soundCache.values()) {
            sound.dispose();
        }
        soundCache.clear();

        System.out.println("[AudioManager] All audio resources disposed");
    }
}
