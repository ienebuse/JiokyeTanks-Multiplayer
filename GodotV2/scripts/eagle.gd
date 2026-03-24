extends Node2D

# Eagle - the base that must be protected (matches Eagle.java)
# Uses sprite-like procedural drawing to look like the original

var tile_dim: float = 0.0
var is_destroyed: bool = false
var protection: int = 2  # 0=dead, 1=shielded, 2=normal

# Death animation
var death_anim_timer: float = 0.0
const DEATH_ANIM_TIME: float = 1.0
var death_frame: int = 0

var fire_texture: Texture2D = null

func init_eagle(td: float) -> void:
	tile_dim = td
	fire_texture = load("res://assets/sprites/fire.png")

func take_damage() -> void:
	if is_destroyed:
		return
	is_destroyed = true
	death_anim_timer = DEATH_ANIM_TIME

func _process(delta: float) -> void:
	if is_destroyed and death_anim_timer > 0:
		death_anim_timer -= delta
		queue_redraw()

func _draw() -> void:
	var sz = tile_dim * 2
	
	if is_destroyed:
		# Destroyed eagle - dark grey with X
		draw_rect(Rect2(2, 2, sz - 4, sz - 4), Color(0.15, 0.15, 0.15))
		draw_rect(Rect2(sz * 0.1, sz * 0.1, sz * 0.8, sz * 0.8), Color(0.25, 0.12, 0.0))
		if death_anim_timer > 0:
			var progress = 1.0 - (death_anim_timer / DEATH_ANIM_TIME)
			var center = Vector2(sz / 2, sz / 2)
			var radius = sz * 0.5 * progress
			draw_circle(center, radius, Color(1, 0.4, 0, 0.6 * (1 - progress)))
		return
	
	# Draw alive eagle - classic red/gold shield icon
	# Background
	draw_rect(Rect2(1, 1, sz - 2, sz - 2), Color(0.08, 0.08, 0.08))
	
	# Shield base (deep red)
	draw_rect(Rect2(sz * 0.08, sz * 0.08, sz * 0.84, sz * 0.84), Color(0.6, 0.0, 0.0))
	
	# Inner body (golden orange)
	draw_rect(Rect2(sz * 0.22, sz * 0.18, sz * 0.56, sz * 0.64), Color(0.9, 0.5, 0.0))
	
	# Eagle head (golden)
	var cx = sz / 2.0
	draw_circle(Vector2(cx, sz * 0.28), sz * 0.14, Color(1.0, 0.7, 0.0))
	
	# Wings (darker orange, spread out)
	draw_rect(Rect2(sz * 0.04, sz * 0.32, sz * 0.22, sz * 0.28), Color(0.85, 0.4, 0.0))
	draw_rect(Rect2(sz * 0.74, sz * 0.32, sz * 0.22, sz * 0.28), Color(0.85, 0.4, 0.0))
	
	# Tail
	draw_rect(Rect2(sz * 0.35, sz * 0.7, sz * 0.3, sz * 0.15), Color(0.75, 0.35, 0.0))
	
	# Eye
	draw_circle(Vector2(cx, sz * 0.26), sz * 0.035, Color.WHITE)
