extends StaticBody2D

export var connected_scene : String = ""
export var target_position : Vector2 = Vector2.ZERO
export var interaction_text = "Enter"

func _ready():
	SceneChanger.connect("scene_changed", self, "_on_scene_changed")

func interactable_can_interact(InteractionComponentParent : Node):
	return InteractionComponentParent is Player

func interactable_get_text():
	return interaction_text

func _on_scene_changed():
	NetworkController.send_change_map_packet(connected_scene, target_position.x, target_position.y)

func interactable_interact(InteractionComponentParent: Node):
	if connected_scene != "":
		NetworkController.clear_players()
		Global.target_position = target_position
		var scene_path = "res://%s.tscn" % connected_scene
		SceneChanger.change_scene(scene_path)
