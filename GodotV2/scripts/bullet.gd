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
var explode_frame: int = 0
var explode_frame_timer: float = 0.0
const EXPLODE_FRAME_COUNT: int = 5
const EXPLODE_FRAME_TIME: float = 0.03  # ~1 game tick at 32 FPS

# Explosion sprite from tanktexture.png: ST_DESTROY_BULLET at (1108, 0, 32, 32), 5 frames vertical
var explode_texture: Texture2D = null
const EXPLODE_SRC_X: int = 1108
const EXPLODE_SRC_Y: int = 0
const EXPLODE_SRC_W: int = 32
const EXPLODE_SRC_H: int = 32

func init_bullet(td: float, dir: int, player: bool, brk: bool = false, clr: bool = false, speed_mult: float = 1.0) -> void:
	tile_dim = td
	direction = dir
	from_player = player
	break_wall = brk
	clear_bush = clr
	size = tile_dim * 0.4
	# Bullet speed: ~15 tiles/sec matches original feel
	speed = tile_dim * 12.0 * speed_mult
	explode_texture = load("res://assets/sprites/tanktexture.png")

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
			explode_frame_timer += delta
			if explode_frame_timer >= EXPLODE_FRAME_TIME:
				explode_frame += 1
				explode_frame_timer = 0.0
			if explode_frame >= EXPLODE_FRAME_COUNT:
				exploding = false
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
	explode_frame = 0
	explode_frame_timer = 0.0

func direction_to_vector(dir: int) -> Vector2:
	match dir:
		GameData.Direction.UP: return Vector2(0, -1)
		GameData.Direction.DOWN: return Vector2(0, 1)
		GameData.Direction.LEFT: return Vector2(-1, 0)
		GameData.Direction.RIGHT: return Vector2(1, 0)
	return Vector2.ZERO

func _draw() -> void:
	if is_destroyed and exploding:
		# Draw explosion using spritesheet frames
		var explode_size = tile_dim * 2  # Explosion is tank-sized (32px sprite → 2 tiles)
		var center_offset = Vector2(size / 2 - explode_size / 2, size / 2 - explode_size / 2)
		if explode_texture and explode_frame < EXPLODE_FRAME_COUNT:
			var src_rect = Rect2(EXPLODE_SRC_X, EXPLODE_SRC_Y + explode_frame * EXPLODE_SRC_H, EXPLODE_SRC_W, EXPLODE_SRC_H)
			draw_texture_rect_region(explode_texture, Rect2(center_offset, Vector2(explode_size, explode_size)), src_rect)
		else:
			# Fallback procedural explosion
			var progress = float(explode_frame) / EXPLODE_FRAME_COUNT
			var radius = explode_size * 0.5 * (0.5 + progress * 0.5)
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
