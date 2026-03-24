extends Control

func _ready() -> void:
    $Panel/VBox/StartButton.pressed.connect(_on_start_pressed)
    $Panel/VBox/QuitButton.pressed.connect(_on_quit_pressed)

func _on_start_pressed() -> void:
    var tree: SceneTree = get_tree()
    if tree != null:
        tree.change_scene_to_file("res://scenes/core/game.tscn")

func _on_quit_pressed() -> void:
    var tree: SceneTree = get_tree()
    if tree != null:
        tree.quit()
