package com.kelompok2.frontend.factories;

import com.badlogic.gdx.math.MathUtils;
import com.kelompok2.frontend.entities.DummyEnemy;
import com.kelompok2.frontend.entities.GameCharacter;

public class EnemyFactory {

    public static com.kelompok2.frontend.entities.BaseEnemy createEnemy(EnemyType type, float x, float y,
            GameCharacter target) {
        com.kelompok2.frontend.entities.BaseEnemy enemy;

        switch (type) {
            case DUMMY:
                enemy = new DummyEnemy(x, y, target);
                break;

            case FAST:
                enemy = new com.kelompok2.frontend.entities.FastEnemy(x, y, target);
                break;

            case TANK:
                enemy = new com.kelompok2.frontend.entities.TankEnemy(x, y, target);
                break;

            case RANGED:
                enemy = new com.kelompok2.frontend.entities.RangedEnemy(x, y, target);
                break;

            default:
                enemy = new DummyEnemy(x, y, target);
                System.out.println("[EnemyFactory] Unknown type, defaulting to DUMMY");
                break;
        }

        return enemy;
    }

    public static EnemyType getRandomEnemyType(int currentLevel) {
        // Level 1-2: Hanya DUMMY
        if (currentLevel <= 2) {
            return EnemyType.DUMMY;
        }
        // Level 3-5: DUMMY dan FAST (70% DUMMY, 30% FAST)
        else if (currentLevel <= 5) {
            float roll = MathUtils.random();
            if (roll < 0.7f) {
                return EnemyType.DUMMY;
            } else {
                return EnemyType.FAST;
            }
        }
        // Level 6+: Semua tipe (40% DUMMY, 30% FAST, 20% TANK, 10% RANGED)
        else {
            float roll = MathUtils.random();
            if (roll < 0.4f) {
                return EnemyType.DUMMY;
            } else if (roll < 0.7f) {
                return EnemyType.FAST;
            } else if (roll < 0.9f) {
                return EnemyType.TANK;
            } else {
                return EnemyType.RANGED;
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
