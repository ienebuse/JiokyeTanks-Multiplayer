extends Node2D

# Bush terrain - matches Bush.java
# Hides entities, can be cleared by player with clear_bush

var tile_dim: float = 0.0

func init_bush(td: float) -> void:
	tile_dim = td

func _draw() -> void:
	var bush_color = GameData.COLOR_BUSH
	draw_rect(Rect2(Vector2.ZERO, Vector2(tile_dim, tile_dim)), bush_color)
	
	# Leaf pattern
	var light = bush_color.lightened(0.3)
	var dark = bush_color.darkened(0.2)
	var r = tile_dim * 0.15
	
	draw_circle(Vector2(tile_dim * 0.25, tile_dim * 0.25), r, light)
	draw_circle(Vector2(tile_dim * 0.75, tile_dim * 0.25), r, dark)
	draw_circle(Vector2(tile_dim * 0.25, tile_dim * 0.75), r, dark)
	draw_circle(Vector2(tile_dim * 0.75, tile_dim * 0.75), r, light)
	draw_circle(Vector2(tile_dim * 0.5, tile_dim * 0.5), r, light)
