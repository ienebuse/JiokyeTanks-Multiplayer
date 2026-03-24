extends Node2D

# Brick wall - matches Brick.java
# Destructible wall with directional damage

var tile_dim: float = 0.0
var damage_state: int = 0  # 0 = full, 1 = half, 2+ = destroyed
var damage_direction: int = -1  # Direction of first hit
var destroyed: bool = false

func init_brick(td: float) -> void:
	tile_dim = td

func take_damage(dir: int) -> void:
	if destroyed:
		return
	damage_state += 1
	if damage_state == 1:
		damage_direction = dir
	if damage_state >= 2:
		destroyed = true
		visible = false
	queue_redraw()

func is_destroyed() -> bool:
	return destroyed

func _draw() -> void:
	if destroyed:
		return
	
	var brick_color = GameData.COLOR_BRICK
	var mortar_color = brick_color.darkened(0.3)
	
	if damage_state == 0:
		# Full brick
		draw_rect(Rect2(Vector2.ZERO, Vector2(tile_dim, tile_dim)), brick_color)
		# Draw brick pattern
		var line_width = max(1.0, tile_dim * 0.05)
		# Horizontal lines
		draw_line(Vector2(0, tile_dim / 2), Vector2(tile_dim, tile_dim / 2), mortar_color, line_width)
		# Vertical lines (offset)
		draw_line(Vector2(tile_dim / 2, 0), Vector2(tile_dim / 2, tile_dim / 2), mortar_color, line_width)
		draw_line(Vector2(tile_dim / 4, tile_dim / 2), Vector2(tile_dim / 4, tile_dim), mortar_color, line_width)
		draw_line(Vector2(tile_dim * 3 / 4, tile_dim / 2), Vector2(tile_dim * 3 / 4, tile_dim), mortar_color, line_width)
	else:
		# Half brick (based on damage direction)
		match damage_direction:
			GameData.Direction.UP:
				draw_rect(Rect2(0, tile_dim / 2, tile_dim, tile_dim / 2), brick_color)
			GameData.Direction.DOWN:
				draw_rect(Rect2(0, 0, tile_dim, tile_dim / 2), brick_color)
			GameData.Direction.LEFT:
				draw_rect(Rect2(tile_dim / 2, 0, tile_dim / 2, tile_dim), brick_color)
			GameData.Direction.RIGHT:
				draw_rect(Rect2(0, 0, tile_dim / 2, tile_dim), brick_color)
			_:
				draw_rect(Rect2(0, 0, tile_dim, tile_dim / 2), brick_color)
