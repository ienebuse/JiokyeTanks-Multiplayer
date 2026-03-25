extends Node2D

# Ice terrain - matches Ice.java
# Causes tank slippage

var tile_dim: float = 0.0
var ice_texture: Texture2D = null

func init_ice(td: float) -> void:
	tile_dim = td
	ice_texture = load("res://assets/sprites/ice.png")
	queue_redraw()

func _draw() -> void:
	if ice_texture:
		draw_texture_rect(ice_texture, Rect2(Vector2.ZERO, Vector2(tile_dim, tile_dim)), false)
	else:
		draw_rect(Rect2(Vector2.ZERO, Vector2(tile_dim, tile_dim)), Color(0.7, 0.85, 0.95))
