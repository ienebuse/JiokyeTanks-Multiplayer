extends Node2D

# Mine - matches Mine.java
# Player-deployed explosive

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

func init_mine(td: float, pos: Vector2, dir: int) -> void:
	tile_dim = td
	direction = dir
	position = pos
	is_dropped = true
	is_moving = true
	velocity = tile_dim * 8.0
	acceleration = -tile_dim * 2.0

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
		# Explosion - expanding circles
		var progress = 1.0 - (explode_timer / EXPLODE_TIME)
		var radius = size * 2 * progress
		draw_circle(Vector2.ZERO, radius, Color(1, 0.3, 0, 0.6 * (1 - progress)))
		draw_circle(Vector2.ZERO, radius * 0.7, Color(1, 0.7, 0, 0.8 * (1 - progress)))
		draw_circle(Vector2.ZERO, radius * 0.3, Color(1, 1, 0.5, 1.0 * (1 - progress)))
		return
	
	# Draw mine
	draw_circle(Vector2.ZERO, size * 0.4, Color(0.3, 0.3, 0.3))
	draw_circle(Vector2.ZERO, size * 0.25, Color(0.5, 0, 0))
	
	# Fuse indicator
	if not is_moving:
		var fuse_pct = fuse_timer / 4.0
		var fuse_color = Color(1, fuse_pct, 0)
		draw_circle(Vector2.ZERO, size * 0.1, fuse_color)
