package com.kelompok2.frontend.skills;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.kelompok2.frontend.entities.GameCharacter;
import com.kelompok2.frontend.entities.MeleeAttack;
import com.kelompok2.frontend.entities.Projectile;
import com.kelompok2.frontend.managers.AssetManager;

public class HurricaneBindSkill extends BaseSkill {

    private float damageMultiplier = 2.0f; //Arts * 2.0
    private Texture skillTexture;
    private Array<Projectile> activeProjectiles;

    public HurricaneBindSkill() {
        super("Hurricane Bind", "Shoots a wind ball that knocks back and stuns.", 10f); // 10s Cooldown

        this.skillTexture = AssetManager.getInstance().loadTexture("skills/HurricaneBind.png");
        this.activeProjectiles = new Array<>();
    }

    @Override
    protected boolean executeSkill(GameCharacter user, Vector2 targetPos,
                                   Array<Projectile> projectiles,
                                   Array<MeleeAttack> meleeAttacks) {

        if (projectiles == null) {
            System.err.println("[HurricaneBind] ERROR: Projectile list is null! Check Whisperwind.java");
            return false;
        }

        float playerCenterX = user.getPosition().x + user.getVisualWidth() / 2;
        float playerCenterY = user.getPosition().y + user.getVisualHeight() / 2;

        Vector2 direction = new Vector2(
            targetPos.x - playerCenterX,
            targetPos.y - playerCenterY).nor();

        float damage = user.getArts() * damageMultiplier;

        if (projectiles != null) {
            Projectile windBall = null;

            for (Projectile p : projectiles) {
                if (!p.active) {
                    windBall = p;
                    break;
                }
            }

            if (windBall == null) {
                windBall = new Projectile(playerCenterX, playerCenterY, direction, damage);
                projectiles.add(windBall);
            } else {
                windBall.reset(playerCenterX, playerCenterY, direction, damage);
            }

            windBall.setTexture(skillTexture);

            // Bigger hitbox
            windBall.getBounds().setSize(32f, 32f);
            windBall.setVisualSize(40f);

            windBall.setPiercing(true);

            activeProjectiles.add(windBall);

            System.out.println("[HurricaneBind] Activated! Damage: " + damage);
        }
        return true;
    }

    @Override
    public void render(SpriteBatch batch) {
        for (int i = activeProjectiles.size - 1; i >= 0; i--) {
            if (!activeProjectiles.get(i).active) {
                activeProjectiles.removeIndex(i);
            }
        }
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        for (int i = activeProjectiles.size - 1; i >= 0; i--) {
            if (!activeProjectiles.get(i).active) {
                activeProjectiles.removeIndex(i);
            }
        }
    }

    public Array<Projectile> getActiveProjectiles() {
        return activeProjectiles;
    }

    @Override
    public Skill copy() {
        return new HurricaneBindSkill();
    }
}
