extends Node2D

# Water terrain - matches Water.java
# Blocks tank movement unless boat equipped, animated

var tile_dim: float = 0.0
var anim_frame: int = 0
var anim_timer: float = 0.0
const ANIM_SPEED: float = 0.5
var water_texture: Texture2D = null

func init_water(td: float) -> void:
	tile_dim = td
	water_texture = load("res://assets/sprites/water.png")

func _process(delta: float) -> void:
	anim_timer += delta
	if anim_timer >= ANIM_SPEED:
		anim_frame = (anim_frame + 1) % 2
		anim_timer = 0.0
		queue_redraw()

func _draw() -> void:
	if water_texture:
		draw_texture_rect(water_texture, Rect2(Vector2.ZERO, Vector2(tile_dim, tile_dim)), false)
		# Subtle animation: offset wave overlay
		if anim_frame == 1:
			var wave_color = Color(0.2, 0.5, 1.0, 0.2)
			draw_rect(Rect2(0, 0, tile_dim, tile_dim / 3), wave_color)
	else:
		draw_rect(Rect2(Vector2.ZERO, Vector2(tile_dim, tile_dim)), Color(0, 0.4, 0.8))
