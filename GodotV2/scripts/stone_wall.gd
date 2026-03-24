extends Node2D

# Stone wall - matches StoneWall.java
# Indestructible (unless player has break_wall power)

var tile_dim: float = 0.0

func init_stone(td: float) -> void:
	tile_dim = td

func _draw() -> void:
	var stone_color = GameData.COLOR_STONE
	draw_rect(Rect2(Vector2.ZERO, Vector2(tile_dim, tile_dim)), stone_color)
	
	# Draw stone pattern (cross-hatch)
	var dark = stone_color.darkened(0.2)
	var line_w = max(1.0, tile_dim * 0.08)
	draw_line(Vector2(0, tile_dim / 3), Vector2(tile_dim, tile_dim / 3), dark, line_w)
	draw_line(Vector2(0, tile_dim * 2 / 3), Vector2(tile_dim, tile_dim * 2 / 3), dark, line_w)
	draw_line(Vector2(tile_dim / 3, 0), Vector2(tile_dim / 3, tile_dim), dark, line_w)
	draw_line(Vector2(tile_dim * 2 / 3, 0), Vector2(tile_dim * 2 / 3, tile_dim), dark, line_w)
