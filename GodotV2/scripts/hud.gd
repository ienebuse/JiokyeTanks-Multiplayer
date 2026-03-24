extends Control

# HUD - matches the game UI from activity_tank.xml

signal resume_pressed
signal retry_pressed
signal next_pressed
signal quit_pressed

@onready var score_label: Label = $MarginContainer/TopBar/ScoreLabel
@onready var lives_label: Label = $MarginContainer/TopBar/LivesLabel
@onready var stage_label: Label = $MarginContainer/TopBar/StageLabel
@onready var enemy_count_label: Label = $MarginContainer/TopBar/EnemyLabel
@onready var game_over_label: Label = $GameOverLabel
@onready var pause_panel: Panel = $PausePanel
@onready var score_panel: Panel = $ScorePanel
@onready var score_detail_label: Label = $ScorePanel/VBoxContainer/ScoreDetail

# Touch controls
@onready var touch_controls: Control = $TouchControls

var game_ref: Node2D = null

func _ready() -> void:
	game_over_label.visible = false
	pause_panel.visible = false
	score_panel.visible = false
	# Show touch controls on devices with touchscreen
	if touch_controls:
		touch_controls.visible = DisplayServer.is_touchscreen_available()

func update_score(score: int) -> void:
	if score_label:
		score_label.text = "SCORE: " + str(score)

func update_lives(lives: int) -> void:
	if lives_label:
		lives_label.text = "LIVES: " + str(lives)

func update_stage(stage: int) -> void:
	if stage_label:
		stage_label.text = "STAGE: " + str(stage)

func update_enemy_count(count: int) -> void:
	if enemy_count_label:
		enemy_count_label.text = "ENEMIES: " + str(count)

func show_game_over() -> void:
	if game_over_label:
		game_over_label.visible = true
		game_over_label.text = "GAME OVER"

func show_pause_menu() -> void:
	if pause_panel:
		pause_panel.visible = true

func hide_pause_menu() -> void:
	if pause_panel:
		pause_panel.visible = false

func show_score_screen(kills: Dictionary, stage_score: int, total_score: int, stage: int, is_complete: bool) -> void:
	if not score_panel:
		return
	
	score_panel.visible = true
	var text = ""
	if is_complete:
		text += "STAGE " + str(stage) + " COMPLETE!\n\n"
	else:
		text += "GAME OVER\n\n"
	
	text += "STAGE SCORE: " + str(stage_score) + "\n"
	text += "TOTAL SCORE: " + str(total_score) + "\n\n"
	text += "KILLS:\n"
	
	var type_names = {
		GameData.ObjectType.ST_TANK_A: "TANK A",
		GameData.ObjectType.ST_TANK_B: "TANK B",
		GameData.ObjectType.ST_TANK_C: "TANK C",
		GameData.ObjectType.ST_TANK_D: "TANK D",
	}
	
	for type in kills:
		var name = type_names.get(type, "UNKNOWN")
		var count = kills[type]
		var score = count * GameData.ENEMY_SCORES.get(type, 100)
		text += name + ": " + str(count) + " x " + str(GameData.ENEMY_SCORES.get(type, 100)) + " = " + str(score) + "\n"
	
	if score_detail_label:
		score_detail_label.text = text

func _on_resume_btn_pressed() -> void:
	resume_pressed.emit()

func _on_retry_btn_pressed() -> void:
	score_panel.visible = false
	game_over_label.visible = false
	retry_pressed.emit()

func _on_next_btn_pressed() -> void:
	score_panel.visible = false
	game_over_label.visible = false
	next_pressed.emit()

func _on_quit_btn_pressed() -> void:
	quit_pressed.emit()
