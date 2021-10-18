extends ParallaxBackground

export var DAY_COLOR = Color("#ffffff")
export var NIGHT_COLOR = Color("#203e57")

onready var canvasModulate = $CanvasModulate
onready var sky = $Sky/Sprite

func _process(delta):
	canvasModulate.color = DAY_COLOR.linear_interpolate(NIGHT_COLOR, clamp(sin(Global.time)*2,0,1))
	sky.modulate.a = 1 - clamp(sin(Global.time)*2,0,1)
