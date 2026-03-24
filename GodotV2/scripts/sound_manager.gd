extends Node

# Sound manager matching original SoundManager.java

var sounds: Dictionary = {}
var active_players: Dictionary = {}
var sound_enabled: bool = true

func _ready() -> void:
	sound_enabled = GameData.sound_enabled

func play_sound(sound_name: String, loop: bool = false, volume_db: float = 0.0) -> void:
	if not sound_enabled:
		return
	var player = AudioStreamPlayer.new()
	if sounds.has(sound_name):
		player.stream = sounds[sound_name]
	else:
		var path = "res://assets/sounds/" + sound_name
		if ResourceLoader.exists(path):
			var stream = load(path)
			sounds[sound_name] = stream
			player.stream = stream
		else:
			player.queue_free()
			return
	player.volume_db = volume_db
	add_child(player)
	if loop:
		player.set_meta("looping", true)
		player.finished.connect(func():
			if player.is_inside_tree() and player.get_meta("looping", false):
				player.play()
			else:
				player.queue_free()
		)
	else:
		player.finished.connect(player.queue_free)
	player.play()
	active_players[sound_name] = player

func stop_sound(sound_name: String) -> void:
	if active_players.has(sound_name):
		var player = active_players[sound_name]
		if is_instance_valid(player):
			player.stop()
			player.queue_free()
		active_players.erase(sound_name)

func stop_all_sounds() -> void:
	for key in active_players.keys():
		stop_sound(key)

func set_sound_enabled(enabled: bool) -> void:
	sound_enabled = enabled
	GameData.sound_enabled = enabled
	if not enabled:
		stop_all_sounds()
