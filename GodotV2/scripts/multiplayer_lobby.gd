extends Control

# Multiplayer lobby - create or join a room via IP address

@onready var ip_label: Label = $VBoxContainer/IPDisplay
@onready var ip_input: LineEdit = $VBoxContainer/JoinSection/IPInput
@onready var status_label: Label = $VBoxContainer/StatusLabel
@onready var create_btn: Button = $VBoxContainer/CreateBtn
@onready var join_btn: Button = $VBoxContainer/JoinSection/JoinBtn
@onready var start_btn: Button = $VBoxContainer/StartBtn
@onready var back_btn: Button = $VBoxContainer/BackBtn

var peer_connected: bool = false

func _ready() -> void:
	start_btn.visible = false
	status_label.text = ""
	ip_label.text = "Your IP: " + NetworkManager.get_local_ip()
	NetworkManager.player_connected.connect(_on_player_connected)
	NetworkManager.player_disconnected.connect(_on_player_disconnected)
	NetworkManager.connection_succeeded.connect(_on_connection_succeeded)
	NetworkManager.connection_failed.connect(_on_connection_failed)
	NetworkManager.server_disconnected.connect(_on_server_disconnected)

func _exit_tree() -> void:
	if NetworkManager.player_connected.is_connected(_on_player_connected):
		NetworkManager.player_connected.disconnect(_on_player_connected)
	if NetworkManager.player_disconnected.is_connected(_on_player_disconnected):
		NetworkManager.player_disconnected.disconnect(_on_player_disconnected)
	if NetworkManager.connection_succeeded.is_connected(_on_connection_succeeded):
		NetworkManager.connection_succeeded.disconnect(_on_connection_succeeded)
	if NetworkManager.connection_failed.is_connected(_on_connection_failed):
		NetworkManager.connection_failed.disconnect(_on_connection_failed)
	if NetworkManager.server_disconnected.is_connected(_on_server_disconnected):
		NetworkManager.server_disconnected.disconnect(_on_server_disconnected)

func _on_create_btn_pressed() -> void:
	var error = NetworkManager.create_server()
	if error == OK:
		status_label.text = "Room created! Waiting for player 2...\nShare your IP: " + NetworkManager.get_local_ip()
		create_btn.disabled = true
		join_btn.disabled = true
		ip_input.editable = false
	else:
		status_label.text = "Failed to create room (Error: " + str(error) + ")"

func _on_join_btn_pressed() -> void:
	var ip = ip_input.text.strip_edges()
	if ip.is_empty():
		status_label.text = "Please enter the host's IP address"
		return
	status_label.text = "Connecting to " + ip + "..."
	create_btn.disabled = true
	join_btn.disabled = true
	var error = NetworkManager.join_server(ip)
	if error != OK:
		status_label.text = "Failed to connect (Error: " + str(error) + ")"
		create_btn.disabled = false
		join_btn.disabled = false

func _on_player_connected(_id: int) -> void:
	peer_connected = true
	if NetworkManager.is_server():
		status_label.text = "Player 2 connected! Press START to begin."
		start_btn.visible = true

func _on_player_disconnected(_id: int) -> void:
	peer_connected = false
	status_label.text = "Player disconnected."
	start_btn.visible = false

func _on_connection_succeeded() -> void:
	status_label.text = "Connected! Waiting for host to start..."

func _on_connection_failed() -> void:
	status_label.text = "Connection failed. Check IP and try again."
	create_btn.disabled = false
	join_btn.disabled = false

func _on_server_disconnected() -> void:
	status_label.text = "Server disconnected."
	create_btn.disabled = false
	join_btn.disabled = false

func _on_start_btn_pressed() -> void:
	if not peer_connected:
		return
	rpc("_remote_start_game")
	_start_game()

@rpc("authority", "reliable")
func _remote_start_game() -> void:
	_start_game()

func _start_game() -> void:
	GameData.is_multiplayer = true
	GameData.current_level = 1
	get_tree().change_scene_to_file("res://scenes/game.tscn")

func _on_back_btn_pressed() -> void:
	NetworkManager.disconnect_from_game()
	get_tree().change_scene_to_file("res://scenes/main_menu.tscn")
