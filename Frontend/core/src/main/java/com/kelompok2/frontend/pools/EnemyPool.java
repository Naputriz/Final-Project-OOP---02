package com.kelompok2.frontend.pools;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.kelompok2.frontend.entities.BaseEnemy;
import com.kelompok2.frontend.entities.DummyEnemy;
import com.kelompok2.frontend.entities.FastEnemy;
import com.kelompok2.frontend.entities.GameCharacter;
import com.kelompok2.frontend.entities.RangedEnemy;
import com.kelompok2.frontend.entities.TankEnemy;
import com.kelompok2.frontend.factories.EnemyType;
import com.kelompok2.frontend.managers.GameManager;

public class EnemyPool {
    // ACTIVE enemies list (Heterogeneous: Dummy, Fast, Tank, etc.)
    private Array<BaseEnemy> activeEnemies;

    private Array<DummyEnemy> dummyPool;
    private Array<FastEnemy> fastPool;
    private Array<TankEnemy> tankPool;
    private Array<RangedEnemy> rangedPool;

    private GameCharacter target;

    public EnemyPool(GameCharacter target, int initialSize) {
        this.target = target;
        this.activeEnemies = new Array<>();

        this.dummyPool = new Array<>();
        this.fastPool = new Array<>();
        this.tankPool = new Array<>();
        this.rangedPool = new Array<>();

        // Pre-allocate some Dummies (most common)
        for (int i = 0; i < initialSize; i++) {
            dummyPool.add(new DummyEnemy(0, 0, target));
        }

        System.out.println("[EnemyPool] Pool initialized");
    }

    public BaseEnemy obtain(EnemyType type, float x, float y) {
        BaseEnemy enemy = null;

        switch (type) {
            case FAST:
                if (fastPool.size > 0)
                    enemy = fastPool.pop();
                else
                    enemy = new FastEnemy(x, y, target);
                break;
            case TANK:
                if (tankPool.size > 0)
                    enemy = tankPool.pop();
                else
                    enemy = new TankEnemy(x, y, target);
                break;
            case RANGED:
                if (rangedPool.size > 0)
                    enemy = rangedPool.pop();
                else
                    enemy = new RangedEnemy(x, y, target);
                break;
            case DUMMY:
            default:
                if (dummyPool.size > 0)
                    enemy = dummyPool.pop();
                else
                    enemy = new DummyEnemy(x, y, target);
                break;
        }

        // Reset and Scale
        enemy.reset(x, y, target);

        // Apply Level Scaling
        int level = GameManager.getInstance().getCurrentLevel();
        enemy.scaleStats(level);

        activeEnemies.add(enemy);
        return enemy;
    }

    public void free(BaseEnemy enemy) {
        if (activeEnemies.removeValue(enemy, true)) {
            if (enemy instanceof FastEnemy)
                fastPool.add((FastEnemy) enemy);
            else if (enemy instanceof TankEnemy)
                tankPool.add((TankEnemy) enemy);
            else if (enemy instanceof RangedEnemy)
                rangedPool.add((RangedEnemy) enemy);
            else if (enemy instanceof DummyEnemy)
                dummyPool.add((DummyEnemy) enemy);
        }
    }

    public void update(float delta) {
        for (int i = activeEnemies.size - 1; i >= 0; i--) {
            BaseEnemy e = activeEnemies.get(i);
            e.update(delta);

            if (e.isDead()) {
                free(e);
            }
        }
    }

    public void render(SpriteBatch batch) {
        for (BaseEnemy e : activeEnemies) {
            if (!e.isDead()) {
                e.render(batch);
            }
        }
    }

    public Array<BaseEnemy> getActiveEnemies() {
        return activeEnemies;
    }

    public void dispose() {
        for (BaseEnemy e : activeEnemies)
            e.dispose();
        for (BaseEnemy e : dummyPool)
            e.dispose();
        for (BaseEnemy e : fastPool)
            e.dispose();
        for (BaseEnemy e : tankPool)
            e.dispose();
        for (BaseEnemy e : rangedPool)
            e.dispose();

        activeEnemies.clear();
        dummyPool.clear();
        fastPool.clear();
        tankPool.clear();
        rangedPool.clear();
    }
}
