extends Control

onready var textOutput = $VBoxContainer/TextOutput
onready var inputLabel = $VBoxContainer/TextInput/Label
onready var textInput = $VBoxContainer/TextInput/LineEdit

var groups = [
	{'name': 'system', 'color': '#fff61e'},
	{'name': 'map', 'color': '#ffffff'},
	{'name': 'party', 'color': '#1effff'},
	{'name': 'guild', 'color': '#1eff4c'},
	{'name': 'world', 'color': '#ff1eb0'}
]

var current_group = 1 setget set_group
var connected = false
var command_character = "/"

func _ready():
	hide()
	Global.chatBox = self
	self.current_group = 1
	textOutput.bbcode_text += "[color=#fff61e][System]: Welcome to the game![/color]"

func set_group(value):
	current_group = clamp(value, 0, groups.size()-1)
	inputLabel.text = "[%s]" % groups[current_group]['name']
	inputLabel.set("custom_colors/font_color", Color(groups[current_group]['color']))

func cycle_group():
#	Skip the first group for system messages
	if(current_group+1 > groups.size()-1):
		self.current_group = 1
	else:
		self.current_group += 1

func add_message(playername, message, group = 0):
	var outputColor = "\n[color=%s]" % groups[group]['color']
	textOutput.append_bbcode(outputColor)
	
	var outputMessage = "[%s]: " % playername
	outputMessage += message
	textOutput.add_text(outputMessage)

func text_entered(text):
	var first_character = text.left(1)
	if first_character == command_character:
		var string_array = text.split(" ", false, 5)
		match string_array[0]:
			"/help":
				add_message("System", "/help")
	elif text != "":
		add_message(Global.username, text, current_group)
		if NetworkController.logged_in:
			NetworkController.send_message_packet(current_group, text)

func _input(_event):
	if Input.is_action_just_pressed("ui_focus_next"):
		cycle_group()
	if Input.is_action_just_pressed("ui_cancel"):
		textInput.release_focus()
	if Input.is_action_just_pressed("grab_chat_focus"):
		if textInput.get_focus_owner() == textInput:
			text_entered(textInput.text)
			textInput.text = ""
			textInput.release_focus()
		else:
			textInput.grab_focus()
