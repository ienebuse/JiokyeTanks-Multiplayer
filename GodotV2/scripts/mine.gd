extends Node2D

# Mine - matches Mine.java
# Player-deployed explosive with sprite rendering

var tile_dim: float = 0.0
var direction: int = GameData.Direction.UP
var is_dropped: bool = false
var is_moving: bool = false
var is_exploding: bool = false
var explode_done: bool = false

# Physics
var velocity: float = 0.0
var acceleration: float = 0.0

# Explosion
var explode_timer: float = 0.0
const EXPLODE_TIME: float = 0.5
var fuse_timer: float = 4.0

var bomb_texture: Texture2D = null

func init_mine(td: float, pos: Vector2, dir: int) -> void:
	tile_dim = td
	direction = dir
	position = pos
	is_dropped = true
	is_moving = true
	velocity = tile_dim * 8.0
	acceleration = -tile_dim * 2.0
	bomb_texture = load("res://assets/sprites/bomb.png")

func _process(delta: float) -> void:
	if is_exploding:
		explode_timer -= delta
		if explode_timer <= 0:
			explode_done = true
			queue_free()
		queue_redraw()
		return
	
	if is_moving:
		velocity += acceleration * delta
		if velocity <= 0:
			is_moving = false
			velocity = 0
		else:
			var dir_vec = direction_to_vector(direction)
			position += dir_vec * velocity * delta
	elif is_dropped:
		fuse_timer -= delta
		if fuse_timer <= 0:
			explode()
	
	queue_redraw()

func explode() -> void:
	is_exploding = true
	explode_timer = EXPLODE_TIME

func get_explosion_rect() -> Rect2:
	# Explosion radius = 2 tiles
	var radius = tile_dim * 2
	return Rect2(position.x - radius, position.y - radius, radius * 2, radius * 2)

func direction_to_vector(dir: int) -> Vector2:
	match dir:
		GameData.Direction.UP: return Vector2(0, -1)
		GameData.Direction.DOWN: return Vector2(0, 1)
		GameData.Direction.LEFT: return Vector2(-1, 0)
		GameData.Direction.RIGHT: return Vector2(1, 0)
	return Vector2.ZERO

func _draw() -> void:
	var size = tile_dim
	
	if is_exploding:
		# Explosion effect - multi-layered expanding circles
		var progress = 1.0 - (explode_timer / EXPLODE_TIME)
		var radius = size * 2.5 * progress
		draw_circle(Vector2.ZERO, radius, Color(1, 0.3, 0, 0.5 * (1 - progress)))
		draw_circle(Vector2.ZERO, radius * 0.7, Color(1, 0.6, 0, 0.7 * (1 - progress)))
		draw_circle(Vector2.ZERO, radius * 0.3, Color(1, 1, 0.5, 0.9 * (1 - progress)))
		return
	
	# Draw mine using bomb sprite or fallback
	if bomb_texture:
		# bomb.png is 48x16 (3 frames of 16x16)
		var frame = 0 if is_moving else (1 if fuse_timer > 2.0 else 2)
		var src = Rect2(frame * 16, 0, 16, 16)
		draw_texture_rect_region(bomb_texture, Rect2(-size/2, -size/2, size, size), src)
	else:
		draw_circle(Vector2.ZERO, size * 0.4, Color(0.3, 0.3, 0.3))
		draw_circle(Vector2.ZERO, size * 0.25, Color(0.5, 0, 0))
	
	# Fuse indicator (pulsing when active)
	if not is_moving:
		var fuse_pct = fuse_timer / 4.0
		var blink = fmod(Time.get_ticks_msec() / 200.0, 2.0) > 1.0
		if blink:
			draw_circle(Vector2.ZERO, size * 0.12, Color(1, fuse_pct, 0))
