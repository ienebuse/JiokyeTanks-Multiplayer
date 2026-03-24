extends Node2D

# Stone wall - matches StoneWall.java
# Indestructible (unless player has break_wall power)

var tile_dim: float = 0.0
var stone_texture: Texture2D = null

func init_stone(td: float) -> void:
	tile_dim = td
	stone_texture = load("res://assets/sprites/stone.png")

func _draw() -> void:
	if stone_texture:
		draw_texture_rect(stone_texture, Rect2(Vector2.ZERO, Vector2(tile_dim, tile_dim)), false)
	else:
		draw_rect(Rect2(Vector2.ZERO, Vector2(tile_dim, tile_dim)), Color(0.7, 0.7, 0.7))
