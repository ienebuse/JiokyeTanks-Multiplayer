extends CharacterBody2D

signal destroyed

const BULLET_SCENE := preload("res://scenes/entities/bullet.tscn")

@export var move_speed: float = 320.0
@export var health: int = 5

var _shoot_cooldown: float = 0.0
var _is_destroyed: bool = false

func _physics_process(delta: float) -> void:
    var input_vector: Vector2 = Input.get_vector("move_left", "move_right", "move_up", "move_down")
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
    if bullet == null:
        return
    var scene_root: Node = get_tree().current_scene
    if scene_root == null:
        return
    bullet.global_position = global_position + Vector2.RIGHT.rotated(rotation) * 36.0
    bullet.direction = Vector2.RIGHT.rotated(rotation)
    bullet.shooter = self
    scene_root.add_child(bullet)

func apply_hit() -> void:
    if _is_destroyed:
        return
    health -= 1
    var parent_node: Node = get_parent()
    if parent_node != null and parent_node.has_node("HUD"):
        var hud_node: Node = parent_node.get_node("HUD")
        if hud_node != null and hud_node.has_method("update_hud"):
            var current_score: int = 0
            var score_value: Variant = parent_node.get("score")
            if typeof(score_value) == TYPE_INT:
                current_score = score_value
            hud_node.update_hud(health, current_score)
    if health <= 0:
        _is_destroyed = true
        destroyed.emit()
        queue_free()
