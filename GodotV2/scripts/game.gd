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
	
	# Create bonus holder
	active_bonus = null
	
	# Update HUD
	if hud:
		hud.update_stage(level)
		hud.update_enemy_count(enemy_lives)
		hud.update_lives(player.lives if player else 3)
		hud.update_score(total_score)
	
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
	eagle_node = null
	gold_node = null

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
	# Player spawns at bottom center (4/13 width from left)
	var px = int(4.0 * GameData.GRID_SIZE / 13.0) * tile_dim
	var py = (GameData.GRID_SIZE - 1) * tile_dim
	player.position = Vector2(px, py)
	entity_layer.add_child(player)
	player.init_player(tile_dim, 1)
	player.bullet_fired.connect(_on_player_bullet_fired)
	player.mine_dropped.connect(_on_mine_dropped)

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
	
	# Check win/lose conditions
	check_game_state()
	
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
	
	# Check collisions
	check_all_collisions()
	
	# Update bonus
	update_bonus(delta)
	
	# Update HUD
	update_hud()

func check_game_state() -> void:
	# Stage complete: all enemies dead
	if enemy_lives <= 0 and enemy_count <= 0 and state == GameState.PLAYING:
		do_stage_complete()
		return
	
	# Game over: player dead or eagle destroyed
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
	if hud:
		hud.show_game_over()

func process_stage_complete(delta: float) -> void:
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
	
	# Spawn position (top of map, 3 possible positions)
	var spawn_pos_idx = randi() % 3
	var spawn_x = spawn_pos_idx * 6 * tile_dim * 2  # 6 tiles apart * 2 for full tile
	spawn_x = min(spawn_x, board_size.x - tile_dim * 2)
	var spawn_y = 0.0
	
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
			# Set target (player position)
			if player and player.lives > 0:
				enemy.set_target(player.position)
			else:
				enemy.set_target(Vector2(board_size.x / 2, board_size.y / 2))
			
			enemy.update_ai(delta)
			check_enemy_collision(enemy)
	
	for enemy in to_remove:
		enemies.erase(enemy)
		if is_instance_valid(enemy):
			enemy.queue_free()

func check_enemy_collision(enemy: Node2D) -> void:
	if not is_instance_valid(enemy) or enemy.is_dead:
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
					var obj_rect = Rect2(obj.position, Vector2(tile_dim, tile_dim))
					if enemy_rect.intersects(obj_rect):
						var obj_type = obj.get_meta("type") if obj.has_meta("type") else ""
						if obj_type in ["brick", "stone", "water"]:
							enemy.handle_collision()
	
	# Check against board boundaries
	if enemy.position.x < 0 or enemy.position.x > board_size.x - tile_dim * 2:
		enemy.handle_collision()
	if enemy.position.y < 0 or enemy.position.y > board_size.y - tile_dim * 2:
		enemy.handle_collision()
	
	# Check against other enemies
	for other in enemies:
		if other == enemy or not is_instance_valid(other) or other.is_dead:
			continue
		var other_rect = Rect2(other.position, Vector2(tile_dim * 2, tile_dim * 2))
		if enemy_rect.intersects(other_rect):
			enemy.handle_collision()

func check_all_collisions() -> void:
	if not player or player.lives <= 0:
		return
	
	check_player_collision()
	check_player_bullets()
	check_enemy_bullets_collision()
	check_player_bonus_collision()

func check_player_collision() -> void:
	if not is_instance_valid(player) or player.is_respawning:
		return
	var player_rect = Rect2(player.position, Vector2(tile_dim * 2, tile_dim * 2))
	
	# Check terrain collisions
	var grid_col = int(player.position.x / tile_dim)
	var grid_row = int(player.position.y / tile_dim)
	
	for r in range(max(0, grid_row - 1), min(level_objects.size(), grid_row + 3)):
		for c in range(max(0, grid_col - 1), min(level_objects[0].size() if level_objects.size() > 0 else 0, grid_col + 3)):
			if r < level_objects.size() and c < level_objects[r].size():
				var obj = level_objects[r][c]
				if obj != null and is_instance_valid(obj):
					var obj_rect = Rect2(obj.position, Vector2(tile_dim, tile_dim))
					if player_rect.intersects(obj_rect):
						var obj_type = obj.get_meta("type") if obj.has_meta("type") else ""
						match obj_type:
							"brick", "stone":
								player.handle_terrain_collision(obj)
							"water":
								if not player.has_boat:
									player.handle_terrain_collision(obj)
							"ice":
								player.on_ice = true
	
	# Check board boundaries
	player.position.x = clamp(player.position.x, 0, board_size.x - tile_dim * 2)
	player.position.y = clamp(player.position.y, 0, board_size.y - tile_dim * 2)
	
	# Check enemy collision
	for enemy in enemies:
		if not is_instance_valid(enemy) or enemy.is_dead:
			continue
		var enemy_rect = Rect2(enemy.position, Vector2(tile_dim * 2, tile_dim * 2))
		if player_rect.intersects(enemy_rect):
			player.handle_terrain_collision(enemy)
	
	# Check eagle collision
	if eagle_node and is_instance_valid(eagle_node):
		var eagle_rect = Rect2(eagle_node.position, Vector2(tile_dim * 2, tile_dim * 2))
		if player_rect.intersects(eagle_rect):
			player.handle_terrain_collision(eagle_node)

func check_player_bullets() -> void:
	var bullets_to_remove: Array = []
	for bullet in player.bullets:
		if not is_instance_valid(bullet):
			bullets_to_remove.append(bullet)
			continue
		if bullet.is_destroyed:
			bullets_to_remove.append(bullet)
			continue
		
		var bullet_rect = Rect2(bullet.position, Vector2(bullet.size, bullet.size))
		
		# Check against terrain
		var grid_col = int(bullet.position.x / tile_dim)
		var grid_row = int(bullet.position.y / tile_dim)
		
		for r in range(max(0, grid_row - 1), min(level_objects.size(), grid_row + 2)):
			for c in range(max(0, grid_col - 1), min(level_objects[0].size() if level_objects.size() > 0 else 0, grid_col + 2)):
				if r < level_objects.size() and c < level_objects[r].size():
					var obj = level_objects[r][c]
					if obj != null and is_instance_valid(obj):
						var obj_rect = Rect2(obj.position, Vector2(tile_dim, tile_dim))
						if bullet_rect.intersects(obj_rect):
							var obj_type = obj.get_meta("type") if obj.has_meta("type") else ""
							match obj_type:
								"brick":
									obj.take_damage(bullet.direction)
									if obj.is_destroyed():
										level_objects[r][c] = null
									bullet.destroy()
								"stone":
									if bullet.break_wall:
										obj.queue_free()
										level_objects[r][c] = null
									bullet.destroy()
								"bush":
									if bullet.clear_bush:
										obj.queue_free()
										# Bushes don't stop bullets in original
		
		# Check against enemies
		for enemy in enemies:
			if not is_instance_valid(enemy) or enemy.is_dead:
				continue
			var enemy_rect = Rect2(enemy.position, Vector2(tile_dim * 2, tile_dim * 2))
			if bullet_rect.intersects(enemy_rect):
				var killed = enemy.take_hit(bullet)
				bullet.destroy()
				if killed:
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
		
		# Check against board boundaries
		if bullet.position.x < 0 or bullet.position.x > board_size.x or \
		   bullet.position.y < 0 or bullet.position.y > board_size.y:
			bullet.destroy()
		
		# Check against eagle
		if eagle_node and is_instance_valid(eagle_node) and not eagle_node.is_destroyed:
			var eagle_rect = Rect2(eagle_node.position, Vector2(tile_dim * 2, tile_dim * 2))
			if bullet_rect.intersects(eagle_rect):
				if is_eagle_protected:
					bullet.destroy()
				else:
					eagle_node.take_damage()
					bullet.destroy()
		
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
		player.bullets.erase(bullet)

func check_enemy_bullets_collision() -> void:
	var to_remove: Array = []
	for bullet in enemy_bullets:
		if not is_instance_valid(bullet):
			to_remove.append(bullet)
			continue
		if bullet.is_destroyed:
			to_remove.append(bullet)
			continue
		
		var bullet_rect = Rect2(bullet.position, Vector2(bullet.size, bullet.size))
		
		# Check against player
		if player and is_instance_valid(player) and not player.is_respawning and player.lives > 0:
			var player_rect = Rect2(player.position, Vector2(tile_dim * 2, tile_dim * 2))
			if bullet_rect.intersects(player_rect):
				if player.has_shield:
					bullet.destroy()
				else:
					player.take_hit()
					bullet.destroy()
					if hud:
						hud.update_lives(player.lives)
				continue
		
		# Check against terrain
		var grid_col = int(bullet.position.x / tile_dim)
		var grid_row = int(bullet.position.y / tile_dim)
		
		for r in range(max(0, grid_row - 1), min(level_objects.size(), grid_row + 2)):
			for c in range(max(0, grid_col - 1), min(level_objects[0].size() if level_objects.size() > 0 else 0, grid_col + 2)):
				if r < level_objects.size() and c < level_objects[r].size():
					var obj = level_objects[r][c]
					if obj != null and is_instance_valid(obj):
						var obj_rect = Rect2(obj.position, Vector2(tile_dim, tile_dim))
						if bullet_rect.intersects(obj_rect):
							var obj_type = obj.get_meta("type") if obj.has_meta("type") else ""
							match obj_type:
								"brick":
									obj.take_damage(bullet.direction)
									if obj.is_destroyed():
										level_objects[r][c] = null
									bullet.destroy()
								"stone":
									bullet.destroy()
		
		# Check against eagle
		if eagle_node and is_instance_valid(eagle_node) and not eagle_node.is_destroyed:
			var eagle_rect = Rect2(eagle_node.position, Vector2(tile_dim * 2, tile_dim * 2))
			if bullet_rect.intersects(eagle_rect):
				if is_eagle_protected:
					bullet.destroy()
				else:
					eagle_node.take_damage()
					bullet.destroy()
		
		# Check board boundaries
		if bullet.position.x < 0 or bullet.position.x > board_size.x or \
		   bullet.position.y < 0 or bullet.position.y > board_size.y:
			bullet.destroy()
	
	for bullet in to_remove:
		enemy_bullets.erase(bullet)
		if is_instance_valid(bullet):
			bullet.queue_free()

func check_player_bonus_collision() -> void:
	if active_bonus == null or not is_instance_valid(active_bonus):
		return
	if not is_instance_valid(player) or player.is_respawning:
		return
	
	var player_rect = Rect2(player.position, Vector2(tile_dim * 2, tile_dim * 2))
	var bonus_rect = Rect2(active_bonus.position, Vector2(tile_dim * 2, tile_dim * 2))
	
	if player_rect.intersects(bonus_rect):
		apply_bonus(active_bonus.bonus_type)
		active_bonus.queue_free()
		active_bonus = null

func spawn_bonus() -> void:
	if active_bonus != null and is_instance_valid(active_bonus):
		return
	
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

func apply_bonus(bonus_type: int) -> void:
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
			if player:
				player.activate_shield()
		GameData.BonusType.CLOCK:
			freeze_enemies()
		GameData.BonusType.SHOVEL:
			protect_eagle()
		GameData.BonusType.TANK:
			if player:
				player.lives += 1
				if hud:
					hud.update_lives(player.lives)
		GameData.BonusType.STAR:
			if player:
				player.upgrade_star()
		GameData.BonusType.GUN:
			if player:
				player.upgrade_gun()
		GameData.BonusType.BOAT:
			if player:
				player.has_boat = true
		GameData.BonusType.MINE:
			if player:
				player.mine_count += 1
		GameData.BonusType.BUILDER:
			if player:
				player.builder_count += 1
	
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
	eagle_protect_original.clear()

func pause_game() -> void:
	state = GameState.PAUSED
	if hud:
		hud.show_pause_menu()

func resume_game() -> void:
	state = GameState.PLAYING
	if hud:
		hud.hide_pause_menu()

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
		if player:
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

func _on_hud_resume() -> void:
	resume_game()

func _on_hud_retry() -> void:
	retry_level()

func _on_hud_next() -> void:
	next_level()

func _on_hud_quit() -> void:
	get_tree().change_scene_to_file("res://scenes/main_menu.tscn")
