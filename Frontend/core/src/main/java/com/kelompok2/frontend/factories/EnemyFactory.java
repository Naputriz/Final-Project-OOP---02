package com.kelompok2.frontend.factories;

import com.badlogic.gdx.math.MathUtils;
import com.kelompok2.frontend.entities.DummyEnemy;
import com.kelompok2.frontend.entities.GameCharacter;

public class EnemyFactory {

    public static DummyEnemy createEnemy(EnemyType type, float x, float y, GameCharacter target) {
        DummyEnemy enemy;

        switch (type) {
            case DUMMY:
                // Basic enemy dengan stats standard
                enemy = new DummyEnemy(x, y, target);
                System.out.println("[EnemyFactory] Created DUMMY enemy at (" + x + ", " + y + ")");
                break;

            case FAST:
                // Future: Musuh cepat dengan HP rendah
                // Speed lebih tinggi, HP lebih rendah
                enemy = new DummyEnemy(x, y, target);
                // TODO: Implement FastEnemy class
                System.out.println("[EnemyFactory] FAST enemy not yet implemented, using DUMMY");
                break;

            case TANK:
                // Future: Musuh lambat dengan HP tinggi
                // Speed lebih rendah, HP lebih tinggi
                enemy = new DummyEnemy(x, y, target);
                // TODO: Implement TankEnemy class
                System.out.println("[EnemyFactory] TANK enemy not yet implemented, using DUMMY");
                break;

            case RANGED:
                // Future: Musuh yang bisa menembak
                enemy = new DummyEnemy(x, y, target);
                // TODO: Implement RangedEnemy class
                System.out.println("[EnemyFactory] RANGED enemy not yet implemented, using DUMMY");
                break;

            default:
                enemy = new DummyEnemy(x, y, target);
                System.out.println("[EnemyFactory] Unknown type, defaulting to DUMMY");
                break;
        }

        return enemy;
    }

    public static EnemyType getRandomEnemyType(int currentLevel) {
        // Level 1-5: Hanya DUMMY
        if (currentLevel <= 5) {
            return EnemyType.DUMMY;
        }
        // Level 6-10: DUMMY dan FAST (70% DUMMY, 30% FAST)
        else if (currentLevel <= 10) {
            float roll = MathUtils.random();
            if (roll < 0.7f) {
                return EnemyType.DUMMY;
            } else {
                return EnemyType.FAST; // Future implementation
            }
        }
        // Level 11+: Semua tipe (50% DUMMY, 25% FAST, 15% TANK, 10% RANGED)
        else {
            float roll = MathUtils.random();
            if (roll < 0.5f) {
                return EnemyType.DUMMY;
            } else if (roll < 0.75f) {
                return EnemyType.FAST; // Future
            } else if (roll < 0.9f) {
                return EnemyType.TANK; // Future
            } else {
                return EnemyType.RANGED; // Future
            }
        }
    }

    public static EnemyType getEnemyTypeByName(String typeName) {
        try {
            return EnemyType.valueOf(typeName.toUpperCase());
        } catch (IllegalArgumentException e) {
            System.out.println("[EnemyFactory] Invalid enemy type: " + typeName + ", defaulting to DUMMY");
            return EnemyType.DUMMY;
        }
    }
}
