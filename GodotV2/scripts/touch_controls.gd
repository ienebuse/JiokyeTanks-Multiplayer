extends Control

# Virtual touch controls matching the Java game layout:
# - Joystick with stickview.png background and navstick.png thumb (left side)
# - Shoot button with shoot_btn.png (large, bottom-right)
# - Mine/Bomb button with mine_btn.png (above shoot)
# - Build button with build_btn.png (above mine)

signal direction_changed(direction: int)  # -1 = no direction
signal fire_pressed
signal mine_pressed
signal build_pressed

# Joystick state
var joystick_active: bool = false
var joystick_touch_index: int = -1
var joystick_center: Vector2 = Vector2.ZERO
var joystick_radius: float = 0.0
var stick_position: Vector2 = Vector2.ZERO  # relative offset from center
var current_direction: int = -1  # -1 = none

# Button touch tracking
var shoot_touch_index: int = -1
var mine_touch_index: int = -1
var build_touch_index: int = -1

# UI element rects (set during layout)
var joystick_rect: Rect2 = Rect2()
var shoot_rect: Rect2 = Rect2()
var mine_rect: Rect2 = Rect2()
var build_rect: Rect2 = Rect2()

# Visual sizing
var joystick_outer_radius: float = 75.0
var stick_radius: float = 22.0

# Controls enabled state
var controls_enabled: bool = true

# Textures (loaded from Java game assets)
var tex_stickview: Texture2D = null
var tex_navstick: Texture2D = null
var tex_shoot: Texture2D = null
var tex_shoot_pressed: Texture2D = null
var tex_mine: Texture2D = null
var tex_mine_pressed: Texture2D = null
var tex_build: Texture2D = null
var tex_build_pressed: Texture2D = null

# Fallback colors (used if textures not found)
const JOY_BG_COLOR = Color(0.3, 0.3, 0.3, 0.4)
const JOY_STICK_COLOR = Color(0.7, 0.7, 0.7, 0.6)
const JOY_STICK_ACTIVE_COLOR = Color(0.9, 0.9, 0.9, 0.8)
const DIR_INDICATOR_COLOR = Color(1, 1, 1, 0.3)
const DIR_INDICATOR_ACTIVE_COLOR = Color(1, 1, 1, 0.7)

func _ready() -> void:
	mouse_filter = Control.MOUSE_FILTER_PASS
	_load_textures()
	_calculate_layout()

func _load_textures() -> void:
	tex_stickview = _try_load("res://assets/sprites/stickview.png")
	tex_navstick = _try_load("res://assets/sprites/navstick.png")
	tex_shoot = _try_load("res://assets/sprites/shoot_btn.png")
	tex_shoot_pressed = _try_load("res://assets/sprites/shoot_btn_pressed.png")
	tex_mine = _try_load("res://assets/sprites/mine_btn.png")
	tex_mine_pressed = _try_load("res://assets/sprites/mine_btn_pressed.png")
	tex_build = _try_load("res://assets/sprites/build_btn.png")
	tex_build_pressed = _try_load("res://assets/sprites/build_btn_pressed.png")

func _try_load(path: String) -> Texture2D:
	if ResourceLoader.exists(path):
		return load(path)
	return null

func _calculate_layout() -> void:
	var vp = get_viewport_rect().size
	var scale_factor = min(vp.x, vp.y) / 600.0

	# Joystick - bottom left (larger, 150dp equivalent)
	joystick_outer_radius = 85.0 * scale_factor
	stick_radius = 28.0 * scale_factor
	var joy_margin = 20.0 * scale_factor
	joystick_center = Vector2(
		joy_margin + joystick_outer_radius,
		vp.y - joy_margin - joystick_outer_radius
	)
	joystick_radius = joystick_outer_radius * 0.85
	joystick_rect = Rect2(
		joystick_center - Vector2(joystick_outer_radius, joystick_outer_radius),
		Vector2(joystick_outer_radius * 2, joystick_outer_radius * 2)
	)

	# Shoot button - bottom right (large, 100dp equivalent)
	var shoot_size = 100.0 * scale_factor
	var shoot_margin_x = 30.0 * scale_factor
	var shoot_margin_y = 20.0 * scale_factor
	shoot_rect = Rect2(
		vp.x - shoot_margin_x - shoot_size,
		vp.y - shoot_margin_y - shoot_size,
		shoot_size,
		shoot_size
	)

	# Mine button - above shoot button (medium, 60dp equivalent)
	var mine_size = 60.0 * scale_factor
	var btn_gap = 8.0 * scale_factor
	mine_rect = Rect2(
		shoot_rect.position.x + (shoot_size - mine_size) / 2.0,
		shoot_rect.position.y - mine_size - btn_gap,
		mine_size,
		mine_size
	)

	# Build button - above mine button (medium, 60dp equivalent)
	var build_size = 60.0 * scale_factor
	build_rect = Rect2(
		shoot_rect.position.x + (shoot_size - build_size) / 2.0,
		mine_rect.position.y - build_size - btn_gap,
		build_size,
		build_size
	)

	stick_position = Vector2.ZERO
	queue_redraw()

func _notification(what: int) -> void:
	if what == NOTIFICATION_RESIZED:
		_calculate_layout()

func disable_controls() -> void:
	controls_enabled = false
	# Release any active touches
	_release_all()
	queue_redraw()

func enable_controls() -> void:
	controls_enabled = true
	queue_redraw()

func _release_all() -> void:
	if joystick_touch_index >= 0:
		joystick_touch_index = -1
		joystick_active = false
		stick_position = Vector2.ZERO
		_set_direction(-1)
		for action in ["move_up", "move_down", "move_left", "move_right"]:
			if Input.is_action_pressed(action):
				Input.action_release(action)
	if shoot_touch_index >= 0:
		shoot_touch_index = -1
		Input.action_release("fire")
	if mine_touch_index >= 0:
		mine_touch_index = -1
		Input.action_release("drop_mine")
	if build_touch_index >= 0:
		build_touch_index = -1
		Input.action_release("build")

func _input(event: InputEvent) -> void:
	if not visible or not controls_enabled:
		return

	if event is InputEventScreenTouch:
		var touch = event as InputEventScreenTouch
		if touch.pressed:
			_handle_touch_down(touch.index, touch.position)
		else:
			_handle_touch_up(touch.index)
	elif event is InputEventScreenDrag:
		var drag = event as InputEventScreenDrag
		_handle_touch_move(drag.index, drag.position)

func _handle_touch_down(index: int, pos: Vector2) -> void:
	# Check joystick area (expanded for easier touch)
	var dist_sq = (pos - joystick_center).length_squared()
	var max_dist = joystick_outer_radius * 1.3
	if dist_sq <= max_dist * max_dist and joystick_touch_index == -1:
		joystick_touch_index = index
		joystick_active = true
		_update_joystick(pos)
		get_viewport().set_input_as_handled()
		return

	# Check shoot button
	if shoot_rect.grow(10).has_point(pos) and shoot_touch_index == -1:
		shoot_touch_index = index
		fire_pressed.emit()
		Input.action_press("fire")
		queue_redraw()
		get_viewport().set_input_as_handled()
		return

	# Check mine button
	if mine_rect.grow(10).has_point(pos) and mine_touch_index == -1:
		mine_touch_index = index
		mine_pressed.emit()
		Input.action_press("drop_mine")
		queue_redraw()
		get_viewport().set_input_as_handled()
		return

	# Check build button
	if build_rect.grow(10).has_point(pos) and build_touch_index == -1:
		build_touch_index = index
		build_pressed.emit()
		Input.action_press("build")
		queue_redraw()
		get_viewport().set_input_as_handled()
		return

func _handle_touch_up(index: int) -> void:
	if index == joystick_touch_index:
		joystick_touch_index = -1
		joystick_active = false
		stick_position = Vector2.ZERO
		_set_direction(-1)
		# Release all direction actions
		for action in ["move_up", "move_down", "move_left", "move_right"]:
			if Input.is_action_pressed(action):
				Input.action_release(action)
		queue_redraw()

	if index == shoot_touch_index:
		shoot_touch_index = -1
		Input.action_release("fire")
		queue_redraw()

	if index == mine_touch_index:
		mine_touch_index = -1
		Input.action_release("drop_mine")
		queue_redraw()

	if index == build_touch_index:
		build_touch_index = -1
		Input.action_release("build")
		queue_redraw()

func _handle_touch_move(index: int, pos: Vector2) -> void:
	if index == joystick_touch_index:
		_update_joystick(pos)

func _update_joystick(touch_pos: Vector2) -> void:
	var rel = touch_pos - joystick_center
	var dist = rel.length()

	# Clamp to joystick radius
	if dist > joystick_radius:
		rel = rel.normalized() * joystick_radius

	stick_position = rel

	# Determine direction using diagonal partitioning (matching Java getDir)
	var deadzone = joystick_radius * 0.2
	if dist < deadzone:
		_set_direction(-1)
	else:
		if rel.y > rel.x and rel.y > -rel.x:
			_set_direction(GameData.Direction.DOWN)
		elif rel.y > rel.x and rel.y < -rel.x:
			_set_direction(GameData.Direction.LEFT)
		elif rel.y < rel.x and rel.y < -rel.x:
			_set_direction(GameData.Direction.UP)
		elif rel.y < rel.x and rel.y > -rel.x:
			_set_direction(GameData.Direction.RIGHT)

	queue_redraw()

func _set_direction(dir: int) -> void:
	if dir == current_direction:
		return

	# Release old direction
	var action_map = {
		GameData.Direction.UP: "move_up",
		GameData.Direction.DOWN: "move_down",
		GameData.Direction.LEFT: "move_left",
		GameData.Direction.RIGHT: "move_right",
	}

	for d in action_map:
		if Input.is_action_pressed(action_map[d]):
			Input.action_release(action_map[d])

	current_direction = dir

	# Press new direction
	if dir >= 0 and dir in action_map:
		Input.action_press(action_map[dir])

	direction_changed.emit(dir)

func _draw() -> void:
	if not visible:
		return

	var alpha_mult = 1.0 if controls_enabled else 0.3

	# --- Joystick Background (stickview.png or fallback circle) ---
	if tex_stickview:
		draw_texture_rect(tex_stickview, joystick_rect, false, Color(1, 1, 1, 0.7 * alpha_mult))
	else:
		draw_circle(joystick_center, joystick_outer_radius, Color(JOY_BG_COLOR.r, JOY_BG_COLOR.g, JOY_BG_COLOR.b, JOY_BG_COLOR.a * alpha_mult))

	# Direction indicator ticks on the outer ring
	var tick_len = joystick_outer_radius * 0.15
	var tick_w = 2.0
	var dirs_info = [
		[Vector2(0, -1), current_direction == GameData.Direction.UP],
		[Vector2(0, 1), current_direction == GameData.Direction.DOWN],
		[Vector2(-1, 0), current_direction == GameData.Direction.LEFT],
		[Vector2(1, 0), current_direction == GameData.Direction.RIGHT],
	]
	for info in dirs_info:
		var dir_vec: Vector2 = info[0]
		var active: bool = info[1]
		var color = DIR_INDICATOR_ACTIVE_COLOR if active else DIR_INDICATOR_COLOR
		color.a *= alpha_mult
		var outer = joystick_center + dir_vec * (joystick_outer_radius - 2)
		var inner = joystick_center + dir_vec * (joystick_outer_radius - 2 - tick_len)
		draw_line(inner, outer, color, tick_w)

	# --- Joystick Thumb (navstick.png or fallback circle) ---
	var stick_pos = joystick_center + stick_position
	if tex_navstick:
		var stick_size = stick_radius * 2.0
		var stick_rect = Rect2(stick_pos - Vector2(stick_size / 2, stick_size / 2), Vector2(stick_size, stick_size))
		var alpha = (0.9 if joystick_active else 0.7) * alpha_mult
		draw_texture_rect(tex_navstick, stick_rect, false, Color(1, 1, 1, alpha))
	else:
		var stick_color = JOY_STICK_ACTIVE_COLOR if joystick_active else JOY_STICK_COLOR
		stick_color.a *= alpha_mult
		draw_circle(stick_pos, stick_radius, stick_color)

	# --- Shoot Button (shoot_btn.png or fallback rect) ---
	var shoot_pressed = shoot_touch_index >= 0
	var shoot_tex = tex_shoot_pressed if shoot_pressed and tex_shoot_pressed else tex_shoot
	if shoot_tex:
		var alpha = (1.0 if shoot_pressed else 0.8) * alpha_mult
		draw_texture_rect(shoot_tex, shoot_rect, false, Color(1, 1, 1, alpha))
	else:
		var shoot_color = Color(1.0, 0.3, 0.3, 0.7) if shoot_pressed else Color(0.8, 0.2, 0.2, 0.5)
		shoot_color.a *= alpha_mult
		draw_rect(shoot_rect, shoot_color)
		draw_rect(shoot_rect, shoot_color.lightened(0.3), false, 2.0)

	# --- Mine Button (mine_btn.png or fallback rect) ---
	var mine_pressed = mine_touch_index >= 0
	var mine_tex = tex_mine_pressed if mine_pressed and tex_mine_pressed else tex_mine
	if mine_tex:
		var alpha = (1.0 if mine_pressed else 0.8) * alpha_mult
		draw_texture_rect(mine_tex, mine_rect, false, Color(1, 1, 1, alpha))
	else:
		var mine_color = Color(0.3, 0.8, 0.3, 0.7) if mine_pressed else Color(0.2, 0.6, 0.2, 0.5)
		mine_color.a *= alpha_mult
		draw_rect(mine_rect, mine_color)
		draw_rect(mine_rect, mine_color.lightened(0.3), false, 2.0)

	# --- Build Button (build_btn.png or fallback rect) ---
	var build_is_pressed = build_touch_index >= 0
	var build_tex = tex_build_pressed if build_is_pressed and tex_build_pressed else tex_build
	if build_tex:
		var alpha = (1.0 if build_is_pressed else 0.8) * alpha_mult
		draw_texture_rect(build_tex, build_rect, false, Color(1, 1, 1, alpha))
	else:
		var build_color = Color(0.3, 0.3, 0.8, 0.7) if build_is_pressed else Color(0.2, 0.2, 0.6, 0.5)
		build_color.a *= alpha_mult
		draw_rect(build_rect, build_color)
		draw_rect(build_rect, build_color.lightened(0.3), false, 2.0)
