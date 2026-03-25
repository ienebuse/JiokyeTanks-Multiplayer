extends Node2D

# Main game controller - equivalent to TankView.java
# Handles game loop, collision detection, enemy spawning, stage management

signal stage_completed
signal game_over
signal score_updated(score: int)
signal lives_updated(lives: int)
signal enemies_remaining_updated(count: int)

# Game state
enum GameState { MENU, CURTAIN_CLOSE, CURTAIN_PAUSE, CURTAIN_OPEN, PLAYING, PAUSED, STAGE_COMPLETE, GAME_OVER, SHOWING_SCORE }
var state: int = GameState.CURTAIN_CLOSE

# Level
var level: int = 1
var level_grid: Array = []
var level_objects: Array = []  # 2D array of terrain nodes
var bushes: Array = []

# Entities
var player: Node2D = null
var player2: Node2D = null
var enemies: Array = []
var enemy_bullets: Array = []
var bonuses: Array = []
var active_bonus: Node2D = null
var eagle_node: Node2D = null
var gold_node: Node2D = null

# Enemy management
var enemy_lives: int = GameData.NUM_ENEMIES
var enemy_count: int = 0
var enemy_next_id: int = 0
var new_enemy_timer: float = 0.0
const GEN_ENEMY_TIME: float = 1.0 / GameData.FPS * 60  # ~2 seconds between spawns
var num_hve: int = 0
var hve_lives: int = 0

# Game timers
var freeze_timer: float = 0.0
var is_frozen: bool = false
var eagle_protect_timer: float = 0.0
var is_eagle_protected: bool = false
var eagle_protect_original: Array = []  # Stores original terrain during eagle protection

# Curtain animation
var curtain_progress: float = 0.0
var curtain_speed: float = 2.0

# Score
var stage_score: int = 0
var total_score: int = 0
var kills: Dictionary = {
	GameData.ObjectType.ST_TANK_A: 0,
	GameData.ObjectType.ST_TANK_B: 0,
	GameData.ObjectType.ST_TANK_C: 0,
	GameData.ObjectType.ST_TANK_D: 0,
}

# Show score timer
var show_score_timer: float = 0.0
const SHOW_SCORE_DELAY: float = 3.0

# Background music (fight scene)
const FIGHT_SCENES: Array = [
	"tnk_fightscene1.wav",
	"tnk_fightscene2.wav",
	"tnk_fightscene3.wav",
	"tnk_fightscene4.wav",
	"tnk_fightscene5.wav",
]
var current_scene_sound: String = ""

# Multiplayer
var sync_timer: float = 0.0
const SYNC_RATE: float = 1.0 / 20.0
var client_enemies: Dictionary = {}
var client_p_bullets: Array = []
var client_e_bullets: Array = []
var client_mines: Array = []
var pending_terrain_changes: Array = []  # Queued terrain changes to sync to client
var active_mines: Array = []  # Track all active mines for sync

# References
var tile_dim: float = 0.0
var board_size: Vector2 = Vector2.ZERO

# Preloaded scenes
var BulletScene = preload("res://scenes/bullet.tscn")
var EnemyScene = preload("res://scenes/enemy.tscn")
var BonusScene = preload("res://scenes/bonus.tscn")
var MineScene = preload("res://scenes/mine.tscn")

# Node references
@onready var terrain_layer: Node2D = $TerrainLayer
@onready var entity_layer: Node2D = $EntityLayer
@onready var bush_layer: Node2D = $BushLayer
@onready var ui_layer: CanvasLayer = $UILayer
@onready var hud: Control = $UILayer/HUD
@onready var curtain_top: ColorRect = $UILayer/CurtainTop
@onready var curtain_bottom: ColorRect = $UILayer/CurtainBottom
@onready var curtain_label: Label = $UILayer/CurtainLabel

func _ready() -> void:
	var viewport_size = get_viewport_rect().size
	GameData.calculate_dimensions(viewport_size)
	tile_dim = GameData.tile_dim
	board_size = Vector2(GameData.board_width, GameData.board_height)
	
	# Offset all game layers so the board is centered on screen
	var offset = GameData.board_offset
	terrain_layer.position = offset
	entity_layer.position = offset
	bush_layer.position = offset
	
	setup_curtain()
	level = GameData.current_level
	start_level(level)

func setup_curtain() -> void:
	var vp_size = get_viewport_rect().size
	curtain_top.size = Vector2(vp_size.x, vp_size.y / 2)
	curtain_top.position = Vector2.ZERO
	curtain_top.color = Color.GRAY
	
	curtain_bottom.size = Vector2(vp_size.x, vp_size.y / 2)
	curtain_bottom.position = Vector2(0, vp_size.y / 2)
	curtain_bottom.color = Color.GRAY
	
	curtain_label.text = ""
	curtain_label.visible = false

func start_level(lvl: int) -> void:
	level = lvl
	state = GameState.CURTAIN_CLOSE
	curtain_progress = 0.0
	
	# Ensure entity processing is enabled (may have been disabled during pause)
	entity_layer.process_mode = Node.PROCESS_MODE_INHERIT
	
	# Clear existing entities
	clear_level()
	
	# Reset enemy state
	enemy_lives = GameData.NUM_ENEMIES
	enemy_count = 0
	enemy_next_id = 0
	new_enemy_timer = 0.0
	is_frozen = false
	freeze_timer = 0.0
	is_eagle_protected = false
	eagle_protect_timer = 0.0
	stage_score = 0
	show_score_timer = SHOW_SCORE_DELAY
	
	# Reset kills
	for key in kills:
		kills[key] = 0
	
	# Calculate HVE count for this level using exponential scaling
	# Formula grows from ~1 HVE at early levels to MAX_HVE at the final level
	var num_hve_calc = pow(10, level * log(GameData.MAX_HVE) / log(10) / GameData.NUM_LEVELS)
	num_hve = int(floor(num_hve_calc + 0.5))
	hve_lives = num_hve
	
	# Load and build level
	level_grid = GameData.load_stage(level)
	build_level()
	
	# Create eagle
	create_eagle()
	
	# Create player
	create_player()
	
	if GameData.is_multiplayer:
		create_player2()
	
	# Create bonus holder
	active_bonus = null
	
	# Update HUD
	if hud:
		hud.update_stage(level)
		hud.update_enemy_count(enemy_lives)
		hud.update_lives(player.lives if player else 3)
		hud.update_score(total_score)
		# Re-show touch controls for gameplay
		if hud.touch_controls and DisplayServer.is_touchscreen_available():
			hud.touch_controls.visible = true
			hud.touch_controls.enable_controls()
	
	# Show curtain - label shown during curtain pause phase
	curtain_label.text = "STAGE " + str(level)
	curtain_label.visible = false  # Will be shown in CURTAIN_PAUSE state

func clear_level() -> void:
	# Clear terrain
	for child in terrain_layer.get_children():
		child.queue_free()
	level_objects.clear()
	
	# Clear bushes
	for child in bush_layer.get_children():
		child.queue_free()
	bushes.clear()
	
	# Clear entities
	for child in entity_layer.get_children():
		child.queue_free()
	enemies.clear()
	enemy_bullets.clear()
	bonuses.clear()
	active_bonus = null
	player = null
	player2 = null
	eagle_node = null
	gold_node = null
	client_enemies.clear()
	for b in client_p_bullets:
		if is_instance_valid(b):
			b.queue_free()
	client_p_bullets.clear()
	for b in client_e_bullets:
		if is_instance_valid(b):
			b.queue_free()
	client_e_bullets.clear()
	for m in client_mines:
		if is_instance_valid(m):
			m.queue_free()
	client_mines.clear()
	active_mines.clear()
	pending_terrain_changes.clear()

func build_level() -> void:
	level_objects = []
	for row_idx in range(level_grid.size()):
		var row_objects: Array = []
		for col_idx in range(level_grid[row_idx].size()):
			var ch = level_grid[row_idx][col_idx]
			var obj = create_terrain_tile(ch, col_idx, row_idx)
			row_objects.append(obj)
		level_objects.append(row_objects)

func create_terrain_tile(ch: String, col: int, row: int) -> Node2D:
	var pos = Vector2(col * tile_dim, row * tile_dim)
	match ch:
		"#":
			return create_brick(pos, col, row)
		"@":
			return create_stone(pos, col, row)
		"~":
			return create_water(pos, col, row)
		"-":
			return create_ice(pos, col, row)
		"%":
			var bush_node = create_bush(pos, col, row)
			bushes.append(bush_node)
			return null
		_:
			return null

func create_brick(pos: Vector2, col: int, row: int) -> Node2D:
	var brick = Node2D.new()
	brick.set_script(preload("res://scripts/brick.gd"))
	brick.position = pos
	brick.set_meta("col", col)
	brick.set_meta("row", row)
	brick.set_meta("type", "brick")
	brick.set_meta("tile_dim", tile_dim)
	terrain_layer.add_child(brick)
	brick.init_brick(tile_dim)
	return brick

func create_stone(pos: Vector2, col: int, row: int) -> Node2D:
	var stone = Node2D.new()
	stone.set_script(preload("res://scripts/stone_wall.gd"))
	stone.position = pos
	stone.set_meta("col", col)
	stone.set_meta("row", row)
	stone.set_meta("type", "stone")
	stone.set_meta("tile_dim", tile_dim)
	terrain_layer.add_child(stone)
	stone.init_stone(tile_dim)
	return stone

func create_water(pos: Vector2, col: int, row: int) -> Node2D:
	var water_node = Node2D.new()
	water_node.set_script(preload("res://scripts/water.gd"))
	water_node.position = pos
	water_node.set_meta("col", col)
	water_node.set_meta("row", row)
	water_node.set_meta("type", "water")
	water_node.set_meta("tile_dim", tile_dim)
	terrain_layer.add_child(water_node)
	water_node.init_water(tile_dim)
	return water_node

func create_ice(pos: Vector2, col: int, row: int) -> Node2D:
	var ice_node = Node2D.new()
	ice_node.set_script(preload("res://scripts/ice.gd"))
	ice_node.position = pos
	ice_node.set_meta("col", col)
	ice_node.set_meta("row", row)
	ice_node.set_meta("type", "ice")
	ice_node.set_meta("tile_dim", tile_dim)
	terrain_layer.add_child(ice_node)
	ice_node.init_ice(tile_dim)
	return ice_node

func create_bush(pos: Vector2, col: int, row: int) -> Node2D:
	var bush_node = Node2D.new()
	bush_node.set_script(preload("res://scripts/bush.gd"))
	bush_node.position = pos
	bush_node.set_meta("col", col)
	bush_node.set_meta("row", row)
	bush_node.set_meta("type", "bush")
	bush_node.set_meta("tile_dim", tile_dim)
	bush_layer.add_child(bush_node)
	bush_node.init_bush(tile_dim)
	return bush_node

func create_eagle() -> void:
	eagle_node = Node2D.new()
	eagle_node.set_script(preload("res://scripts/eagle.gd"))
	# Eagle position: center bottom of map, just above the last 2 rows
	var eagle_col = int(GameData.GRID_SIZE / 2) - 1
	var eagle_row = GameData.GRID_SIZE - 2
	eagle_node.position = Vector2(eagle_col * tile_dim, eagle_row * tile_dim)
	eagle_node.set_meta("type", "eagle")
	terrain_layer.add_child(eagle_node)
	eagle_node.init_eagle(tile_dim)

func create_player() -> void:
	player = Node2D.new()
	player.set_script(preload("res://scripts/player.gd"))
	# Player spawns at bottom (matching original: 4/13 width, row 24)
	var px = int(4.0 * GameData.GRID_SIZE / 13.0) * tile_dim
	var py = (GameData.GRID_SIZE - 2) * tile_dim
	player.position = Vector2(px, py)
	entity_layer.add_child(player)
	player.init_player(tile_dim, 1)
	player.bullet_fired.connect(_on_player_bullet_fired)
	player.mine_dropped.connect(_on_mine_dropped)

func create_player2() -> void:
	player2 = Node2D.new()
	player2.set_script(preload("res://scripts/player.gd"))
	var px = int(9.0 * GameData.GRID_SIZE / 13.0) * tile_dim
	var py = (GameData.GRID_SIZE - 2) * tile_dim
	player2.position = Vector2(px, py)
	entity_layer.add_child(player2)
	player2.init_player(tile_dim, 2)
	if NetworkManager.is_multiplayer_mode:
		player2.is_local = NetworkManager.is_client()
		player.is_local = NetworkManager.is_server()
	player2.bullet_fired.connect(_on_player_bullet_fired)
	player2.mine_dropped.connect(_on_mine_dropped)

func _process(delta: float) -> void:
	match state:
		GameState.CURTAIN_CLOSE:
			process_curtain_close(delta)
		GameState.CURTAIN_PAUSE:
			process_curtain_pause(delta)
		GameState.CURTAIN_OPEN:
			process_curtain_open(delta)
		GameState.PLAYING:
			process_game(delta)
		GameState.PAUSED:
			pass
		GameState.STAGE_COMPLETE:
			process_stage_complete(delta)
		GameState.GAME_OVER:
			process_game_over(delta)
		GameState.SHOWING_SCORE:
			pass

func process_curtain_close(delta: float) -> void:
	curtain_progress += delta * curtain_speed
	update_curtain_position()
	if curtain_progress >= 1.0:
		curtain_progress = 1.0
		state = GameState.CURTAIN_PAUSE
		curtain_label.visible = true

func process_curtain_pause(delta: float) -> void:
	curtain_progress += delta * curtain_speed
	if curtain_progress >= 2.0:
		state = GameState.CURTAIN_OPEN
		curtain_label.visible = false
		# Play sounds when curtain begins opening (matching Java)
		SoundManager.play_sound("tnkgamestart.wav")
		current_scene_sound = FIGHT_SCENES[randi() % FIGHT_SCENES.size()]
		SoundManager.play_sound(current_scene_sound, true, -10.0)

func process_curtain_open(delta: float) -> void:
	curtain_progress += delta * curtain_speed
	var open_progress = curtain_progress - 2.0
	update_curtain_open(open_progress)
	if curtain_progress >= 3.0:
		curtain_top.visible = false
		curtain_bottom.visible = false
		state = GameState.PLAYING

func update_curtain_position() -> void:
	var vp = get_viewport_rect().size
	var half = vp.y / 2.0
	var target_y = half * curtain_progress
	curtain_top.position.y = -half + target_y
	curtain_bottom.position.y = vp.y - target_y
	curtain_top.visible = true
	curtain_bottom.visible = true

func update_curtain_open(progress: float) -> void:
	var vp = get_viewport_rect().size
	var half = vp.y / 2.0
	curtain_top.position.y = -(half * progress)
	curtain_bottom.position.y = vp.y / 2.0 + half * progress

func process_game(delta: float) -> void:
	if Input.is_action_just_pressed("pause"):
		pause_game()
		return
	
	# Multiplayer client: send input to server, skip local game logic
	if GameData.is_multiplayer and NetworkManager.is_client():
		if player2 and player2.is_local:
			var dir = player2.direction
			var mov = false
			if Input.is_action_pressed("move_up"):
				dir = GameData.Direction.UP
				mov = true
			elif Input.is_action_pressed("move_down"):
				dir = GameData.Direction.DOWN
				mov = true
			elif Input.is_action_pressed("move_left"):
				dir = GameData.Direction.LEFT
				mov = true
			elif Input.is_action_pressed("move_right"):
				dir = GameData.Direction.RIGHT
				mov = true
			var firing = Input.is_action_pressed("fire")
			var mine_pressed = Input.is_action_just_pressed("drop_mine")
			_receive_player_input.rpc_id(1, dir, mov, firing, mine_pressed)
			# Run local collision for player2 so it doesn't visually walk through walls
			if player2.lives > 0:
				check_player_collision_for(player2)
		update_hud()
		return
	
	# Freeze timer
	if is_frozen:
		freeze_timer -= delta
		if freeze_timer <= 0:
			is_frozen = false
			unfreeze_enemies()
	
	# Eagle protection timer
	if is_eagle_protected:
		eagle_protect_timer -= delta
		if eagle_protect_timer <= 0:
			is_eagle_protected = false
			remove_eagle_protection()
	
	# Generate enemies
	generate_enemy(delta)
	
	# Update enemies
	update_enemies(delta)
	
	# Check collisions (includes bonus pickup)
	check_all_collisions()
	
	# Update bonus
	update_bonus(delta)
	
	# Update HUD
	update_hud()
	
	# Check win/lose conditions AFTER collisions so bonuses can still be collected
	check_game_state()
	
	# Multiplayer server: sync state to client
	if GameData.is_multiplayer and NetworkManager.is_server():
		# Send terrain changes immediately (reliable)
		if pending_terrain_changes.size() > 0:
			_receive_terrain_changes.rpc(pending_terrain_changes)
			pending_terrain_changes.clear()
		sync_timer += delta
		if sync_timer >= SYNC_RATE:
			sync_timer = 0.0
			var game_state = _build_game_state()
			_receive_game_state.rpc(game_state)

func check_game_state() -> void:
	# Stage complete: all enemies dead - count actual live enemies to avoid counter bugs
	if enemy_lives <= 0 and state == GameState.PLAYING:
		var live_enemies = 0
		for enemy in enemies:
			if is_instance_valid(enemy) and not enemy.is_dead:
				live_enemies += 1
		if live_enemies <= 0:
			do_stage_complete()
			return
	
	# Game over: player dead or eagle destroyed
	if GameData.is_multiplayer:
		var p1_dead = not player or player.lives <= 0
		var p2_dead = not player2 or player2.lives <= 0
		if p1_dead and p2_dead:
			do_game_over()
			return
	else:
		if player and player.lives <= 0:
			do_game_over()
			return
	
	if eagle_node and eagle_node.is_destroyed:
		do_game_over()
		return

func do_stage_complete() -> void:
	state = GameState.STAGE_COMPLETE
	show_score_timer = SHOW_SCORE_DELAY
	stage_completed.emit()
	SoundManager.stop_all_sounds()

func do_game_over() -> void:
	state = GameState.GAME_OVER
	show_score_timer = SHOW_SCORE_DELAY
	game_over.emit()
	SoundManager.stop_all_sounds()
	_play_synced_sound("tnkgameover.wav")
	if hud:
		hud.show_game_over()

func process_stage_complete(delta: float) -> void:
	# Client: just count down to score screen
	if GameData.is_multiplayer and NetworkManager.is_client():
		show_score_timer -= delta
		if show_score_timer <= 0:
			show_scores()
		return
	
	# Game continues to run during stage complete delay (player can still move, collect bonuses)
	# Freeze timer
	if is_frozen:
		freeze_timer -= delta
		if freeze_timer <= 0:
			is_frozen = false
			unfreeze_enemies()
	
	# Eagle protection timer
	if is_eagle_protected:
		eagle_protect_timer -= delta
		if eagle_protect_timer <= 0:
			is_eagle_protected = false
			remove_eagle_protection()
	
	# Update enemies (they may still be dying/animating)
	update_enemies(delta)
	
	# Check collisions - player can still move and collect bonuses
	if player and is_instance_valid(player) and player.lives > 0:
		check_player_collision_for(player)
		check_player_bullets_for(player)
		check_enemy_bullets_collision()
		check_player_bonus_collision()
	if player2 and is_instance_valid(player2) and player2.lives > 0:
		check_player_collision_for(player2)
		check_player_bullets_for(player2)
	
	# Update bonus
	update_bonus(delta)
	
	# Update HUD
	update_hud()
	
	show_score_timer -= delta
	if show_score_timer <= 0:
		show_scores()

func process_game_over(delta: float) -> void:
	show_score_timer -= delta
	if show_score_timer <= 0:
		show_scores()

func show_scores() -> void:
	var was_stage_complete = (state == GameState.STAGE_COMPLETE)
	state = GameState.SHOWING_SCORE
	if hud:
		# Disable touch controls so player can't move/shoot, but keep them visible (grayed out)
		if hud.touch_controls:
			hud.touch_controls.disable_controls()
		hud.show_score_screen(kills, stage_score, total_score, level, was_stage_complete)

# Enemy generation matching Java generateEnemy()
func generate_enemy(delta: float) -> void:
	new_enemy_timer += delta
	if new_enemy_timer < GEN_ENEMY_TIME:
		return
	
	if enemy_count >= GameData.MAX_ENEMIES or enemy_lives <= 0:
		return
	
	new_enemy_timer = 0.0
	
	# Determine armor group
	var g = randf()
	var level_factor = float(level) / GameData.NUM_LEVELS
	var g1 = lerp(0.8, 0.02, level_factor)
	var g2 = lerp(0.15, 0.03, level_factor)
	var g3 = lerp(0.03, 0.15, level_factor)
	var group = 1
	if g < g1:
		group = 1
	elif g < g1 + g2:
		group = 2
	elif g < g1 + g2 + g3:
		group = 3
	else:
		group = 4
	
	# Determine enemy type
	var lprob = 0.6 / GameData.NUM_LEVELS
	var e_rand = randf()
	var type_val = 3 if e_rand < 0.2 + lprob * level else randi() % 3
	var tank_type = GameData.ObjectType.ST_TANK_A
	match type_val:
		0: tank_type = GameData.ObjectType.ST_TANK_A
		1: tank_type = GameData.ObjectType.ST_TANK_B
		2: tank_type = GameData.ObjectType.ST_TANK_C
		3: tank_type = GameData.ObjectType.ST_TANK_D
	
	# Check if HVE should spawn
	var is_hve = false
	if enemy_lives <= num_hve:
		if enemy_lives <= hve_lives:
			is_hve = true
			hve_lives -= 1
		elif randf() < 0.5:
			is_hve = true
			hve_lives -= 1
	
	# Spawn position (top of map, 3 possible positions matching original)
	# Positions at grid columns 0, 12, 24
	var spawn_positions = [0.0, 12.0 * tile_dim, 24.0 * tile_dim]
	var spawn_pos_idx = randi() % 3
	var spawn_x = spawn_positions[spawn_pos_idx]
	var spawn_y = 0.0
	
	# Check if spawn position is occupied by another tank; try alternate positions
	var tank_sz = tile_dim * 2
	var found_free = false
	for attempt in range(3):
		var test_idx = (spawn_pos_idx + attempt) % 3
		var test_x = spawn_positions[test_idx]
		var test_rect = Rect2(test_x, spawn_y, tank_sz, tank_sz)
		var blocked = false
		for other in enemies:
			if is_instance_valid(other) and not other.is_dead:
				var other_rect = Rect2(other.position, Vector2(tank_sz, tank_sz))
				if test_rect.intersects(other_rect):
					blocked = true
					break
		if not blocked:
			spawn_x = test_x
			found_free = true
			break
	if not found_free:
		# All three positions occupied — skip this spawn attempt, try next tick
		new_enemy_timer = GEN_ENEMY_TIME * 0.5
		return
	
	# Create enemy
	var enemy_node = EnemyScene.instantiate()
	entity_layer.add_child(enemy_node)
	
	if is_hve:
		var v = 0.3 * pow(10, level * log(3.7) / log(10) / GameData.NUM_LEVELS)
		var bv = 0.8 * pow(10, level * log(1.75) / log(10) / GameData.NUM_LEVELS)
		enemy_node.init_enemy(tile_dim, GameData.ObjectType.ST_HVE, group, enemy_next_id, true, v, bv)
	else:
		enemy_node.init_enemy(tile_dim, tank_type, group, enemy_next_id, false)
	
	enemy_node.position = Vector2(spawn_x, spawn_y)
	
	# 20% chance to have bonus
	if randf() < 0.2:
		enemy_node.has_bonus = true
	
	enemy_node.bullet_fired.connect(_on_enemy_bullet_fired)
	enemy_node.destroyed.connect(_on_enemy_destroyed)
	enemies.append(enemy_node)
	
	enemy_lives -= 1
	enemy_count += 1
	enemy_next_id += 1
	
	if hud:
		hud.update_enemy_count(enemy_lives)

func update_enemies(delta: float) -> void:
	var to_remove: Array = []
	for enemy in enemies:
		if not is_instance_valid(enemy):
			to_remove.append(enemy)
			continue
		if enemy.is_dead and enemy.death_anim_done:
			to_remove.append(enemy)
			continue
		
		if not enemy.is_dead and not is_frozen:
			# Set target (closest alive player position)
			var target_pos = Vector2(board_size.x / 2, board_size.y / 2)
			if player and player.lives > 0 and not player.is_respawning:
				target_pos = player.position
			if player2 and player2.lives > 0 and not player2.is_respawning:
				var p2_dist = enemy.position.distance_squared_to(player2.position)
				var p1_dist = enemy.position.distance_squared_to(target_pos)
				if p2_dist < p1_dist or not (player and player.lives > 0 and not player.is_respawning):
					target_pos = player2.position
			enemy.set_target(target_pos)
			
			enemy.update_ai(delta)
			check_enemy_collision(enemy)
	
	for enemy in to_remove:
		enemies.erase(enemy)
		if is_instance_valid(enemy):
			enemy.queue_free()

func check_enemy_collision(enemy: Node2D) -> void:
	if not is_instance_valid(enemy) or enemy.is_dead or enemy.is_spawning:
		return
	var enemy_rect = Rect2(enemy.position, Vector2(tile_dim * 2, tile_dim * 2))
	
	# Check against terrain
	var grid_col = int(enemy.position.x / tile_dim)
	var grid_row = int(enemy.position.y / tile_dim)
	
	for r in range(max(0, grid_row - 1), min(level_objects.size(), grid_row + 3)):
		for c in range(max(0, grid_col - 1), min(level_objects[0].size() if level_objects.size() > 0 else 0, grid_col + 3)):
			if r < level_objects.size() and c < level_objects[r].size():
				var obj = level_objects[r][c]
				if obj != null and is_instance_valid(obj):
					var obj_rect: Rect2
					if obj.has_method("get_collision_rect"):
						obj_rect = obj.get_collision_rect()
					else:
						obj_rect = Rect2(obj.position, Vector2(tile_dim, tile_dim))
					if enemy_rect.intersects(obj_rect):
						var obj_type = obj.get_meta("type") if obj.has_meta("type") else ""
						if obj_type in ["brick", "stone"]:
							enemy.handle_collision()
						elif obj_type == "water" and not enemy.has_boat:
							enemy.handle_collision()
	
	# Check against board boundaries
	if enemy.position.x < 0 or enemy.position.x > board_size.x - tile_dim * 2:
		enemy.handle_collision()
	if enemy.position.y < 0 or enemy.position.y > board_size.y - tile_dim * 2:
		enemy.handle_collision()
	
	# Check against other enemies
	for other in enemies:
		if other == enemy or not is_instance_valid(other) or other.is_dead or other.is_spawning:
			continue
		var other_rect = Rect2(other.position, Vector2(tile_dim * 2, tile_dim * 2))
		if enemy_rect.intersects(other_rect):
			# If both tanks are stuck at the same position, push them apart
			var diff = enemy.position - other.position
			if diff.length() < 1.0:
				# Exactly overlapping — push in their facing directions
				enemy.push_out(enemy.direction_to_vector(enemy.direction), tile_dim)
			else:
				enemy.handle_collision()
	
	# Check against player
	if player and is_instance_valid(player) and not player.is_respawning and player.lives > 0:
		var player_rect = Rect2(player.position, Vector2(tile_dim * 2, tile_dim * 2))
		if enemy_rect.intersects(player_rect):
			enemy.handle_collision()
	
	# Check against player2
	if player2 and is_instance_valid(player2) and not player2.is_respawning and player2.lives > 0:
		var p2_rect = Rect2(player2.position, Vector2(tile_dim * 2, tile_dim * 2))
		if enemy_rect.intersects(p2_rect):
			enemy.handle_collision()
	
	# Check against eagle
	if eagle_node and is_instance_valid(eagle_node):
		var eagle_rect = Rect2(eagle_node.position, Vector2(tile_dim * 2, tile_dim * 2))
		if enemy_rect.intersects(eagle_rect):
			enemy.handle_collision()

func check_all_collisions() -> void:
	if player and player.lives > 0:
		check_player_collision_for(player)
		check_player_bullets_for(player)
	if player2 and player2.lives > 0:
		check_player_collision_for(player2)
		check_player_bullets_for(player2)
	
	check_enemy_bullets_collision()
	check_player_bonus_collision()
	check_enemy_bonus_collision()

func check_player_collision_for(p: Node2D) -> void:
	if not is_instance_valid(p) or p.is_respawning:
		return
	var p_rect = Rect2(p.position, Vector2(tile_dim * 2, tile_dim * 2))
	
	# Check terrain collisions
	var grid_col = int(p.position.x / tile_dim)
	var grid_row = int(p.position.y / tile_dim)
	
	for r in range(max(0, grid_row - 1), min(level_objects.size(), grid_row + 3)):
		for c in range(max(0, grid_col - 1), min(level_objects[0].size() if level_objects.size() > 0 else 0, grid_col + 3)):
			if r < level_objects.size() and c < level_objects[r].size():
				var obj = level_objects[r][c]
				if obj != null and is_instance_valid(obj):
					# Use brick's own collision rect if available
					var obj_rect: Rect2
					if obj.has_method("get_collision_rect"):
						obj_rect = obj.get_collision_rect()
					else:
						obj_rect = Rect2(obj.position, Vector2(tile_dim, tile_dim))
					if p_rect.intersects(obj_rect):
						var obj_type = obj.get_meta("type") if obj.has_meta("type") else ""
						match obj_type:
							"brick", "stone":
								p.handle_terrain_collision(obj)
							"water":
								if not p.has_boat:
									p.handle_terrain_collision(obj)
							"ice":
								p.on_ice = true
	
	# Check board boundaries
	p.position.x = clamp(p.position.x, 0, board_size.x - tile_dim * 2)
	p.position.y = clamp(p.position.y, 0, board_size.y - tile_dim * 2)
	
	# Check enemy collision
	for enemy in enemies:
		if not is_instance_valid(enemy) or enemy.is_dead or enemy.is_spawning:
			continue
		var enemy_rect = Rect2(enemy.position, Vector2(tile_dim * 2, tile_dim * 2))
		if p_rect.intersects(enemy_rect):
			p.handle_terrain_collision(enemy)
	
	# Check eagle collision
	if eagle_node and is_instance_valid(eagle_node):
		var eagle_rect = Rect2(eagle_node.position, Vector2(tile_dim * 2, tile_dim * 2))
		if p_rect.intersects(eagle_rect):
			p.handle_terrain_collision(eagle_node)
	
	# Check collision with other player
	var other = player2 if p == player else player
	if other and is_instance_valid(other) and not other.is_respawning and other.lives > 0:
		var other_rect = Rect2(other.position, Vector2(tile_dim * 2, tile_dim * 2))
		if p_rect.intersects(other_rect):
			p.handle_terrain_collision(other)

func check_player_bullets_for(p: Node2D) -> void:
	var bullets_to_remove: Array = []
	for bullet in p.bullets:
		if not is_instance_valid(bullet):
			bullets_to_remove.append(bullet)
			continue
		if bullet.is_destroyed:
			bullets_to_remove.append(bullet)
			continue
		
		# Use expanded collision rect (matching Java Bullet.collides_with)
		var bullet_rect = bullet.get_collision_rect()
		
		# Check board boundaries first
		if bullet.position.x < 0 or bullet.position.x > board_size.x or \
		   bullet.position.y < 0 or bullet.position.y > board_size.y:
			bullet.destroy()
			continue
		
		# Check against terrain
		var grid_col = int(bullet.position.x / tile_dim)
		var grid_row = int(bullet.position.y / tile_dim)
		var hit_terrain = false
		
		for r in range(max(0, grid_row - 1), min(level_objects.size(), grid_row + 3)):
			if hit_terrain:
				break
			for c in range(max(0, grid_col - 1), min(level_objects[0].size() if level_objects.size() > 0 else 0, grid_col + 3)):
				if r < level_objects.size() and c < level_objects[r].size():
					var obj = level_objects[r][c]
					if obj != null and is_instance_valid(obj):
						# Use brick's own collision rect if available
						var obj_rect: Rect2
						if obj.has_method("get_collision_rect"):
							obj_rect = obj.get_collision_rect()
						else:
							obj_rect = Rect2(obj.position, Vector2(tile_dim, tile_dim))
						
						if bullet_rect.intersects(obj_rect):
							var obj_type = obj.get_meta("type") if obj.has_meta("type") else ""
							match obj_type:
								"brick":
									# Matching Java: brick takes directional damage
									obj.take_damage(bullet.direction)
									_record_terrain_change(r, c, "brick_damage", bullet.direction)
									if obj.is_destroyed():
										level_objects[r][c] = null
									# If player has break_wall, destroy brick entirely
									if bullet.break_wall and not obj.is_destroyed():
										obj.queue_free()
										level_objects[r][c] = null
										_record_terrain_change(r, c, "brick_destroy")
									bullet.destroy()
									hit_terrain = true
									_play_synced_sound("tnkbrick.wav")
								"stone":
									if bullet.break_wall:
										obj.queue_free()
										level_objects[r][c] = null
										_record_terrain_change(r, c, "stone_destroy")
									bullet.destroy()
									hit_terrain = true
									_play_synced_sound("tnksteel.wav")
								"bush":
									if bullet.clear_bush:
										obj.queue_free()
										level_objects[r][c] = null
										_record_terrain_change(r, c, "bush_destroy")
										# Bushes don't stop bullets in original
		
		if bullet.is_destroyed:
			continue
		
		# Check against enemies
		for enemy in enemies:
			if not is_instance_valid(enemy) or enemy.is_dead or enemy.is_spawning:
				continue
			var enemy_rect = Rect2(enemy.position, Vector2(tile_dim * 2, tile_dim * 2))
			if bullet_rect.intersects(enemy_rect):
				var killed = enemy.take_hit(bullet)
				bullet.destroy()
				if killed:
					_play_synced_sound("tnkexplosion.wav")
					var score = GameData.ENEMY_SCORES.get(enemy.tank_type, 100)
					stage_score += score
					total_score += score
					if kills.has(enemy.tank_type):
						kills[enemy.tank_type] += 1
					enemy_count -= 1
					# Check if enemy had bonus
					if enemy.has_bonus:
						spawn_bonus()
				break
		
		if bullet.is_destroyed:
			continue
		
		# Check against eagle
		if eagle_node and is_instance_valid(eagle_node) and not eagle_node.is_destroyed:
			var eagle_rect = Rect2(eagle_node.position, Vector2(tile_dim * 2, tile_dim * 2))
			if bullet_rect.intersects(eagle_rect):
				if is_eagle_protected:
					bullet.destroy()
				else:
					eagle_node.take_damage()
					bullet.destroy()
					var ec = int(eagle_node.position.x / tile_dim)
					var er = int(eagle_node.position.y / tile_dim)
					_record_terrain_change(er, ec, "eagle_destroy")
		
		if bullet.is_destroyed:
			continue
		
		# Check against enemy bullets
		for eb in enemy_bullets:
			if not is_instance_valid(eb) or eb.is_destroyed:
				continue
			var eb_rect = Rect2(eb.position, Vector2(eb.size, eb.size))
			if bullet_rect.intersects(eb_rect):
				bullet.destroy()
				eb.destroy()
				break
	
	for bullet in bullets_to_remove:
		p.bullets.erase(bullet)

func check_enemy_bullets_collision() -> void:
	var to_remove: Array = []
	for bullet in enemy_bullets:
		if not is_instance_valid(bullet):
			to_remove.append(bullet)
			continue
		if bullet.is_destroyed:
			# Only remove from tracking after explosion animation completes
			if bullet.exploding:
				continue
			to_remove.append(bullet)
			continue
		
		# Use expanded collision rect (matching Java Bullet.collides_with)
		var bullet_rect = bullet.get_collision_rect()
		
		# Check board boundaries first
		if bullet.position.x < 0 or bullet.position.x > board_size.x or \
		   bullet.position.y < 0 or bullet.position.y > board_size.y:
			bullet.destroy()
			continue
		
		# Check against player
		if player and is_instance_valid(player) and not player.is_respawning and player.lives > 0:
			var player_rect = Rect2(player.position, Vector2(tile_dim * 2, tile_dim * 2))
			if bullet_rect.intersects(player_rect):
				if player.has_shield:
					bullet.destroy()
				else:
					player.take_hit()
					bullet.destroy()
					_play_synced_sound("tnkexplosion.wav")
					if hud:
						hud.update_lives(player.lives)
				continue
		
		# Check against player2
		if player2 and is_instance_valid(player2) and not player2.is_respawning and player2.lives > 0:
			var p2_rect = Rect2(player2.position, Vector2(tile_dim * 2, tile_dim * 2))
			if bullet_rect.intersects(p2_rect):
				if player2.has_shield:
					bullet.destroy()
				else:
					player2.take_hit()
					bullet.destroy()
					_play_synced_sound("tnkexplosion.wav")
				continue
		
		# Check against terrain
		var grid_col = int(bullet.position.x / tile_dim)
		var grid_row = int(bullet.position.y / tile_dim)
		var hit_terrain = false
		
		for r in range(max(0, grid_row - 1), min(level_objects.size(), grid_row + 3)):
			if hit_terrain:
				break
			for c in range(max(0, grid_col - 1), min(level_objects[0].size() if level_objects.size() > 0 else 0, grid_col + 3)):
				if r < level_objects.size() and c < level_objects[r].size():
					var obj = level_objects[r][c]
					if obj != null and is_instance_valid(obj):
						var obj_rect: Rect2
						if obj.has_method("get_collision_rect"):
							obj_rect = obj.get_collision_rect()
						else:
							obj_rect = Rect2(obj.position, Vector2(tile_dim, tile_dim))
						
						if bullet_rect.intersects(obj_rect):
							var obj_type = obj.get_meta("type") if obj.has_meta("type") else ""
							match obj_type:
								"brick":
									obj.take_damage(bullet.direction)
									_record_terrain_change(r, c, "brick_damage", bullet.direction)
									if obj.is_destroyed():
										level_objects[r][c] = null
									bullet.destroy()
									hit_terrain = true
								"stone":
									bullet.destroy()
									hit_terrain = true
		
		if bullet.is_destroyed:
			continue
		
		# Check against eagle
		if eagle_node and is_instance_valid(eagle_node) and not eagle_node.is_destroyed:
			var eagle_rect = Rect2(eagle_node.position, Vector2(tile_dim * 2, tile_dim * 2))
			if bullet_rect.intersects(eagle_rect):
				if is_eagle_protected:
					bullet.destroy()
				else:
					eagle_node.take_damage()
					bullet.destroy()
					var ec = int(eagle_node.position.x / tile_dim)
					var er = int(eagle_node.position.y / tile_dim)
					_record_terrain_change(er, ec, "eagle_destroy")
	
	for bullet in to_remove:
		enemy_bullets.erase(bullet)
		# Don't queue_free() here - bullets self-destruct after explosion animation

func check_player_bonus_collision() -> void:
	if active_bonus == null or not is_instance_valid(active_bonus):
		return
	
	var bonus_rect = Rect2(active_bonus.position, Vector2(tile_dim * 2, tile_dim * 2))
	
	if player and is_instance_valid(player) and not player.is_respawning and player.lives > 0:
		var player_rect = Rect2(player.position, Vector2(tile_dim * 2, tile_dim * 2))
		if player_rect.intersects(bonus_rect):
			apply_bonus(active_bonus.bonus_type, player)
			active_bonus.queue_free()
			active_bonus = null
			_play_synced_sound("tnkpowerup.wav")
			return
	
	if player2 and is_instance_valid(player2) and not player2.is_respawning and player2.lives > 0:
		var p2_rect = Rect2(player2.position, Vector2(tile_dim * 2, tile_dim * 2))
		if p2_rect.intersects(bonus_rect):
			apply_bonus(active_bonus.bonus_type, player2)
			active_bonus.queue_free()
			active_bonus = null
			_play_synced_sound("tnkpowerup.wav")
			return

func check_enemy_bonus_collision() -> void:
	# Matching Java checkCollisionEnemyWithBonus - enemies can collect bonuses
	if active_bonus == null or not is_instance_valid(active_bonus):
		return
	var bonus_rect = Rect2(active_bonus.position, Vector2(tile_dim * 2, tile_dim * 2))
	for enemy in enemies:
		if not is_instance_valid(enemy) or enemy.is_dead or enemy.is_spawning:
			continue
		var enemy_rect = Rect2(enemy.position, Vector2(tile_dim * 2, tile_dim * 2))
		if enemy_rect.intersects(bonus_rect):
			apply_enemy_bonus(active_bonus.bonus_type, enemy)
			active_bonus.queue_free()
			active_bonus = null
			break

func apply_enemy_bonus(bonus_type: int, enemy: Node2D) -> void:
	# Matching Java Enemy.collidsWithBonus - reversed effects for enemy
	match bonus_type:
		GameData.BonusType.GRENADE:
			# Enemy gets grenade → kills both players
			if player and is_instance_valid(player) and not player.is_respawning:
				player.take_hit()
				_play_synced_sound("tnkexplosion.wav")
				if hud:
					hud.update_lives(player.lives)
			if player2 and is_instance_valid(player2) and not player2.is_respawning:
				player2.take_hit()
				_play_synced_sound("tnkexplosion.wav")
		GameData.BonusType.HELMET:
			# Enemy gets shield
			enemy.activate_shield_if_available()
		GameData.BonusType.CLOCK:
			# Enemy gets clock → freezes both players
			if player and is_instance_valid(player):
				player.freeze()
			if player2 and is_instance_valid(player2):
				player2.freeze()
		GameData.BonusType.SHOVEL:
			# Enemy gets shovel → protects eagle (benefits enemy side)
			protect_eagle()
		GameData.BonusType.TANK:
			# Enemy gets extra life → spawn an extra enemy
			enemy_lives += 1
			if hud:
				hud.update_enemy_count(enemy_lives)
		GameData.BonusType.STAR:
			# Enemy gets star → upgrade enemy
			enemy.upgrade_star()
		GameData.BonusType.GUN:
			# Enemy gets gun → upgrade enemy
			enemy.upgrade_gun()
		GameData.BonusType.BOAT:
			# Enemy gets boat
			enemy.has_boat = true

func spawn_bonus() -> void:
	# Replace existing bonus if one is active (matching Java setBonus() behavior)
	if active_bonus != null and is_instance_valid(active_bonus):
		active_bonus.queue_free()
		active_bonus = null
	
	# Determine bonus type based on probabilities
	var r = randf()
	var cumulative = 0.0
	var bonus_type = GameData.BonusType.GRENADE
	for i in range(GameData.BONUS_PROBABILITIES.size()):
		cumulative += GameData.BONUS_PROBABILITIES[i]
		if r < cumulative:
			bonus_type = i
			break
	
	# Random position
	var bx = randf_range(tile_dim * 2, board_size.x - tile_dim * 4)
	var by = randf_range(tile_dim * 2, board_size.y - tile_dim * 4)
	
	active_bonus = BonusScene.instantiate()
	entity_layer.add_child(active_bonus)
	active_bonus.init_bonus(tile_dim, bonus_type)
	active_bonus.position = Vector2(bx, by)

func apply_bonus(bonus_type: int, bonus_player: Node2D = null) -> void:
	if bonus_player == null:
		bonus_player = player
	match bonus_type:
		GameData.BonusType.GRENADE:
			# Kill all enemies
			for enemy in enemies:
				if is_instance_valid(enemy) and not enemy.is_dead:
					enemy.take_hit(null)
					enemy_count -= 1
					var score = GameData.ENEMY_SCORES.get(enemy.tank_type, 100)
					stage_score += score
					total_score += score
		GameData.BonusType.HELMET:
			if bonus_player:
				bonus_player.activate_shield()
		GameData.BonusType.CLOCK:
			freeze_enemies()
		GameData.BonusType.SHOVEL:
			protect_eagle()
		GameData.BonusType.TANK:
			if bonus_player:
				bonus_player.lives += 1
				_play_synced_sound("tnk1up.wav")
		GameData.BonusType.STAR:
			if bonus_player:
				bonus_player.upgrade_star()
		GameData.BonusType.GUN:
			if bonus_player:
				bonus_player.upgrade_gun()
		GameData.BonusType.BOAT:
			if bonus_player:
				bonus_player.has_boat = true
		GameData.BonusType.MINE:
			if bonus_player:
				bonus_player.mine_count += 1
		GameData.BonusType.BUILDER:
			if bonus_player:
				bonus_player.builder_count += 1
	
	stage_score += 500

func update_bonus(delta: float) -> void:
	if active_bonus and is_instance_valid(active_bonus):
		active_bonus.update_bonus(delta)
		if active_bonus.is_expired:
			active_bonus.queue_free()
			active_bonus = null

func freeze_enemies() -> void:
	is_frozen = true
	freeze_timer = GameData.FREEZE_TIME
	for enemy in enemies:
		if is_instance_valid(enemy):
			enemy.freeze()

func unfreeze_enemies() -> void:
	for enemy in enemies:
		if is_instance_valid(enemy):
			enemy.unfreeze()

func protect_eagle() -> void:
	is_eagle_protected = true
	eagle_protect_timer = GameData.EAGLE_PROTECT_TIME
	eagle_protect_original.clear()
	# Place stone walls around eagle, saving original terrain
	if eagle_node and is_instance_valid(eagle_node):
		var eagle_col = int(eagle_node.position.x / tile_dim)
		var eagle_row = int(eagle_node.position.y / tile_dim)
		var protect_positions = [
			[eagle_col - 1, eagle_row - 1], [eagle_col, eagle_row - 1], [eagle_col + 1, eagle_row - 1], [eagle_col + 2, eagle_row - 1],
			[eagle_col - 1, eagle_row], [eagle_col - 1, eagle_row + 1],
			[eagle_col + 2, eagle_row], [eagle_col + 2, eagle_row + 1],
		]
		for pos in protect_positions:
			var c = pos[0]
			var r = pos[1]
			if r >= 0 and r < level_objects.size() and c >= 0 and c < level_objects[0].size():
				# Save original terrain character for restoration
				var original_ch = "."
				if r < level_grid.size() and c < level_grid[r].size():
					original_ch = level_grid[r][c]
				eagle_protect_original.append({"c": c, "r": r, "ch": original_ch})
				if level_objects[r][c] != null and is_instance_valid(level_objects[r][c]):
					level_objects[r][c].queue_free()
				level_objects[r][c] = create_stone(Vector2(c * tile_dim, r * tile_dim), c, r)
				_record_terrain_change(r, c, "place_stone")

func remove_eagle_protection() -> void:
	# Restore original terrain around eagle (matching Java: replaces with bricks)
	for entry in eagle_protect_original:
		var c = entry["c"]
		var r = entry["r"]
		var ch = entry["ch"]
		if r >= 0 and r < level_objects.size() and c >= 0 and c < level_objects[0].size():
			if level_objects[r][c] != null and is_instance_valid(level_objects[r][c]):
				level_objects[r][c].queue_free()
			# Restore as brick (matching original Java behavior)
			level_objects[r][c] = create_brick(Vector2(c * tile_dim, r * tile_dim), c, r)
			_record_terrain_change(r, c, "place_brick")
	eagle_protect_original.clear()

func pause_game() -> void:
	state = GameState.PAUSED
	# Stop all entity processing so bullets, player, enemies freeze
	entity_layer.process_mode = Node.PROCESS_MODE_DISABLED
	# Stop game start sound if still playing
	SoundManager.stop_sound("tnkgamestart.wav")
	# Pause fight scene music (matching Java pauseNoAds)
	if current_scene_sound != "":
		SoundManager.pause_sound(current_scene_sound)
	SoundManager.play_sound("tnkpause.wav")
	if hud:
		hud.show_pause_menu()
		if hud.touch_controls:
			hud.touch_controls.disable_controls()

func resume_game() -> void:
	state = GameState.PLAYING
	# Resume entity processing
	entity_layer.process_mode = Node.PROCESS_MODE_INHERIT
	# Resume fight scene music (matching Java resumeNoAds)
	if current_scene_sound != "":
		SoundManager.resume_sound(current_scene_sound)
	if hud:
		hud.hide_pause_menu()
		if hud.touch_controls and DisplayServer.is_touchscreen_available():
			hud.touch_controls.enable_controls()

func next_level() -> void:
	level += 1
	if level > GameData.NUM_LEVELS:
		level = 1
	if level > GameData.unlocked_level:
		GameData.unlocked_level = level
		GameData.save_settings()
	start_level(level)

func retry_level() -> void:
	start_level(level)

func update_hud() -> void:
	if hud:
		hud.update_score(total_score)
		if GameData.is_multiplayer:
			var p1_lives = player.lives if player else 0
			var p2_lives = player2.lives if player2 else 0
			hud.update_lives_multiplayer(p1_lives, p2_lives)
		elif player:
			hud.update_lives(player.lives)

# Signal handlers
func _on_player_bullet_fired(bullet: Node2D) -> void:
	entity_layer.add_child(bullet)

func _on_enemy_bullet_fired(bullet: Node2D) -> void:
	entity_layer.add_child(bullet)
	enemy_bullets.append(bullet)

func _on_enemy_destroyed(enemy: Node2D) -> void:
	pass  # Handled in check_player_bullets

func _on_mine_dropped(mine_node: Node2D) -> void:
	entity_layer.add_child(mine_node)
	active_mines.append(mine_node)

func _on_pause_btn_pressed() -> void:
	if state == GameState.PLAYING:
		pause_game()

func _input(event: InputEvent) -> void:
	# Fallback touch detection for pause button (GUI buttons may not reliably
	# receive emulated mouse events from touch on all devices/configurations)
	if state == GameState.PLAYING and event is InputEventScreenTouch and event.pressed:
		var pause_btn = $UILayer/HUD/MarginContainer/TopBar/PauseBtn
		if pause_btn and pause_btn.visible:
			var btn_rect = pause_btn.get_global_rect().grow(10.0)
			if btn_rect.has_point(event.position):
				pause_game()
				get_viewport().set_input_as_handled()

func _on_hud_resume() -> void:
	resume_game()

func _on_hud_retry() -> void:
	retry_level()

func _on_hud_next() -> void:
	next_level()

func _on_hud_quit() -> void:
	if NetworkManager.is_multiplayer_mode:
		NetworkManager.disconnect_from_game()
	get_tree().change_scene_to_file("res://scenes/main_menu.tscn")

func _draw() -> void:
	# Draw a square border outline around the stage
	var offset = GameData.board_offset
	var border_rect = Rect2(offset, board_size)
	draw_rect(border_rect, Color.GRAY, false, 2.0)

# --- Multiplayer RPC functions ---

@rpc("any_peer", "unreliable_ordered")
func _receive_player_input(dir: int, mov: bool, firing: bool, mine_drop: bool) -> void:
	if player2 and not player2.is_local:
		player2.set_remote_input(dir, mov, firing, mine_drop)

@rpc("authority", "unreliable_ordered")
func _receive_game_state(data: Dictionary) -> void:
	_apply_game_state(data)

@rpc("authority", "reliable")
func _receive_sound_event(sound_name: String) -> void:
	SoundManager.play_sound(sound_name)

func _play_synced_sound(sound_name: String) -> void:
	SoundManager.play_sound(sound_name)
	if GameData.is_multiplayer and NetworkManager.is_server():
		_receive_sound_event.rpc(sound_name)

func _record_terrain_change(row: int, col: int, action: String, direction: int = -1) -> void:
	if GameData.is_multiplayer and NetworkManager.is_server():
		pending_terrain_changes.append([row, col, action, direction])

@rpc("authority", "reliable")
func _receive_terrain_changes(changes: Array) -> void:
	if not GameData.is_multiplayer or not NetworkManager.is_client():
		return
	for change in changes:
		if change.size() < 4:
			continue
		var r = int(change[0])
		var c = int(change[1])
		var action = str(change[2])
		var dir = int(change[3])
		if r < 0 or r >= level_objects.size() or level_objects.size() == 0:
			continue
		if c < 0 or c >= level_objects[r].size():
			continue
		match action:
			"brick_damage":
				var obj = level_objects[r][c]
				if obj != null and is_instance_valid(obj) and obj.has_method("take_damage"):
					obj.take_damage(dir)
					if obj.is_destroyed():
						level_objects[r][c] = null
			"brick_destroy", "stone_destroy", "bush_destroy":
				var obj = level_objects[r][c]
				if obj != null and is_instance_valid(obj):
					obj.queue_free()
					level_objects[r][c] = null
			"place_stone":
				if level_objects[r][c] != null and is_instance_valid(level_objects[r][c]):
					level_objects[r][c].queue_free()
				level_objects[r][c] = create_stone(Vector2(c * tile_dim, r * tile_dim), c, r)
			"place_brick":
				if level_objects[r][c] != null and is_instance_valid(level_objects[r][c]):
					level_objects[r][c].queue_free()
				level_objects[r][c] = create_brick(Vector2(c * tile_dim, r * tile_dim), c, r)
			"eagle_destroy":
				if eagle_node and is_instance_valid(eagle_node):
					eagle_node.take_damage()

func _build_game_state() -> Dictionary:
	var data: Dictionary = {}
	
	# Player states
	if player and is_instance_valid(player):
		data["p1"] = player.get_sync_state()
	if player2 and is_instance_valid(player2):
		data["p2"] = player2.get_sync_state()
	
	# Enemy states
	var e_states: Array = []
	for enemy in enemies:
		if is_instance_valid(enemy):
			e_states.append(enemy.get_sync_state())
	data["enemies"] = e_states
	
	# Player bullets (from both players) - include exploding bullets for explosion visuals
	var pb: Array = []
	if player:
		for b in player.bullets:
			if is_instance_valid(b):
				if b.is_destroyed and not b.exploding:
					continue
				pb.append([b.position.x, b.position.y, b.direction, b.from_player, int(b.exploding), b.explode_frame])
	if player2:
		for b in player2.bullets:
			if is_instance_valid(b):
				if b.is_destroyed and not b.exploding:
					continue
				pb.append([b.position.x, b.position.y, b.direction, b.from_player, int(b.exploding), b.explode_frame])
	data["p_bullets"] = pb
	
	# Enemy bullets - include exploding bullets
	var eb: Array = []
	for b in enemy_bullets:
		if is_instance_valid(b):
			if b.is_destroyed and not b.exploding:
				continue
			eb.append([b.position.x, b.position.y, b.direction, int(b.exploding), b.explode_frame])
	data["e_bullets"] = eb
	
	# Mines
	var mines: Array = []
	var to_remove: Array = []
	for mine in active_mines:
		if not is_instance_valid(mine) or mine.explode_done:
			to_remove.append(mine)
			continue
		mines.append([mine.position.x, mine.position.y, mine.direction, int(mine.is_moving), int(mine.is_exploding), mine.fuse_timer])
	for mine in to_remove:
		active_mines.erase(mine)
	if mines.size() > 0:
		data["mines"] = mines
	
	# Bonus
	if active_bonus and is_instance_valid(active_bonus) and not active_bonus.is_expired:
		data["bonus"] = [active_bonus.position.x, active_bonus.position.y, active_bonus.bonus_type]
	
	# Game state
	data["score"] = total_score
	data["stage_score"] = stage_score
	data["enemy_lives"] = enemy_lives
	data["frozen"] = is_frozen
	data["state"] = state
	
	return data

func _apply_game_state(data: Dictionary) -> void:
	if not GameData.is_multiplayer or not NetworkManager.is_client():
		return
	
	# Apply player states
	if data.has("p1") and player and is_instance_valid(player):
		player.apply_sync_state(data["p1"])
	if data.has("p2") and player2 and is_instance_valid(player2):
		player2.apply_sync_state(data["p2"])
	
	# Apply enemy states
	if data.has("enemies"):
		var server_enemies: Array = data["enemies"]
		var seen_ids: Dictionary = {}
		
		for e_data in server_enemies:
			if e_data.size() < 19:
				continue
			var eid = int(e_data[0])
			seen_ids[eid] = true
			
			if client_enemies.has(eid):
				var e_node = client_enemies[eid]
				if is_instance_valid(e_node):
					e_node.apply_sync_state(e_data)
				else:
					client_enemies.erase(eid)
			else:
				var e_node = EnemyScene.instantiate()
				entity_layer.add_child(e_node)
				var tank_t = int(e_data[6])
				var grp = int(e_data[7])
				var is_hve_flag = bool(e_data[8])
				e_node.init_enemy(tile_dim, tank_t, grp, eid, is_hve_flag)
				e_node.network_controlled = true
				e_node.apply_sync_state(e_data)
				client_enemies[eid] = e_node
				var existing_ids = _get_enemy_ids()
				if eid not in existing_ids:
					enemies.append(e_node)
		
		# Remove enemies no longer on server
		var to_erase: Array = []
		for eid in client_enemies:
			if not seen_ids.has(eid):
				var e_node = client_enemies[eid]
				if is_instance_valid(e_node):
					enemies.erase(e_node)
					e_node.queue_free()
				to_erase.append(eid)
		for eid in to_erase:
			client_enemies.erase(eid)
	
	# Apply player bullet visuals (with explosion support)
	for b in client_p_bullets:
		if is_instance_valid(b):
			b.queue_free()
	client_p_bullets.clear()
	if data.has("p_bullets"):
		for bd in data["p_bullets"]:
			var b_node = BulletScene.instantiate()
			entity_layer.add_child(b_node)
			b_node.init_bullet(tile_dim, int(bd[2]), bool(bd[3]), false, false)
			b_node.position = Vector2(bd[0], bd[1])
			# Apply explosion state if present
			if bd.size() > 4 and bool(bd[4]):
				b_node.is_destroyed = true
				b_node.exploding = true
				b_node.explode_frame = int(bd[5]) if bd.size() > 5 else 0
			client_p_bullets.append(b_node)
	
	# Apply enemy bullet visuals (with explosion support)
	for b in client_e_bullets:
		if is_instance_valid(b):
			b.queue_free()
	client_e_bullets.clear()
	if data.has("e_bullets"):
		for bd in data["e_bullets"]:
			var b_node = BulletScene.instantiate()
			entity_layer.add_child(b_node)
			b_node.init_bullet(tile_dim, int(bd[2]), false, false, false)
			b_node.position = Vector2(bd[0], bd[1])
			# Apply explosion state if present
			if bd.size() > 3 and bool(bd[3]):
				b_node.is_destroyed = true
				b_node.exploding = true
				b_node.explode_frame = int(bd[4]) if bd.size() > 4 else 0
			client_e_bullets.append(b_node)
	
	# Apply mine visuals
	for m in client_mines:
		if is_instance_valid(m):
			m.queue_free()
	client_mines.clear()
	if data.has("mines"):
		for md in data["mines"]:
			var m_node = MineScene.instantiate()
			entity_layer.add_child(m_node)
			m_node.init_mine(tile_dim, Vector2(md[0], md[1]), int(md[2]))
			m_node.position = Vector2(md[0], md[1])
			m_node.is_moving = bool(md[3])
			if not m_node.is_moving:
				m_node.velocity = 0
			m_node.is_exploding = bool(md[4])
			m_node.fuse_timer = md[5]
			# Stop _process movement on client mines since position is synced
			m_node.is_dropped = not bool(md[4])
			client_mines.append(m_node)
	
	# Apply bonus
	if data.has("bonus"):
		var bd = data["bonus"]
		if active_bonus == null or not is_instance_valid(active_bonus):
			active_bonus = BonusScene.instantiate()
			entity_layer.add_child(active_bonus)
			active_bonus.init_bonus(tile_dim, int(bd[2]))
		active_bonus.position = Vector2(bd[0], bd[1])
	else:
		if active_bonus and is_instance_valid(active_bonus):
			active_bonus.queue_free()
			active_bonus = null
	
	# Apply game state
	if data.has("score"):
		total_score = int(data["score"])
	if data.has("stage_score"):
		stage_score = int(data["stage_score"])
	if data.has("enemy_lives"):
		enemy_lives = int(data["enemy_lives"])
	if data.has("frozen"):
		is_frozen = bool(data["frozen"])
	if data.has("state"):
		var server_state = int(data["state"])
		if server_state == GameState.GAME_OVER and state == GameState.PLAYING:
			do_game_over()
		elif server_state == GameState.STAGE_COMPLETE and state == GameState.PLAYING:
			state = GameState.STAGE_COMPLETE
			show_score_timer = SHOW_SCORE_DELAY
	
	# Update HUD
	update_hud()
	if hud:
		hud.update_enemy_count(enemy_lives)

func _get_enemy_ids() -> Array:
	var ids: Array = []
	for e in enemies:
		if is_instance_valid(e):
			ids.append(e.enemy_id)
	return ids
