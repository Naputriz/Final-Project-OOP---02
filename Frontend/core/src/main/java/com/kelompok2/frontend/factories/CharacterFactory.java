package com.kelompok2.frontend.factories;

import com.kelompok2.frontend.entities.*;
import java.util.Map;
import java.util.HashMap;

public class CharacterFactory {
    @FunctionalInterface
    private interface CharacterCreator {
        GameCharacter create(float x, float y);
    }

    private static final Map<String, CharacterCreator> characterRegistry = new HashMap<>();

    static {
        characterRegistry.put("Ryze", Ryze::new);
        characterRegistry.put("Isolde", Isolde::new);
        characterRegistry.put("Insania", Insania::new);
        characterRegistry.put("Blaze", Blaze::new);
        characterRegistry.put("Whisperwind", Whisperwind::new);
        characterRegistry.put("Aelita", Aelita::new);
        characterRegistry.put("Aegis", Aegis::new);
        characterRegistry.put("Lumi", Lumi::new);
        characterRegistry.put("Alice", Alice::new);
    }

    public static GameCharacter createCharacter(String characterId, float x, float y) {
        if (characterId == null) {
            System.err.println("[CharacterFactory] Character ID is null, defaulting to Isolde.");
            return new Isolde(x, y);
        }

        CharacterCreator creator = characterRegistry.get(characterId);

        if (creator != null) {
            return creator.create(x, y);
        } else {
            System.err.println("[CharacterFactory] Character ID undefined: " + characterId + ", defaulting to Isolde.");
            return new Isolde(x, y);
        }
    }
}
