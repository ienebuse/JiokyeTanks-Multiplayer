extends Node2D

# Eagle - matches Eagle.java
# Game objective to protect

var tile_dim: float = 0.0
var is_destroyed: bool = false
var protection: int = 2  # 0=dead, 1=shielded, 2=normal

# Death animation
var death_anim_timer: float = 0.0
const DEATH_ANIM_TIME: float = 1.0
var death_frame: int = 0

func init_eagle(td: float) -> void:
	tile_dim = td

func take_damage() -> void:
	if is_destroyed:
		return
	is_destroyed = true
	death_anim_timer = DEATH_ANIM_TIME

func _process(delta: float) -> void:
	if is_destroyed and death_anim_timer > 0:
		death_anim_timer -= delta
		queue_redraw()

func _draw() -> void:
	var size = tile_dim * 2
	
	if is_destroyed:
		# Destroyed eagle
		var progress = 1.0 - (death_anim_timer / DEATH_ANIM_TIME)
		draw_rect(Rect2(Vector2.ZERO, Vector2(size, size)), Color(0.3, 0.3, 0.3))
		if death_anim_timer > 0:
			var radius = size * 0.5 * progress
			draw_circle(Vector2(size / 2, size / 2), radius, Color(1, 0.5, 0, 0.5 * (1 - progress)))
		# Draw X over destroyed eagle
		var line_w = max(2.0, tile_dim * 0.15)
		draw_line(Vector2(2, 2), Vector2(size - 2, size - 2), Color.RED, line_w)
		draw_line(Vector2(size - 2, 2), Vector2(2, size - 2), Color.RED, line_w)
		return
	
	# Draw eagle body
	var eagle_color = GameData.COLOR_EAGLE
	draw_rect(Rect2(Vector2.ZERO, Vector2(size, size)), Color.BLACK)
	
	# Eagle symbol (simplified bird shape)
	var center = Vector2(size / 2, size / 2)
	
	# Body
	draw_circle(center, size * 0.3, eagle_color)
	
	# Wings
	var wing_color = eagle_color.lightened(0.2)
	draw_rect(Rect2(size * 0.1, size * 0.3, size * 0.25, size * 0.15), wing_color)
	draw_rect(Rect2(size * 0.65, size * 0.3, size * 0.25, size * 0.15), wing_color)
	
	# Head
	draw_circle(Vector2(center.x, size * 0.25), size * 0.12, eagle_color.lightened(0.1))
	
	# Border
	draw_rect(Rect2(Vector2.ZERO, Vector2(size, size)), Color.WHITE, false, max(1.0, tile_dim * 0.08))
