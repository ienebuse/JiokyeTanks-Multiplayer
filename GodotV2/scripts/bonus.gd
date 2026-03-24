extends Node2D

# Bonus - matches Bonus.java
# Collectible powerups with sprite-based rendering

var tile_dim: float = 0.0
var bonus_type: int = GameData.BonusType.GRENADE
var is_expired: bool = false
var lifetime: float = 0.0
const MAX_LIFETIME: float = 6.0  # seconds

# Visual
var blink_timer: float = 0.0
var blink_visible: bool = true
const BLINK_SPEED: float = 0.3

# Sprite textures for each bonus type
var bonus_texture: Texture2D = null

const BONUS_SPRITE_PATHS: Dictionary = {
	0: "res://assets/sprites/bonus_grenade.png",
	1: "res://assets/sprites/bonus_helmet.png",
	2: "res://assets/sprites/bonus_clock.png",
	3: "res://assets/sprites/bonus_shovel.png",
	4: "res://assets/sprites/bonus_tank.png",
	5: "res://assets/sprites/bonus_star.png",
	6: "res://assets/sprites/bonus_gun.png",
	7: "res://assets/sprites/bonus_boat.png",
	8: "res://assets/sprites/bonus_mine.png",
	9: "res://assets/sprites/bonus_builder.png",
}

func init_bonus(td: float, type: int) -> void:
	tile_dim = td
	bonus_type = type
	lifetime = 0.0
	is_expired = false
	
	# Load the sprite for this bonus type
	var path = BONUS_SPRITE_PATHS.get(type, "")
	if path != "":
		bonus_texture = load(path)

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
	
	var sz = tile_dim * 2
	
	if bonus_texture:
		draw_texture_rect(bonus_texture, Rect2(Vector2.ZERO, Vector2(sz, sz)), false)
	else:
		# Fallback colored square
		draw_rect(Rect2(Vector2.ZERO, Vector2(sz, sz)), Color.RED)
		draw_rect(Rect2(Vector2.ZERO, Vector2(sz, sz)), Color.WHITE, false, max(1.0, tile_dim * 0.1))
