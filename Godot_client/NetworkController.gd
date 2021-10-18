extends Node

var connection = null
var peerstream = null
var inventoryScene = null

var networkPlayer = preload("res://Player/NetworkPlayer.tscn")
var inventoryDisplay = preload("res://Items/InventoryDisplay/Inventory.tscn")
var inventory = preload("res://Items/Inventory.tres")

var logged_in = false
var inventoryOpened = false

var current_players = []

func _ready():
	print("Connecting to the server")
	connection = StreamPeerTCP.new()
	connection.connect_to_host("127.0.0.1", 8082)
	peerstream = PacketPeerStream.new()
	peerstream.set_stream_peer(connection)
	if connection.get_status() != 1:
		print("Connected to the server")

func send_message_packet(chat_group, message):
	var buffer = StreamPeerBuffer.new()
	buffer.put_u16(7)
	buffer.put_16(chat_group)
	buffer.put_string(message)
	peerstream.put_packet(buffer.get_data_array())

func send_login_packet(username, password):
	var buffer = StreamPeerBuffer.new()
	buffer.put_u16(3)
	buffer.put_string(username)
	buffer.put_string(password)
	peerstream.put_packet(buffer.get_data_array())

func send_position_packet(x, y):
	var buffer = StreamPeerBuffer.new()
	buffer.put_u16(5)
	buffer.put_u32(ceil(x))
	buffer.put_u32(ceil(y))
	peerstream.put_packet(buffer.get_data_array())

func send_character_state_packet(facing_direction, state):
	var buffer = StreamPeerBuffer.new()
	buffer.put_u16(6)
	buffer.put_u16(facing_direction)
	buffer.put_u16(state)
	peerstream.put_packet(buffer.get_data_array())

func send_change_map_packet(new_map, x, y):
	print("sending new map packet " + str(new_map))
	var buffer = StreamPeerBuffer.new()
	buffer.put_u16(8)
	buffer.put_string(new_map)
	buffer.put_u16(x)
	buffer.put_u16(y)
	peerstream.put_packet(buffer.get_data_array())

func send_alter_inventory_packet(slot1, slot2):
	# slot1 is the item thats being moved somewhere
	# enter -1 for slot2 to drop the item in slot1
	var buffer = StreamPeerBuffer.new()
	buffer.put_u16(10)
	buffer.put_u16(slot1)
	buffer.put_u16(slot2)
	peerstream.put_packet(buffer.get_data_array())

func clear_players():
	current_players.clear()

func _process(_delta):
		if connection.is_connected_to_host():
			if peerstream.get_available_packet_count() > 0 :
				var packet = peerstream.get_packet()
				var buffer = StreamPeerBuffer.new()
				buffer.set_data_array(packet)
				
				var type = buffer.get_16()
				print("Recieved a packet with command: %s" % type)
				match type:
					1:
						# Debug message
						var string_length = buffer.get_32()
						var message = buffer.get_string(string_length)
						print("Got a debug message: " + str(message))
					2:
						# Handshake packet
						var string_length = buffer.get_u32()
						var server_time = buffer.get_string(string_length)
						Global.time = int(server_time)
						var accepted_online = buffer.get_u8()
						if accepted_online:
							print("connection accepted")
					3:
						# Register packet
						return
					4:
						# Login packet
						var login_success = buffer.get_8()
						if login_success == 1:
							var string_length = buffer.get_32()
							var character_name = buffer.get_string(string_length)
							string_length = buffer.get_32()
							var room_name = buffer.get_string(string_length)
							var hor_pos = buffer.get_u32()
							var ver_pos = buffer.get_u32()
							
							Global.username = character_name
							Global.target_position = Vector2(hor_pos, ver_pos)
							Global.chatBox.show()
							get_tree().change_scene("res://%s.tscn" % room_name)
							logged_in = true
						else:
							print("failed logging in")
					5:
						# Update position packet
						var username_length = buffer.get_32()
						var username = buffer.get_string(username_length)
						var x_position = buffer.get_32()
						var y_position = buffer.get_32()
						var target_position = Vector2(x_position, y_position)
						
						for otherplayer in current_players:
							if otherplayer is Node:
								if otherplayer.username == username:
									otherplayer.update_target_position(target_position)
									break
					6:
						# Update character state packet
						var username_length = buffer.get_32()
						var username = buffer.get_string(username_length)
						var facing_direction = buffer.get_16()
						var character_state = buffer.get_16()
						
						for otherplayer in current_players:
							if otherplayer is Node:
								if otherplayer.username == username:
									otherplayer.update_facing_direction(facing_direction)
									break
					7:
						# Chat message packet
						var chat_type = buffer.get_16()
						var username_length = buffer.get_32()
						var username = buffer.get_string(username_length)
						var message_length = buffer.get_32()
						var message = buffer.get_string(message_length)
						if Global.chatBox != null:
							Global.chatBox.add_message(username, message, chat_type)
					8:
						# Other player entered map packet
						var username_length = buffer.get_32()
						var username = buffer.get_string(username_length)
						var x_position = buffer.get_32()
						var y_position = buffer.get_32()
						var character_pos = Vector2(x_position, y_position)
						# Instance the network player into the scene
						var netplayer = networkPlayer.instance()
						get_tree().get_current_scene().add_child(netplayer)
						netplayer.username = username
						netplayer.previous_pos = character_pos
						netplayer.target_pos = character_pos
						current_players.append(netplayer)
						print("Adding new player: " + str(netplayer.username))
						print(current_players)
					9:
						# Other player leaving map packet
						var username_length = buffer.get_32()
						var username = buffer.get_string(username_length)
						var i = 1
						for netplayer in current_players:
							i += 1
							if netplayer is Node:
								if netplayer.username == username:
									netplayer.queue_free()
									break
						current_players.remove(i)
					10:
						# NPC ok chat popup packet
						return
					11:
						# Item dropped on the floor packet
						return
					12:
						# Update inventory packet packet
						var inventory_position = buffer.get_16()
						var item_id = buffer.get_16()
						var item_quantity = buffer.get_32()
						inventory.update_slot_by_id(inventory_position, item_id)
					13:
						# Clear item drop on the floor packet
						return
					14:
						# Update character stat packet
						return

func _input(event):
	if Input.is_action_just_pressed("inventory"):
		if inventoryOpened:
			inventoryScene.queue_free()
			inventoryOpened = false
		else:
			if logged_in:
				inventoryScene = inventoryDisplay.instance()
				var cl = ChatBox.get_node("CanvasLayer")
				cl.add_child(inventoryScene)
				inventoryOpened = true
