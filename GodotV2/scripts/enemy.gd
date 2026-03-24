extends Node2D

# Enemy tank - matches Enemy.java and HVE.java

signal bullet_fired(bullet: Node2D)
signal destroyed(enemy: Node2D)

# Type
var tank_type: int = GameData.ObjectType.ST_TANK_A
var group: int = 1  # Armor level (1-4)
var enemy_id: int = 0
var is_hve: bool = false

# State
var is_dead: bool = false
var death_anim_done: bool = false
var is_frozen: bool = false
var has_bonus: bool = false

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
var color: Color = GameData.COLOR_ENEMY_A
var death_timer: float = 0.0
const DEATH_ANIM_TIME: float = 0.5

# Collision
var last_valid_position: Vector2 = Vector2.ZERO

var BulletScene = preload("res://scenes/bullet.tscn")

func init_enemy(td: float, type: int, grp: int, eid: int, hve: bool = false, v: float = 0.0, bv: float = 0.0) -> void:
	tile_dim = td
	tank_type = type
	group = grp
	enemy_id = eid
	is_hve = hve
	tank_size = tile_dim * 2
	direction = GameData.Direction.DOWN
	
	if is_hve:
		hve_speed_mult = v
		hve_bullet_mult = bv
		speed = tile_dim * 6.0 / GameData.FPS * GameData.FPS * hve_speed_mult
		bullet_speed = hve_bullet_mult
		color = GameData.COLOR_HVE
		fire_interval = 0.8
		hve_health = 20
		hve_max_health = 20
	else:
		var speed_mult = GameData.ENEMY_SPEEDS.get(type, 1.0)
		speed = tile_dim * 6.0 / GameData.FPS * GameData.FPS * speed_mult
		
		match type:
			GameData.ObjectType.ST_TANK_A:
				color = GameData.COLOR_ENEMY_A
				fire_interval = 2.0
			GameData.ObjectType.ST_TANK_B:
				color = GameData.COLOR_ENEMY_B
				fire_interval = 1.5
			GameData.ObjectType.ST_TANK_C:
				color = GameData.COLOR_ENEMY_C
				fire_interval = 1.0
				bullet_speed = 1.15
			GameData.ObjectType.ST_TANK_D:
				color = GameData.COLOR_ENEMY_D
				fire_interval = 1.2
	
	# Higher group = more armor, shown by darker color
	if group > 1:
		color = color.darkened(0.1 * (group - 1))
	
	dir_change_interval = randf_range(0.3, 1.0)
	last_valid_position = position

func update_ai(delta: float) -> void:
	if is_dead or is_frozen:
		return
	
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
	
	# Fire
	fire_timer += delta
	if fire_timer >= fire_interval:
		fire()
		fire_timer = 0.0
	
	queue_redraw()

func change_direction() -> void:
	var target_prob = 0.85 if is_hve else 0.80
	
	if randf() < target_prob and target != Vector2.ZERO:
		# Target player
		var dx = target.x - position.x
		var dy = target.y - position.y
		
		if abs(dx) > abs(dy):
			if dx > 0:
				direction = GameData.Direction.RIGHT
			else:
				direction = GameData.Direction.LEFT
		else:
			if dy > 0:
				direction = GameData.Direction.DOWN
			else:
				direction = GameData.Direction.UP
		
		# Add some randomness
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
	var bullet_size = tile_dim * 0.5
	
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
			# Lighten color as armor decreases
			match tank_type:
				GameData.ObjectType.ST_TANK_A:
					color = GameData.COLOR_ENEMY_A.darkened(0.1 * (group - 1))
				GameData.ObjectType.ST_TANK_B:
					color = GameData.COLOR_ENEMY_B.darkened(0.1 * (group - 1))
				GameData.ObjectType.ST_TANK_C:
					color = GameData.COLOR_ENEMY_C.darkened(0.1 * (group - 1))
				GameData.ObjectType.ST_TANK_D:
					color = GameData.COLOR_ENEMY_D.darkened(0.1 * (group - 1))
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
			# Death animation - expanding circle
			var progress = 1.0 - (death_timer / DEATH_ANIM_TIME)
			var radius = tank_size * progress
			var center = Vector2(tank_size / 2, tank_size / 2)
			draw_circle(center, radius, Color(1, 0.5, 0, 0.8 * (1 - progress)))
			draw_circle(center, radius * 0.6, Color(1, 1, 0, 0.6 * (1 - progress)))
		return
	
	# Draw tank body
	var body_rect = Rect2(Vector2.ZERO, Vector2(tank_size, tank_size))
	draw_rect(body_rect, color)
	
	# Draw direction indicator (barrel)
	var barrel_color = color.lightened(0.3)
	var center = Vector2(tank_size / 2, tank_size / 2)
	var barrel_width = tile_dim * 0.4
	
	match direction:
		GameData.Direction.UP:
			draw_rect(Rect2(center.x - barrel_width / 2, 0, barrel_width, center.y), barrel_color)
		GameData.Direction.DOWN:
			draw_rect(Rect2(center.x - barrel_width / 2, center.y, barrel_width, center.y), barrel_color)
		GameData.Direction.LEFT:
			draw_rect(Rect2(0, center.y - barrel_width / 2, center.x, barrel_width), barrel_color)
		GameData.Direction.RIGHT:
			draw_rect(Rect2(center.x, center.y - barrel_width / 2, center.x, barrel_width), barrel_color)
	
	# Draw treads
	var tread_color = color.darkened(0.3)
	var tread_width = tile_dim * 0.3
	match direction:
		GameData.Direction.UP, GameData.Direction.DOWN:
			draw_rect(Rect2(0, 0, tread_width, tank_size), tread_color)
			draw_rect(Rect2(tank_size - tread_width, 0, tread_width, tank_size), tread_color)
		GameData.Direction.LEFT, GameData.Direction.RIGHT:
			draw_rect(Rect2(0, 0, tank_size, tread_width), tread_color)
			draw_rect(Rect2(0, tank_size - tread_width, tank_size, tread_width), tread_color)
	
	# Group/armor indicator
	if group > 1 or is_hve:
		var indicator_color = Color.WHITE
		var count = group if not is_hve else min(hve_health / 5 + 1, 4)
		for i in range(count):
			var dot_pos = Vector2(tank_size / 2 - (count - 1) * 3 + i * 6, tank_size - 4)
			draw_circle(dot_pos, 2, indicator_color)
	
	# HVE health bar
	if is_hve:
		var bar_width = tank_size
		var bar_height = 3.0
		var health_pct = float(hve_health) / float(hve_max_health)
		draw_rect(Rect2(0, -6, bar_width, bar_height), Color.RED)
		draw_rect(Rect2(0, -6, bar_width * health_pct, bar_height), Color.GREEN)
	
	# Bonus indicator
	if has_bonus:
		var blink = fmod(Time.get_ticks_msec() / 200.0, 2.0) > 1.0
		if blink:
			draw_rect(Rect2(Vector2.ZERO, Vector2(tank_size, tank_size)), Color(1, 0, 0, 0.3))
	
	# Frozen indicator
	if is_frozen:
		draw_rect(Rect2(Vector2.ZERO, Vector2(tank_size, tank_size)), Color(0, 0.5, 1, 0.3))
