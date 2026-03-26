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

# Tank type order for animation (including HVE)
const SCORE_TYPES = [
	GameData.ObjectType.ST_TANK_A,
	GameData.ObjectType.ST_TANK_B,
	GameData.ObjectType.ST_TANK_C,
	GameData.ObjectType.ST_TANK_D,
	GameData.ObjectType.ST_HVE,
]
const SCORE_VALUES = [100, 200, 300, 400, 400]

# Tank sprite regions from tanktexture.png for scoreboard display
# Each entry: [src_x, src_y, src_w, src_h] for a single frame (facing DOWN, group 1, anim 0)
# Col = 4 * group + direction; Row = 2 * type_val + anim_frame
const TANK_SPRITE_REGIONS = [
	Rect2(6 * 32, 0 * 32, 32, 32),   # Tank A: col=4*1+2=6, row=2*0+0=0
	Rect2(6 * 32, 2 * 32, 32, 32),   # Tank B: col=6, row=2*1+0=2
	Rect2(6 * 32, 4 * 32, 32, 32),   # Tank C: col=6, row=2*2+0=4
	Rect2(6 * 32, 6 * 32, 32, 32),   # Tank D: col=6, row=2*3+0=6
]

# Dynamic score row nodes
var kills_container: VBoxContainer = null
var kill_row_labels: Array = []  # Array of Labels for each kill row's text
var total_kills_label: Label = null
var tank_textures: Array = []  # Pre-created AtlasTextures for tank images

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
	
	# Disable "Next" button on game over (matching Java: nxtBtn.setAlpha(0.2f))
	var next_btn = score_panel.get_node_or_null("VBoxContainer/HBoxContainer/NextBtn")
	if next_btn:
		next_btn.disabled = not is_complete
		next_btn.modulate.a = 1.0 if is_complete else 0.4
	
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
	
	# Build the image-based kill rows
	_build_kill_rows()
	
	# Show initial text (header only, kills will animate in)
	_update_score_text()

func _build_kill_rows() -> void:
	# Remove previous kills container if it exists (total_kills_label is a child, freed with it)
	if kills_container and is_instance_valid(kills_container):
		kills_container.get_parent().remove_child(kills_container)
		kills_container.free()
	kills_container = null
	total_kills_label = null
	kill_row_labels.clear()
	tank_textures.clear()
	
	# Load the tank spritesheet
	var tank_texture = load("res://assets/sprites/tanktexture.png")
	var hve_texture = load("res://assets/sprites/hve.png")
	
	# Create atlas textures for each tank type
	for i in range(SCORE_TYPES.size()):
		if i < TANK_SPRITE_REGIONS.size():
			# Standard tank from tanktexture.png
			var atlas = AtlasTexture.new()
			atlas.atlas = tank_texture
			atlas.region = TANK_SPRITE_REGIONS[i]
			tank_textures.append(atlas)
		else:
			# HVE from hve.png
			var atlas = AtlasTexture.new()
			atlas.atlas = hve_texture
			# HVE layout: 4 columns (directions) × 2 rows (anim frames)
			var frame_w = hve_texture.get_width() / 4.0
			var frame_h = hve_texture.get_height() / 2.0
			# Use direction DOWN (2), anim_frame 0
			atlas.region = Rect2(2 * frame_w, 0, frame_w, frame_h)
			tank_textures.append(atlas)
	
	# Get the VBoxContainer inside ScorePanel
	var vbox = score_panel.get_node_or_null("VBoxContainer")
	if not vbox:
		return
	
	# Create kills container and insert it before the buttons HBoxContainer
	kills_container = VBoxContainer.new()
	kills_container.name = "KillsContainer"
	kills_container.size_flags_vertical = Control.SIZE_EXPAND_FILL
	kills_container.add_theme_constant_override("separation", 2)
	# Insert before the last child (HBoxContainer with buttons)
	var btn_container = vbox.get_node_or_null("HBoxContainer")
	if btn_container:
		vbox.add_child(kills_container)
		vbox.move_child(kills_container, btn_container.get_index())
	else:
		vbox.add_child(kills_container)
	
	# Create a row for each enemy type
	for i in range(SCORE_TYPES.size()):
		var row = HBoxContainer.new()
		row.alignment = BoxContainer.ALIGNMENT_CENTER
		row.add_theme_constant_override("separation", 8)
		row.visible = false  # Hidden until animation reaches this type
		
		# Tank image
		var tex_rect = TextureRect.new()
		tex_rect.texture = tank_textures[i]
		tex_rect.custom_minimum_size = Vector2(24, 24)
		tex_rect.stretch_mode = TextureRect.STRETCH_KEEP_ASPECT_CENTERED
		tex_rect.expand_mode = TextureRect.EXPAND_FIT_WIDTH_PROPORTIONAL
		row.add_child(tex_rect)
		
		# Kill count and score label
		var label = Label.new()
		label.text = "  0 x " + str(SCORE_VALUES[i]) + " = 0"
		label.add_theme_font_size_override("font_size", 14)
		row.add_child(label)
		
		kills_container.add_child(row)
		kill_row_labels.append(label)
	
	# Total kills label (shown after animation completes)
	total_kills_label = Label.new()
	total_kills_label.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	total_kills_label.add_theme_font_size_override("font_size", 14)
	total_kills_label.visible = false
	kills_container.add_child(total_kills_label)

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
	# Update header label with title and scores only
	var text = ""
	if score_is_complete:
		text += "STAGE " + str(score_stage) + " COMPLETE!\n\n"
	else:
		text += "GAME OVER\n\n"
	
	text += "STAGE SCORE: " + str(score_stage_score) + "\n"
	text += "TOTAL SCORE: " + str(score_total_score)
	
	if score_detail_label:
		score_detail_label.text = text
	
	# Update image-based kill rows
	var total_kills = 0
	for i in range(SCORE_TYPES.size()):
		if i > score_enemy_frame:
			break  # Haven't reached this type yet
		var type = SCORE_TYPES[i]
		var count = score_display_kills.get(type, 0)
		var score = count * SCORE_VALUES[i]
		total_kills += count
		
		# Show the row and update its label
		if kills_container and i < kills_container.get_child_count():
			var row = kills_container.get_child(i)
			row.visible = true
		if i < kill_row_labels.size():
			kill_row_labels[i].text = "  " + str(count) + " x " + str(SCORE_VALUES[i]) + " = " + str(score)
	
	# Show total line once animation is complete
	if total_kills_label and is_instance_valid(total_kills_label):
		if score_enemy_frame >= SCORE_TYPES.size():
			total_kills_label.text = "TOTAL KILLS: " + str(total_kills)
			total_kills_label.visible = true
		else:
			total_kills_label.visible = false

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
