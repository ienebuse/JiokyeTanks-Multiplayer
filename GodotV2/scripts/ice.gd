extends Node2D

# Ice terrain - matches Ice.java
# Causes tank slippage

var tile_dim: float = 0.0

func init_ice(td: float) -> void:
	tile_dim = td

func _draw() -> void:
	var ice_color = GameData.COLOR_ICE
	draw_rect(Rect2(Vector2.ZERO, Vector2(tile_dim, tile_dim)), ice_color)
	
	# Ice shine effect
	var shine = ice_color.lightened(0.4)
	var line_w = max(1.0, tile_dim * 0.06)
	draw_line(Vector2(tile_dim * 0.2, tile_dim * 0.3), Vector2(tile_dim * 0.5, tile_dim * 0.3), shine, line_w)
	draw_line(Vector2(tile_dim * 0.4, tile_dim * 0.7), Vector2(tile_dim * 0.8, tile_dim * 0.7), shine, line_w)
