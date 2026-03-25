extends Node

# Network manager for WiFi multiplayer using ENet (UDP-based, low latency)
# Supports connection via room code (6 alphanumeric characters) or IP address

signal player_connected(peer_id: int)
signal player_disconnected(peer_id: int)
signal connection_succeeded()
signal connection_failed()
signal server_disconnected()

const DEFAULT_PORT: int = 7000
const MAX_CONNECTIONS: int = 1  # 2-player only
const ROOM_CODE_LENGTH: int = 6
const ROOM_CODE_CHARS: String = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"  # No I/O/0/1 to avoid confusion

var peer: ENetMultiplayerPeer = null
var is_host: bool = false
var is_multiplayer_mode: bool = false
var room_code: String = ""

func create_server(port: int = DEFAULT_PORT) -> Error:
	peer = ENetMultiplayerPeer.new()
	var error = peer.create_server(port, MAX_CONNECTIONS)
	if error != OK:
		return error
	multiplayer.multiplayer_peer = peer
	is_host = true
	is_multiplayer_mode = true
	room_code = _generate_room_code()
	multiplayer.peer_connected.connect(_on_peer_connected)
	multiplayer.peer_disconnected.connect(_on_peer_disconnected)
	return OK

func join_server(address: String, port: int = DEFAULT_PORT) -> Error:
	peer = ENetMultiplayerPeer.new()
	var error = peer.create_client(address, port)
	if error != OK:
		return error
	multiplayer.multiplayer_peer = peer
	is_host = false
	is_multiplayer_mode = true
	multiplayer.peer_connected.connect(_on_peer_connected)
	multiplayer.peer_disconnected.connect(_on_peer_disconnected)
	multiplayer.connected_to_server.connect(_on_connected_to_server)
	multiplayer.connection_failed.connect(_on_connection_failed)
	multiplayer.server_disconnected.connect(_on_server_disconnected)
	return OK

func disconnect_from_game() -> void:
	_disconnect_signals()
	if peer:
		peer.close()
	multiplayer.multiplayer_peer = null
	peer = null
	is_host = false
	is_multiplayer_mode = false
	room_code = ""

func _disconnect_signals() -> void:
	if multiplayer.peer_connected.is_connected(_on_peer_connected):
		multiplayer.peer_connected.disconnect(_on_peer_connected)
	if multiplayer.peer_disconnected.is_connected(_on_peer_disconnected):
		multiplayer.peer_disconnected.disconnect(_on_peer_disconnected)
	if multiplayer.connected_to_server.is_connected(_on_connected_to_server):
		multiplayer.connected_to_server.disconnect(_on_connected_to_server)
	if multiplayer.connection_failed.is_connected(_on_connection_failed):
		multiplayer.connection_failed.disconnect(_on_connection_failed)
	if multiplayer.server_disconnected.is_connected(_on_server_disconnected):
		multiplayer.server_disconnected.disconnect(_on_server_disconnected)

func _on_peer_connected(id: int) -> void:
	player_connected.emit(id)

func _on_peer_disconnected(id: int) -> void:
	player_disconnected.emit(id)

func _on_connected_to_server() -> void:
	connection_succeeded.emit()

func _on_connection_failed() -> void:
	connection_failed.emit()

func _on_server_disconnected() -> void:
	server_disconnected.emit()

func is_server() -> bool:
	return is_multiplayer_mode and is_host

func is_client() -> bool:
	return is_multiplayer_mode and not is_host

func _generate_room_code() -> String:
	# Deterministic room code derived from local IP so the same host always
	# produces the same code (allows reconnection). Uses IP octets as seed.
	var ip = get_local_ip()
	var parts = ip.split(".")
	var seed_val: int = 0
	for p in parts:
		seed_val = seed_val * 256 + int(p)
	# Mix in the port so different ports on the same IP get different codes
	seed_val = seed_val ^ (DEFAULT_PORT * 31)
	var rng = RandomNumberGenerator.new()
	rng.seed = seed_val
	var code = ""
	for i in range(ROOM_CODE_LENGTH):
		code += ROOM_CODE_CHARS[rng.randi() % ROOM_CODE_CHARS.length()]
	return code

func resolve_room_code(code: String) -> String:
	# Try to resolve a room code to an IP address by scanning the local subnet.
	# Since codes are deterministic from IP, we check each possible IP on the
	# local /24 subnet to see if it would produce the given code.
	var local_ip = get_local_ip()
	var parts = local_ip.split(".")
	if parts.size() < 4:
		return ""
	var base = parts[0] + "." + parts[1] + "." + parts[2] + "."
	for i in range(1, 255):
		var test_ip = base + str(i)
		var test_parts = test_ip.split(".")
		var seed_val: int = 0
		for p in test_parts:
			seed_val = seed_val * 256 + int(p)
		seed_val = seed_val ^ (DEFAULT_PORT * 31)
		var rng = RandomNumberGenerator.new()
		rng.seed = seed_val
		var test_code = ""
		for _j in range(ROOM_CODE_LENGTH):
			test_code += ROOM_CODE_CHARS[rng.randi() % ROOM_CODE_CHARS.length()]
		if test_code == code:
			return test_ip
	return ""

func is_room_code(input: String) -> bool:
	# A room code is exactly ROOM_CODE_LENGTH alphanumeric characters with no dots
	if input.length() != ROOM_CODE_LENGTH:
		return false
	if "." in input:
		return false
	for ch in input:
		if ch not in ROOM_CODE_CHARS and ch.to_upper() not in ROOM_CODE_CHARS:
			return false
	return true

func get_local_ip() -> String:
	var addresses = IP.get_local_addresses()
	for addr in addresses:
		# Skip IPv6 and loopback
		if ":" in addr or addr == "127.0.0.1":
			continue
		if addr.begins_with("192.168.") or addr.begins_with("10."):
			return addr
		if addr.begins_with("172."):
			var parts = addr.split(".")
			if parts.size() >= 2:
				var second = int(parts[1])
				if second >= 16 and second <= 31:
					return addr
	return "127.0.0.1"
