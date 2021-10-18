extends KinematicBody2D

var username = "username" setget set_username
var facing_direction = 1
var touching_ground = false

# Movement lerp variables
var time = 0
var moveDuration = 0.1
onready var previous_pos = global_position
onready var target_pos = global_position

onready var label = $NameLabel
onready var sprite = $Sprite
onready var animations = $PlayerAnimator

func _ready():
	label.text = username

func set_username(value):
	username = value
	label.text = username

func update_target_position(new_pos):
	previous_pos = global_position
	target_pos = new_pos
	time = 0

func update_facing_direction(direction):
	facing_direction = direction
	sprite.flip_h = facing_direction < 0

func is_floored():
	return test_move(self.transform, Vector2.DOWN)

func _physics_process(delta):
	touching_ground = is_floored()
	if !touching_ground:
		if previous_pos.y > target_pos.y:
			animations.play("Jump")
		else:
			animations.play("Fall")
	else:
		var hor_difference = target_pos.x - previous_pos.x
		# Make the player appear idle if it is close to the target position
		if abs(hor_difference) < 2:
			animations.play("Idle")
		else:
			animations.play("Run")
	
	time += delta
	var t = time / moveDuration
	t = min(t, 1)
	
	global_position = lerp(previous_pos, target_pos, t)
