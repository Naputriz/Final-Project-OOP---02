package com.kelompok2.frontend.factories;

import com.kelompok2.frontend.entities.*;

public class CharacterFactory {

    public static GameCharacter createCharacter(String className, float x, float y) {
        switch (className) {
            case "Ryze":
                return new Ryze(x, y);
            case "Isolde":
                return new Isolde(x, y);
            case "Insania":
                return new Insania(x, y);
            case "Blaze":
                return new Blaze(x, y);
            case "Whisperwind":
                return new Whisperwind(x, y);
            case "Aelita":
                return new Aelita(x, y);
            case "Aegis":
                return new Aegis(x, y);
            case "Lumi":
                return new Lumi(x, y);
            default:
                // Default fallback
                System.out.println("[CharacterFactory] Unknown character: " + className + ". Defaulting to Ryze.");
                return new Ryze(x, y);
        }
    }
}
