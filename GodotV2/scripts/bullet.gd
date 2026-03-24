extends Node2D

# Bullet - matches Bullet.java with proper collision expansion

var direction: int = GameData.Direction.UP
var speed: float = 0.0
var tile_dim: float = 0.0
var size: float = 0.0
var from_player: bool = false
var break_wall: bool = false
var clear_bush: bool = false
var is_destroyed: bool = false

# Explosion animation
var exploding: bool = false
var explode_timer: float = 0.0
const EXPLODE_TIME: float = 0.15

func init_bullet(td: float, dir: int, player: bool, brk: bool = false, clr: bool = false, speed_mult: float = 1.0) -> void:
	tile_dim = td
	direction = dir
	from_player = player
	break_wall = brk
	clear_bush = clr
	size = tile_dim * 0.4
	# Bullet speed: ~15 tiles/sec matches original feel
	speed = tile_dim * 12.0 * speed_mult

func get_collision_rect() -> Rect2:
	# Matching Java Bullet.collides_with: expand perpendicular to direction
	var rect = Rect2(position, Vector2(size, size))
	var offset = max(1, int(tile_dim / 4))
	match direction:
		GameData.Direction.UP, GameData.Direction.DOWN:
			rect.position.x -= offset
			rect.size.x += offset * 2
		GameData.Direction.LEFT, GameData.Direction.RIGHT:
			rect.position.y -= offset
			rect.size.y += offset * 2
	return rect

func _process(delta: float) -> void:
	if is_destroyed:
		if exploding:
			explode_timer -= delta
			if explode_timer <= 0:
				queue_free()
			queue_redraw()
		return
	
	# Move bullet
	var move_vec = direction_to_vector(direction) * speed * delta
	position += move_vec
	queue_redraw()

func destroy() -> void:
	if is_destroyed:
		return
	is_destroyed = true
	exploding = true
	explode_timer = EXPLODE_TIME

func direction_to_vector(dir: int) -> Vector2:
	match dir:
		GameData.Direction.UP: return Vector2(0, -1)
		GameData.Direction.DOWN: return Vector2(0, 1)
		GameData.Direction.LEFT: return Vector2(-1, 0)
		GameData.Direction.RIGHT: return Vector2(1, 0)
	return Vector2.ZERO

func _draw() -> void:
	if is_destroyed and exploding:
		# Explosion effect - orange/yellow expanding circle
		var progress = 1.0 - (explode_timer / EXPLODE_TIME)
		var radius = size * (1 + progress * 2.5)
		var center = Vector2(size / 2, size / 2)
		draw_circle(center, radius, Color(1, 0.4, 0, 0.7 * (1 - progress)))
		draw_circle(center, radius * 0.5, Color(1, 0.8, 0, 0.5 * (1 - progress)))
		return
	
	if is_destroyed:
		return
	
	# Draw bullet as a small directional shape
	var half = size / 2.0
	var center = Vector2(half, half)
	
	if from_player:
		# White/yellow bullet for player
		draw_circle(center, half * 0.7, Color(1.0, 1.0, 0.8))
		draw_circle(center, half * 0.4, Color(1.0, 1.0, 1.0))
	else:
		# Orange bullet for enemy
		draw_circle(center, half * 0.7, Color(1.0, 0.5, 0.1))
		draw_circle(center, half * 0.4, Color(1.0, 0.7, 0.3))
