extends Control
class_name InteractionComponentUI

export var interaction_component_nodepath : NodePath
export var interaction_text_nodepath : NodePath
export var interaction_text_default : String

func _ready():
	get_node(interaction_component_nodepath).connect("on_interactable_changed", self, "interactable_target_changed")
	
	# Hide the interact text
	hide()

func interactable_target_changed(interactable : Node):
	if interactable == null:
		hide()
		return
	
	var interaction_text = interaction_text_default
	if interactable.has_method("interactable_get_text"):
		interaction_text = interactable.interactable_get_text()
	
	get_node(interaction_text_nodepath).text = interaction_text
	
	# Show the interact text again
	show()
