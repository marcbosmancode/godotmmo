extends Control

var drag_pos = null
var inventory = preload("res://Items/Inventory.tres")

func _ready():
	rect_global_position = inventory.previous_position

func _on_InventoryContainer_gui_input(event):
	if event is InputEventMouseButton:
		if event.pressed:
			# start dragging
			# rect_global_position is the top left corner of the node
			drag_pos = get_global_mouse_position() - rect_global_position
		else:
			# stop dragging
			drag_pos = null
			inventory.previous_position = rect_global_position
	if event is InputEventMouseMotion and drag_pos:
		rect_global_position = get_global_mouse_position() - drag_pos
