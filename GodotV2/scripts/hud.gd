extends Control

# HUD - matches the game UI from activity_tank.xml

signal resume_pressed
signal retry_pressed
signal next_pressed
signal quit_pressed
signal continue_single_pressed

@onready var score_label: Label = $MarginContainer/TopBar/ScoreLabel
@onready var lives_label: Label = $MarginContainer/TopBar/LivesLabel
@onready var stage_label: Label = $MarginContainer/TopBar/StageLabel
@onready var enemy_count_label: Label = $MarginContainer/TopBar/EnemyLabel
@onready var game_over_label: Label = $GameOverLabel
@onready var pause_panel: Panel = $PausePanel
@onready var pause_overlay: ColorRect = $PauseOverlay
@onready var score_panel: Panel = $ScorePanel
@onready var score_detail_label: Label = $ScorePanel/VBoxContainer/ScoreDetail

# Touch controls
@onready var touch_controls: Control = $TouchControls

# Dynamically created panels for multiplayer
var disconnect_panel: Panel = null
var waiting_retry_label: Label = null

var game_ref: Node2D = null

# Animated score display state (matching Java showScores())
var score_anim_active: bool = false
var score_anim_timer: float = 0.0
const SCORE_FRAME_TIME: float = 0.1  # 0.1 seconds per frame increment (matching Java)
var score_enemy_frame: int = 0  # Which tank type we're animating (0=A, 1=B, 2=C, 3=D, 4=total)
var score_display_kills: Dictionary = {}  # Current displayed kill counts
var score_actual_kills: Dictionary = {}   # Target kill counts
var score_stage: int = 0
var score_stage_score: int = 0
var score_total_score: int = 0
var score_is_complete: bool = false

# Tank type order for animation
const SCORE_TYPES = [
	GameData.ObjectType.ST_TANK_A,
	GameData.ObjectType.ST_TANK_B,
	GameData.ObjectType.ST_TANK_C,
	GameData.ObjectType.ST_TANK_D,
]
const SCORE_VALUES = [100, 200, 300, 400]
const TYPE_NAMES = ["TANK A", "TANK B", "TANK C", "TANK D"]

func _ready() -> void:
	game_over_label.visible = false
	pause_panel.visible = false
	if pause_overlay:
		pause_overlay.visible = false
	score_panel.visible = false
	# Show touch controls on devices with touchscreen
	if touch_controls:
		touch_controls.visible = DisplayServer.is_touchscreen_available()
	_create_disconnect_panel()
	_create_waiting_retry_label()

func _process(delta: float) -> void:
	if score_anim_active:
		_update_score_animation(delta)

func update_score(score: int) -> void:
	if score_label:
		score_label.text = "SCORE: " + str(score)

func update_lives(lives: int) -> void:
	if lives_label:
		lives_label.text = "LIVES: " + str(lives)

func update_lives_multiplayer(p1_lives: int, p2_lives: int) -> void:
	if lives_label:
		lives_label.text = "P1: " + str(p1_lives) + " | P2: " + str(p2_lives)

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
	if pause_overlay:
		pause_overlay.visible = true
	if pause_panel:
		pause_panel.visible = true

func hide_pause_menu() -> void:
	if pause_overlay:
		pause_overlay.visible = false
	if pause_panel:
		pause_panel.visible = false

func show_score_screen(kills: Dictionary, stage_score: int, total_score: int, stage: int, is_complete: bool) -> void:
	if not score_panel:
		return
	
	score_panel.visible = true
	
	# Initialize animated score display
	score_actual_kills = kills.duplicate()
	score_stage = stage
	score_stage_score = stage_score
	score_total_score = total_score
	score_is_complete = is_complete
	score_enemy_frame = 0
	score_anim_timer = 0.0
	score_anim_active = true
	
	# Reset displayed kill counts to 0
	score_display_kills = {}
	for type in SCORE_TYPES:
		score_display_kills[type] = 0
	
	# Show initial text (header only, kills will animate in)
	_update_score_text()

func _update_score_animation(delta: float) -> void:
	score_anim_timer -= delta
	if score_anim_timer > 0:
		return
	
	score_anim_timer = SCORE_FRAME_TIME
	
	if score_enemy_frame >= SCORE_TYPES.size():
		# Animation complete - show totals
		score_anim_active = false
		_update_score_text()
		return
	
	var current_type = SCORE_TYPES[score_enemy_frame]
	var target = score_actual_kills.get(current_type, 0)
	var current = score_display_kills.get(current_type, 0)
	
	if current < target:
		# Increment one kill at a time
		score_display_kills[current_type] = current + 1
		SoundManager.play_sound("tnkscore.wav")
		_update_score_text()
	else:
		# Move to next tank type
		score_enemy_frame += 1
		_update_score_text()

func _update_score_text() -> void:
	var text = ""
	if score_is_complete:
		text += "STAGE " + str(score_stage) + " COMPLETE!\n\n"
	else:
		text += "GAME OVER\n\n"
	
	text += "STAGE SCORE: " + str(score_stage_score) + "\n"
	text += "TOTAL SCORE: " + str(score_total_score) + "\n\n"
	
	# Show kills animated per type (only show types that have been reached)
	var total_kills = 0
	for i in range(SCORE_TYPES.size()):
		if i > score_enemy_frame:
			break  # Haven't reached this type yet
		var type = SCORE_TYPES[i]
		var count = score_display_kills.get(type, 0)
		var score = count * SCORE_VALUES[i]
		total_kills += count
		text += TYPE_NAMES[i] + ":  " + str(count) + " x " + str(SCORE_VALUES[i]) + " = " + str(score) + "\n"
	
	# Show total line once animation is complete
	if score_enemy_frame >= SCORE_TYPES.size():
		text += "\nTOTAL KILLS: " + str(total_kills) + "\n"
	
	if score_detail_label:
		score_detail_label.text = text

func _on_resume_btn_pressed() -> void:
	resume_pressed.emit()

func _on_retry_btn_pressed() -> void:
	score_panel.visible = false
	game_over_label.visible = false
	score_anim_active = false
	retry_pressed.emit()

func _on_next_btn_pressed() -> void:
	score_panel.visible = false
	game_over_label.visible = false
	score_anim_active = false
	next_pressed.emit()

func _on_quit_btn_pressed() -> void:
	score_anim_active = false
	quit_pressed.emit()

func _create_disconnect_panel() -> void:
	# Overlay
	var overlay = ColorRect.new()
	overlay.name = "DisconnectOverlay"
	overlay.set_anchors_and_offsets_preset(Control.PRESET_FULL_RECT)
	overlay.color = Color(0, 0, 0, 0.6)
	overlay.mouse_filter = Control.MOUSE_FILTER_STOP
	overlay.visible = false
	add_child(overlay)

	# Panel
	disconnect_panel = Panel.new()
	disconnect_panel.name = "DisconnectPanel"
	add_child(disconnect_panel)
	disconnect_panel.set_anchors_preset(Control.PRESET_CENTER)
	disconnect_panel.offset_left = -140.0
	disconnect_panel.offset_top = -80.0
	disconnect_panel.offset_right = 140.0
	disconnect_panel.offset_bottom = 80.0
	disconnect_panel.visible = false

	var vbox = VBoxContainer.new()
	vbox.set_anchors_and_offsets_preset(Control.PRESET_FULL_RECT)
	vbox.alignment = BoxContainer.ALIGNMENT_CENTER
	vbox.add_theme_constant_override("separation", 10)
	disconnect_panel.add_child(vbox)

	var label = Label.new()
	label.text = "Other player disconnected"
	label.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	vbox.add_child(label)

	var continue_btn = Button.new()
	continue_btn.text = "CONTINUE SOLO"
	continue_btn.pressed.connect(_on_continue_single_btn_pressed)
	vbox.add_child(continue_btn)

	var quit_btn = Button.new()
	quit_btn.text = "QUIT"
	quit_btn.pressed.connect(_on_quit_btn_pressed)
	vbox.add_child(quit_btn)

func _create_waiting_retry_label() -> void:
	waiting_retry_label = Label.new()
	waiting_retry_label.name = "WaitingRetryLabel"
	add_child(waiting_retry_label)
	waiting_retry_label.set_anchors_preset(Control.PRESET_CENTER)
	waiting_retry_label.offset_left = -150.0
	waiting_retry_label.offset_top = 100.0
	waiting_retry_label.offset_right = 150.0
	waiting_retry_label.offset_bottom = 130.0
	waiting_retry_label.text = "Waiting for other player..."
	waiting_retry_label.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	waiting_retry_label.add_theme_color_override("font_color", Color(1, 1, 0, 1))
	waiting_retry_label.visible = false

func show_disconnect_panel() -> void:
	if disconnect_panel:
		var overlay = get_node_or_null("DisconnectOverlay")
		if overlay:
			overlay.visible = true
		disconnect_panel.visible = true
	# Hide other panels
	if pause_panel:
		pause_panel.visible = false
	if pause_overlay:
		pause_overlay.visible = false

func hide_disconnect_panel() -> void:
	if disconnect_panel:
		disconnect_panel.visible = false
	var overlay = get_node_or_null("DisconnectOverlay")
	if overlay:
		overlay.visible = false

func show_waiting_retry() -> void:
	if waiting_retry_label:
		waiting_retry_label.visible = true

func hide_waiting_retry() -> void:
	if waiting_retry_label:
		waiting_retry_label.visible = false

func _on_continue_single_btn_pressed() -> void:
	continue_single_pressed.emit()
