extends Node2D

# Bonus - matches Bonus.java
# Collectible powerups

var tile_dim: float = 0.0
var bonus_type: int = GameData.BonusType.GRENADE
var is_expired: bool = false
var lifetime: float = 0.0
const MAX_LIFETIME: float = 6.0  # seconds

# Visual
var blink_timer: float = 0.0
var blink_visible: bool = true
const BLINK_SPEED: float = 0.3

# Colors for each bonus type
const BONUS_COLORS: Dictionary = {
	0: Color(1, 0, 0),      # GRENADE - red
	1: Color(0.3, 0.6, 1),  # HELMET - blue
	2: Color(0, 0.8, 0.8),  # CLOCK - cyan
	3: Color(0.6, 0.4, 0.2),# SHOVEL - brown
	4: Color(0, 1, 0),      # TANK - green
	5: Color(1, 1, 0),      # STAR - yellow
	6: Color(1, 0.5, 0),    # GUN - orange
	7: Color(0, 0.5, 1),    # BOAT - blue
	8: Color(0.5, 0, 0),    # MINE - dark red
	9: Color(0.8, 0.8, 0.8),# BUILDER - gray
}

const BONUS_LABELS: Dictionary = {
	0: "G",  # GRENADE
	1: "H",  # HELMET
	2: "C",  # CLOCK
	3: "S",  # SHOVEL
	4: "T",  # TANK
	5: "*",  # STAR
	6: "W",  # GUN
	7: "B",  # BOAT
	8: "M",  # MINE
	9: "R",  # BUILDER
}

func init_bonus(td: float, type: int) -> void:
	tile_dim = td
	bonus_type = type
	lifetime = 0.0
	is_expired = false

func update_bonus(delta: float) -> void:
	lifetime += delta
	if lifetime >= MAX_LIFETIME:
		is_expired = true
		return
	
	# Blink faster near end
	var speed = BLINK_SPEED
	if lifetime > MAX_LIFETIME * 0.7:
		speed = BLINK_SPEED * 0.5
	
	blink_timer += delta
	if blink_timer >= speed:
		blink_visible = not blink_visible
		blink_timer = 0.0
	
	queue_redraw()

func _draw() -> void:
	if not blink_visible:
		return
	
	var size = tile_dim * 2
	var bonus_color = BONUS_COLORS.get(bonus_type, Color.RED)
	
	# Background
	draw_rect(Rect2(Vector2.ZERO, Vector2(size, size)), bonus_color)
	
	# Border
	draw_rect(Rect2(Vector2.ZERO, Vector2(size, size)), Color.WHITE, false, max(1.0, tile_dim * 0.1))
	
	# Label
	var label = BONUS_LABELS.get(bonus_type, "?")
	var font = ThemeDB.fallback_font
	var font_size = int(tile_dim * 1.2)
	var text_size = font.get_string_size(label, HORIZONTAL_ALIGNMENT_CENTER, -1, font_size)
	var text_pos = Vector2((size - text_size.x) / 2, (size + text_size.y * 0.6) / 2)
	draw_string(font, text_pos, label, HORIZONTAL_ALIGNMENT_LEFT, -1, font_size, Color.WHITE)
