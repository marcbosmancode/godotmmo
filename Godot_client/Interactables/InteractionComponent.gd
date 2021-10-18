extends Area2D

export var interaction_parent : NodePath

signal on_interactable_changed(interactable)

var interaction_target : Node

func _process(delta):
	# Check if trying to interact
	if interaction_target != null and Input.is_action_just_pressed("interact"):
		if interaction_target.has_method("interactable_interact"):
			interaction_target.interactable_interact(self)

func _on_InteractionComponent_body_entered(body):
	var canInteract = false
	
	if body.has_method("interactable_can_interact"):
		canInteract = body.interactable_can_interact(get_node(interaction_parent))
	
	if canInteract:
		interaction_target = body
		emit_signal("on_interactable_changed", interaction_target)

func _on_InteractionComponent_body_exited(body):
	if body == interaction_target:
		interaction_target = null
		emit_signal("on_interactable_changed", null)
	
