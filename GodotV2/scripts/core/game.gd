extends Node2D

var score: int = 0
var is_finished: bool = false

@onready var player: CharacterBody2D = $Player
@onready var enemies: Node2D = $EnemyContainer
@onready var hud: CanvasLayer = $HUD

func _ready() -> void:
    set_process_input(false)
    player.destroyed.connect(_on_player_destroyed)
    for enemy in enemies.get_children():
        enemy.target = player
        enemy.destroyed.connect(_on_enemy_destroyed)
    hud.update_hud(player.health, score)

func _process(_delta: float) -> void:
    if is_finished:
        return
    if enemies.get_child_count() == 0:
        _finish_battle(true)

func _on_enemy_destroyed(enemy: Node) -> void:
    if is_finished:
        return
    score += 100
    hud.update_hud(player.health, score)
    if is_instance_valid(enemy):
        enemy.queue_free()

func _on_player_destroyed() -> void:
    _finish_battle(false)

func _finish_battle(victory: bool) -> void:
    if is_finished:
        return
    is_finished = true
    if victory:
        hud.set_state_text("VICTORY - Press Enter")
    else:
        hud.set_state_text("DEFEAT - Press Enter")
    set_process_input(true)

func _input(event: InputEvent) -> void:
    if not is_finished:
        return
    if event.is_action_pressed("ui_accept") or event.is_action_pressed("shoot"):
        var tree := get_tree()
        if tree != null:
            tree.change_scene_to_file("res://scenes/ui/main_menu.tscn")
