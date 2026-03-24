extends CharacterBody2D

signal destroyed(enemy)

const BULLET_SCENE := preload("res://scenes/entities/bullet.tscn")

@export var move_speed: float = 220.0
@export var health: int = 2

var target: Node2D = null
var _shoot_timer: float = 0.0

func _physics_process(delta: float) -> void:
    if health <= 0:
        return
    if target and is_instance_valid(target):
        var to_target := target.global_position - global_position
        if to_target.length() > 220.0:
            velocity = to_target.normalized() * move_speed
        else:
            velocity = velocity.lerp(Vector2.ZERO, 0.2)
        rotation = to_target.angle()
    else:
        velocity = Vector2.ZERO
    move_and_slide()

    _shoot_timer -= delta
    if _shoot_timer <= 0.0 and target and is_instance_valid(target):
        _fire_bullet()
        _shoot_timer = randf_range(0.8, 1.4)

func _fire_bullet() -> void:
    var bullet: Area2D = BULLET_SCENE.instantiate()
    bullet.global_position = global_position + Vector2.RIGHT.rotated(rotation) * 30.0
    bullet.direction = Vector2.RIGHT.rotated(rotation)
    bullet.owner = self
    bullet.speed = 760.0
    get_tree().current_scene.add_child(bullet)

func apply_hit() -> void:
    health -= 1
    if health <= 0:
        destroyed.emit(self)
