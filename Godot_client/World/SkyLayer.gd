extends ParallaxLayer

export(float) var SKY_SPEED = -4

func _process(delta):
	self.motion_offset.y += SKY_SPEED * delta
