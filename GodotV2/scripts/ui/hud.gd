extends CanvasLayer

func update_hud(health: int, score: int) -> void:
    $TopBar/HBox/HealthLabel.text = "Armor: %d" % health
    $TopBar/HBox/ScoreLabel.text = "Score: %d" % score

func set_state_text(value: String) -> void:
    $StateLabel.text = value
