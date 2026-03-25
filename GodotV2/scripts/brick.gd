extends Node2D

# Brick wall - matches Brick.java
# Destructible wall with directional damage and collision rect resizing

var tile_dim: float = 0.0
var damage_state: int = 0  # 0 = full, 1 = half, 2+ = destroyed
var damage_direction: int = -1  # Direction of bullet that caused first hit
var destroyed: bool = false

# Collision rect - shrinks when damaged (matching Java Brick.collidsWithBullet)
var col_offset: Vector2 = Vector2.ZERO  # Offset for collision rect relative to position
var col_size: Vector2 = Vector2.ZERO     # Size of the collision rect

# Sprite
var brick_texture: Texture2D = null

func init_brick(td: float) -> void:
	tile_dim = td
	col_offset = Vector2.ZERO
	col_size = Vector2(tile_dim, tile_dim)
	brick_texture = load("res://assets/sprites/brick.png")
	queue_redraw()

func get_collision_rect() -> Rect2:
	return Rect2(position + col_offset, col_size)

func take_damage(dir: int) -> void:
	if destroyed:
		return
	damage_state += 1
	if damage_state == 1:
		damage_direction = dir
		# Shrink collision rect based on bullet direction (matching Java Brick.collidsWithBullet)
		# The half the bullet hits FIRST is destroyed; the far half remains
		match dir:
			GameData.Direction.UP:
				# Bullet moving up hits bottom of brick - bottom half destroyed, top remains
				col_size.y = tile_dim / 2.0
				# col_offset stays (0,0) - top half remains at original position
			GameData.Direction.DOWN:
				# Bullet moving down hits top of brick - top half destroyed, bottom remains
				col_size.y = tile_dim / 2.0
				col_offset.y = tile_dim / 2.0  # bottom half starts at midpoint
			GameData.Direction.LEFT:
				# Bullet moving left hits right of brick - right half destroyed, left remains
				col_size.x = tile_dim / 2.0
				# col_offset stays (0,0) - left half remains at original position
			GameData.Direction.RIGHT:
				# Bullet moving right hits left of brick - left half destroyed, right remains
				col_size.x = tile_dim / 2.0
				col_offset.x = tile_dim / 2.0  # right half starts at midpoint
		queue_redraw()
	elif damage_state >= 2:
		destroyed = true
		visible = false

func is_destroyed() -> bool:
	return destroyed

func _draw() -> void:
	if destroyed:
		return
	
	if damage_state == 0:
		# Full brick - draw sprite scaled to tile_dim
		if brick_texture:
			draw_texture_rect(brick_texture, Rect2(Vector2.ZERO, Vector2(tile_dim, tile_dim)), false)
		else:
			_draw_fallback_full()
	else:
		# Half brick - draw only the remaining half (matching Java d1bitmaps)
		if brick_texture:
			match damage_direction:
				GameData.Direction.UP:
					# Bottom destroyed by UP bullet, top half remains
					# d1bitmaps[0] = top half of sprite
					draw_texture_rect_region(brick_texture,
						Rect2(0, 0, tile_dim, tile_dim / 2),
						Rect2(0, 0, 16, 8))
				GameData.Direction.DOWN:
					# Top destroyed by DOWN bullet, bottom half remains
					# d1bitmaps[2] = bottom half of sprite
					draw_texture_rect_region(brick_texture,
						Rect2(0, tile_dim / 2, tile_dim, tile_dim / 2),
						Rect2(0, 8, 16, 8))
				GameData.Direction.LEFT:
					# Right destroyed by LEFT bullet, left half remains
					# d1bitmaps[3] = left half of sprite
					draw_texture_rect_region(brick_texture,
						Rect2(0, 0, tile_dim / 2, tile_dim),
						Rect2(0, 0, 8, 16))
				GameData.Direction.RIGHT:
					# Left destroyed by RIGHT bullet, right half remains
					# d1bitmaps[1] = right half of sprite
					draw_texture_rect_region(brick_texture,
						Rect2(tile_dim / 2, 0, tile_dim / 2, tile_dim),
						Rect2(8, 0, 8, 16))
		else:
			_draw_fallback_damaged()

func _draw_fallback_full() -> void:
	var brick_color = Color(0.72, 0.33, 0.0)
	var mortar_color = Color(0.45, 0.22, 0.0)
	draw_rect(Rect2(Vector2.ZERO, Vector2(tile_dim, tile_dim)), brick_color)
	var lw = max(1.0, tile_dim * 0.06)
	draw_line(Vector2(0, tile_dim / 2), Vector2(tile_dim, tile_dim / 2), mortar_color, lw)
	draw_line(Vector2(tile_dim / 2, 0), Vector2(tile_dim / 2, tile_dim / 2), mortar_color, lw)
	draw_line(Vector2(tile_dim / 4, tile_dim / 2), Vector2(tile_dim / 4, tile_dim), mortar_color, lw)
	draw_line(Vector2(tile_dim * 3 / 4, tile_dim / 2), Vector2(tile_dim * 3 / 4, tile_dim), mortar_color, lw)

func _draw_fallback_damaged() -> void:
	var brick_color = Color(0.72, 0.33, 0.0)
	draw_rect(Rect2(col_offset, col_size), brick_color)
