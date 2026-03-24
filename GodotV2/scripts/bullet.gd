extends Node2D

# Bullet - matches Bullet.java

var direction: int = GameData.Direction.UP
var speed: float = 0.0
var tile_dim: float = 0.0
var size: float = 0.0
var from_player: bool = false
var break_wall: bool = false
var clear_bush: bool = false
var is_destroyed: bool = false
var color: Color = GameData.COLOR_BULLET

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
	size = tile_dim * 0.5
	speed = tile_dim * 15.0 / GameData.FPS * GameData.FPS * speed_mult
	
	if from_player:
		color = Color.WHITE
	else:
		color = Color(1.0, 0.6, 0.2)

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
		# Explosion effect
		var progress = 1.0 - (explode_timer / EXPLODE_TIME)
		var radius = size * (1 + progress * 2)
		draw_circle(Vector2(size / 2, size / 2), radius, Color(1, 0.5, 0, 0.8 * (1 - progress)))
		draw_circle(Vector2(size / 2, size / 2), radius * 0.5, Color(1, 1, 0, 0.6 * (1 - progress)))
		return
	
	if is_destroyed:
		return
	
	# Draw bullet
	draw_rect(Rect2(Vector2.ZERO, Vector2(size, size)), color)
