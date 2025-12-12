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

### A. Pillar OOP (Modules 1-3)

#### 1. Class & Object (Module 1)
* **Implementation:** All game entities are represented as classes with instantiated objects
* **Examples:**
    * `GameCharacter` class → `Ryze`, `DummyEnemy` objects
    * `Projectile` class → Individual projectile instances
    * `MeleeAttack` class → Temporary attack hitbox objects
    * `GameManager` singleton instance
    * `AssetManager` singleton instance

#### 2. Encapsulation (Module 2)
* **Implementation:** Private fields with public getter/setter methods
* **Examples:**
    * `GameCharacter` class:
        * Private: `hp`, `maxHp`, `atk`, `arts`, `def`, `level`
        * Public getters: `getHp()`, `getMaxHp()`, `getAtk()`, etc.
    * `AssetManager` class:
        * Private: `textureCache` (HashMap)
        * Public methods: `loadTexture()`, `dispose()`

#### 3. Inheritance (Module 2)
* **Implementation:** Parent-child class relationships
* **Class Hierarchy:**
    ```
    GameCharacter (abstract parent)
    ├── Ryze
    ├── Whisperwind (planned)
    ├── Aelita (planned)
    ├── Aegis (planned)
    └── DummyEnemy
    ```

#### 4. Polymorphism & Abstraction (Module 3)
* **Implementation:** Abstract methods overridden by subclasses
* **Examples:**
    * Abstract method in `GameCharacter`: `performInnateSkill()`
    * Overridden in `Ryze`: Implements Spectral Body skill logic
    * Overridden in `DummyEnemy`: Empty implementation (no skill)
    * Interface `AttackStrategy`: Implemented by `MeleeAttackStrategy` and `RangedAttackStrategy`

---

### B. Backend Integration (Modules 5-6)

#### Spring Boot & RESTful API (Module 5)
* **Planned Implementation:** Backend server to handle game data
* **Endpoints:**
    * `POST /api/scores` - Submit game over data
    * `GET /api/leaderboard` - Retrieve top scores

#### Spring Data JPA & CRUD (Module 6)
* **Planned Implementation:** Database operations for persistent storage
* **Entities:**
    * `Player` entity (username, highestLevel, characterUsed, timestamp)
    * Repository: `PlayerRepository` extends `JpaRepository`

---

### C. Design Patterns (Modules 7-11)

10 Design Patterns are implemented to keep the code structured and scalable.

#### 1. Singleton Pattern (Module 7) ✅ **IMPLEMENTED**
* **Classes:** `GameManager`, `AssetManager`
* **Implementation Details:**
    * Private static instance field
    * Private constructor to prevent external instantiation
    * Public `getInstance()` method (thread-safe with `synchronized`)
* **Location:**
    * `com.kelompok2.frontend.managers.GameManager`
    * `com.kelompok2.frontend.managers.AssetManager`
* **Function:**
    * `GameManager`: Manages global game state (level, time, character name, game over status)
    * `AssetManager`: Caches textures to prevent duplicate loading and reduce memory usage

#### 2. Factory Method Pattern (Module 8) ✅ **IMPLEMENTED**
* **Classes:** `EnemyFactory`, `EnemyType` enum
* **Implementation Details:**
    * Static factory method `createEnemy(type, x, y, target)`
    * Returns different enemy instances based on EnemyType
    * `getRandomEnemyType(level)` for difficulty scaling
* **Location:**
    * `com.kelompok2.frontend.factories.EnemyFactory`
    * `com.kelompok2.frontend.factories.EnemyType`
* **Function:** 
    * Dynamically creates enemy objects without exposing creation logic
    * Enables level-based difficulty scaling (currently all DUMMY, ready for expansion)
* **Future Use:** Will create FAST, TANK, and RANGED enemy types as gameplay expands

#### 3. Object Pool Pattern (Module 8) ✅ **IMPLEMENTED**
* **Classes:** `ProjectilePool`, `EnemyPool`
* **Implementation Details:**
    * Maintains pools of available and active objects
    * `obtain()` method retrieves from pool or creates new if empty
    * `free(object)` method returns object to available pool
    * Auto-frees inactive/dead objects during update()
* **Location:**
    * `com.kelompok2.frontend.pools.ProjectilePool`
    * `com.kelompok2.frontend.pools.EnemyPool`
* **Function:**
    * `ProjectilePool`: Reuses projectile objects to prevent constant allocation/deallocation
    * `EnemyPool`: Reuses enemy objects to reduce Garbage Collection lag
* **Performance Impact:** Significant reduction in GC overhead, especially with many projectiles/enemies

#### 4. Command Pattern (Module 9) 🔧 **PARTIALLY IMPLEMENTED**
* **Class:** `InputHandler`
* **Current Implementation:**
    * Handles input processing for player controls
    * Separates input logic from game logic
* **Planned Enhancement:**
    * Wrap each input action in command objects (e.g., `MoveCommand`, `AttackCommand`, `SkillCommand`)
    * Enable rebindable controls by changing command mappings
* **Location:** `com.kelompok2.frontend.utils.InputHandler`

#### 5. Observer Pattern (Module 9) ⏳ **PLANNED**
* **Implementation:** HUD System with observer-subject relationship
* **Components:**
    * **Subject:** `GameCharacter` (maintains list of observers)
    * **Observer:** `HealthBarUI`, `XPBarUI` (implements `Observer` interface)
* **Function:**
    * When player HP changes, automatically notify all UI observers
    * UI updates without manual polling every frame
* **Future Use:** Cleaner UI code, easier to add new UI elements

#### 6. State Pattern (Module 10) ⏳ **PLANNED**
* **Classes:** `EntityState`, `GameState`
* **Implementation Details:**
    * Each state is a separate class implementing common interface
    * Context object switches between states
* **States:**
    * **Character States:** `IdleState`, `RunState`, `AttackState`, `HurtState`, `DeadState`
    * **Game States:** `MainMenuState`, `GameplayState`, `PauseState`, `GameOverState`
* **Function:**
    * Manage animations to prevent overlap
    * Control screen transitions
* **Future Use:** Clean state transitions, easier animation management

#### 7. Strategy Pattern (Module 10) ✅ **IMPLEMENTED**
* **Interface:** `AttackStrategy`
* **Implementations:** `MeleeAttackStrategy`, `RangedAttackStrategy`
* **Implementation Details:**
    * Interface defines `execute()` method
    * Each strategy implements different attack behavior
    * Assigned to `GameCharacter` via `setAttackStrategy()`
* **Location:**
    * `com.kelompok2.frontend.strategies.AttackStrategy` (interface)
    * `com.kelompok2.frontend.strategies.MeleeAttackStrategy` (Ryze's scythe swing)
    * `com.kelompok2.frontend.strategies.RangedAttackStrategy` (Projectile shooting)
* **Function:**
    * Allows characters to change attack behavior dynamically
    * Lootable skills can change Q button behavior without modifying Player class
    * Different scaling formulas for different skills (ATK vs Arts scaling)

#### 8. Iterator Pattern (Module 4) 🔧 **IN USE (via Collections)**
* **Implementation:** Java Collections Framework
* **Usage:**
    * `Array<Projectile>` with iterator for update/collision loops
    * `Array<DummyEnemy>` with iterator for safe removal during iteration
    * `HashMap<String, Texture>` in AssetManager for cached textures
* **Location:** Throughout `GameScreen.java` collision detection and update loops
* **Function:**
    * Safe iteration with removal (using `Iterator.remove()`)
    * Prevents `ConcurrentModificationException`

#### 9. Template Method Pattern (Module 11) 🔧 **PARTIALLY IMPLEMENTED**
* **Class:** `GameCharacter` (abstract template)
* **Current Implementation:**
    * `update(delta)` method in GameCharacter provides template structure
    * Subclasses like `DummyEnemy` override and extend with specific behavior
    * Common timer updates handled in base class
* **Implementation Details:**
    * Define skeleton of character update algorithm in base class
    * Subclasses override specific steps
* **Planned Enhancement - Template Methods:**
    * Add more hook methods: `onLevelUp()`, `onDeath()`, `onSkillUse()`
    * Formalize the template structure with protected abstract/hook methods
* **Location:** `com.kelompok2.frontend.entities.GameCharacter`
* **Future Use:**
    * Common character behavior in base class
    * Specific behaviors (death effects, level-up bonuses) in subclasses
    * Reduces code duplication

#### 10. Facade Pattern (Module 11) ⏳ **PLANNED**
* **Class:** `GameFacade`
* **Implementation Details:**
    * Provides simplified interface to complex subsystems
    * Hides complexity from Main/Launcher classes
* **Subsystems to Wrap:**
    * GameManager (state management)
    * AssetManager (resource loading)
    * InputHandler (controls)
    * AudioManager (sound/music)
* **Future Use:**
    * Simplified game initialization: `GameFacade.initialize()`
    * Easier testing and maintenance
    * Reduced coupling between subsystems

---

### D. Design Pattern Summary

| Pattern | Status | Classes | Module |
|---------|--------|---------|--------|
| Singleton | ✅ Implemented | GameManager, AssetManager | 7 |
| Strategy | ✅ Implemented | AttackStrategy, MeleeAttackStrategy, RangedAttackStrategy | 10 |
| Factory Method | ✅ Implemented | EnemyFactory, EnemyType | 8 |
| Object Pool | ✅ Implemented | ProjectilePool, EnemyPool | 8 |
| Command | 🔧 Partial | InputHandler | 9 |
| Iterator | 🔧 In Use | Java Collections (Array, HashMap) | 4 |
| Template Method | 🔧 Partial | GameCharacter | 11 |
| Observer | ⏳ Planned | HUD System | 9 |
| State | ⏳ Planned | EntityState, GameState | 10 |
| Facade | ⏳ Planned | GameFacade | 11 |

**Total:** 10 Patterns (4 implemented, 3 partially/in-use, 3 planned)
