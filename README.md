# JiokyeTanks-Multiplayer

## Overview
Tank Battle Multiplayer game with two implementations:

### TankBattleMultiplayer (Original)
The original production Android Java game built with Android SDK and Gradle.

### GodotV2 (Version 2 - Godot 4 Recreation)
A recreation of the original game using the Godot 4 engine with GDScript.

**Features recreated:**
- 26×26 grid-based game board matching original stage layouts
- 36 levels loaded from original stage data files
- Player tank with movement, grid-snapping, and ice slippage
- 4 enemy tank types (A/B/C/D) with AI targeting
- HVE boss enemy with health system
- Bullet system with directional firing and collision
- Destructible brick walls with directional damage
- Indestructible stone walls (breakable with power-up)
- Water, ice, and bush terrain types
- Eagle base protection objective
- 10 bonus/power-up types with spawn probabilities matching original
- Mine/bomb mechanic
- Shield system
- Curtain stage transition animation
- Score system with per-enemy-type kill tracking
- Game state management (stage complete, game over, pause)
- HUD with score, lives, stage, and enemy count

**Controls:**
- WASD or Arrow Keys: Move tank
- Space or Enter: Fire
- B: Drop mine
- P or Escape: Pause

**Running:**
1. Install Godot 4.2+
2. Open the `GodotV2/` folder as a Godot project
3. Run the project