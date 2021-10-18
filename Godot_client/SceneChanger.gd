extends CanvasLayer

signal scene_changed()

onready var animations = $AnimationPlayer

func change_scene(path):
	animations.play("fade")
	yield(animations, "animation_finished")
	get_tree().change_scene(path)
	emit_signal("scene_changed")
	animations.play_backwards("fade")
	yield(animations, "animation_finished")
