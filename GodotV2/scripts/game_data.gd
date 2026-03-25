extends Node

# Game constants matching the original Java CONST.java

# Directions
enum Direction { UP = 0, RIGHT = 1, DOWN = 2, LEFT = 3 }

# Game settings
const FPS: int = 32
const NUM_LEVELS: int = 35
const NUM_ENEMIES: int = 20
const MAX_ENEMIES: int = 6
const MAX_GAME_COUNT: int = 5
const MAX_BUILDER: int = 10
const NUM_OBJECTIVES: int = 11
const GRID_SIZE: int = 26
const MAX_HVE: int = 8

# Tile dimensions (will be calculated based on viewport)
var tile_dim: float = 0.0
var board_width: float = 0.0
var board_height: float = 0.0
var board_offset: Vector2 = Vector2.ZERO

# Object types
enum ObjectType {
	ST_TANK, ST_TANK_A, ST_TANK_B, ST_TANK_C, ST_TANK_D, ST_HVE,
	ST_PLAYER_1, ST_PLAYER_2,
	ST_BRICK_WALL, ST_STONE_WALL, ST_WATER, ST_BUSH, ST_ICE,
	ST_BONUS_GRENADE, ST_BONUS_HELMET, ST_BONUS_CLOCK, ST_BONUS_SHOVEL,
	ST_BONUS_TANK, ST_BONUS_STAR, ST_BONUS_GUN, ST_BONUS_BOAT,
	ST_SHIELD, ST_BULLET, ST_EAGLE, ST_MINE, ST_FIRE, ST_BOMB,
	ST_BOAT_P1, ST_BOAT_P2, ST_FLAG, ST_CURTAIN,
	ST_CREATE, ST_DESTROY_TANK, ST_DESTROY_BULLET,
	ST_LEFT_ENEMY, ST_STAGE_STATUS, ST_TANKS_LOGO, ST_GOLD
}

# Bonus types
enum BonusType {
	GRENADE = 0, HELMET = 1, CLOCK = 2, SHOVEL = 3, TANK = 4,
	STAR = 5, GUN = 6, BOAT = 7, MINE = 8, BUILDER = 9
}

# Bonus spawn probabilities (matching Java Bonus.java)
const BONUS_PROBABILITIES: Array = [
	0.10,  # GRENADE
	0.15,  # HELMET
	0.10,  # CLOCK
	0.15,  # SHOVEL
	0.07,  # TANK
	0.10,  # STAR
	0.07,  # GUN
	0.15,  # BOAT
	0.06,  # MINE
	0.05   # BUILDER
]

# Enemy scores
const ENEMY_SCORES: Dictionary = {
	ObjectType.ST_TANK_A: 100,
	ObjectType.ST_TANK_B: 200,
	ObjectType.ST_TANK_C: 300,
	ObjectType.ST_TANK_D: 400,
	ObjectType.ST_HVE: 400,
}

# Enemy speed multipliers
const ENEMY_SPEEDS: Dictionary = {
	ObjectType.ST_TANK_A: 0.6,
	ObjectType.ST_TANK_B: 1.1,
	ObjectType.ST_TANK_C: 1.0,
	ObjectType.ST_TANK_D: 1.0,
}

# Colors
const COLOR_BRICK: Color = Color(0.666, 0.290, 0.267)
const COLOR_STONE: Color = Color(0.7, 0.7, 0.7)
const COLOR_WATER: Color = Color(0.0, 0.4, 0.8)
const COLOR_BUSH: Color = Color(0.0, 0.5, 0.0)
const COLOR_ICE: Color = Color(0.7, 0.85, 0.95)
const COLOR_PLAYER1: Color = Color(1.0, 0.85, 0.0)
const COLOR_PLAYER2: Color = Color(0.0, 0.85, 0.0)
const COLOR_ENEMY_A: Color = Color(0.8, 0.8, 0.8)
const COLOR_ENEMY_B: Color = Color(0.85, 0.6, 0.2)
const COLOR_ENEMY_C: Color = Color(0.2, 0.7, 0.5)
const COLOR_ENEMY_D: Color = Color(0.7, 0.2, 0.2)
const COLOR_HVE: Color = Color(0.5, 0.0, 0.5)
const COLOR_EAGLE: Color = Color(0.9, 0.1, 0.1)
const COLOR_BULLET: Color = Color(1.0, 1.0, 1.0)
const COLOR_SHIELD: Color = Color(0.3, 0.6, 1.0, 0.5)
const COLOR_BONUS: Color = Color(1.0, 0.0, 0.0)

# Freeze duration (in seconds)
const FREEZE_TIME: float = 8.0
const EAGLE_PROTECT_TIME: float = 20.0
const SHIELD_TIME: float = 5.0
const BONUS_LIFETIME: float = 200.0 / FPS

# Game state
var current_level: int = 0
var is_multiplayer: bool = false
var hi_score: int = 0
var unlocked_level: int = 0
var sound_enabled: bool = true
var vibrate_enabled: bool = true

# Persistent data keys
const SAVE_FILE = "user://tank_save.cfg"

func _ready() -> void:
	load_settings()

func calculate_dimensions(viewport_size: Vector2) -> void:
	# The game board is a square (26x26 grid). Size it to 90% of the smaller
	# screen dimension so it fits on any device, then center it.
	var min_dim = min(viewport_size.x, viewport_size.y)
	var board_dim = min_dim * 0.9
	# Quantize tile_dim so sprites stay pixel-aligned
	tile_dim = floor(board_dim / GRID_SIZE)
	board_width = tile_dim * GRID_SIZE
	board_height = tile_dim * GRID_SIZE
	# Offset to center the square board on the viewport
	board_offset = Vector2(
		(viewport_size.x - board_width) / 2.0,
		(viewport_size.y - board_height) / 2.0
	)

func save_settings() -> void:
	var config = ConfigFile.new()
	config.set_value("game", "hi_score", hi_score)
	config.set_value("game", "unlocked_level", unlocked_level)
	config.set_value("game", "sound_enabled", sound_enabled)
	config.set_value("game", "vibrate_enabled", vibrate_enabled)
	config.save(SAVE_FILE)

func load_settings() -> void:
	var config = ConfigFile.new()
	if config.load(SAVE_FILE) == OK:
		hi_score = config.get_value("game", "hi_score", 0)
		unlocked_level = config.get_value("game", "unlocked_level", 0)
		sound_enabled = config.get_value("game", "sound_enabled", true)
		vibrate_enabled = config.get_value("game", "vibrate_enabled", true)

func load_stage(level_num: int) -> Array:
	# Use embedded stage data so levels work in exported builds (mobile).
	# Raw stage files without extensions are not included in the PCK.
	var StageData = preload("res://scripts/stage_data.gd")
	if not StageData.STAGES.has(level_num):
		push_error("Stage not found: " + str(level_num))
		return []
	var lines: Array = StageData.STAGES[level_num]
	var grid: Array = []
	for line in lines:
		if line.length() > 0:
			var row: Array = []
			for ch in line:
				row.append(ch)
			grid.append(row)
	return grid
