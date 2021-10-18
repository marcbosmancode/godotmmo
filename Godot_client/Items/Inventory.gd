extends Resource
class_name Inventory

var drag_data = null
var instanced = false
var previous_position = Vector2.ZERO

signal items_changed(indexes)

export(Array, Resource) var items = [
	null, null, null, null, null, null, null, null, null
]

func set_item(item_index, item):
	var previousItem = items[item_index]
	items[item_index] = item
	emit_signal("items_changed", [item_index])
	return previousItem

func swap_items(item_index, target_index):
	print("swapping item in index " + str(target_index) + " with item in index " + str(item_index))
	var targetItem = items[target_index]
	var item = items[item_index]
	items[target_index] = item
	items[item_index] = targetItem
	emit_signal("items_changed", [item_index, target_index])
	NetworkController.send_alter_inventory_packet(target_index, item_index)

func remove_item(item_index):
	var previousItem = items[item_index]
	items[item_index] = null
	emit_signal("items_changed", [item_index])
	return previousItem

func add_item_by_id(item_id):
	var item = load("res://Items/item_%s.tres" % item_id)
	var result = false
	var empty_index = 0
	for item_ in items:
		if item_ == null:
			result = true
			break
		empty_index += 1
	
	if result:
		items[empty_index] = item.duplicate()
		emit_signal("items_changed", [empty_index])
	
	return result

func update_slot_by_id(inventory_slot, item_id):
	var item = null
	if item_id != -1:
		var item_path = "res://Items/item_%s.tres" % item_id
		item = load(item_path)
		print(str(item_path) + " in slot " + str(inventory_slot))
		if item is Item:
			items[inventory_slot] = item.duplicate()
		else:
			items[inventory_slot] = null
	else:
		items[inventory_slot] = null
	emit_signal("items_changed", [inventory_slot])

func instance_items():
	if instanced:
		return
	var instanced_items = []
	for item in items:
		if item is Item:
			instanced_items.append(item.duplicate())
		else:
			instanced_items.append(null)
	items = instanced_items
	instanced = true
