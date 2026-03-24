extends Node2D

# Water terrain - matches Water.java
# Blocks tank movement unless boat equipped, animated

var tile_dim: float = 0.0
var anim_frame: int = 0
var anim_timer: float = 0.0
const ANIM_SPEED: float = 0.5

func init_water(td: float) -> void:
	tile_dim = td

func _process(delta: float) -> void:
	anim_timer += delta
	if anim_timer >= ANIM_SPEED:
		anim_frame = (anim_frame + 1) % 2
		anim_timer = 0.0
		queue_redraw()

func _draw() -> void:
	var base_color = GameData.COLOR_WATER
	draw_rect(Rect2(Vector2.ZERO, Vector2(tile_dim, tile_dim)), base_color)
	
	# Wave pattern
	var wave_color = base_color.lightened(0.3)
	var offset = tile_dim * 0.15 * anim_frame
	var line_w = max(1.0, tile_dim * 0.08)
	
	for i in range(3):
		var y = tile_dim * (i + 1) / 4.0 + offset
		if y < tile_dim:
			draw_line(Vector2(0, y), Vector2(tile_dim, y), wave_color, line_w)
