extends CanvasModulate

export var DAY_COLOR = Color("#ffffff")
export var NIGHT_COLOR = Color("#203e57")

func _process(delta):
	color = DAY_COLOR.linear_interpolate(NIGHT_COLOR, clamp(sin(Global.time)*2,0,1))
