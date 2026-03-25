extends Node2D

# Eagle - the base that must be protected (matches Eagle.java)
# Uses actual sprite from tanktexture.png spritesheet

var tile_dim: float = 0.0
var is_destroyed: bool = false
var protection: int = 2  # 0=dead, 1=shielded, 2=normal

# Death animation (matching Java Eagle: ST_DESTROY_EAGLE at 1040,0 64x64 7 frames)
var death_anim_timer: float = 0.0
const DEATH_ANIM_FRAME_TIME: float = 0.03  # ~1 tick at 32 FPS, matching Java frame_time=1
var death_frame: int = 0
const DEATH_FRAME_COUNT: int = 7
var dead: bool = false

# Sprites
var eagle_texture: Texture2D = null
# Eagle sprite region in tanktexture.png: (944, 0, 32, 32) for alive
# Second frame (dead) at (944, 32, 32, 32)
const EAGLE_SRC_X: int = 944
const EAGLE_SRC_Y: int = 0
const EAGLE_SRC_W: int = 32
const EAGLE_SRC_H: int = 32

# Explosion sprite region in tanktexture.png: ST_DESTROY_EAGLE at (1040, 0, 64, 64), 7 frames vertical
const EXPLODE_SRC_X: int = 1040
const EXPLODE_SRC_Y: int = 0
const EXPLODE_SRC_W: int = 64
const EXPLODE_SRC_H: int = 64

func init_eagle(td: float) -> void:
	tile_dim = td
	eagle_texture = load("res://assets/sprites/tanktexture.png")
	queue_redraw()

func take_damage() -> void:
	if is_destroyed:
		return
	is_destroyed = true
	death_frame = 0
	death_anim_timer = DEATH_ANIM_FRAME_TIME

func _process(delta: float) -> void:
	if is_destroyed and not dead:
		death_anim_timer -= delta
		if death_anim_timer <= 0:
			death_frame += 1
			death_anim_timer = DEATH_ANIM_FRAME_TIME
		if death_frame >= DEATH_FRAME_COUNT:
			dead = true
		queue_redraw()

func _draw() -> void:
	var sz = tile_dim * 2
	
	if eagle_texture:
		if is_destroyed:
			if not dead and death_frame < DEATH_FRAME_COUNT:
				# Draw explosion animation using spritesheet frames (matching Java Eagle.draw)
				var explode_size = sz * 2  # Explosion is larger than eagle (64px sprite vs 32px eagle)
				var center_offset = Vector2((sz - explode_size) / 2, (sz - explode_size) / 2)
				var src_rect = Rect2(EXPLODE_SRC_X, EXPLODE_SRC_Y + death_frame * EXPLODE_SRC_H, EXPLODE_SRC_W, EXPLODE_SRC_H)
				draw_texture_rect_region(eagle_texture, Rect2(center_offset, Vector2(explode_size, explode_size)), src_rect)
			else:
				# Draw destroyed eagle frame (second frame, y offset = 32)
				var src_rect = Rect2(EAGLE_SRC_X, EAGLE_SRC_Y + EAGLE_SRC_H, EAGLE_SRC_W, EAGLE_SRC_H)
				draw_texture_rect_region(eagle_texture, Rect2(Vector2.ZERO, Vector2(sz, sz)), src_rect)
		else:
			# Draw alive eagle (first frame)
			var src_rect = Rect2(EAGLE_SRC_X, EAGLE_SRC_Y, EAGLE_SRC_W, EAGLE_SRC_H)
			draw_texture_rect_region(eagle_texture, Rect2(Vector2.ZERO, Vector2(sz, sz)), src_rect)
	else:
		# Fallback procedural drawing
		_draw_procedural()

func _draw_procedural() -> void:
	var sz = tile_dim * 2
	if is_destroyed:
		draw_rect(Rect2(2, 2, sz - 4, sz - 4), Color(0.15, 0.15, 0.15))
		if not dead and death_frame < DEATH_FRAME_COUNT:
			var progress = float(death_frame) / DEATH_FRAME_COUNT
			var center = Vector2(sz / 2, sz / 2)
			var radius = sz * 0.5 * (0.5 + progress * 0.5)
			draw_circle(center, radius, Color(1, 0.4, 0, 0.7 * (1 - progress)))
	else:
		draw_rect(Rect2(1, 1, sz - 2, sz - 2), Color(0.08, 0.08, 0.08))
		draw_rect(Rect2(sz * 0.08, sz * 0.08, sz * 0.84, sz * 0.84), Color(0.6, 0.0, 0.0))
		draw_rect(Rect2(sz * 0.22, sz * 0.18, sz * 0.56, sz * 0.64), Color(0.9, 0.5, 0.0))
