extends Sprite

export(int) var speed = 5
var pos = 0.0
onready var original_pos = position
onready var animations = $AnimationPlayer

func _ready():
	animations.play("Fly")
	pos = rand_range(0, 1000)

func _process(delta):
	pos += delta * speed
	position.y = original_pos.y + sin(pos)
