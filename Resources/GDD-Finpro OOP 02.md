# Maestra Trials - Game Design Document

## 1. Overview
**Title:** Maestra Trials  
**Genre:** Roguelike Dungeon-Crawler

### Game Description
Maestra Trials is a roguelike game where the player selects a character to enter a dungeon and defeat enemies within it. Each level increases in enemy difficulty, and players can choose specific buffs or effects after leveling up.

### Core Loop Gameplay
* **Character Selection:** Player selects a character.
* **Game Start:** Player explores the dungeon filled with enemies.
* **Combat:** Player enters combat with enemies.
* **Reward & Progression:** As the player kills more enemies, EXP increases, and the player can choose specific effects upon leveling up.
* **Progression:** Enemies become more numerous and stronger as the player's level rises.
* **Game Over:** If all characters die, the highest level reached is recorded and sent to the backend.

---

## 2. References and Inspiration

| Reference/Inspiration | Object of Inspiration/Reference |
| :--- | :--- |
| **Arknights** | Main inspiration for character designs. |
| **Soul Knight** | Characters have their own weapons but can add more in the dungeon. |
| **HoloCure** | Buff-picking after level-up & Overall gameplay loop. |

---

## 3. Mechanics

### Combat Mechanics
* **Type:** On-Field combat.
* **Perspective:** Top-Down 2D.
* **Default Controls:**
    * **WASD:** Move.
    * **Left Click:** Basic attack.
    * **E:** Innate skill.
    * **Q:** Second skill (looted skill).
* **Mechanics:**
    * Players and enemies have Hitboxes.
    * If player attacks and the weapon hits the enemy -> Enemy HP decreases.
    * If an enemy collides with/attacks the player -> Player HP decreases.

### Roguelike Mechanics
* **Permadeath:** No save; death results in Game Over, and the score is sent to the backend.
* **Level Up Effects:** After every level, the player can choose one of the following effects:
    * Recover HP.
    * Increase ATK & Arts attack.
    * Increase Max HP.
    * Increase DEF.
    * Pick a new skill (for the 2nd slot).

### Bosses
* **Spawning:** Spawns every X minutes.
* **Rewards:**
    * After defeating a boss, the player can loot the boss's "ultimate".
    * The "ultimate" is a one-time use skill.
    * Upon defeat, the boss becomes a playable character.

### UI/UX Mechanics

#### Main Menu
* **Main Menu Screen:**
    * Displays game logo ("MAESTRA TRIALS")
    * **Menu Options:**
        * **MULAI** (Start) - Goes to Character Selection
        * **PENGATURAN** (Settings) - Opens settings menu (keybinds, audio)
        * **KELUAR** (Logout/Exit) - Exits the game
    * Note: Future versions may include login system (Username + Password)

#### Character Selection
* **Character Selection Screen:**
    * **Character Grid:** Displays character portraits in a grid layout
    * **Character Preview:**
        * Character portrait/photo on hover or selection
        * Idle animation plays when hovering over character
    * **Character Information Panel:**
        * **Stats Display:** Shows HP, ATK, Arts, DEF, SPEED
        * **Innate Skill Description:** Description of character's unique E skill
        * **Role Indicator:** Shows character role (Physical Attacker, Arts Attacker, Tank, Healing Attacker)
    * **Confirm Button:** Starts the game with selected character


---

## 4. Character Roster
Each character has a unique innate skill but has a second skill slot that can be filled via monster drops or level-up rewards.

**Development Targets:**
* **Pessimistic:** 3 Characters.
* **Realistic:** 5 Characters.
* **Optimistic:** 10 Characters.

**Stats & Roles:**
* **Stats:** HP, DEF, ATK, ARTS, SPEED
* **Roles:**
    * **Physical Attacker:** Higher ATK.
    * **Arts Attacker:** Higher Arts ATK.
    * **Tank:** Higher HP and/or Defence.
    * **Healing Attacker:** Lower ATK stats but can heal.

### Character List

#### Ryze - The Ghost of Insania
* **Role:** Physical Attacker.
* **Stats:** Moderate HP, High ATK, Low Arts, Low Defence, High Speed.
* **Basic Attack:** Swings a scythe.
* **Innate Skill (Spectral Body):** Active for 3 seconds. Enemies can target Ryze, but attacks pass through him without effect.
* **Cooldown:** 15 seconds.

#### Whisperwind - The Silent Caster
* **Role:** Arts Attacker.
* **Stats:** Moderate HP, Low ATK, High Arts, Moderate Defence, Moderate Speed.
* **Basic Attack:** Air Slash (Ranged projectile slash).
* **Innate Skill (Hurricane Bind):** Shoots a wind ball that knocks back enemies and stuns them for 3 seconds.
* **Cooldown:** 10 seconds.

#### Aelita - The Evergreen Healer
* **Role:** Healing Attacker.
* **Stats:** High HP, Low ATK, Moderate Arts, Moderate Defence, Moderate Speed.
* **Basic Attack:** Shoots projectile.
* **Innate Skill (Verdant Domain):** Consumes 5% HP to deploy a zone that increases Arts and ATK, and heals 3% HP over 5 seconds.
* **Cooldown:** 15 seconds.

#### Aegis - The Impenetrable Shield
* **Role:** Tank.
* **Stats:** High HP, Low ATK, Low Arts, High Defence, Moderate Speed.
* **Basic Attack:** Shield Bash (slight forward movement).
* **Innate Skill ("Here, I shall stand!"):** Immobilized for 2 seconds, blocks all frontal damage, and reflects 50% of damage back to enemies.
* **Cooldown:** 10 seconds.

#### Lumi - The Pale Renegade
* **Role:** Physical Attacker.
* **Stats:** Low HP, High ATK, Low Arts, Moderate Defence, High Speed.
* **Basic Attack:** Punch (applies a mark to the enemy).
* **Innate Skill (Returnious Pull):** Pulls the nearest marked enemy, deals high damage, and stuns for 1 second.

#### Alice - The Reckless Princess
* **Role:** Physical Attacker.
* **Stats:** Moderate HP, High ATK, Low Arts, Low Defence, High Speed.
* **Basic Attack:** Scratch.
* **Innate Skill (Feral Rush):** 5x scratch in quick succession while dashing forward.
* **Cooldown:** 5 seconds.

#### Alex - The Calculating Prince
* **Role:** Arts Attacker.
* **Stats:** Moderate HP, Low ATK, High Arts, High Defence, Low Speed.
* **Basic Attack:** Shoot with wand.
* **Innate Skill (Tactical Counter):** Nullifies damage for 0.5 seconds; if attacked during this window, deals high damage to all surrounding enemies.

#### Raiden - The Speed Demon
* **Role:** Physical Attacker.
* **Stats:** Low HP, High ATK, Low Arts, Moderate Def, Very High Speed.
* **Basic Attack:** Punch (Lower cooldown than others).
* **Innate Skill (Lightning Boost):** Increases speed and attack (based on Arts) for 5 seconds.
* **Cooldown:** 10 seconds.

#### Artorias - King of Lumina
* **Role:** Tank.
* **Stats:** High HP, Low ATK, High Arts, High Def, Low Speed.
* **Basic Attack:** Shoot beam.
* **Innate Skill (Light Construct):** Summons walls around him, blocking damage.

#### Funami - The White Raven
* **Role:** Arts Attacker.
* **Stats:** Low HP, Low ATK, Very High Arts, Low Def, Moderate Speed.
* **Basic Attack:** Shoot gun.
* **Innate Skill ("KNEEL!"):** Surrounding enemies are immobilized and take DoT (Damage over Time) for 3 seconds.

---

## 5. List Boss

#### Insania - The Chaos Kaiser
* **Role:** Physical Attacker with some Arts capabilities.
* **Stats:** Moderate HP, High ATK, Moderate Arts, Low Defence, Moderate Speed.
* **Basic Attack:** Mad Claw (Physical Scaling).
* **Innate Skill (Mind Fracture):**
    * Surrounding enemies receive "Insanity" debuff for 5 seconds.
    * Insane enemies have increased Arts/ATK but move randomly and can friendly fire.
    * Insania deals extra damage to enemies with "Insanity".
    * **Cooldown:** 10 seconds (Arts scaling).

#### Blaze - The Flame Kaiser
* **Role:** Arts Attacker.
* **Stats:** Moderate HP, Moderate ATK, High Arts, Low Defence, Moderate Speed.
* **Basic Attack:** Flame Punch (Scaling: 0.7 ATK, 0.3 Arts).
* **Innate Skill (Hellfire Pillar):** Summons a pillar at the cursor location, dealing high damage.
* **Cooldown:** 5 seconds.

#### Isolde - The Frost Kaiser
* **Role:** Arts Attacker.
* **Stats:** Moderate HP, Low ATK, High Arts, Moderate Defence, Moderate Speed.
* **Basic Attack:** Shoot icicles.
* **Innate Skill (Glacial Breath):** Shoots in a cone hitbox, freezing enemies on hit (Duration: 3 seconds or until hit again).
* **Cooldown:** 10 seconds.

---

---

## 6. Design Pattern & Pillar OOP

### A. Pillar OOP
* **Class & Object:** Main blueprints for Player, Enemy, Projectile, and Item.
* **Encapsulation:** Using private variables for sensitive stats (HP, ATK, Arts) accessible only via getter/setter methods.
* **Inheritance:**
    * **Parent Class:** `GameCharacter`.
    * **Child Classes:** `Enemy` and characters in the roster.
* **Polymorphism & Abstraction:**
    * Abstract method `performInnateSkill()` in `GameCharacter` class, overridden with different behaviors by each character.

### B. Backend Integration
* Receives HTTP POST requests upon Game Over to save the score and leaderboard data (Player Name, Highest Level, Character Used) into the database.

### C. Design Patterns
7 Design Patterns are implemented to keep the code structured and scalable.

1.  **Singleton Pattern**
    * **Implementation:** `GameManager` and `AssetManager`.
    * **Function:** Ensures only one instance manages global data (score, level) and loads assets once to save memory.

2.  **Factory Method Pattern**
    * **Implementation:** `EnemyFactory`.
    * **Function:** Dynamically creates enemy objects based on the level difficulty.

3.  **Object Pool Pattern**
    * **Implementation:** `ProjectilePool` and `EnemyPool`.
    * **Function:**
        * **ProjectilePool:** Manages projectiles to prevent memory load from constant firing.
        * **EnemyPool:** Manages enemy objects to prevent lag from excessive Garbage Collection as enemy count increases. Dead enemies are deactivated and stored for reuse with reset stats.

4.  **Command Pattern**
    * **Implementation:** `InputHandler`.
    * **Function:** Separates keyboard logic from character logic. Inputs (WASD, Attack, Skills) are wrapped in command objects, facilitating keybinding changes.

5.  **Observer Pattern**
    * **Implementation:** HUD System.
    * **Function:** The Health Bar UI acts as an Observer monitoring the Player. When Player HP drops, the Player notifies the UI to update, removing the need for manual checking every frame.

6.  **State Pattern**
    * **Implementation:** `EntityState` & `GameState`.
    * **Function:**
        * **Character:** Manages animations to prevent overlap (IDLE, RUN, ATTACK, HURT, DEAD).
        * **Game:** Manages active screens (MainMenu, Gameplay, Pause, GameOver).

7.  **Strategy Pattern**
    * **Implementation:** Skill System.
    * **Function:** Changes skill attack behavior dynamically. Picking up a new skill item changes the behavior of the Q button without modifying the Player class code. It also simplifies implementing innate skills with different scaling.
