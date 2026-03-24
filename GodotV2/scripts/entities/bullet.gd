extends Area2D

var speed: float = 900.0
var direction: Vector2 = Vector2.RIGHT
var owner: Node = null

func _ready() -> void:
    body_entered.connect(_on_body_entered)

func _physics_process(delta: float) -> void:
    global_position += direction * speed * delta
    var viewport_rect := get_viewport_rect()
    var cull_margin := 100.0
    if global_position.x < -cull_margin \
        or global_position.x > viewport_rect.size.x + cull_margin \
        or global_position.y < -cull_margin \
        or global_position.y > viewport_rect.size.y + cull_margin:
        queue_free()

func _on_body_entered(body: Node) -> void:
    if body == owner:
        return
    if body.has_method("apply_hit"):
        body.apply_hit()
    queue_free()
