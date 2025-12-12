Here is the content of the document converted into a formatted Markdown file.

# Maestra Trials - Game Design Document

## 1. Overview
[cite_start]**Title:** Maestra Trials [cite: 2]
[cite_start]**Genre:** Roguelike Dungeon-Crawler [cite: 3]

### Game Description
[cite_start]Maestra Trials is a roguelike game where the player selects a character to enter a dungeon and defeat enemies within it[cite: 5]. [cite_start]Each level increases in enemy difficulty, and players can choose specific buffs or effects after leveling up[cite: 6].

### Core Loop Gameplay
* [cite_start]**Character Selection:** Player selects a character[cite: 8].
* [cite_start]**Game Start:** Player explores the dungeon filled with enemies[cite: 9].
* [cite_start]**Combat:** Player enters combat with enemies[cite: 10].
* [cite_start]**Reward & Progression:** As the player kills more enemies, EXP increases, and the player can choose specific effects upon leveling up[cite: 11].
* [cite_start]**Progression:** Enemies become more numerous and stronger as the player's level rises[cite: 12].
* [cite_start]**Game Over:** If all characters die, the highest level reached is recorded and sent to the backend[cite: 13].

---

## 2. References and Inspiration

| Reference/Inspiration | Object of Inspiration/Reference |
| :--- | :--- |
| **Arknights** | [cite_start]Main inspiration for character designs[cite: 15]. |
| **Soul Knight** | [cite_start]Characters have their own weapons but can add more in the dungeon[cite: 15]. |
| **HoloCure** | [cite_start]Buff-picking after level-up & Overall gameplay loop[cite: 15]. |

---

## 3. Mechanics

### Combat Mechanics
* [cite_start]**Type:** On-Field combat[cite: 17].
* [cite_start]**Perspective:** Top-Down 2D[cite: 18].
* **Default Controls:**
    * [cite_start]**WASD:** Move[cite: 20].
    * [cite_start]**Left Click:** Basic attack[cite: 21].
    * [cite_start]**E:** Innate skill[cite: 22].
    * [cite_start]**Q:** Second skill (looted skill)[cite: 23].
* **Mechanics:**
    * [cite_start]Players and enemies have Hitboxes[cite: 25].
    * [cite_start]If player attacks and the weapon hits the enemy -> Enemy HP decreases[cite: 26].
    * [cite_start]If an enemy collides with/attacks the player -> Player HP decreases[cite: 27].

### Roguelike Mechanics
* [cite_start]**Permadeath:** No save; death results in Game Over, and the score is sent to the backend[cite: 30].
* **Level Up Effects:** After every level, the player can choose one of the following effects:
    * [cite_start]Recover HP[cite: 32].
    * [cite_start]Increase ATK & Arts attack[cite: 33].
    * [cite_start]Increase Max HP[cite: 34].
    * [cite_start]Increase DEF[cite: 35].
    * [cite_start]Pick a new skill (for the 2nd slot)[cite: 36].

### Bosses
* [cite_start]**Spawning:** Spawns every X minutes[cite: 38].
* **Rewards:**
    * [cite_start]After defeating a boss, the player can loot the boss's "ultimate"[cite: 39].
    * [cite_start]The "ultimate" is a one-time use skill[cite: 41].
    * [cite_start]Upon defeat, the boss becomes a playable character[cite: 40].

---

## 4. Character Roster
[cite_start]Each character has a unique innate skill but has a second skill slot that can be filled via monster drops or level-up rewards[cite: 44].

**Development Targets:**
* [cite_start]**Pessimistic:** 3 Characters[cite: 46].
* [cite_start]**Realistic:** 5 Characters[cite: 47].
* [cite_start]**Optimistic:** 10 Characters[cite: 48].

**Stats & Roles:**
* [cite_start]**Stats:** HP, DEF, ATK, ARTS, SPEED [cite: 50-54].
* **Roles:**
    * [cite_start]**Physical Attacker:** Higher ATK[cite: 56].
    * [cite_start]**Arts Attacker:** Higher Arts ATK[cite: 57].
    * [cite_start]**Tank:** Higher HP and/or Defence[cite: 58].
    * [cite_start]**Healing Attacker:** Lower ATK stats but can heal[cite: 59].

### Character List

#### [cite_start]Ryze - The Ghost of Insania [cite: 60]
* [cite_start]**Role:** Physical Attacker[cite: 63].
* [cite_start]**Stats:** Moderate HP, High ATK, Low Arts, Low Defence, High Speed[cite: 62].
* [cite_start]**Basic Attack:** Swings a scythe[cite: 64].
* **Innate Skill (Spectral Body):** Active for 3 seconds. [cite_start]Enemies can target Ryze, but attacks pass through him without effect[cite: 65].
* [cite_start]**Cooldown:** 15 seconds[cite: 66].

#### [cite_start]Whisperwind - The Silent Caster [cite: 67]
* [cite_start]**Role:** Arts Attacker[cite: 69].
* [cite_start]**Stats:** Moderate HP, Low ATK, High Arts, Moderate Defence, Moderate Speed[cite: 68].
* [cite_start]**Basic Attack:** Air Slash (Ranged projectile slash)[cite: 70].
* [cite_start]**Innate Skill (Hurricane Bind):** Shoots a wind ball that knocks back enemies and stuns them for 3 seconds[cite: 71].
* [cite_start]**Cooldown:** 10 seconds[cite: 71].

#### [cite_start]Aelita - The Evergreen Healer [cite: 72]
* [cite_start]**Role:** Healing Attacker[cite: 74].
* [cite_start]**Stats:** High HP, Low ATK, Moderate Arts, Moderate Defence, Moderate Speed[cite: 73].
* [cite_start]**Basic Attack:** Shoots projectile[cite: 75].
* [cite_start]**Innate Skill (Verdant Domain):** Consumes 5% HP to deploy a zone that increases Arts and ATK, and heals 3% HP over 5 seconds[cite: 76].
* [cite_start]**Cooldown:** 15 seconds[cite: 77].

#### [cite_start]Aegis - The Impenetrable Shield [cite: 78]
* [cite_start]**Role:** Tank[cite: 80].
* [cite_start]**Stats:** High HP, Low ATK, Low Arts, High Defence, Moderate Speed[cite: 79].
* [cite_start]**Basic Attack:** Shield Bash (slight forward movement)[cite: 81].
* [cite_start]**Innate Skill ("Here, I shall stand!"):** Immobilized for 2 seconds, blocks all frontal damage, and reflects 50% of damage back to enemies[cite: 81].
* [cite_start]**Cooldown:** 10 seconds[cite: 82].

#### [cite_start]Lumi - The Pale Renegade [cite: 83]
* [cite_start]**Role:** Physical Attacker[cite: 86].
* [cite_start]**Stats:** Low HP, High ATK, Low Arts, Moderate Defence, High Speed[cite: 85].
* [cite_start]**Basic Attack:** Punch (applies a mark to the enemy)[cite: 87].
* [cite_start]**Innate Skill (Returnious Pull):** Pulls the nearest marked enemy, deals high damage, and stuns for 1 second[cite: 87].

#### [cite_start]Alice - The Reckless Princess [cite: 88]
* [cite_start]**Role:** Physical Attacker[cite: 90].
* [cite_start]**Stats:** Moderate HP, High ATK, Low Arts, Low Defence, High Speed[cite: 89].
* [cite_start]**Basic Attack:** Scratch[cite: 91].
* [cite_start]**Innate Skill (Feral Rush):** 5x scratch in quick succession while dashing forward[cite: 92].
* [cite_start]**Cooldown:** 5 seconds[cite: 92].

#### [cite_start]Alex - The Calculating Prince [cite: 93]
* [cite_start]**Role:** Arts Attacker[cite: 95].
* [cite_start]**Stats:** Moderate HP, Low ATK, High Arts, High Defence, Low Speed[cite: 94].
* [cite_start]**Basic Attack:** Shoot with wand[cite: 96].
* [cite_start]**Innate Skill (Tactical Counter):** Nullifies damage for 0.5 seconds; if attacked during this window, deals high damage to all surrounding enemies[cite: 97].

#### [cite_start]Raiden - The Speed Demon [cite: 98]
* [cite_start]**Role:** Physical Attacker[cite: 100].
* [cite_start]**Stats:** Low HP, High ATK, Low Arts, Moderate Def, Very High Speed[cite: 99].
* [cite_start]**Basic Attack:** Punch (Lower cooldown than others)[cite: 101].
* [cite_start]**Innate Skill (Lightning Boost):** Increases speed and attack (based on Arts) for 5 seconds[cite: 102].
* [cite_start]**Cooldown:** 10 seconds[cite: 102].

#### [cite_start]Artorias - King of Lumina [cite: 103]
* [cite_start]**Role:** Tank[cite: 105].
* [cite_start]**Stats:** High HP, Low ATK, High Arts, High Def, Low Speed[cite: 104].
* [cite_start]**Basic Attack:** Shoot beam[cite: 106].
* [cite_start]**Innate Skill (Light Construct):** Summons walls around him, blocking damage[cite: 107].

#### [cite_start]Funami - The White Raven [cite: 108]
* [cite_start]**Role:** Arts Attacker[cite: 110].
* [cite_start]**Stats:** Low HP, Low ATK, Very High Arts, Low Def, Moderate Speed[cite: 109].
* [cite_start]**Basic Attack:** Shoot gun[cite: 111].
* [cite_start]**Innate Skill ("KNEEL!"):** Surrounding enemies are immobilized and take DoT (Damage over Time) for 3 seconds[cite: 112].

---

## 5. List Boss

#### [cite_start]Insania - The Chaos Kaiser [cite: 114]
* [cite_start]**Role:** Physical Attacker with some Arts capabilities[cite: 116].
* [cite_start]**Stats:** Moderate HP, High ATK, Moderate Arts, Low Defence, Moderate Speed[cite: 115].
* [cite_start]**Basic Attack:** Mad Claw (Physical Scaling)[cite: 117].
* **Innate Skill (Mind Fracture):**
    * [cite_start]Surrounding enemies receive "Insanity" debuff for 5 seconds[cite: 117].
    * [cite_start]Insane enemies have increased Arts/ATK but move randomly and can friendly fire[cite: 118].
    * [cite_start]Insania deals extra damage to enemies with "Insanity"[cite: 119].
    * [cite_start]**Cooldown:** 10 seconds (Arts scaling)[cite: 119].

#### [cite_start]Blaze - The Flame Kaiser [cite: 120]
* [cite_start]**Role:** Arts Attacker[cite: 122].
* [cite_start]**Stats:** Moderate HP, Moderate ATK, High Arts, Low Defence, Moderate Speed[cite: 121].
* [cite_start]**Basic Attack:** Flame Punch (Scaling: 0.7 ATK, 0.3 Arts)[cite: 123].
* [cite_start]**Innate Skill (Hellfire Pillar):** Summons a pillar at the cursor location, dealing high damage[cite: 124].
* [cite_start]**Cooldown:** 5 seconds[cite: 124].

#### [cite_start]Isolde - The Frost Kaiser [cite: 125]
* [cite_start]**Role:** Arts Attacker[cite: 127].
* [cite_start]**Stats:** Moderate HP, Low ATK, High Arts, Moderate Defence, Moderate Speed[cite: 126].
* [cite_start]**Basic Attack:** Shoot icicles[cite: 128].
* [cite_start]**Innate Skill (Glacial Breath):** Shoots in a cone hitbox, freezing enemies on hit (Duration: 3 seconds or until hit again)[cite: 129].
* [cite_start]**Cooldown:** 10 seconds[cite: 130].

---

## 6. Design Pattern & Pillar OOP

### A. Pillar OOP
* [cite_start]**Class & Object:** Main blueprints for Player, Enemy, Projectile, and Item[cite: 134].
* [cite_start]**Encapsulation:** Using private variables for sensitive stats (HP, ATK, Arts) accessible only via getter/setter methods[cite: 135].
* **Inheritance:**
    * [cite_start]**Parent Class:** `GameCharacter`[cite: 137].
    * [cite_start]**Child Classes:** `Enemy` and characters in the roster[cite: 138].
* **Polymorphism & Abstraction:**
    * [cite_start]Abstract method `performInnateSkill()` in `GameCharacter` class, overridden with different behaviors by each character[cite: 140].

### B. Backend Integration
* [cite_start]Receives HTTP POST requests upon Game Over to save the score and leaderboard data (Player Name, Highest Level, Character Used) into the database[cite: 142].

### C. Design Patterns
[cite_start]7 Design Patterns are implemented to keep the code structured and scalable[cite: 144].

1.  **Singleton Pattern**
    * [cite_start]**Implementation:** `GameManager` and `AssetManager`[cite: 146].
    * [cite_start]**Function:** Ensures only one instance manages global data (score, level) and loads assets once to save memory[cite: 147].

2.  **Factory Method Pattern**
    * [cite_start]**Implementation:** `EnemyFactory`[cite: 149].
    * [cite_start]**Function:** Dynamically creates enemy objects based on the level difficulty[cite: 150].

3.  **Object Pool Pattern**
    * [cite_start]**Implementation:** `ProjectilePool` and `EnemyPool`[cite: 152].
    * **Function:**
        * [cite_start]**ProjectilePool:** Manages projectiles to prevent memory load from constant firing[cite: 154].
        * [cite_start]**EnemyPool:** Manages enemy objects to prevent lag from excessive Garbage Collection as enemy count increases[cite: 155]. [cite_start]Dead enemies are deactivated and stored for reuse with reset stats[cite: 156].

4.  **Command Pattern**
    * [cite_start]**Implementation:** `InputHandler`[cite: 158].
    * **Function:** Separates keyboard logic from character logic. [cite_start]Inputs (WASD, Attack, Skills) are wrapped in command objects, facilitating keybinding changes[cite: 159, 160].

5.  **Observer Pattern**
    * [cite_start]**Implementation:** HUD System[cite: 162].
    * [cite_start]**Function:** The Health Bar UI acts as an Observer monitoring the Player[cite: 163]. [cite_start]When Player HP drops, the Player notifies the UI to update, removing the need for manual checking every frame[cite: 164].

6.  **State Pattern**
    * [cite_start]**Implementation:** `EntityState` & `GameState`[cite: 166].
    * **Function:**
        * [cite_start]**Character:** Manages animations to prevent overlap (IDLE, RUN, ATTACK, HURT, DEAD)[cite: 168].
        * [cite_start]**Game:** Manages active screens (MainMenu, Gameplay, Pause, GameOver)[cite: 169].

7.  **Strategy Pattern**
    * [cite_start]**Implementation:** Skill System[cite: 171].
    * **Function:** Changes skill attack behavior dynamically. [cite_start]Picking up a new skill item changes the behavior of the Q button without modifying the Player class code[cite: 172]. [cite_start]It also simplifies implementing innate skills with different scaling[cite: 173].