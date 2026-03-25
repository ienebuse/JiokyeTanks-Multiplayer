extends Control

# Main menu - matches original game's menu system

@onready var level_spinbox: SpinBox = $VBoxContainer/LevelSelect/LevelSpinBox

func _ready() -> void:
	if level_spinbox:
		level_spinbox.max_value = GameData.NUM_LEVELS

func _on_start_btn_pressed() -> void:
	GameData.current_level = 1
	get_tree().change_scene_to_file("res://scenes/game.tscn")

func _on_continue_btn_pressed() -> void:
	GameData.current_level = max(1, GameData.unlocked_level)
	if GameData.current_level > GameData.NUM_LEVELS:
		GameData.current_level = 1
	get_tree().change_scene_to_file("res://scenes/game.tscn")

func _on_go_btn_pressed() -> void:
	if level_spinbox:
		GameData.current_level = int(level_spinbox.value)
		get_tree().change_scene_to_file("res://scenes/game.tscn")

func _on_multiplayer_btn_pressed() -> void:
	get_tree().change_scene_to_file("res://scenes/multiplayer_lobby.tscn")

func _on_quit_btn_pressed() -> void:
	get_tree().quit()
