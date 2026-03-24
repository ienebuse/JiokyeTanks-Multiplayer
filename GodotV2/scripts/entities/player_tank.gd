extends CharacterBody2D

signal destroyed

const BULLET_SCENE := preload("res://scenes/entities/bullet.tscn")

@export var move_speed: float = 320.0
@export var health: int = 5

var _shoot_cooldown: float = 0.0

func _physics_process(delta: float) -> void:
	var input_vector := Input.get_vector("move_left", "move_right", "move_up", "move_down")
	velocity = input_vector * move_speed
	move_and_slide()
	if input_vector.length_squared() > 0.001:
		rotation = input_vector.angle()
	_shoot_cooldown = max(0.0, _shoot_cooldown - delta)
	if Input.is_action_pressed("shoot") and _shoot_cooldown <= 0.0:
		_fire_bullet()
		_shoot_cooldown = 0.22

func _fire_bullet() -> void:
var bullet: Area2D = BULLET_SCENE.instantiate()
bullet.global_position = global_position + Vector2.RIGHT.rotated(rotation) * 36.0
bullet.direction = Vector2.RIGHT.rotated(rotation)
bullet.owner = self
get_tree().current_scene.add_child(bullet)

func apply_hit() -> void:
health -= 1
if get_parent().has_node("HUD"):
get_parent().get_node("HUD").update_hud(health, get_parent().score)
if health <= 0:
destroyed.emit()
queue_free()
