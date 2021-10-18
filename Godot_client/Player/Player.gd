extends KinematicBody2D
class_name Player

export var ACCELERATION = 400
export var MAX_SPEED = 100
export var FRICTION = 10
export var AIR_RESISTANCE = 1
export var GRAVITY = 500
export var JUMP_FORCE = 230

var motion = Vector2.ZERO

var jump_allowed = false
var touching_ground = false

var previous_facing_direction = 1
var facing_direction = 1

var position_timer_time = 0.1
var sent_last_position_packet = false

onready var previous_x = global_position.x
onready var previous_y = global_position.y

var username = "" setget set_username
onready var sprite = $Sprite
onready var spirit_sprite = $Spirit
onready var c_timer = $CoyoteTimer
onready var up_timer = $UpdatePositionTimer
onready var label = $NameLabel
onready var animations = $PlayerAnimator

func _ready():
	if Global.target_position != null:
		global_position = Global.target_position
	set_username(Global.username)
	up_timer.start(position_timer_time)

func set_username(value):
	username = value
	label.text = username

func _physics_process(delta):
	touching_ground = is_on_floor()
	var x_input = Input.get_action_strength("ui_right") - Input.get_action_strength("ui_left")
	
	if touching_ground:
		jump_allowed = true
		c_timer.start(0.05)
	
	if jump_allowed:
		if x_input != 0:
			animations.play("Run")
		else:
			animations.play("Idle")
	else:
		if motion.y < 0:
			animations.play("Jump")
		else:
			animations.play("Fall")
	
	if x_input != 0:
		motion.x += x_input * ACCELERATION * delta
		motion.x = clamp(motion.x, -MAX_SPEED, MAX_SPEED)
		facing_direction = x_input
		sprite.flip_h = x_input < 0
		spirit_sprite.flip_h = x_input < 0
		
	motion.y += GRAVITY * delta
	
	if Input.is_action_just_pressed("ui_up"):
		if jump_allowed:
			motion.y = -JUMP_FORCE
	
	if touching_ground:
		if x_input == 0:
			motion.x = lerp(motion.x, 0, FRICTION * delta)
	else:
		if x_input == 0:
			motion.x = lerp(motion.x, 0, AIR_RESISTANCE * delta)
	
	motion = move_and_slide(motion, Vector2.UP)

func _on_CoyoteTimer_timeout():
	jump_allowed = false

func _on_UpdatePositionTimer_timeout():
	if(facing_direction != previous_facing_direction):
		previous_facing_direction = facing_direction
		NetworkController.send_character_state_packet(facing_direction, 1)
	
	if ceil(global_position.x) != previous_x:
		previous_x = ceil(global_position.x)
		NetworkController.send_position_packet(global_position.x, global_position.y)
		up_timer.start(position_timer_time)
		sent_last_position_packet = false
		return
	elif ceil(global_position.y) != previous_y:
		previous_y = ceil(global_position.y)
		NetworkController.send_position_packet(global_position.x, global_position.y)
		up_timer.start(position_timer_time)
		sent_last_position_packet = false
		return
	elif not sent_last_position_packet:
		NetworkController.send_position_packet(global_position.x, global_position.y)
		up_timer.start(position_timer_time)
		sent_last_position_packet = true
		return
	up_timer.start(position_timer_time)
