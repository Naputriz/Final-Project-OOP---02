package com.kelompok2.frontend.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.kelompok2.frontend.strategies.AttackStrategy;
import com.kelompok2.frontend.managers.GameManager;
import com.kelompok2.frontend.skills.Skill;
import com.kelompok2.frontend.managers.GameEventManager;
import com.kelompok2.frontend.events.HealthChangedEvent;
import com.kelompok2.frontend.events.XpChangedEvent;
import com.kelompok2.frontend.events.CooldownChangedEvent;

public abstract class GameCharacter {
    protected Vector2 position;
    protected float speed;
    protected float hp;
    protected float maxHp;
    protected float atk;
    protected float arts;
    protected float def;

    // Texture
    protected boolean isFacingRight;
    protected Texture texture; // Sprite karakter
    protected Rectangle bounds; // Hitbox untuk collision
    protected float renderWidth = -1; // -1 artinya belum diset (default ikut bounds)
    protected float renderHeight = -1;

    // Player status effects (for boss skills)
    protected boolean isInsane = false;
    protected float insanityTimer = 0f;
    protected boolean isFrozen = false;
    protected float freezeTimer = 0f;
    protected boolean isSlowed = false;
    protected float slowTimer = 0f;
    protected boolean isStunned = false; // Stun status - different from freeze
    protected float stunTimer = 0f;
    protected boolean isMarked = false; // Lumi's mark
    protected float markTimer = 0f;
    protected boolean isConfused = false; // Kei's hallucination effect
    protected float confusionTimer = 0f;
    // Balance Fix: Per-attacker hit cooldown (prevents same enemy from
    // multi-hitting)
    protected java.util.HashMap<GameCharacter, Float> attackerCooldowns = new java.util.HashMap<>();
    protected static final float PER_ATTACKER_COOLDOWN = 0.2f; // 0.2 seconds per attacker
    protected float boundsOffsetX = 0;
    protected float boundsOffsetY = 0;

    // Leveling
    protected int level;
    protected float currentXp;
    protected float xpToNextLevel;
    protected float attackCooldown; // Cooldown value
    protected float attackTimer; // Cooldown timer
    protected boolean autoAttack; // True = Hold, False = Click
    protected boolean levelUpPending; // Flag untuk menandakan level up yang belum dipilih effectnya

    // Strategy Pattern untuk attack behavior
    protected AttackStrategy attackStrategy;

    // Secondary Skill System (Q key) - Command Pattern
    protected Skill secondarySkill; // Q skill slot
    protected boolean hasSecondarySkill; // Flag untuk cek apakah ada skill

    // Dependency Injection for complex characters (e.g. Lumi)
    public void injectDependencies(com.kelompok2.frontend.systems.GameFacade facade,
            com.kelompok2.frontend.pools.EnemyPool enemyPool) {
        // Default implementation does nothing
    }

    public GameCharacter(float x, float y, float speed, float maxHp) {
        this.position = new Vector2(x, y);
        this.speed = speed;
        this.maxHp = maxHp;
        this.hp = maxHp;
        this.isFacingRight = true; // Default menghadap kanan
        this.bounds = new Rectangle(x, y, 32, 32); // Default size 32x32
        this.level = 1;
        this.currentXp = 0;
        this.xpToNextLevel = 100;

        // Default stats (akan di-override oleh subclass)
        this.atk = 10f;
        this.arts = 10f;
        this.def = 5f;

        this.attackCooldown = 0.5f;
        this.attackTimer = 0;
        this.autoAttack = false;
    }

    public void attack(Vector2 target, Array<Projectile> projectiles, Array<MeleeAttack> meleeAttacks) {
        // Can't attack if frozen or stunned
        if (isFrozen || isStunned) {
            return;
        }

        if (attackStrategy != null) {
            attackStrategy.execute(this, target, projectiles, meleeAttacks);
        }
    }

    // Method abstract buat innate skill
    public abstract void performInnateSkill();

    // Overload untuk skills yang aim ke mouse position
    public void performInnateSkill(Vector2 targetPos) {
        // Default: call basic skill (untuk backward compatibility)
        performInnateSkill();
    }

    public void confuse(float duration) {
        if (isInvulnerable()) {
            return;
        }
        this.isConfused = true;
        this.confusionTimer = duration;
    }

    public boolean isConfused() {
        return isConfused;
    }

    public void clearConfusion() {
        this.isConfused = false;
        this.confusionTimer = 0f;
    }

    public abstract float getInnateSkillTimer();

    public abstract float getInnateSkillCooldown();

    public abstract String getAttackAnimationType();

    public void clearInsanity() {
        this.isInsane = false;
        this.insanityTimer = 0f;
    }

    public boolean isInsane() {
        return isInsane;
    }

    public boolean isInvulnerable() {
        return false;
    }

    // Freeze status (for player)
    public void freeze(float duration) {
        if (isInvulnerable()) {
            return;
        }
        this.isFrozen = true;
        this.freezeTimer = duration;
    }

    public boolean isFrozen() {
        return isFrozen;
    }

    public void clearFreeze() {
        this.isFrozen = false;
        this.freezeTimer = 0f;
    }

    // Slow status (for player)
    public void slow(float duration) {
        if (isInvulnerable()) {
            return;
        }
        this.isSlowed = true;
        this.slowTimer = duration;
    }

    public boolean isSlowed() {
        return isSlowed;
    }

    public void clearSlow() {
        this.isSlowed = false;
        this.slowTimer = 0f;
    }

    // Stun status (for Hurricane Bind and other stun effects)
    public void stun(float duration) {
        if (isInvulnerable()) {
            return;
        }
        this.isStunned = true;
        this.stunTimer = duration;
    }

    public boolean isStunned() {
        return isStunned;
    }

    public void clearStun() {
        this.isStunned = false;
        this.stunTimer = 0;
    }

    // Insanity status (for player hit by Mind Fracture)
    public void makeInsane(float duration) {
        if (isInvulnerable()) {
            return;
        }
        this.isInsane = true;
        this.insanityTimer = duration;
    }

    // Update untuk mengurangi timer
    public void update(float delta) {
        if (attackTimer > 0) {
            attackTimer -= delta;
        }

        // Update secondary skill cooldown
        if (secondarySkill != null) {
            secondarySkill.update(delta);
        }

        // Update insanity timer
        if (insanityTimer > 0) {
            insanityTimer -= delta;
            if (insanityTimer <= 0) {
                clearInsanity();
            }
        }

        // Update freeze timer
        if (freezeTimer > 0) {
            freezeTimer -= delta;
            if (freezeTimer <= 0) {
                clearFreeze();
            }
        }

        // Update slow timer
        if (slowTimer > 0) {
            slowTimer -= delta;
            if (slowTimer <= 0) {
                clearSlow();
            }
        }

        // Update stun timer
        if (stunTimer > 0) {
            stunTimer -= delta;
            if (stunTimer <= 0) {
                clearStun();
            }
        }

        // Update mark timer
        if (markTimer > 0) {
            markTimer -= delta;
            if (markTimer <= 0) {
                isMarked = false;
            }
        }

        // Update confusion timer
        if (confusionTimer > 0) {
            confusionTimer -= delta;
            if (confusionTimer <= 0) {
                clearConfusion();
            }
        }

        // Update per-attacker cooldowns (Fix for bosses only hitting once)
        if (!attackerCooldowns.isEmpty()) {
            java.util.Iterator<java.util.Map.Entry<GameCharacter, Float>> it = attackerCooldowns.entrySet().iterator();
            while (it.hasNext()) {
                java.util.Map.Entry<GameCharacter, Float> entry = it.next();
                entry.setValue(entry.getValue() - delta);
                if (entry.getValue() <= 0) {
                    it.remove();
                }
            }
        }

        // Update Pull Movement
        if (isBeingPulled && pullTarget != null) {
            Vector2 toTarget = pullTarget.cpy().sub(position);
            float dist = toTarget.len();

            if (dist < 10f) {
                // Arrived
                isBeingPulled = false;
                setPosition(pullTarget.x, pullTarget.y); // Snap to target
                takeDamage(pullDamageOnArrival);
                stun(pullStunOnArrival);

                // Publish Damage Event (moved from ReturniousPullSkill)
                // False for Physical damage (White numbers)
                com.kelompok2.frontend.managers.GameEventManager.getInstance().publish(
                        new com.kelompok2.frontend.events.EnemyDamagedEvent(this, pullDamageOnArrival, false));

                // Award XP if enemy died from pull damage (same pattern as
                // FrozenApocalypseSkill)
                if (isDead() && pullAttacker != null && this instanceof DummyEnemy) {
                    pullAttacker.gainXp(((DummyEnemy) this).getXpReward());
                    System.out.println(
                            "[Returnious Pull] Killed enemy, granted " + ((DummyEnemy) this).getXpReward() + " XP!");
                }

                // Clear pull attacker reference
                pullAttacker = null;
            } else {
                // Move towards target
                toTarget.nor();
                position.add(toTarget.x * pullSpeed * delta, toTarget.y * pullSpeed * delta);
                bounds.setPosition(position.x + boundsOffsetX, position.y + boundsOffsetY);
            }
        }

        // Publish Cooldown Events
        // 1. Innate Skill
        if (getInnateSkillCooldown() > 0) {
            GameEventManager.getInstance().publish(new CooldownChangedEvent(this, CooldownChangedEvent.SkillType.INNATE,
                    getInnateSkillTimer(), getInnateSkillCooldown()));
        }

        // 2. Secondary Skill
        if (hasSecondarySkill()) {
            GameEventManager.getInstance()
                    .publish(new CooldownChangedEvent(this, CooldownChangedEvent.SkillType.SECONDARY,
                            secondarySkill.getRemainingCooldown(), secondarySkill.getCooldown()));
        }

        // 3. Ultimate Skill
        if (hasUltimateSkill()) {
            GameEventManager.getInstance()
                    .publish(new CooldownChangedEvent(this, CooldownChangedEvent.SkillType.ULTIMATE, 0, 0)); // 0
                                                                                                             // remaining
                                                                                                             // means
                                                                                                             // ready
        }
    }

    // Mark status (for Lumi)
    public void mark(float duration) {
        this.isMarked = true;
        this.markTimer = duration;
    }

    public boolean isMarked() {
        return isMarked;
    }

    public void pull(Vector2 target, float speed, float damage, float stunDuration, GameCharacter attacker) {
        this.isBeingPulled = true;
        this.pullTarget = target;
        this.pullSpeed = speed;
        this.pullDamageOnArrival = damage;
        this.pullStunOnArrival = stunDuration;
        this.pullAttacker = attacker;

        // Interrupt other states if reasonable
        clearFreeze();
    }

    public boolean isBeingPulled() {
        return isBeingPulled;
    }

    // Pull mechanics (for Returnious Pull)
    protected boolean isBeingPulled = false;
    protected Vector2 pullTarget;
    protected float pullSpeed;
    protected float pullDamageOnArrival;
    protected float pullStunOnArrival;
    protected GameCharacter pullAttacker;

    // Check bisa attack jika timer habis
    public boolean canAttack() {
        return attackTimer <= 0;
    }

    // Reset timer
    public void resetAttackTimer() {
        this.attackTimer = attackCooldown;
    }

    // Cek tipe attack
    public boolean isAutoAttack() {
        return autoAttack;
    }

    public void move(Vector2 direction, float delta) {
        // Can't move if frozen or stunned
        if (isFrozen || isStunned) {
            return;
        }

        // Apply slow effect (50% speed reduction)
        float effectiveSpeed = isSlowed ? speed * 0.5f : speed;

        direction.nor();

        // Update facing direction
        if (direction.x != 0) {
            this.isFacingRight = direction.x > 0;
        }

        position.add(direction.x * effectiveSpeed * delta, direction.y * effectiveSpeed * delta);

        // Position sekarang merepresentasikan posisi pojok kiri-bawah GAMBAR (Visual),
        // bukan Hitbox
        bounds.setPosition(position.x + boundsOffsetX, position.y + boundsOffsetY);
    }

    // Flag to distinguish player characters from enemies
    protected boolean isPlayerCharacter = true;

    public void setIsPlayerCharacter(boolean isPlayer) {
        this.isPlayerCharacter = isPlayer;
    }

    public boolean isPlayerCharacter() {
        return isPlayerCharacter;
    }

    public void takeDamage(float amount) {
        takeDamage(amount, null);
    }

    public void takeDamage(float amount, GameCharacter attacker) {
        // Balance Fix: Check per-attacker cooldown
        if (attacker != null && attackerCooldowns.containsKey(attacker)) {
            // This attacker hit us recently, ignore this hit
            return;
        }

        // Check for specific defensive skills via polymorphic hook
        if (secondarySkill != null) {
            amount = secondarySkill.onOwnerTakeDamage(this, amount);
        }

        // If damage is negated (0 or less), return early
        if (amount <= 0) {
            // Check if it was negated by a player shield (Aegis), we satisfied visual
            // feedback in Aegis.java via prints/reflect
            return;
        }

        float oldHp = this.hp;
        hp -= amount;
        if (hp < 0)
            hp = 0;

        float actualDamage = oldHp - this.hp;

        if (isFrozen) {
            clearFreeze();
        }

        // Balance Fix: Add cooldown for this attacker
        if (attacker != null) {
            attackerCooldowns.put(attacker, PER_ATTACKER_COOLDOWN);
        }

        // Publish Events
        // 1. HealthChangedEvent (UI Update)
        GameEventManager.getInstance().publish(new HealthChangedEvent(this, this.hp, this.maxHp));

        // 2. PlayerDamagedEvent (Damage Number - only for players)
        if (isPlayerCharacter && actualDamage > 0) {
            GameEventManager.getInstance()
                    .publish(new com.kelompok2.frontend.events.PlayerDamagedEvent(this, actualDamage, this.hp));
        }
    }

    public void heal(float amount) {
        hp += amount;
        if (hp > maxHp)
            hp = maxHp;

        // Publish HealthChangedEvent
        GameEventManager.getInstance().publish(new HealthChangedEvent(this, this.hp, this.maxHp));
    }

    public void gainXp(float xpAmount) {
        this.currentXp += xpAmount;
        // Publish XpChangedEvent
        GameEventManager.getInstance()
                .publish(new XpChangedEvent(this, this.currentXp, this.xpToNextLevel, this.level));
    }

    public boolean canLevelUp() {
        return this.currentXp >= this.xpToNextLevel;
    }

    public void levelUp() {
        if (!canLevelUp())
            return;

        this.currentXp -= this.xpToNextLevel;
        this.level++;
        this.xpToNextLevel = (float) Math.ceil(this.xpToNextLevel * 1.2f);

        // ✨ Passive Stat Growth (1-2% per level)
        // Max HP +2%
        float oldMaxHp = this.maxHp;
        this.maxHp = (float) Math.ceil(this.maxHp * 1.02f);
        this.hp += (this.maxHp - oldMaxHp); // Heal the amount gained

        // ATK, Arts, DEF +1%
        this.atk = (float) Math.ceil(this.atk * 1.01f);
        this.arts = (float) Math.ceil(this.arts * 1.01f);
        this.def = (float) Math.ceil(this.def * 1.01f);

        // TIDAK lagi auto-increase maxHp secara besar, akan ditangani oleh
        // LevelUpEffect yang dipilih

        // Set flag bahwa level-up belum dipilih effectnya
        this.levelUpPending = true;

        // Sync dengan GameManager (Singleton Pattern)
        GameManager.getInstance().incrementLevel();

        GameEventManager.getInstance()
                .publish(new XpChangedEvent(this, this.currentXp, this.xpToNextLevel, this.level));

        System.out.println(this.getClass().getSimpleName() + " Level Up! lv: " + level + " (Stats grown passively)");

    }

    public boolean isDead() {
        return hp <= 0;
    }

    public void setFacingRight(boolean isFacingRight) {
        this.isFacingRight = isFacingRight;
    }

    public Texture getTexture() {
        return texture;
    }

    public void render(SpriteBatch batch) {
        // Gambar karakter di posisi X, Y
        if (texture != null) {
            // Cek apakah texture perlu di-flip
            // Gambar asli (aset png) karakter menghadap ke KIRI
            // Jika isFacingRight true (mau hadap kanan), flipX harus true (dibalik)
            // Jika isFacingRight false (mau hadap kiri), flipX false (jangan dibalik, pakai
            // asli)
            boolean flipX = isFacingRight;
            // [LOGIKA BARU] Gunakan renderWidth/Height jika ada. Jika tidak, pakai bounds.
            float drawWidth = (renderWidth > 0) ? renderWidth : bounds.width;
            float drawHeight = (renderHeight > 0) ? renderHeight : bounds.height;
            // Gambar dirender seukuran hitbox
            batch.draw(
                    texture,
                    position.x,
                    position.y,
                    drawWidth,
                    drawHeight,
                    0,
                    0,
                    texture.getWidth(),
                    texture.getHeight(),
                    flipX,
                    false // flipY (tidak perlu dibalik secara vertikal)
            );
        }
    }

    public void dispose() {
        // Note: Don't dispose texture - it's owned and managed by AssetManager
        // This character only holds a reference, not ownership
        // AssetManager will handle texture lifecycle and cleanup
        // If we dispose here, the texture remains in AssetManager's cache as a disposed
        // (invalid) texture, causing black box rendering when reused
    }

    public void setPosition(float x, float y) {
        this.position.set(x, y);
        this.bounds.setPosition(x + boundsOffsetX, y + boundsOffsetY);
    }

    // Getter Setter standar
    public Vector2 getPosition() {
        return position;
    } // Helper buat kamera

    public Rectangle getBounds() {
        return bounds;
    }

    public float getHp() {
        return hp;
    }

    public float getMaxHp() {
        return maxHp;
    }

    public float getWidth() {
        return bounds.width;
    }

    public float getHeight() {
        return bounds.height;
    }

    public int getLevel() {
        return level;
    }

    public float getCurrentXp() {
        return currentXp;
    }

    public float getXpToNextLevel() {
        return xpToNextLevel;
    }

    public float getVisualWidth() {
        return (renderWidth > 0) ? renderWidth : bounds.width;
    }

    public float getVisualHeight() {
        return (renderHeight > 0) ? renderHeight : bounds.height;
    }

    // Getters untuk stats (GDD stats)
    public float getAtk() {
        return atk;
    }

    public float getArts() {
        return arts;
    }

    public float getDef() {
        return def;
    }

    // Setters untuk stats (digunakan oleh LevelUpEffect)
    public void setAtk(float atk) {
        this.atk = atk;
    }

    public void setArts(float arts) {
        this.arts = arts;
    }

    public void setDef(float def) {
        this.def = def;
    }

    public void setMaxHp(float maxHp) {
        if (this.maxHp > 0) {
            float ratio = this.hp / this.maxHp;
            this.maxHp = maxHp;
            this.hp = maxHp * ratio;
        } else {
            // Edge case initialization
            this.maxHp = maxHp;
            this.hp = maxHp;
        }

        // Publish HealthChangedEvent
        GameEventManager.getInstance().publish(new HealthChangedEvent(this, this.hp, this.maxHp));
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    // Getter dan setter untuk level-up pending flag
    public boolean isLevelUpPending() {
        return levelUpPending;
    }

    public void setLevelUpPending(boolean levelUpPending) {
        this.levelUpPending = levelUpPending;
    }

    // Setter untuk attack strategy (Strategy Pattern)
    public void setAttackStrategy(AttackStrategy strategy) {
        this.attackStrategy = strategy;
    }

    public AttackStrategy getAttackStrategy() {
        return attackStrategy;
    }

    // Reset movement/attack state (used when pausing/leveling up)
    public void stop() {
        this.isAimingUltimate = false;
    }

    // Secondary Skill System methods
    public void setSecondarySkill(Skill skill) {
        this.secondarySkill = skill;
        this.hasSecondarySkill = (skill != null);

        if (skill != null) {
            System.out.println("[" + this.getClass().getSimpleName() + "] Learned skill: " + skill.getName());
        }
    }

    public Skill getSecondarySkill() {
        return secondarySkill;
    }

    public boolean hasSecondarySkill() {
        return hasSecondarySkill && secondarySkill != null;
    }

    public void performSecondarySkill(Vector2 targetPos,
            Array<Projectile> projectiles,
            Array<MeleeAttack> meleeAttacks) {
        if (!hasSecondarySkill()) {
            System.out.println("[" + this.getClass().getSimpleName() + "] No secondary skill equipped!");
            return;
        }

        // Activate skill via Command Pattern
        secondarySkill.activate(this, targetPos, projectiles, meleeAttacks);
    }

    // Ultimate Skill System (R key) - Looted from bosses
    protected Skill ultimateSkill = null;
    protected boolean ultimateUsed = false;
    protected boolean isAimingUltimate = false;

    public void setAimingUltimate(boolean isAiming) {
        this.isAimingUltimate = isAiming;
    }

    public boolean isAimingUltimate() {
        return isAimingUltimate;
    }

    public void setUltimateSkill(Skill skill) {
        this.ultimateSkill = skill;
        this.ultimateUsed = false;

        if (skill != null) {
            System.out.println("[" + this.getClass().getSimpleName() + "] Acquired ULTIMATE: " + skill.getName() + "!");
        }
    }

    public Skill getUltimateSkill() {
        return ultimateSkill;
    }

    public boolean hasUltimateSkill() {
        return ultimateSkill != null && !ultimateUsed;
    }

    public boolean isUltimateUsed() {
        return ultimateUsed;
    }

    public void performUltimateSkill(Vector2 targetPos,
            Array<Projectile> projectiles,
            Array<MeleeAttack> meleeAttacks) {
        if (!hasUltimateSkill()) {
            if (ultimateUsed) {
                System.out.println("[" + this.getClass().getSimpleName() + "] Ultimate already used!");
            } else {
                System.out.println("[" + this.getClass().getSimpleName() + "] No ultimate skill!");
            }
            return;
        }

        // Activate ultimate skill
        ultimateSkill.activate(this, targetPos, projectiles, meleeAttacks);

        // Mark as used
        ultimateUsed = true;

        // Publish event to update UI immediately (Cooldown > 0 means not ready)
        // Using arbitrary value 999 to signify "used/unavailable" since it's one-time
        // use
        GameEventManager.getInstance()
                .publish(new CooldownChangedEvent(this, CooldownChangedEvent.SkillType.ULTIMATE, 999, 999));

        System.out.println(
                "[" + this.getClass().getSimpleName() + "] Acquired ULTIMATE: " + ultimateSkill.getName() + "!");
    }
}
