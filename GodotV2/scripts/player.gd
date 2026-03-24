extends Node2D

# Player tank - matches Player.java with sprite-based rendering

signal bullet_fired(bullet: Node2D)
signal mine_dropped(mine_node: Node2D)

# State
var player_num: int = 1
var lives: int = 3
var is_respawning: bool = false
var respawn_timer: float = 0.0
const RESPAWN_TIME: float = 2.0
const BASE_SPEED_TILES_PER_SEC: float = 3.2  # Matches original ~3 tiles/sec at FPS=32

# Movement
var direction: int = GameData.Direction.UP
var moving: bool = false
var speed: float = 0.0
var tile_dim: float = 0.0
var on_ice: bool = false
var ice_sliding: bool = false
var ice_direction: int = GameData.Direction.UP
var move_enabled: bool = true

# Shooting
var max_bullets: int = 1
var bullets: Array = []
var fire_enabled: bool = true
var reload_timer: float = 0.0
const RELOAD_TIME: float = 0.3
var bullet_speed_multiplier: float = 1.0

# Upgrades
var armour: int = 0
var star_count: int = 0
var has_shield: bool = false
var shield_timer: float = 0.0
var has_boat: bool = false
var break_wall: bool = false
var clear_bush: bool = false

# Mine
var mine_count: int = 0
var builder_count: int = 0

# Visual
var tank_size: float = 0.0
var color: Color = Color(1.0, 0.85, 0.0)
var shield_blink: bool = true
var shield_blink_timer: float = 0.0
var respawn_blink: bool = true
var respawn_blink_timer: float = 0.0

# Animation
var anim_frame: int = 0
var anim_timer: float = 0.0
const ANIM_SPEED: float = 0.1  # Frame duration

# Collision
var last_valid_position: Vector2 = Vector2.ZERO

# Textures
var tank_texture: Texture2D = null
var BulletScene = preload("res://scenes/bullet.tscn")

func init_player(td: float, p_num: int) -> void:
	tile_dim = td
	player_num = p_num
	tank_size = tile_dim * 2
	speed = tile_dim * BASE_SPEED_TILES_PER_SEC
	color = Color(1.0, 0.85, 0.0) if p_num == 1 else Color(0.0, 0.85, 0.0)
	
	# Load tank texture from tanktexture.png spritesheet
	tank_texture = load("res://assets/sprites/tanktexture.png")
	
	activate_shield()
	last_valid_position = position

func _process(delta: float) -> void:
	if is_respawning:
		respawn_timer -= delta
		respawn_blink_timer += delta
		if respawn_blink_timer >= 0.1:
			respawn_blink = not respawn_blink
			respawn_blink_timer = 0.0
		if respawn_timer <= 0:
			is_respawning = false
		queue_redraw()
		return
	
	if lives <= 0:
		return
	
	# Handle input
	handle_input(delta)
	
	# Shield timer
	if has_shield:
		shield_timer -= delta
		shield_blink_timer += delta
		if shield_blink_timer >= 0.15:
			shield_blink = not shield_blink
			shield_blink_timer = 0.0
		if shield_timer <= 0:
			has_shield = false
	
	# Reload timer
	if reload_timer > 0:
		reload_timer -= delta
	
	# Update bullets
	update_bullets()
	
	# Ice slippage
	if on_ice and not moving and ice_sliding:
		var move_vec = direction_to_vector(ice_direction) * speed * 0.5 * delta
		position += move_vec
		ice_sliding = false
	on_ice = false
	
	# Animation
	if moving:
		anim_timer += delta
		if anim_timer >= ANIM_SPEED:
			anim_frame = (anim_frame + 1) % 2
			anim_timer = 0.0
	
	# Snap to grid
	snap_to_grid()
	
	queue_redraw()

func handle_input(delta: float) -> void:
	if not move_enabled:
		return
	
	moving = false
	var new_dir = direction
	
	if Input.is_action_pressed("move_up"):
		new_dir = GameData.Direction.UP
		moving = true
	elif Input.is_action_pressed("move_down"):
		new_dir = GameData.Direction.DOWN
		moving = true
	elif Input.is_action_pressed("move_left"):
		new_dir = GameData.Direction.LEFT
		moving = true
	elif Input.is_action_pressed("move_right"):
		new_dir = GameData.Direction.RIGHT
		moving = true
	
	if moving:
		if new_dir != direction:
			direction = new_dir
			snap_to_grid()
		
		last_valid_position = position
		var move_vec = direction_to_vector(direction) * speed * delta
		position += move_vec
		
		if on_ice:
			ice_sliding = true
			ice_direction = direction
	
	# Fire
	if Input.is_action_just_pressed("fire"):
		fire()
	
	# Mine
	if Input.is_action_just_pressed("drop_mine"):
		drop_mine()

func fire() -> void:
	if not fire_enabled or is_respawning or lives <= 0:
		return
	if bullets.size() >= max_bullets:
		return
	if reload_timer > 0 and bullets.size() > 0:
		return
	
	var bullet = BulletScene.instantiate()
	var bullet_pos = position
	var half_tank = tank_size / 2.0
	var bullet_size = tile_dim * 0.4
	
	match direction:
		GameData.Direction.UP:
			bullet_pos = Vector2(position.x + half_tank - bullet_size / 2, position.y - bullet_size)
		GameData.Direction.DOWN:
			bullet_pos = Vector2(position.x + half_tank - bullet_size / 2, position.y + tank_size)
		GameData.Direction.LEFT:
			bullet_pos = Vector2(position.x - bullet_size, position.y + half_tank - bullet_size / 2)
		GameData.Direction.RIGHT:
			bullet_pos = Vector2(position.x + tank_size, position.y + half_tank - bullet_size / 2)
	
	bullet.init_bullet(tile_dim, direction, true, break_wall, clear_bush, bullet_speed_multiplier)
	bullet.position = bullet_pos
	bullets.append(bullet)
	reload_timer = RELOAD_TIME
	bullet_fired.emit(bullet)

func drop_mine() -> void:
	if mine_count <= 0 or is_respawning:
		return
	mine_count -= 1
	var mine_node = preload("res://scenes/mine.tscn").instantiate()
	mine_node.init_mine(tile_dim, position + Vector2(tank_size / 2, tank_size / 2), direction)
	mine_dropped.emit(mine_node)

func update_bullets() -> void:
	var to_remove: Array = []
	for bullet in bullets:
		if not is_instance_valid(bullet) or bullet.is_destroyed:
			to_remove.append(bullet)
	for bullet in to_remove:
		bullets.erase(bullet)

func take_hit() -> void:
	if has_shield:
		return
	if armour > 0:
		armour -= 1
		return
	
	lives -= 1
	if lives > 0:
		respawn()
	else:
		visible = false

func respawn() -> void:
	is_respawning = true
	respawn_timer = RESPAWN_TIME
	
	# Reset position
	var px = int(4.0 * GameData.GRID_SIZE / 13.0) * tile_dim
	var py = (GameData.GRID_SIZE - 2) * tile_dim
	position = Vector2(px, py)
	
	# Reset upgrades
	direction = GameData.Direction.UP
	armour = 0
	star_count = 0
	has_boat = false
	break_wall = false
	clear_bush = false
	max_bullets = 1
	speed = tile_dim * BASE_SPEED_TILES_PER_SEC
	activate_shield()

func activate_shield() -> void:
	has_shield = true
	shield_timer = GameData.SHIELD_TIME
	shield_blink = true
	shield_blink_timer = 0.0

func upgrade_star() -> void:
	star_count += 1
	match star_count:
		1:
			speed *= 1.1
		2:
			max_bullets = 2
			armour = min(armour + 1, 3)
		3:
			clear_bush = true
			armour = min(armour + 1, 3)

func upgrade_gun() -> void:
	break_wall = true
	max_bullets = 2
	speed *= 1.05
	bullet_speed_multiplier = 1.15

func snap_to_grid() -> void:
	var half_tile = tile_dim / 2.0
	match direction:
		GameData.Direction.UP, GameData.Direction.DOWN:
			position.x = round(position.x / half_tile) * half_tile
		GameData.Direction.LEFT, GameData.Direction.RIGHT:
			position.y = round(position.y / half_tile) * half_tile

func handle_terrain_collision(obj: Node2D) -> void:
	position = last_valid_position
	if on_ice:
		ice_sliding = false

func direction_to_vector(dir: int) -> Vector2:
	match dir:
		GameData.Direction.UP: return Vector2(0, -1)
		GameData.Direction.DOWN: return Vector2(0, 1)
		GameData.Direction.LEFT: return Vector2(-1, 0)
		GameData.Direction.RIGHT: return Vector2(1, 0)
	return Vector2.ZERO

func _draw() -> void:
	if lives <= 0:
		return
	
	if is_respawning and not respawn_blink:
		return
	
	# Draw using tank sprite from tanktexture.png
	# The spritesheet has tanks arranged in rows of frames
	# Player 1 tank is in the first row (y=0) of the spritesheet
	# Each frame is 28px wide (based on the Java sprite setup: w*28 columns)
	# The spritesheet is 1216x512, tank frame size is ~16x16 at original scale
	
	if tank_texture:
		# Tank sprite from spritesheet
		# Original layout: Each tank row has frames for different directions
		# We use a simple 16x16 region from the texture, row 0 for player
		var frame_w = 16
		var frame_h = 16
		var src_x = anim_frame * frame_w
		var src_y = 0  # Row 0 = player 1 base tank
		
		# Draw tank from spritesheet
		var src_rect = Rect2(src_x, src_y, frame_w, frame_h)
		var dest_rect = Rect2(Vector2.ZERO, Vector2(tank_size, tank_size))
		
		# Apply direction by rotation or mirroring
		# For simplicity, draw with a rotation transform
		var center = Vector2(tank_size / 2, tank_size / 2)
		draw_set_transform(center, deg_to_rad(direction * 90.0), Vector2.ONE)
		draw_texture_rect_region(tank_texture, Rect2(-tank_size/2, -tank_size/2, tank_size, tank_size), src_rect)
		draw_set_transform(Vector2.ZERO, 0, Vector2.ONE)
	else:
		_draw_procedural()
	
	# Draw shield
	if has_shield and shield_blink:
		var shield_alpha = 0.4
		var shield_color = Color(0.3, 0.6, 1.0, shield_alpha)
		draw_rect(Rect2(-2, -2, tank_size + 4, tank_size + 4), shield_color, false, 2.0)
		draw_rect(Rect2(-1, -1, tank_size + 2, tank_size + 2), Color(0.5, 0.8, 1.0, shield_alpha * 0.5), false, 1.0)

func _draw_procedural() -> void:
	# Fallback procedural drawing for the tank
	var body_color = color
	var barrel_color = color.lightened(0.3)
	var tread_color = color.darkened(0.3)
	var center = Vector2(tank_size / 2, tank_size / 2)
	var barrel_w = tile_dim * 0.35
	var tread_w = tile_dim * 0.25
	
	# Treads
	match direction:
		GameData.Direction.UP, GameData.Direction.DOWN:
			draw_rect(Rect2(0, 0, tread_w, tank_size), tread_color)
			draw_rect(Rect2(tank_size - tread_w, 0, tread_w, tank_size), tread_color)
		GameData.Direction.LEFT, GameData.Direction.RIGHT:
			draw_rect(Rect2(0, 0, tank_size, tread_w), tread_color)
			draw_rect(Rect2(0, tank_size - tread_w, tank_size, tread_w), tread_color)
	
	# Body
	var body_inset = tread_w * 0.8
	match direction:
		GameData.Direction.UP, GameData.Direction.DOWN:
			draw_rect(Rect2(body_inset, body_inset * 0.5, tank_size - body_inset * 2, tank_size - body_inset), body_color)
		GameData.Direction.LEFT, GameData.Direction.RIGHT:
			draw_rect(Rect2(body_inset * 0.5, body_inset, tank_size - body_inset, tank_size - body_inset * 2), body_color)
	
	# Barrel
	match direction:
		GameData.Direction.UP:
			draw_rect(Rect2(center.x - barrel_w / 2, 0, barrel_w, center.y), barrel_color)
		GameData.Direction.DOWN:
			draw_rect(Rect2(center.x - barrel_w / 2, center.y, barrel_w, center.y), barrel_color)
		GameData.Direction.LEFT:
			draw_rect(Rect2(0, center.y - barrel_w / 2, center.x, barrel_w), barrel_color)
		GameData.Direction.RIGHT:
			draw_rect(Rect2(center.x, center.y - barrel_w / 2, center.x, barrel_w), barrel_color)
	
	# Center turret
	var turret_r = tile_dim * 0.35
	draw_circle(center, turret_r, body_color.lightened(0.15))
	
	# Armour indicator
	if armour > 0:
		for i in range(armour):
			var dot_pos = Vector2(tank_size / 2 - (armour - 1) * 3 + i * 6, tank_size - 4)
			draw_circle(dot_pos, 2, Color.WHITE)
