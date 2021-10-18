extends Node

var chatBox
var username = ""
var target_position
var time = 0 setget , get_time

# The higher the time scale the slower time moves
var time_scale = 10000

func get_time():
	return time/time_scale

func _physics_process(delta):
	# Multiplying from seconds to milliseconds
	time += delta * 1000
