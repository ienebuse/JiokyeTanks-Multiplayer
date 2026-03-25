extends Node2D

# Gold collectible - matches Gold.java
# Secret bonus item worth 800 points

var tile_dim: float = 0.0
var is_available: bool = false
var is_taken: bool = false
var alpha: float = 0.5
var alpha_dir: float = 1.0

func init_gold(td: float) -> void:
	tile_dim = td

func set_available(avail: bool) -> void:
	is_available = avail
	visible = avail

func _process(delta: float) -> void:
	if not is_available or is_taken:
		return
	
	# Pulsing alpha animation
	alpha += alpha_dir * delta * 2.0
	if alpha >= 1.0:
		alpha = 1.0
		alpha_dir = -1.0
	elif alpha <= 0.1:
		alpha = 0.1
		alpha_dir = 1.0
	
	queue_redraw()

func collect() -> void:
	is_taken = true
	is_available = false
	visible = false

func _draw() -> void:
	if not is_available or is_taken:
		return
	
	var size = tile_dim
	var gold_color = Color(1, 0.84, 0, alpha)
	
	# Gold coin
	draw_circle(Vector2(size / 2, size / 2), size * 0.4, gold_color)
	draw_circle(Vector2(size / 2, size / 2), size * 0.25, gold_color.lightened(0.3))
