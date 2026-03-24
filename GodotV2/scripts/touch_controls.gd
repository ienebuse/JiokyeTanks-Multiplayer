extends Control

# Virtual touch controls matching the Java game layout:
# - Joystick (left side) for directional movement
# - Shoot button (large, bottom-right)
# - Mine/Bomb button (smaller, above shoot)

signal direction_changed(direction: int)  # -1 = no direction
signal fire_pressed
signal mine_pressed

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

# UI element rects (set during layout)
var joystick_rect: Rect2 = Rect2()
var shoot_rect: Rect2 = Rect2()
var mine_rect: Rect2 = Rect2()

# Visual sizing
var joystick_outer_radius: float = 75.0
var stick_radius: float = 22.0

# Colors
const JOY_BG_COLOR = Color(0.3, 0.3, 0.3, 0.4)
const JOY_STICK_COLOR = Color(0.7, 0.7, 0.7, 0.6)
const JOY_STICK_ACTIVE_COLOR = Color(0.9, 0.9, 0.9, 0.8)
const BTN_COLOR = Color(0.3, 0.3, 0.3, 0.5)
const BTN_PRESSED_COLOR = Color(0.5, 0.5, 0.5, 0.7)
const SHOOT_COLOR = Color(0.8, 0.2, 0.2, 0.5)
const SHOOT_PRESSED_COLOR = Color(1.0, 0.3, 0.3, 0.7)
const MINE_COLOR = Color(0.2, 0.6, 0.2, 0.5)
const MINE_PRESSED_COLOR = Color(0.3, 0.8, 0.3, 0.7)
const LABEL_COLOR = Color(1, 1, 1, 0.8)
const DIR_INDICATOR_COLOR = Color(1, 1, 1, 0.3)
const DIR_INDICATOR_ACTIVE_COLOR = Color(1, 1, 1, 0.7)

func _ready() -> void:
	mouse_filter = Control.MOUSE_FILTER_PASS
	_calculate_layout()

func _calculate_layout() -> void:
	var vp = get_viewport_rect().size
	var scale_factor = min(vp.x, vp.y) / 600.0

	# Joystick - bottom left
	joystick_outer_radius = 65.0 * scale_factor
	stick_radius = 20.0 * scale_factor
	var joy_margin = 30.0 * scale_factor
	joystick_center = Vector2(
		joy_margin + joystick_outer_radius,
		vp.y - joy_margin - joystick_outer_radius
	)
	joystick_radius = joystick_outer_radius * 0.85
	joystick_rect = Rect2(
		joystick_center - Vector2(joystick_outer_radius, joystick_outer_radius),
		Vector2(joystick_outer_radius * 2, joystick_outer_radius * 2)
	)

	# Shoot button - bottom right (large)
	var shoot_size = 80.0 * scale_factor
	var shoot_margin_x = 40.0 * scale_factor
	var shoot_margin_y = 30.0 * scale_factor
	shoot_rect = Rect2(
		vp.x - shoot_margin_x - shoot_size,
		vp.y - shoot_margin_y - shoot_size,
		shoot_size,
		shoot_size
	)

	# Mine button - above shoot button (smaller)
	var mine_size = 50.0 * scale_factor
	mine_rect = Rect2(
		shoot_rect.position.x + (shoot_size - mine_size) / 2.0,
		shoot_rect.position.y - mine_size - 10.0 * scale_factor,
		mine_size,
		mine_size
	)

	stick_position = Vector2.ZERO
	queue_redraw()

func _notification(what: int) -> void:
	if what == NOTIFICATION_RESIZED:
		_calculate_layout()

func _input(event: InputEvent) -> void:
	if not visible:
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
	# Two diagonal lines: y = x and y = -x through center
	var deadzone = joystick_radius * 0.2
	if dist < deadzone:
		_set_direction(-1)
	else:
		# Java uses: yL1 = x - cx + cy, yL2 = cx - x + cy
		# Relative: yL1 = relX, yL2 = -relX
		# DOWN: relY > relX AND relY > -relX  (below both diagonals)
		# LEFT: relY > relX AND relY < -relX
		# UP: relY < relX AND relY < -relX (above both diagonals)
		# RIGHT: relY < relX AND relY > -relX
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

	# --- Joystick Background ---
	draw_circle(joystick_center, joystick_outer_radius, JOY_BG_COLOR)

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
		var outer = joystick_center + dir_vec * (joystick_outer_radius - 2)
		var inner = joystick_center + dir_vec * (joystick_outer_radius - 2 - tick_len)
		draw_line(inner, outer, color, tick_w)

	# --- Joystick Thumb ---
	var stick_pos = joystick_center + stick_position
	var stick_color = JOY_STICK_ACTIVE_COLOR if joystick_active else JOY_STICK_COLOR
	draw_circle(stick_pos, stick_radius, stick_color)

	# --- Shoot Button ---
	var shoot_color = SHOOT_PRESSED_COLOR if shoot_touch_index >= 0 else SHOOT_COLOR
	_draw_rounded_rect(shoot_rect, shoot_color)
	# "FIRE" label
	var shoot_center = shoot_rect.get_center()
	_draw_label(shoot_center, "FIRE", shoot_rect.size.x * 0.2)

	# --- Mine Button ---
	var mine_color = MINE_PRESSED_COLOR if mine_touch_index >= 0 else MINE_COLOR
	_draw_rounded_rect(mine_rect, mine_color)
	var mine_center = mine_rect.get_center()
	_draw_label(mine_center, "MINE", mine_rect.size.x * 0.2)

func _draw_rounded_rect(rect: Rect2, color: Color) -> void:
	var radius = min(rect.size.x, rect.size.y) * 0.15
	# Draw as simple filled rect with slightly rounded feel (border + fill)
	draw_rect(rect, color)
	draw_rect(rect, color.lightened(0.3), false, 2.0)

func _draw_label(center: Vector2, text: String, font_size: float) -> void:
	var font = ThemeDB.fallback_font
	if font:
		var fs = int(font_size)
		var text_size = font.get_string_size(text, HORIZONTAL_ALIGNMENT_CENTER, -1, fs)
		var pos = center - Vector2(text_size.x / 2.0, -text_size.y / 4.0)
		draw_string(font, pos, text, HORIZONTAL_ALIGNMENT_LEFT, -1, fs, LABEL_COLOR)
