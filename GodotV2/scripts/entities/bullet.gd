extends Area2D

var speed: float = 900.0
var direction: Vector2 = Vector2.RIGHT
var owner: Node = null

func _ready() -> void:
body_entered.connect(_on_body_entered)

func _physics_process(delta: float) -> void:
global_position += direction * speed * delta
if global_position.x < -100 or global_position.x > 2020 or global_position.y < -100 or global_position.y > 1180:
queue_free()

func _on_body_entered(body: Node) -> void:
if body == owner:
return
if body.has_method("apply_hit"):
body.apply_hit()
queue_free()
