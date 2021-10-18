extends Button

var usernameInput : NodePath
onready var usernameLine = get_node("../LineEditUsername")
onready var passwordLine = get_node("../LineEditPassword")

func _on_Button_pressed():
	if usernameLine.text != "" and passwordLine.text != "":
		NetworkController.send_login_packet(usernameLine.text, passwordLine.text)
