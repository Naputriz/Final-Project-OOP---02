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

#### Aelita - The Evergreen Healer ✨ NEW
* **Role:** Healing Attacker.
* **Stats:** High HP (140), Low ATK (15), Moderate Arts (30), Moderate DEF (20), Moderate Speed (170).
* **Basic Attack:** Ranged green projectiles (Arts scaling).
* **Innate Skill (Verdant Domain):** 
  - Costs 25% current HP to activate
  - Creates healing zone at activation point (150px radius, 5s duration)
  - Heals 50% max HP over 5 seconds (10% per second)
  - Grants +25% ATK and +25% Arts while inside zone
  - High-risk, high-reward healing mechanic
* **Cooldown:** 15 seconds.
* **Implementation Status:** ✅ Fully implemented as playable character

---

## 7. Implementation Status

### ✅ Completed Features

#### Core Systems
- **Character Selection Screen**
  - Character grid with portraits (6 characters: Ryze, Isolde, Insania, Blaze, Whisperwind, Aelita)
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

- **Settings System** ✨ NEW
  - **Reusable Settings Window:**
    - Accessible from Main Menu and Pause Menu
    - Music Volume Slider (0-100%)
    - SFX Volume Slider (0-100%)
    - Changes applied immediately via AudioManager
  
- **Pause System** ✨ NEW
  - **Enhanced Pause Menu:**
    - Triggered by ESC key or Alt-Tab/Focus Loss
    - **Visuals:** Game world freezes in background with gray overlay (no black screen)
    - **Options:** Resume, Restart, Settings, Character Select, Main Menu
    - **Consistency:** Uniform appearance regardless of trigger method


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
     - Idle: 4 frames (2×2 grid, 0.15s/frame)
     - Running: 10 frames (3x4 grid, 0.1s/frame)
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
     - Visual size: 128px

5. **Aegis - The Impenetrable Shield** ✅
   - Role: Tank
   - Stats: High HP (120), Low ATK (15), Low Arts (10), High Defence (25), Moderate Speed (130)
   - Basic Attack: Shield Bash (Scaling: 0.7 ATK + 0.5 DEF) with forward dash
   - Innate Skill (Shield Stance):
     - Blocks frontal damage (200-degree arc)
     - Reflects 50% damage back to attacker
     - Immobilizes player for 2 seconds
     - Visual: Red shield arc
   - State-based animations:
     - Idle: 4 frames (2x2 grid, 0.15s/frame)
     - Running: 10 frames (3x4 grid, 0.1s/frame)
   - Implementation Status: ✅ Fully implemented

6. **Whisperwind - The Silent Caster** ✅
   - Role: Arts Attacker
   - Stats: Moderate HP (100), Low ATK (15), High Arts (40), Moderate Defence (20), Moderate Speed (170)
   - Ranged Air Slash attacks
   - Hurricane Bind skill (wind ball knockback + stun, 10s cooldown)
   - Implementation Status: ✅ Fully implemented

7. **Lumi - The Pale Renegade** ✅ **NEW**
   - Role: Physical Attacker
   - Stats: Low HP (90), High ATK (45), Low Arts (10), Moderate Defence (15), High Speed (210)
   - Melee punch attacks with slash animation (manual click)
   - **Marking Mechanic:** Basic attacks apply 5-second mark to enemies and bosses
   - Returnious Pull skill (E key):
     - Pulls nearest marked enemy/boss to player
     - Deals 200% ATK damage on arrival
     - Stuns target for 1 second
     - Cooldown: 12 seconds
     - Works on both regular enemies AND bosses
   - **Special Mechanics:**
     - Pulled enemies don't deal collision damage during pull
     - Attack range: 120f, width: 100f (matching other melee characters)
   - **Design Pattern:** Uses `MarkingMeleeAttackStrategy` (Strategy Pattern extension)
   - Visual size: 128px
   - Implementation Status: ✅ Fully implemented

8. **Alice - The Reckless Princess** ✅ **NEW**
   - Role: Physical Attacker
   - Stats: Moderate HP (100), High ATK (40), Low Arts (10), Low Defence (10), High Speed (200)
   - Melee scratch attacks (Physical)
   - **Innate Skill (Feral Rush):**
     - Dashes forward rapidly (High speed)
     - Unleashes 5x scratch attacks in quick succession
     - Damage: 50% ATK per hit (Total 250%)
     - Cooldown: 5 seconds
   - Visual size: 128px
   - Implementation Status: ✅ Fully implemented

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
   - MeleeAttackStrategy (Ryze, Insania, Blaze, Aegis)
   - RangedAttackStrategy (Isolde, Aelita, Whisperwind)
   - **MarkingMeleeAttackStrategy** ✨ **NEW** - Extends MeleeAttackStrategy for Lumi
     - Adds mark application to melee attacks
     - Demonstrates Strategy Pattern extension via inheritance
     - Reuses attack positioning logic from parent class

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
   - Skills: VerdantDomainSkill, BladeFurySkill, HurricaneBindSkill, etc.

#### 7. Facade Pattern ✅ NEW
**Purpose:** Simplify complex system interactions
**Implementation:**
- **GameFacade:** Coordinate all subsystems (Rendering, Collision, Spawning, UI, Cinematic)
- Decouples GameScreen from specific system implementations
- Centralized update and render loops

**Benefits:**
- Reduced GameScreen complexity (2000+ lines -> ~350 lines)
- Better code organization
- Easier testing and maintenance

### 🚧 Current TODOs 

#### Lootable skill + selected character combo ✅ **IMPLEMENTED**
- **Feature:** Characters gain significant bonuses when using their signature skills (e.g. from loot/rewards).
  - **Fireball + Blaze:** Damage multiplier increases from 3.0x to 5.0x (Arts), Radius increases to 600px.
  - **Ice Shield + Isolde:** Damage reduction increases from 50% to 80%.
  - **Wind Dash + Whisperwind:** Deals damage and knockback at arrival point.
  - **Healing Wave + Aelita:** Healing increases from 10% to 20% Max HP.
  - **Frozen Apocalypse + Isolde:** Ultimate damage multiplier increases to 3.5x.
  - **Insanity Burst + Insania:** Ultimate duration increases to 10s, damage to 5.0x.
  - **Inferno Nova + Blaze:** Ultimate radius increases to 600px, damage to 6.0x.

#### Character System Scalability
- ~~**TODO:** Extract CharacterInfo as separate class~~ **(DONE)** ✅
  - Location: `CharacterSelectionScreen.java:350`
  - Current: Inner class in CharacterSelectionScreen
  - Goal: Standalone class for better organization

#### Skill Cooldown System Scalability
- **TODO:** Make skill cooldown detection more scalable
  - Location: `GameScreen.java:250-258, 278-311`
  - Current: instanceof checks for each character type
  - Goal: Use interface or base class method for skill cooldown access

#### HUD Bar Position Scalability
- ~~**TODO:** Make isPlayer check more maintainable~~ ✅ **COMPLETED**
  - **Solution:** Implemented `UISystem` with **Observer Pattern**.
  - **Details:** UI no longer polls `instanceof` checks. Instead, it listens for `HealthChangedEvent`, `XpChangedEvent`, etc.
  - **Benefit:** Adding new characters requires ZERO changes to UI code.

#### Backend Integration
- **TODO:** Send game data to backend on Game Over
  - Data to send: Level, Character Name, Time
  - Implementation: HTTP POST request to backend API

#### Enemy System Expansion
- ~~**TODO:** Support multiple enemy types beyond DummyEnemy~~ ✅ **COMPLETED**
  - Location: `EnemyFactory.java`, `EnemyPool.java`, `BaseEnemy.java`
  - Current: Full polymorphism implemented with `BaseEnemy`
  - Implemented Types:
    - **DummyEnemy:** Basic chaser (Red)
    - **FastEnemy:** High speed, low HP (Yellow)
    - **TankEnemy:** High HP, slow speed, large size (Green)
    - **RangedEnemy:** Ranged attacks, kiting behavior (Orange)
  - Bosses: Insania, Blaze, Isolde already added as playable ccharacters, implement as boss next

#### UI Responsiveness
- **TODO:** Test and adjust layout for different screen resolutions
  - Location: `CharacterSelectionScreen.java:35`
  - Current: Optimized for 1920x1080 only
  - Goal: Responsive layout for various resolutions

#### Game Balancing ✅ COMPLETED
- **Boss Balancing Issues** - All critical balance issues have been addressed:
  
  - **Insania (Boss):** Insanity debuff duration reduced ✅
    - **Previous:** 5 seconds of chaotic movement + friendly fire
    - **Issue:** Players lost control for too long, making fights frustrating
    - **Fix Implemented:** Reduced duration to 0.5 seconds for players (90% reduction)
    - **Status:** ✅ Balanced - Brief disruption without being a death sentence
    - **Note:** Only dangerous when player is directly on top of boss (which they shouldn't be)
  
  - **Isolde (Boss):** Movement speed and kiting behavior improved ✅
    - **Previous:** Isolde kited away constantly, melee characters lost 50%+ HP before reaching her
    - **Fixes Implemented:**
      - Reduced retreat speed from 120f to 80f when moving away
      - Added post-attack slow (0.5s at 50% speed) for engagement windows
      - Simplified AI for more predictable behavior
    - **Status:** ✅ Balanced - Melee characters can now catch and fight her effectively
  
  - **Blaze (Boss):** Hellfire Pillar warning system added ✅
    - **Previous:** Pillar spawned and dealt damage immediately with no warning
    - **Issue:** Player HP suddenly dropped, especially right after boss spawns
    - **Fixes Implemented:**
      - Added 0.5s warning phase before damage (visual circle appears instantly)
      - Increased damage multiplier to 35x Arts/second to compensate for warning delay
      - Pillar is now a serious threat that rewards dodging but not instant death
    - **Status:** ✅ Balanced - Fair warning system with significant damage for those who don't dodge
  
  - **Melee vs Boss General Balance:** Multiple improvements ✅
    - **Issues:** Unavoidable damage trading, tanks ineffective, rapid multi-hits
    - **Fixes Implemented:**
      - Improved Aegis DEF scaling from 1.0x to 1.5x (50% better mitigation)
      - Reduced boss melee damage multiplier from 1.5x to 1.0x (33% damage reduction)
      - Added per-attacker hit cooldown (0.2s) - prevents same enemy from multi-hitting
      - Allows multiple enemies to attack simultaneously while preventing rapid spam
    - **Status:** ✅ Balanced - All character types viable, melee characters can survive boss fights

  - **Enemy Level Scaling Tuned** ✅
    - **Issue:** Enemies became "bullet sponges" too quickly at higher levels.
    - **Adjustment:** Reduced stat scaling (HP/ATK) from 10% to **3% per level**.
    - **Result:** Smoother difficulty curve allowing for longer runs without enemies becoming unkillable.

#### New Features
- ~~**TODO:** Add Speed Buff to Level-Up Rewards~~ ✅ **COMPLETED**
  - Added `IncreaseSpeedEffect` (+10% speed).
  - Added `getSpeed()`/`setSpeed()` to `GameCharacter`.

- ~~**TODO:** Implement Passive Stat Growth~~ ✅ **COMPLETED**
  - Players now gain +2% Max HP and +1% ATK/Arts/DEF per level automatically.
  - Implemented in `GameCharacter.levelUp()`.

#### System Refactoring
- **TODO:** Refactor Character Selection in GameScreen ✅ **COMPLETED**
  - Location: `GameScreen.java` character initialization
  - Status: Implemented `CharacterFactory` to centralize character creation using Factory Pattern.
  - Benefits: Adding new characters only requires registering them in the factory, no code changes in GameScreen.

- ~~**TODO:** Implement Map Boundaries (Room System)~~ ✅ **COMPLETED**
  - Implemented `MapBoundarySystem`.
  - Map Size: **4000x4000**.
  - Player spawn randomized with safe padding (700 units) to ensure camera centering.
  - Camera clamps to boundaries to prevent viewing the void.

- ~~**TODO:** Add Visual Hitbox Indicators for Ultimate Skills~~ ✅ **COMPLETED**
  - **Mechanic:** Hold 'R' to preview range around player, Release to activate.
  - **Visuals:** 
    - **Inferno Nova:** Orange semi-transparent circle (400px radius around player).
    - **Insanity Burst:** Purple semi-transparent circle (500px radius around player).
    - **Frozen Apocalypse:** Screen-wide Cyan overlay.
  - Benefits: Clear area-of-effect feedback for self-centered skills.
  - Goal: Add visual circle/indicator showing ultimate skill range before and during activation
  - Implementation Suggestions:
    - Show semi-transparent circle at mouse position when R key is held
    - Display radius indicator during skill activation
    - Use different colors for different ultimates (purple for Insanity, red for Inferno, blue for Frozen)
  - Location: `RenderingSystem.java` or create `UltimateSkillRenderer`
  - Benefits: Better player feedback, more strategic ultimate usage

- **TODO:** Create Dedicated Sprites for New Enemy Types
  - **Priority:** Medium
  - Current: `FastEnemy` (Yellow), `TankEnemy` (Green), `RangedEnemy` (Orange) use tinted `DummyEnemy` sprites.
  - Goal: Create unique sprite assets for each enemy variant.
    - Fast: Sleek, agile design.
    - Tank: Bulky, armored design.
    - Ranged: Archer/Caster design.
  - location: `assets/Enemies/`

- **TODO:** Background Asset Search
  - **Priority:** High
  - Current: Using placeholder "FireflyPlaceholder.jpg"
  - Goal: Find/Create a high-quality map background asset (tiled or single large texture)
  - Requirements: 4000x4000 or seamless tileable texture
  - Theme: Dungeon / Frost / Chaos theme matching the game

- **TODO:** Future Feature Suggestions
  - **Minimap:** 
    - Essential for the new 4000x4000 map size.
    - Show player (Green dot), Enemies (Red dots), Boss (Skull icon).
  - **Floating Damage Numbers:**
    - Visual feedback when hitting enemies.
    - Color coding: White (Normal), Yellow (Crit), Blue (Arts).
  - **Save/Load System:**
    - Persist unlocked characters and high scores between sessions.

- ~~**TODO:** Resolution Consistency and Responsive UI~~ ✅ **COMPLETED**
  - **Problem Summary:** UI was hardcoded for 1920x1080, causing issues on other screens.
  - **Solution:**
    - `UISystem`: Refactored to use **Adaptive Scaling** (`uiScale = screenHeight / 720f`). HUD elements, fonts, and overlays now resize dynamically based on window height.
    - `CharacterSelectionScreen`: Uses `FitViewport` (1920x1080 virtual resolution) to ensure correct aspect ratio with letterboxing on all screens.
  - **Status:** ✅ Validated for 720p, 1080p, and arbitrary resolutions.

- ~~**TODO:** Expand Settings Menu Options~~ ✅ **PARTIALLY COMPLETED**
  - **Status:** Audio controls and access structure fully implemented. Keybinds/Resolution pending.
  - **Implemented Features:**
    - **Audio Controls:**
      - Separate sliders for Music and SFX volume
      - Accessible via "Settings" in Main Menu and Pause Menu
    - **Pause Menu Enhancement:**
      - Added pause menu (ESC key) with Resume, Settings, Restart, Character Select, Main Menu options
  - **Pending:**
    - Custom Keybinds
    - Resolution Change
    - Display Mode

- **TODO:** Quality of Life Improvements
  - ~~**Pause Menu Enhancement:**~~ ✅ **Done**
    - Add pause menu (ESC key) with Resume, Settings, Main Menu options
    - Currently game can only be paused via level-up screen
  - **Death Screen Improvements:**
    - Add stats summary on death (enemies killed, time survived, level reached)
    - Show comparison to previous best run
    - Add "Try Again" button for quick restart
  - **Enemy HP Bars:**
    - Status: ✅ **IMPLEMENTED** for regular enemies
    - TODO: Add HP bar for bosses (larger, more prominent)
  - **Damage Numbers:**
    - Show floating damage numbers when hitting enemies
    - Different colors for physical (red) vs arts (blue) damage
    - Critical hit indicators
  - **Minimap:**
    - Small minimap showing player position and nearby enemies
    - Especially useful if map boundaries are implemented
  - Benefits: Better player feedback, more engaging gameplay, improved UX

- **TODO:** Character Balancing
  - **Issue:** Arts/Ranged characters (Isolde, Whisperwind) easily defeat bosses but struggle with crowds (single-target attacks). Melee characters (Ryze, Lumi) clear crowds easily but struggle against bosses due to damage trading.
  - **Goal:** Balance effectiveness across different scenarios.
  - **Proposed Solutions:**
    - **Melee:** Add slight damage reduction or lifesteal vs bosses?
    - **Ranged:** Add small AoE to basic attacks or increase projectile size?
    - **Bosses:** Add specific mechanics to counter kiting (e.g., gap closers).

- **TODO:** Ultimate Skill QoL - Cancellation ✅ **COMPLETED**
  - **Issue:** One-time use ultimates can be wasted if accidentally pressed or if targets move.
  - **Goal:** Allow canceling the "Preview Mode" without firing.
  - **Implementation:** Right-click to cancel while holding 'R'.

#### 🐛 Bug Fixes (High Priority)
- ~~**TODO:** Fix Ryze's Spectral Body Skill~~ ✅ **FIXED**
  - **Issue:** Skill activates but player still takes damage (Invincibility not working).
  - **Fix:** Overridden `takeDamage(float, GameCharacter)` in `Ryze.java` to properly intercept damage calls from the collision system.
  - **Status:** ✅ Verified fix in code.

- ~~**TODO:** Fix Lumi's Skill Damage Counter~~ ✅ **FIXED**
  - **Issue:** Damage counter triggers on skill usage, should trigger only when enemy is pulled and damaged.
  - **Goal:** Provide accurate feedback to player.
  - **Status:** ✅ Moved event publishing to `GameCharacter.update()` on pull arrival.

- ~~**TODO:** Fix Stun Status Reset~~ ✅ **FIXED**
  - **Issue:** Enemies killed while stunned retain status when returned to pool.
  - **Fix:** Added `clearStun()` call in `BaseEnemy.reset()`.
  - **Status:** ✅ Verified fix in code.

- ~~**TODO:** Fix Pause Screen Transitions~~ ✅ **FIXED**
  - **Issue:** Quickly pausing/unpausing may cause unexpected screen switches (Main Menu, Restart).
  - **Goal:** Stabilize the state transition logic in `PauseScreen`.
  - **Status:** ✅ Fixed. Added `hasTransitioned` guard to prevent double-clicks/logic races.

- ~~**TODO:** Fix Camera Shift on Alt-Tab/Pause~~ ✅ **FIXED**
  - **Issue:** Camera moves away when window loses focus or game is paused, requiring manual correction.
  - **Goal:** Ensure camera position is saved/restored or locked to player during these events.
  - **Status:** ✅ Fixed. Added re-centering logic in `resize()` and `resumeFromPause()`.

- ~~**TODO:** Fix Visual Glitches on Pause~~ ✅ **FIXED**
  - **Issue:** Afterimages and "Hall of Mirrors" effect when dragging paused window.
  - **Fix:** Refactored `GameScreen.render` to always clear screen and render game world (frozen) even when paused.
  - **Status:** ✅ Verified fix.

- **TODO:** Suggestion: Visual Polish
  - **Goal:** Enhance game feel.
  - **Ideas:**
    - **Screen Shake:** Add slight shake on heavy hits or crits.
    - **Hit Flash:** Flash enemy sprite white when damaged.
    - **Damage Log:** (Optional) Text log of combat for debugging/clarity.

#### Game Balancing (New)
- **TODO:** Aegis Balance Improvements
  - **Skill Range:** Shield Stance currently only blocks frontal. Change to 360-degree protection or Omni-directional block/reflect.
  - **Basic Attack Hitbox:** Fix issue where enemies "on top" of Aegis (point-blank) are missed.
    - *Technical Note:* `MeleeAttackStrategy` starts hitbox at `edgeOffset + 5px`. Needs to start closer to center or include character bounds.

#### UI System Improvements
- **TODO:** Implement Observer Pattern for UI Bars ✅ **COMPLETED**
  - Current: `UISystem` manually checks player stats and renders bars at world coordinates above character.
  - Goal: Decouple UI from game logic and move UI to a HUD (Head-Up Display) layout.
  - Implementation:
    - `GameCharacter` publishes events (HealthChanged, XPChanged, CooldownChanged).
    - `UISystem` subscribes to these events (Observer).
    - Render bars at fixed screen coordinates (top-left) instead of world coordinates.
  - Benefits: Better performance (no polling), cleaner code, professional HUD look.

#### Known Issues & Balancing (TODO)
- ~~**TODO:** Leaderboard Filter Fix~~ ✅ **FIXED**
  - **Issue:** Alice currently doesn't show up on leaderboard filter options.
  - **Status:** ✅ Added Alice to filter list.

- ~~**TODO:** Boss Balancing Adjustments~~ ✅ **COMPLETED**
  - **Issue:** Bosses deal too much damage (one-shot potential) and die too quickly (DPS race).
  - **Goal:** Increase Boss HP (longer fight) and Decrease Boss Damage (fairer fight).
  - **Changes Implemented (Boss Insania):**
    - **HP:** 500 -> 1000 (Base), +50 -> +100 (Per Level).
    - **ATK:** 25 -> 15 (Base), +2.0 -> +1.5 (Per Level).
    - **ARTS:** 35 -> 25 (Base), +3.0 -> +2.5 (Per Level).
  - **Changes Implemented (Boss Blaze):**
    - **HP:** 450 -> 900 (Base), +45 -> +90 (Per Level).
    - **ATK:** 20 -> 15 (Base), +2.0 -> +1.5 (Per Level).
    - **ARTS:** 60 -> 40 (Base), +5.0 -> +3.5 (Per Level).
  - **Changes Implemented (Boss Isolde):**
    - **HP:** 400 -> 800 (Base), +40 -> +80 (Per Level).
    - **ATK:** 8 -> 6 (Base), +0.8 -> +0.6 (Per Level).
    - **ARTS:** 15 -> 12 (Base), +1.5 -> +1.2 (Per Level).

- **TODO:** Skill Visual Strategy Refactor
  - **Issue:** Some skills (Ground Slam, Wind Dash Combo) use `MeleeAttackStrategy` for visuals which looks wrong (slash animations for impact effects).
  - **Goal:** Create dedicated visual effects or `ImpactStrategy` for these skills to separate mechanics from "Slash" animations.


#### Code Quality Improvements
- **TODO:** Extract Max HP Percentage Maintenance Logic
  - Status: ✅ **IMPLEMENTED** 
  - Location: `GameCharacter.setMaxHp()` method
  - Current: When max HP increases, current HP percentage is maintained
  - Example: Player at 90/100 HP (90%) gets +10% max HP → becomes 99/110 HP (90%)

- ~~**TODO:** Refactor Skill System for Consistency~~ ✅ **FULLY IMPLEMENTED**
  - All innate skills are now separate skill classes extending `BaseSkill`
  - Implemented skills: `SpectralBodySkill`, `HellfirePillarSkill`, `MindFractureSkill`, `GlacialBreathSkill`, `VerdantDomainSkill`, `ShieldStanceSkill`, `ReturniousPullSkill`, `HurricaneBindSkill`
  - Secondary skills: `BladeFurySkill`, `FireballSkill`, `IceShieldSkill`, `WindDashSkill`, `HealingWaveSkill`, etc.
  - Benefits: Single Responsibility Principle achieved, easier testing and maintenance

- ~~**TODO:** Improve Collision System Modularity~~ ✅ **FULLY IMPLEMENTED**
  - Collision system fully modularized into separate handler classes:
    - `PlayerCollisionHandler` - Player vs enemies/bosses collision
    - `ProjectileCollisionHandler` - Projectile collision detection
    - `SkillCollisionHandler` - Skill-specific collision (AoE, zones, etc.)
    - `CollisionSystem` - Coordinates all collision handlers
    - `SkillCollisionHandler` - Skill-specific collision (AoE, zones, etc.)
    - `CollisionSystem` - Coordinates all collision handlers
  - Benefits: Excellent separation of concerns, highly maintainable

- ~~**TODO:** Refactor Enemy System to Polymorphism~~ ✅ **FULLY IMPLEMENTED**
  - Created `BaseEnemy` abstract class extending `GameCharacter`.
  - Refactored `DummyEnemy`, `FastEnemy`, `TankEnemy`, `RangedEnemy` to extend `BaseEnemy`.
  - Updated `EnemyPool` to manage strictly typed pools for each enemy variant.
  - Updated `EnemyFactory` to spawn enemies based on Probability Distribution at current level.
  - Benefits: EASILY extensible enemy roster, centralized AI logic.

### 📋 Pending Features (From Original GDD)

#### Characters Not Yet Implemented
- ~~Whisperwind - The Silent Caster~~ ✅ **IMPLEMENTED**
- ~~Aelita - The Evergreen Healer~~ ✅ **IMPLEMENTED**
- ~~Aegis - The Impenetrable Shield~~ ✅ **IMPLEMENTED**
- ~~Lumi - The Pale Renegade~~ ✅ **IMPLEMENTED**
- ~~Alice - The Reckless Princess~~ ✅ **IMPLEMENTED**
- Alex - The Calculating Prince
- Raiden - The Speed Demon
- Artorias - King of Lumina
- Funami - The White Raven

#### Boss System ✅ **PARTIALLY IMPLEMENTED**
- ✅ Boss spawning every 5 minutes
- ✅ Boss "ultimate" looting mechanic  
- ✅ Boss cinematic camera pan and zoom on spawn
- ✅ Boss-specific music themes
- ✅ Boss AI and attack patterns
- ✅ Insania as boss (playable character available)
- ✅ Blaze as boss (playable character available)
- ✅ Isolde as boss (playable character available)
- ⏳ Boss-to-playable-character unlocking (pending backend integration)

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

---

## 9. Deep Design Pattern Analysis

### ✅ 9.1 Implemented Patterns (By Module)

#### Modul 4: Generic Type, Collection, and Iterator Pattern
*   **Where it's used:**
    *   **Generics:** Used in `GameEventManager` (`subscribe<T>`), `ObjectPool<T>`, and LibGDX `Array<T>`.
    *   **Collections:** `HashMap` for `attackerCooldowns` in `GameCharacter`, `ArrayList` for event listeners.
    *   **Iterator:** Explicit `Iterator` usage in `GameCharacter.update()` to safely remove expired cooldowns from the `attackerCooldowns` map while iterating.
*   **Why:** Ensures type safety and allows flexible data structure manipulation without runtime type errors.

#### Modul 7: Singleton Pattern
*   **Where it's used:**
    *   **`GameManager`:** Manages global game state (score, level).
    *   **`AssetManager`:** Centralized loading/caching of textures and sounds.
    *   **`AudioManager`:** Controls BGM/SFX playback globally.
    *   **`GameEventManager`:** Global event bus for system communication.
*   **Why:** Ensures exactly one instance manages these shared resources, preventing conflicts (e.g., trying to load the same asset twice) and providing a global access point.

#### Modul 8: Factory Method and Object Pool Pattern
*   **Factory Method:**
    *   **`CharacterFactory`:** Centralizes player character creation. Replaces complex `switch-case` logic in `GameScreen`.
    *   **`EnemyFactory`:** (Planned/Implicit) Spawning logic creates specific enemy types based on level.
*   **Object Pool:**
      - **`ProjectilePool`:** Reuses `Projectile` objects.
    - **`EnemyPool`:** Reuses `BaseEnemy` objects (Maintains separate pools for Dummy, Fast, Tank, Ranged).
*   **Why:**
    *   **Factory:** Decouples object creation from usage. Adding a new character doesn't break the game loop codebase.
    *   **Pool:** Eliminates lag spikes from Garbage Collection by reusing memory instead of constantly creating/destroying thousands of bullets/enemies.

#### Modul 9: Command and Observer Pattern
*   **Command Pattern:**
    *   **`Skill` / `BaseSkill`:** Each skill is an object with an `activate()` method. Allows skills to be queued, swapped, or executed generically.
    *   **`LevelUpEffect`:** Effects like "Increase HP" are commands executed upon selection.
*   **Observer Pattern:**
    *   **`GameEventManager`:** Acts as the Subject.
    *   **`UISystem`, `GameFacade`:** Act as Observers.
    *   **Usage:** When a boss is defeated, `GameFacade` publishes `BossDefeatedEvent`. `UISystem` listens and shows the victory text.
*   **Why:**
    *   **Command:** Encapsulates "actions" as objects, allowing flexible skill systems (e.g., picking up a random skill).
    *   **Observer:** Decouples systems. The Boss logic doesn't need to know the UI exists; it just fires an event.

#### Modul 10: State and Strategy Pattern
*   **State Pattern:**
    *   **Animation System:** `IdleState`, `RunningState`. The character delegates animation logic to the current state object.
*   **Strategy Pattern:**
    *   **`AttackStrategy`:** `MeleeAttackStrategy`, `RangedAttackStrategy`, `MarkingMeleeAttackStrategy`.
    *   **Usage:** Characters delegate *how* they attack to their strategy. Lumi uses a special strategy that applies a "Mark" on hit.
*   **Why:**
    *   **State:** Prevents massive `if-else` chains for animation (e.g., `if (running) ... else if (jumping)...`).
    *   **Strategy:** Allows swapping, extending, or reusing attack behaviors without modifying the character class (Composition over Inheritance).

#### Modul 11: Template Method and Facade Pattern
*   **Template Method:**
    *   **`BaseSkill` (Implicit):** The `activate()` method (Skeleton) handles cooldown checks and logging, then calls the abstract `executeSkill()` (Implementation) which subclasses must define.
*   **Facade Pattern:**
    *   **`GameFacade`:** Wraps `RenderingSystem`, `CollisionSystem`, `SpawningSystem`, `UISystem`.
*   **Why:**
    *   **Template:** Guarantees that cooldowns are *always* checked before any skill executes, preventing duplicate code in every skill.
    *   **Facade:** The `GameScreen` is clean and readable. It calls `facade.update()` instead of manually managing 5 different systems and their dependencies.

---

### 🚀 9.2 Potential Future Patterns (How to Implement)

#### 1. Decorator Pattern (For Status Effects)
*   **Current State:** Status effects (Frozen, Stunned, Insane) are boolean flags inside `GameCharacter`. This violates OCP (Open-Closed Principle) because adding a "Burn" effect requires modifying `GameCharacter`.
*   **How to Implement:**
    *   Create `CharacterDecorator` abstract class implementing `GameCharacter`.
    *   Create `FrozenCharacter`, `BurningCharacter` wrappers.
    *   **Usage:** `player = new FrozenCharacter(player);`
    *   **Benefit:** Dynamically stack infinite effects (Burning + Frozen) without touching the `GameCharacter` class.

#### 2. Builder Pattern (For Complex Entities)
*   **Current State:** Bosses/Levels are created with long constructors.
*   **How to Implement:**
    *   Create `BossBuilder`.
    *   `new BossBuilder().setName("Insania").setHp(5000).addSkill(new MindFracture()).build();`
*   **Benefit:** Readable creation code, easier to create variations of bosses/enemies.

#### 3. Flyweight Pattern (For Enemy Data)
*   **Current State:** Each `DummyEnemy` stores its own `texture`, `maxHp` stats, etc. If there are 1000 enemies, that's 1000 duplicate float values and texture references.
*   **How to Implement:**
    *   Create `EnemyTypeData` (Flyweight) storing shared data (Texture, MaxHP, DEF).
    *   `DummyEnemy` only stores specific state (CurrentHP, Position) and a reference to `EnemyTypeData`.
*   **Benefit:** Significant memory savings for large hordes of enemies.

#### 4. Prototype Pattern (For Spawning)
*   **Current State:** Factory creates new instances.
*   **How to Implement:**
    *   Create a "prototype" enemy instance.
    *   When spawning, call `prototype.clone()`.
    *   (Note: `Skill.copy()` already implements this for skills).
*   **Benefit:** Efficiently spawn pre-configured enemy variants without hardcoding factory logic for every variation.

#### 5. Mediator Pattern (For UI-Game Logic)
*   **Current State:** `GameEventManager` handles events, but UI sometimes polls data directly from Player.
*   **How to Implement:**
    *   Create `UIMediator`.
    *   UI components only talk to Mediator. Mediator talks to Player/Game components.
*   **Benefit:** Complete decoupling. The UI becomes a separate "skin" that can be swapped entirely.

---

## 10. Future Improvements and TODOs

### 🔧 Pending Bug Fixes and Enhancements

#### 1. Max HP Buff HP Recovery ✅ **FIXED**
**Priority:** Done
**Description:** Logic to maintain HP percentage when Max HP increases has been implemented in `GameCharacter.setMaxHp()`.

---

#### 2. Blade Fury Attack Animation ✅ **FIXED**
**Priority:** Done
**Description:** Blade Fury now spawns randomized melee attacks around the player instead of static sprites.
**Status:** Implemented dynamic spawning of `MeleeAttack` entities with random rotation and positioning.

---

#### 3. Collision System Refactoring ✅ **COMPLETED**
**Priority:** Done
**Description:** `CollisionSystem.java` was handling too much logic.

---

#### 4. Bug Fix: Lumi Pulling Unmarked Enemies ✅ **COMPLETED**
**Priority:** High
**Description:** Lumi's Returnious Pull skill sometimes pulls enemies that are not currently marked.
**Suspected Cause:** Object Pooling issue. Enemies returned to pool might retain their "marked" status. When respawned, they are treated as marked immediately.
**Action:** Ensure `isMarked` status is reset when enemy is returned to pool or spawned.

---

#### 5. Feature: Main Menu Revamp
**Priority:** Medium
**Description:** Current Main Menu is basic. Needs a visual overhaul to match the game's aesthetic.
**Action:** Redesign UI layout, add better background, improve buttons.

---

#### 6. Feature: Handle Multiple Level Ups ✅ COMPLETED
**Priority:** Done
**Description:** Bosses provide massive XP, potentially causing multiple level-ups at once. Currently, the game only grants one buff selection even if XP overflows for multiple levels.
**Desired Behavior:**
- Sequential level-ups: If player gains enough XP for 2 levels, they should select a buff, then immediately level up again and select another buff.
- Or simply ensure XP overflow is handled correctly so the next level-up triggers immediately after the first interaction.
**Status:** Fixed. XP overflow is now handled correctly. If a player gains enough XP for multiple levels (e.g., from a boss kill), the level-up screen will appear sequentially for each level gained until the XP is exhausted.

---

#### 7. Bug Fix: Ranged Enemies Don't Fire ✅ **FIXED**
**Priority:** Done
**Description:** Ranged enemies were spawning but not firing projectiles at the player.
**Fix:** Injected `ProjectilePool` into `EnemyPool` and `RangedEnemy`. Added logic to set projectile ownership (`isEnemyProjectile`) to prevent self-damage.
**Status:** ✅ Enemies shoot player correctly without hitting themselves.

---

#### 8. Bug Fix: Enemy Pooling Stat Reset ✅ **FIXED**
**Priority:** Done
**Description:** Enemies (e.g., FastEnemy) sometimes retain stats from other types (e.g., TankEnemy HP) when reused from pool.
**Fix:** Implemented `baseMaxHp`, `baseAtk`, `baseArts` in `BaseEnemy`. `reset()` now restores these base values before scaling logic is applied.
**Status:** ✅ Verified fix in code.

---

#### 9. Bug Fix: Ryze Skill vs Boss
**Priority:** High
**Description:** Ryze's `SpectralBodySkill` (Invulnerability) does not prevent damage from Boss attacks.
**Action:** Ensure `takeDamage` override handles Boss damage sources correctly.

---

#### 10. Bug Fix: Damage Numbers vs Boss
**Priority:** Medium
**Description:** Damage numbers (floating text) do not appear when attacking Bosses with skills.
**Action:** Verify event publishing in `SkillCollisionHandler` for Bosses.

---

#### 11. Balance: Melee Attack Hitbox
**Priority:** High
**Description:** Melee attacks require "slight range" and miss enemies directly on top of the player (point-blank).
**Action:** Adjust `MeleeAttackStrategy` or `MeleeAttack` hitbox to include the character's own center/bounds.

---

#### 12. Bug Fix: Ground Slam No Stun
**Priority:** High
**Description:** Ground slam attacks currently deal damage but fail to apply the intended stun effect.
**Action:** Investigate `SkillCollisionHandler` or `GroundSlamSkill` to ensure stun status is applied to affected enemies.

#### 13. UI: Level Up Stats Display
**Priority:** Medium
**Description:** Player cannot see their current stats (HP, ATK, etc.) while selecting a level-up upgrade, making it hard to choose relevant buffs.
**Action:** Display current character stats on the Level Up screen/overlay.

#### 14. UI: Level Indicator on HUD
**Priority:** Medium
**Description:** Current level is not clearly visible on the main HUD or Level Up bar.
**Action:** Add a clear Level indicator (e.g., "Lv. 5") to the XP bar or HUD.

---

#### 8. Refactor: Character Selection Screen Scalability
**Priority:** Medium
**Description:** The validation and initialization logic in `CharacterSelectionScreen.java` (including `InitializeCharacters`) is becoming bloated and hard to maintain as more characters are added.
**Action:** Refactor `InitializeCharacters` to be more scalable, potentially moving character data definitions to a separate configuration file (JSON/XML) or a `CharacterDefinition` class/factory pattern.

---

#### 3. GameScreen Refactoring ✅ COMPLETED
**Priority:** Done
**Description:** `GameScreen.java` was refactored using Facade Pattern.
**Status:** Implemented `GameFacade` to coordinate `RenderingSystem`, `CollisionSystem`, `SpawningSystem`, `UISystem`, and `BossCinematicSystem`. Line count reduced from ~2000 to ~350.

---

**📄 See design patterns documentation for detailed implementations**

---

## 11. Technical Implementation Details

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
