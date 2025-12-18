package com.kelompok2.frontend.skills;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.kelompok2.frontend.entities.GameCharacter;
import com.kelompok2.frontend.entities.MeleeAttack;
import com.kelompok2.frontend.entities.Projectile;

public class VerdantDomainSkill extends BaseSkill {
    private static final float HP_COST_PERCENT = 0.25f; // 25% HP cost
    private static final float TOTAL_HEAL_PERCENT = 0.50f; // 50% total healing
    private static final float ZONE_DURATION = 5.0f; // 5 seconds
    private static final float ZONE_RADIUS = 150f; // pixels
    private static final float ATK_BUFF = 1.25f; // +25% ATK
    private static final float ARTS_BUFF = 1.25f; // +25% Arts

    private boolean zoneActive = false;
    private Vector2 zonePosition = new Vector2();
    private float zoneTimer = 0f;
    private float maxHpAtActivation = 0f; // Store max HP when skill activated

    public VerdantDomainSkill() {
        super("Verdant Domain",
                "Consume 25% HP to create a zone that heals 50% HP over 5s and boosts ATK/Arts +25%",
                15.0f); // 15 second cooldown
    }

    @Override
    protected void executeSkill(GameCharacter user, Vector2 targetPos,
            Array<Projectile> projectiles, Array<MeleeAttack> meleeAttacks) {
        // Calculate and apply HP cost
        float hpCost = user.getMaxHp() * HP_COST_PERCENT;
        user.takeDamage(hpCost);

        // Store max HP for healing calculation
        maxHpAtActivation = user.getMaxHp();

        // Activate zone at target position (player's center)
        zoneActive = true;
        zonePosition.set(targetPos);
        zoneTimer = 0f;

        System.out.println("[Aelita] Verdant Domain activated! HP cost: " + hpCost +
                ", Position: (" + zonePosition.x + ", " + zonePosition.y + ")");
    }

    @Override
    public void update(float delta) {
        super.update(delta); // Update cooldown timer

        // Update zone timer
        if (zoneActive) {
            zoneTimer += delta;
            if (zoneTimer >= ZONE_DURATION) {
                zoneActive = false;
                zoneTimer = 0f;
                System.out.println("[VerdantDomain] Zone expired");
            }
        }
    }

    // Getters for zone state
    public boolean isZoneActive() {
        return zoneActive;
    }

    public Vector2 getZonePosition() {
        return zonePosition;
    }

    public float getZoneRadius() {
        return ZONE_RADIUS;
    }

    // Get healing per second (10% of max HP per second)
    public float getHealPerSecond() {
        return maxHpAtActivation * (TOTAL_HEAL_PERCENT / ZONE_DURATION);
    }

    public float getAtkBuff() {
        return ATK_BUFF;
    }

    public float getArtsBuff() {
        return ARTS_BUFF;
    }

    public float getZoneTimer() {
        return zoneTimer;
    }

    public float getZoneDuration() {
        return ZONE_DURATION;
    }

    /**
     * Update with player reference for healing (hybrid approach)
     */
    public void update(float delta, GameCharacter player) {
        // Update cooldown
        super.update(delta);

        // Update zone
        if (zoneActive) {
            zoneTimer += delta;
            if (zoneTimer >= ZONE_DURATION) {
                zoneActive = false;
                zoneTimer = 0f;
                // Clear buffs when zone expires
                if (player instanceof com.kelompok2.frontend.entities.Aelita) {
                    ((com.kelompok2.frontend.entities.Aelita) player).clearVerdantBuff();
                }
                System.out.println("[VerdantDomain] Zone expired");
            } else {
                // Apply healing to player if in zone
                float playerCenterX = player.getPosition().x + player.getVisualWidth() / 2;
                float playerCenterY = player.getPosition().y + player.getVisualHeight() / 2;
                float distance = (float) Math.sqrt(
                        Math.pow(playerCenterX - zonePosition.x, 2) +
                                Math.pow(playerCenterY - zonePosition.y, 2));

                if (distance <= ZONE_RADIUS) {
                    // Heal 10% max HP per second
                    float healAmount = getHealPerSecond() * delta;
                    player.heal(healAmount);

                    // Apply ATK/Arts buffs (only for Aelita)
                    if (player instanceof com.kelompok2.frontend.entities.Aelita) {
                        ((com.kelompok2.frontend.entities.Aelita) player).applyVerdantBuff(ATK_BUFF, ARTS_BUFF);
                    }

                    System.out.println("[VerdantDomain] Healing player: +" + healAmount + " HP - Buffs active!");
                } else {
                    // Player left zone, clear buffs
                    if (player instanceof com.kelompok2.frontend.entities.Aelita) {
                        ((com.kelompok2.frontend.entities.Aelita) player).clearVerdantBuff();
                    }
                }
            }
        }
    }

    @Override
    public Skill copy() {
        return new VerdantDomainSkill();
    }
}
