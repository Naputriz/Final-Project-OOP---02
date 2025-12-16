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
* **Implementation Status:** ✅ Fully implemented as playable character

#### Blaze - The Flame Kaiser
* **Role:** Arts Attacker.
* **Stats:** Moderate HP, Moderate ATK, High Arts, Low Defence, Moderate Speed.
* **Basic Attack:** Flame Punch (Scaling: 0.7 ATK, 0.3 Arts).
* **Innate Skill (Hellfire Pillar):** Summons a pillar at the cursor location, dealing high damage (Arts × 2.0/sec).
* **Cooldown:** 5 seconds.
* **Duration:** 2 seconds active.
* **Radius:** 40 pixels (80px diameter).
* **Implementation Status:** ✅ Fully implemented as playable character

#### Isolde - The Frost Kaiser
* **Role:** Arts Attacker.
* **Stats:** Moderate HP, Low ATK, High Arts, Moderate Defence, Moderate Speed.
* **Basic Attack:** Shoot icicles.
* **Innate Skill (Glacial Breath):** Shoots in a cone hitbox, freezing enemies on hit (Duration: 3 seconds or until hit again).
* **Cooldown:** 10 seconds.
* **Implementation Status:** ✅ Fully implemented as playable character

---

## 7. Implementation Status

### ✅ Completed Features

#### Core Systems
- **Character Selection Screen**
  - Character grid with portraits (4 characters: Ryze, Isolde, Insania, Blaze)
  - Character preview with animated sprites
  - Stats panel displaying HP, ATK, Arts, DEF, Speed
  - Skill panel showing innate skill description
  - Mouse hover detection for character preview
  - Click to select and start game
  - Centered UI layout optimized for 1920x1080 fullscreen

- **Main Menu**
  - Basic menu structure with Start, Settings, Exit options
  - Title display
  - Navigation to character selection
  - Background music (Menu BGM)

- **Gameplay Systems**
  - Top-down 2D combat
  - Player movement (WASD)
  - Basic attack system (Left Click) with auto-attack
  - Innate skills (E key) with cooldown tracking
  - Enemy spawning and AI
  - Collision detection
  - Health and XP systems
  - Level-up progression with effect selection screen

- **Audio System** ✨ NEW
  - AudioManager (Singleton pattern)
  - Background music system (BGM)
    - Main Menu BGM
    - Battle BGM
  - Sound effects (SFX)
    - Game Over sound effect
  - Volume control
  - Music state management (play, stop, pause, resume)
  - Asset caching for audio files

- **Animation System** ✨ NEW
  - State Pattern implementation
    - IdleState for stationary characters
    - RunningState for moving characters
  - Automatic state transitions based on movement
  - Support for multi-frame sprite sheets
  - Configurable frame duration

- **Attack Sprite Animations** ✨ NEW
  - Slash animation (for Ryze, Blaze)
  - Scratch animation (for Insania)
  - 360° rotation based on mouse direction
  - Vertical flip for left-facing attacks to maintain correct orientation
  - 7 frames per animation (3×3 grid layout)

#### Playable Characters (4 Total)

1. **Ryze - The Ghost of Insania** ✅
   - Role: Physical Attacker
   - Stats: Moderate HP (100), High ATK (35), Low Arts (10), Low Defence (15), High Speed (200)
   - Melee scythe attacks with slash animation
   - Spectral Body skill (3s invulnerability, 15s cooldown)
   - State-based animations:
     - Idle: 8 frames (3×3 grid)
     - Running: 10 frames (3×4 grid)
   - Visual size: 128px

2. **Isolde - The Frost Kaiser** ✅
   - Role: Arts Attacker
   - Stats: Moderate HP (100), Low ATK (15), High Arts (40), Moderate Defence (20), Moderate Speed (180)
   - Ranged icicle attacks
   - Glacial Breath skill (cone freeze, 10s cooldown)
   - Animated sprite (10×10 frames)
   - Visual size: 128px

3. **Insania - The Chaos Kaiser** ✅
   - Role: Physical/Arts Hybrid
   - Stats: Moderate HP (115), High ATK (30), Moderate Arts (25), Low Defence (10), Moderate Speed (180)
   - Melee Mad Claw attacks with scratch animation (auto-attack enabled)
   - Mind Fracture skill (AoE insanity debuff, damage: Arts × 0.5, 10s cooldown)
   - State-based animations:
     - Idle: 4 frames (2×2 grid)
   - Visual size: 128px
   - Bonus damage to insane enemies

4. **Blaze - The Flame Kaiser** ✅ NEW
   - Role: Arts Attacker
   - Stats: Moderate HP (110), Moderate ATK (25), High Arts (40), Low Defence (5), Moderate Speed (180)
   - Hybrid damage basic attack: Flame Punch (0.7 ATK + 0.3 Arts)
   - Melee attacks with slash animation (auto-attack enabled)
   - Hellfire Pillar skill (E key):
     - Cursor-targeted placement
     - Continuous AoE damage (Arts × 2.0/sec)
     - Duration: 2 seconds
     - Cooldown: 5 seconds
     - Radius: 40 pixels
     - Visual: Orange semi-transparent circle
   - State-based animations:
     - Idle: 92 frames (4×23 grid)
     - Running: 92 frames (4×23 grid)
   - Visual size: 128px

#### UI/UX Features
- **HUD System:**
  - HP bar (green) - Top position, furthest from character
  - Skill Cooldown bar (yellow-orange) - Middle position
  - XP bar (cyan) - Bottom position, closest to character
  - All bars properly positioned at +25, +19, +13 offsets respectively
- **Fullscreen Mode:** Game runs in native fullscreen (1920x1080)
- **Centered Character Selection:** All UI elements properly centered for widescreen displays
- **Level-Up Screen:**
  - Pause game on level-up
  - Display 3 random effect cards
  - Hover and click to select effect
  - Semi-transparent overlay
  - Effect descriptions

#### Design Patterns Implemented

1. **Singleton Pattern** ✅
   - GameManager - Central game state management
   - AssetManager - Centralized asset loading and caching
   - AudioManager - Audio playback and volume control

2. **Strategy Pattern** ✅
   - AttackStrategy interface
   - MeleeAttackStrategy (Ryze, Insania, Blaze)
   - RangedAttackStrategy (Isolde)

3. **State Pattern** ✅ NEW
   - AnimationState interface
   - IdleState - For stationary characters
   - RunningState - For moving characters
   - Automatic transitions based on movement

4. **Factory Method Pattern** ✅
   - EnemyFactory - Enemy type selection based on level
   - EnemyType enum for different enemy varieties

5. **Object Pool Pattern** ✅
   - ProjectilePool - Reuses projectile instances
   - EnemyPool - Reuses enemy instances
   - Automatic return to pool on death/deactivation

6. **Command Pattern** ✅
   - LevelUpEffect interface
   - IncreaseMaxHPEffect, IncreaseAtkEffect, IncreaseArtsEffect
   - IncreaseDefenseEffect, RecoverHPEffect, NewSkillEffect

### 🚧 Current TODOs 

#### Character System Scalability
- **TODO:** Extract CharacterInfo as separate class
  - Location: `CharacterSelectionScreen.java:350`
  - Current: Inner class in CharacterSelectionScreen
  - Goal: Standalone class for better organization

#### Skill Cooldown System Scalability
- **TODO:** Make skill cooldown detection more scalable
  - Location: `GameScreen.java:250-258, 278-311`
  - Current: instanceof checks for each character type
  - Goal: Use interface or base class method for skill cooldown access

#### HUD Bar Position Scalability
- **TODO:** Make isPlayer check more maintainable
  - Location: `GameScreen.java:253`
  - Current: `instanceof Ryze || instanceof Isolde || instanceof Insania || instanceof Blaze`
  - Goal: Use marker interface or base class property

#### Backend Integration
- **TODO:** Send game data to backend on Game Over
  - Data to send: Level, Character Name, Time
  - Implementation: HTTP POST request to backend API

#### Enemy System Expansion
- **TODO:** Support multiple enemy types beyond DummyEnemy
  - Location: `EnemyFactory.java`
  - Current: Only DummyEnemy implemented
  - Planned: FastEnemy, TankEnemy, RangedEnemy
  - Bosses: Insania, Blaze, Isolde already added as playable ccharacters, implement as boss next

#### UI Responsiveness
- **TODO:** Test and adjust layout for different screen resolutions
  - Location: `CharacterSelectionScreen.java:35`
  - Current: Optimized for 1920x1080 only
  - Goal: Responsive layout for various resolutions

### 📋 Pending Features (From Original GDD)

#### Characters Not Yet Implemented
- ~~Whisperwind - The Silent Caster~~ ✅ **IMPLEMENTED**
- Aelita - The Evergreen Healer
- Aegis - The Impenetrable Shield
- Lumi - The Pale Renegade
- Alice - The Reckless Princess
- Alex - The Calculating Prince
- Raiden - The Speed Demon
- Artorias - King of Lumina
- Funami - The White Raven

#### Boss System Not Yet Implemented
- Boss spawning every 5 minutes
- Boss "ultimate" looting mechanic
- Boss-to-playable-character unlocking
- Insania as boss (currently only playable)
- Blaze as boss (currently only playable)
- Isolde as boss (currently only playable)

#### Systems Not Yet Implemented
- Secondary skill slot (Q key)
- Skill looting from enemies/bosses
- Settings menu (keybinds, audio controls)
- Backend score submission
- Leaderboard system
- Login system

#### Recommended Future Design Patterns
1. **Observer Pattern** - For HUD event system (decouple UI from game logic)
2. **Template Method Pattern** - For character initialization process
3. **Decorator Pattern** - For buff/debuff system
4. **Builder Pattern** - For character configuration from data files
5. **Facade Pattern** - For game service unification

**📄 See design patterns documentation for detailed implementations**

---

## 8. Technical Implementation Details

### Audio Assets
- **BGM (Music):**
  - `MainMenuTheme.mp3` - Main menu background music
  - `BattleTheme.mp3` - In-game battle music
- **SFX (Sound Effects):**
  - `GameOver.wav` - Game over sound effect

### Visual Assets
- **Character Spritesheets:**
  - `RyzeStillplaceholder.png` (Idle: 3×3, 8 frames)
  - `RyzeRunPlaceholder.png` (Run: 3×4, 10 frames)
  - `InsaniaIdlePlaceholder.png` (Idle: 2×2, 4 frames)
  - `BlazeCharacterPlaceholder.png` (Idle/Run: 4×23, 92 frames)
- **Attack Animations:**
  - `Slash Animation.png` (7 frames, 3×3 grid) - Used by Ryze, Blaze
  - `Scratch Animation.png` (7 frames, 3×3 grid) - Used by Insania

### Architecture Highlights
- **Separation of Concerns:** Managers handle specific domains (Audio, Assets, Game State)
- **Loose Coupling:** Strategy and State patterns allow runtime behavior changes
- **Resource Efficiency:** Object pooling prevents memory spikes during gameplay
- **Scalable Commands:** Level-up effects as self-contained commands

---
