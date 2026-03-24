extends Node2D

# Bush terrain - matches Bush.java
# Hides entities, can be cleared by player with clear_bush

var tile_dim: float = 0.0
var bush_texture: Texture2D = null

func init_bush(td: float) -> void:
	tile_dim = td
	bush_texture = load("res://assets/sprites/bush.png")

func _draw() -> void:
	if bush_texture:
		draw_texture_rect(bush_texture, Rect2(Vector2.ZERO, Vector2(tile_dim, tile_dim)), false)
	else:
		draw_rect(Rect2(Vector2.ZERO, Vector2(tile_dim, tile_dim)), Color(0, 0.5, 0))
