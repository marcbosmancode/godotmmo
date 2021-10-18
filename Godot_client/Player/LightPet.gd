extends Light2D

export(OpenSimplexNoise) var noise
export(int) var speed = 100
var pos = 0.0
onready var original_scale = texture_scale

func _ready():
	pos = rand_range(0, 1000)

func _process(delta):
	pos += delta * speed
	texture_scale = 1 + noise.get_noise_1d(pos) * 0.1
