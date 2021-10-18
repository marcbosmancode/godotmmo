extends CenterContainer

var inventory = preload("res://Items/Inventory.tres")
var emptyTexture = preload("res://Items/InventoryDisplay/empty_inventory_slot.png")

onready var itemTexture = $ItemTextureRect
onready var quantityLabel = $ItemTextureRect/QuantityLabel

func display_item(item):
	if item is Item:
		itemTexture.texture = item.texture
		quantityLabel.text = str(item.quantity)
	else:
		itemTexture.texture = emptyTexture
		quantityLabel.text = ""

func get_drag_data(_position):
	var item_index = get_index()
	var item = inventory.remove_item(item_index)
#	check if item is not null
	if item is Item:
		var data = {}
		data.item = item
		data.item_index = item_index
		
		var dragPreview = TextureRect.new()
		dragPreview.texture = item.texture
		set_drag_preview(dragPreview)
		inventory.drag_data = data
		
		return data

func can_drop_data(_position, data):
	return data is Dictionary and data.has("item")

func drop_data(_position, data):
	var drop_item_index = get_index()
	var drop_item = inventory.items[drop_item_index]
	if drop_item is Item and drop_item.name == data.item.name:
		drop_item.quantity += data.item.quantity
		inventory.emit_signal("items_changed", [drop_item_index])
	else:
		inventory.swap_items(drop_item_index, data.item_index)
		inventory.set_item(drop_item_index, data.item)
	inventory.drag_data = null
