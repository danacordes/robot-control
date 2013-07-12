#!/usr/bin/python

import RPi.GPIO as GPIO
import time

step_angle = 1.8
micro_steps = 1
minimum_step_speed = 0.01
maximum_step_speed = 0.1 
reset_pin = 12

class Motor:
	steps_left = 0
	speed = 0
	next_step_tick = 0
	powered = False
	direction = 0
	STEPS = 0
	SPEED = 1
	mode = STEPS
	"""Defines a motor"""
	def __init__(self, name, step_pin, dir_pin):
		self.name = name
		self.step_pin = step_pin
		self.dir_pin = dir_pin

	def rotate_degrees(self, degrees, speed):
		self.mode = self.STEPS
		self.powered = True
		#direction = (degrees>0)?GPIO.HIGH:GPIO.LOW
		self.set_direction(degrees)
		self.set_speed(speed)

		self.steps_left = int(abs(degrees)/(step_angle/micro_steps))

		self.next_step_tick = 0

	def move_speed(self, direction, speed):
		self.mode = self.SPEED
		self.powered = True
		self.set_direction(direction)
		self.set_speed(speed)
		self.next_step_tick = 0

	def set_direction(self, direction):
		direction = 1 if (direction>0) else 0 if (direction==0) else -1
		self.direction = direction

		if direction != 0:
			dir_control = GPIO.HIGH if (direction==1) else GPIO.LOW
			GPIO.output(self.dir_pin, dir_control)
 
	def set_speed(self, speed):
		"""-100 - 100"""
		self.set_direction(speed)
		speed = abs(speed)
		speed = 100 if (speed > 100) else 0 if (speed < 0) else speed
		self.speed = maximum_step_speed	- ( (speed / 100.0 ) * (maximum_step_speed - minimum_step_speed))
		self.next_step_tick = 0
		#print "set speed to ", self.speed

	def step(self):
		GPIO.output(self.step_pin, GPIO.HIGH)
		GPIO.output(self.step_pin, GPIO.LOW)

	def run_tick(self, tick):

#		elif self.mode = self.SPEED				
#			else:
#				True = True
		#print tick, "\t", self.next_step_tick
		if(self.direction != 0 and self.next_step_tick <= tick):
			#handle step decrementing 
			if self.mode == self.STEPS:
				if self.steps_left > 0:
					self.steps_left -= 1
				if self.steps_left <= 0:
					self.direction = 0

			self.step()		
			#self.next_step_tick = tick + self.speed
			if (self.next_step_tick == 0):
				self.next_step_tick = tick + self.speed
			else:
				self.next_step_tick += self.speed
		

motors = [
	Motor('Left Wheel', 7, 11), #name, step pin, dir_pin
	Motor('Right Wheel', 13, 15)
]
	

# to use Raspberry Pi board pin numbers
GPIO.setmode(GPIO.BOARD)

# set up GPIO output channel
#GPIO.setup(pin_step,GPIO.OUT)
#GPIO.setup(pin_dir,GPIO.OUT)
GPIO.setup(reset_pin,GPIO.OUT)
for motor in motors:
	print "initializing ", motor.name
	GPIO.setup(motor.step_pin,GPIO.OUT)
	GPIO.setup(motor.dir_pin,GPIO.OUT)

delay = 0.00001
#for i in range(0,2*int((360/step_angle)*8)):
	#for step in io_order:
		#set_color(step[0], step[1], step[2])	
#		GPIO.output(pin_step,GPIO.HIGH)
#		time.sleep(delay)
#		GPIO.output(pin_step,GPIO.LOW)
#		time.sleep(delay)


#rotate_degrees(360,10000)
#rotate_degrees(180,5000)

try:
	run_game = True

	while run_game:
		#output
		tick = time.clock()
		motors_moving = False 
		for motor in motors:
	
			if (motor.direction == 0):
				True = True
			else:
				#print "steps left: ", motor.steps_left 
				if (not motors_moving):
					GPIO.output(reset_pin, GPIO.HIGH)
					motors_moving = True 
				motor.run_tick(tick)
				#time.sleep(0.001)
		if (not motors_moving):
			GPIO.output(reset_pin, GPIO.LOW)
		#input
		if (not motors_moving) :
			GPIO.output(reset_pin, GPIO.LOW)
			try:
				run_game = False
				degrees = int(raw_input('Number of degrees: '))
				speed = int(raw_input('Speed [1-100]: '))
				motors[0].rotate_degrees(degrees, -speed)
				motors[1].rotate_degrees(degrees, speed)
		
				run_game = True 
			except ValueError:
				print "Not a number"


finally:
	GPIO.cleanup()
