extends Node2D

# Enemy tank - matches Enemy.java and HVE.java with sprite-based rendering

signal bullet_fired(bullet: Node2D)
signal destroyed(enemy: Node2D)

# Type
var tank_type: int = GameData.ObjectType.ST_TANK_A
var type_val: int = 0  # 0=A, 1=B, 2=C, 3=D (for spritesheet row)
var group: int = 1  # Armor level (1-4)
var enemy_id: int = 0
var is_hve: bool = false

# State
var is_dead: bool = false
var death_anim_done: bool = false
var is_frozen: bool = false
var has_bonus: bool = false
var is_spawning: bool = true

# Movement
var direction: int = GameData.Direction.DOWN
var speed: float = 0.0
var tile_dim: float = 0.0
var tank_size: float = 0.0

# AI
var target: Vector2 = Vector2.ZERO
var dir_change_timer: float = 0.0
var dir_change_interval: float = 0.5
var collision_count: int = 0

# Shooting
var fire_timer: float = 0.0
var fire_interval: float = 1.5
var bullet_speed: float = 1.0

# HVE specific
var hve_health: int = 20
var hve_max_health: int = 20
var hve_speed_mult: float = 1.0
var hve_bullet_mult: float = 1.0

# Visual
var color: Color = Color(0.8, 0.8, 0.8)
var death_timer: float = 0.0
const DEATH_ANIM_TIME: float = 0.5

# Bonus blinking - cycles 0,1,2; when >0 uses red flash (group 0) sprite
var life_frame: int = 0
var life_frame_timer: float = 0.0
const LIFE_FRAME_TIME: float = 1.0 / 32.0  # Match Java tick rate

# Spawn (creation) animation - ST_CREATE from spritesheet
var spawn_frame: int = 0
var spawn_frame_timer: float = 0.0
const SPAWN_FRAME_COUNT: int = 10
const SPAWN_FRAME_TIME: float = 1.0 / 32.0  # 1 game tick at 32 FPS
const SPAWN_SRC_X: int = 1008
const SPAWN_SRC_Y: int = 0
const SPAWN_SRC_W: int = 32
const SPAWN_SRC_H: int = 32

# Animation
var anim_frame: int = 0
var anim_timer: float = 0.0
const ANIM_SPEED: float = 0.12

# Collision
var last_valid_position: Vector2 = Vector2.ZERO

# Textures
var enemy_texture: Texture2D = null  # Individual PNG (HVE only)
var tank_texture: Texture2D = null   # tanktexture.png spritesheet (standard enemies)
var BulletScene = preload("res://scenes/bullet.tscn")

func init_enemy(td: float, type: int, grp: int, eid: int, hve: bool = false, v: float = 0.0, bv: float = 0.0) -> void:
	tile_dim = td
	tank_type = type
	group = grp
	enemy_id = eid
	is_hve = hve
	tank_size = tile_dim * 2
	direction = GameData.Direction.DOWN
	
	# Start with spawn animation
	is_spawning = true
	spawn_frame = 0
	spawn_frame_timer = 0.0
	
	# Load tanktexture.png spritesheet for spawn animation and standard enemies
	tank_texture = load("res://assets/sprites/tanktexture.png")
	
	if is_hve:
		hve_speed_mult = v
		hve_bullet_mult = bv
		speed = tile_dim * 3.2 * hve_speed_mult
		bullet_speed = hve_bullet_mult
		color = Color(0.5, 0, 0.5)
		fire_interval = 0.8
		hve_health = 20
		hve_max_health = 20
		enemy_texture = load("res://assets/sprites/hve.png")
	else:
		# Compute typeVal for spritesheet row lookup (0=A, 1=B, 2=C, 3=D)
		type_val = tank_type - GameData.ObjectType.ST_TANK_A  # ST_TANK_A=1, so type_val=0
		
		var speed_mult = GameData.ENEMY_SPEEDS.get(type, 1.0)
		speed = tile_dim * 3.2 * speed_mult
		
		match type:
			GameData.ObjectType.ST_TANK_A:
				color = Color(0.8, 0.8, 0.8)
				fire_interval = 2.0
			GameData.ObjectType.ST_TANK_B:
				color = Color(0.85, 0.6, 0.2)
				fire_interval = 1.5
			GameData.ObjectType.ST_TANK_C:
				color = Color(0.2, 0.7, 0.5)
				fire_interval = 1.0
				bullet_speed = 1.15
			GameData.ObjectType.ST_TANK_D:
				color = Color(0.7, 0.2, 0.2)
				fire_interval = 1.2
	
	# Higher group = more armor
	if group > 1:
		color = color.darkened(0.1 * (group - 1))
	
	dir_change_interval = randf_range(0.3, 1.0)
	last_valid_position = position

func update_ai(delta: float) -> void:
	if is_spawning:
		# Advance spawn animation
		spawn_frame_timer += delta
		if spawn_frame_timer >= SPAWN_FRAME_TIME:
			spawn_frame += 1
			spawn_frame_timer = 0.0
		if spawn_frame >= SPAWN_FRAME_COUNT:
			is_spawning = false
			spawn_frame = 0
		queue_redraw()
		return
	
	if is_dead or is_frozen:
		return
	
	# Bonus blinking - cycle life_frame through 0,1,2
	if has_bonus:
		life_frame_timer += delta
		if life_frame_timer >= LIFE_FRAME_TIME:
			life_frame = (life_frame + 1) % 3
			life_frame_timer = 0.0
	else:
		life_frame = 0
	
	# Direction change
	dir_change_timer += delta
	if dir_change_timer >= dir_change_interval or collision_count > 2:
		change_direction()
		dir_change_timer = 0.0
		dir_change_interval = randf_range(0.3, 1.0)
		collision_count = 0
	
	# Move
	last_valid_position = position
	var move_vec = direction_to_vector(direction) * speed * delta
	position += move_vec
	
	# Animate treads
	anim_timer += delta
	if anim_timer >= ANIM_SPEED:
		anim_frame = (anim_frame + 1) % 2
		anim_timer = 0.0
	
	# Fire
	fire_timer += delta
	if fire_timer >= fire_interval:
		fire()
		fire_timer = 0.0
	
	queue_redraw()

func change_direction() -> void:
	var target_prob = 0.85 if is_hve else 0.80
	
	if randf() < target_prob and target != Vector2.ZERO:
		var dx = target.x - position.x
		var dy = target.y - position.y
		
		if abs(dx) > abs(dy):
			direction = GameData.Direction.RIGHT if dx > 0 else GameData.Direction.LEFT
		else:
			direction = GameData.Direction.DOWN if dy > 0 else GameData.Direction.UP
		
		if randf() < 0.2:
			direction = randi() % 4
	else:
		direction = randi() % 4

func fire() -> void:
	if is_dead or is_frozen:
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
	
	bullet.init_bullet(tile_dim, direction, false, false, false, bullet_speed)
	bullet.position = bullet_pos
	bullet_fired.emit(bullet)

func take_hit(bullet) -> bool:
	if is_dead:
		return false
	
	if is_hve:
		hve_health -= 1
		if hve_health <= 0:
			die()
			return true
		queue_redraw()
		return false
	else:
		if group > 1:
			group -= 1
			match tank_type:
				GameData.ObjectType.ST_TANK_A:
					color = Color(0.8, 0.8, 0.8).darkened(0.1 * (group - 1))
				GameData.ObjectType.ST_TANK_B:
					color = Color(0.85, 0.6, 0.2).darkened(0.1 * (group - 1))
				GameData.ObjectType.ST_TANK_C:
					color = Color(0.2, 0.7, 0.5).darkened(0.1 * (group - 1))
				GameData.ObjectType.ST_TANK_D:
					color = Color(0.7, 0.2, 0.2).darkened(0.1 * (group - 1))
			queue_redraw()
			return false
		else:
			die()
			return true

func die() -> void:
	is_dead = true
	death_timer = DEATH_ANIM_TIME
	destroyed.emit(self)

func handle_collision() -> void:
	position = last_valid_position
	collision_count += 1
	if collision_count > 2:
		change_direction()
		collision_count = 0

func set_target(pos: Vector2) -> void:
	target = pos

func freeze() -> void:
	is_frozen = true

func unfreeze() -> void:
	is_frozen = false

func direction_to_vector(dir: int) -> Vector2:
	match dir:
		GameData.Direction.UP: return Vector2(0, -1)
		GameData.Direction.DOWN: return Vector2(0, 1)
		GameData.Direction.LEFT: return Vector2(-1, 0)
		GameData.Direction.RIGHT: return Vector2(1, 0)
	return Vector2.ZERO

func _process(delta: float) -> void:
	if is_dead:
		death_timer -= delta
		if death_timer <= 0:
			death_anim_done = true
		queue_redraw()

func _draw() -> void:
	if is_dead:
		if not death_anim_done:
			# Death explosion animation
			var progress = 1.0 - (death_timer / DEATH_ANIM_TIME)
			var radius = tank_size * (0.5 + progress * 0.8)
			var center = Vector2(tank_size / 2, tank_size / 2)
			draw_circle(center, radius, Color(1, 0.4, 0, 0.7 * (1 - progress)))
			draw_circle(center, radius * 0.6, Color(1, 0.8, 0, 0.5 * (1 - progress)))
			draw_circle(center, radius * 0.3, Color(1, 1, 0.5, 0.4 * (1 - progress)))
		return
	
	if is_spawning:
		# Draw ST_CREATE spawn animation from spritesheet
		if tank_texture and spawn_frame < SPAWN_FRAME_COUNT:
			var src_rect = Rect2(SPAWN_SRC_X, SPAWN_SRC_Y + spawn_frame * SPAWN_SRC_H, SPAWN_SRC_W, SPAWN_SRC_H)
			draw_texture_rect_region(tank_texture, Rect2(Vector2.ZERO, Vector2(tank_size, tank_size)), src_rect)
		return
	
	# Draw enemy tank
	if not is_hve and tank_texture:
		# Use tanktexture.png spritesheet for standard enemies
		# Row = 2 * type_val + anim_frame
		# Col = 4 * group + direction (or 4*0+direction when bonus-flashing)
		var frame_w = 32
		var frame_h = 32
		var col_group = 0 if (has_bonus and life_frame > 0) else group
		var col = 4 * col_group + direction
		var row = 2 * type_val + anim_frame
		var src_rect = Rect2(col * frame_w, row * frame_h, frame_w, frame_h)
		draw_texture_rect_region(tank_texture, Rect2(Vector2.ZERO, Vector2(tank_size, tank_size)), src_rect)
	elif is_hve and enemy_texture:
		# HVE uses separate PNG with rotation
		var center = Vector2(tank_size / 2, tank_size / 2)
		draw_set_transform(center, deg_to_rad(direction * 90.0), Vector2.ONE)
		draw_texture_rect(enemy_texture, Rect2(-tank_size/2, -tank_size/2, tank_size, tank_size), false)
		draw_set_transform(Vector2.ZERO, 0, Vector2.ONE)
	else:
		_draw_procedural()
	
	# HVE health bar
	if is_hve:
		var bar_width = tank_size
		var bar_height = 3.0
		var health_pct = float(hve_health) / float(hve_max_health)
		draw_rect(Rect2(0, -6, bar_width, bar_height), Color.RED)
		draw_rect(Rect2(0, -6, bar_width * health_pct, bar_height), Color.GREEN)
	
	# Frozen indicator
	if is_frozen:
		draw_rect(Rect2(Vector2.ZERO, Vector2(tank_size, tank_size)), Color(0, 0.5, 1, 0.3))

func _draw_procedural() -> void:
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
			# Tread marks
			for i in range(4):
				var y = tile_dim * 0.5 * i + (anim_frame * tile_dim * 0.25)
				if y < tank_size:
					draw_line(Vector2(0, y), Vector2(tread_w, y), body_color.darkened(0.5), 1)
					draw_line(Vector2(tank_size - tread_w, y), Vector2(tank_size, y), body_color.darkened(0.5), 1)
		GameData.Direction.LEFT, GameData.Direction.RIGHT:
			draw_rect(Rect2(0, 0, tank_size, tread_w), tread_color)
			draw_rect(Rect2(0, tank_size - tread_w, tank_size, tread_w), tread_color)
			for i in range(4):
				var x = tile_dim * 0.5 * i + (anim_frame * tile_dim * 0.25)
				if x < tank_size:
					draw_line(Vector2(x, 0), Vector2(x, tread_w), body_color.darkened(0.5), 1)
					draw_line(Vector2(x, tank_size - tread_w), Vector2(x, tank_size), body_color.darkened(0.5), 1)
	
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
	draw_circle(center, tile_dim * 0.35, body_color.lightened(0.15))
	
	# Group/armor indicator dots
	if group > 1:
		var count = group
		for i in range(count):
			var dot_pos = Vector2(tank_size / 2 - (count - 1) * 3 + i * 6, tank_size - 3)
			draw_circle(dot_pos, 1.5, Color.WHITE)
