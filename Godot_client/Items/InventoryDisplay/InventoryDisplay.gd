extends GridContainer

var inventory = preload("res://Items/Inventory.tres")

func _ready():
	inventory.connect("items_changed", self, "on_items_changed")
	inventory.instance_items()
	update_inventory_display()

func update_inventory_display():
	for item_index in inventory.items.size():
		update_inventory_slot_display(item_index)

func update_inventory_slot_display(item_index):
	var inventorySlot = get_child(item_index)
	var item = inventory.items[item_index]
	inventorySlot.display_item(item)

func on_items_changed(indexes):
	for item_index in indexes:
		update_inventory_slot_display(item_index)

func _unhandled_input(event):
	if event.is_action_released("ui_left_mouse"):
		if inventory.drag_data is Dictionary:
			inventory.set_item(inventory.drag_data.item_index, inventory.drag_data.item)
