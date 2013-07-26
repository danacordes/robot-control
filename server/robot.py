#!/usr/bin/python

#import RPi.GPIO as GPIO
import RPIO as GPIO
import time
import sys
import select
import tty
import termios
import asyncore
import asynchat
import socket

step_angle = 1.8
micro_steps = 1
minimum_step_speed = 0.01
maximum_step_speed = 0.1 
reset_pin = 12

class Server(asyncore.dispatcher):
        def __init__(self, host, port):
                asyncore.dispatcher.__init__(self)
                self.create_socket(socket.AF_INET, socket.SOCK_STREAM)
                self.set_reuse_addr()
                self.bind((host, port))
                print "listening on ",host,":",port
                self.listen(5)

        def handle_accept(self):
                pair = self.accept()
                if pair is not None:
                        sock, addr = pair
                        print 'Incoming connection from %s' % repr(addr)
                        handler = RoboHandler(sock, addr)
class RoboHandler(asynchat.async_chat):
        def __init__(self, sock, addr):
                asynchat.async_chat.__init__(self, sock)

                self.addr = addr
                self.ibuffer = []
                self.obuffer = ""
                self.set_terminator("\n")

        def collect_incoming_data(self, data):
                """buffer the data"""
                self.ibuffer.append(data)

        def found_terminator(self):
                print self.ibuffer[0]
		execute_command(self.ibuffer[0])
                self.ibuffer = []

        def handle_close(self):
                print 'Disconnecting connection from %s' % repr(self.addr)
                self.close()


class Motor:
	steps_left = 0
	speed = 0
	next_step_tick = 0
	powered = False
	direction = 0
	stepping_multiplier = 1
	STEPS = 0
	SPEED = 1
	mode = STEPS
	"""Defines a motor"""
	def __init__(self, name, step_pin, dir_pin, ms1_pin, ms2_pin):
		self.name = name
		self.step_pin = step_pin
		self.dir_pin = dir_pin
		self.ms1_pin = ms1_pin
		self.ms2_pin = ms2_pin

	def rotate_degrees(self, degrees, speed):
		self.mode = self.STEPS
		self.powered = True
		#direction = (degrees>0)?GPIO.HIGH:GPIO.LOW
		self.set_direction(degrees)
		self.set_speed(speed)

		self.steps_left = int(abs(degrees)/(step_angle/micro_steps))

		self.next_step_tick = 0

	#def move_speed(self, direction, speed):
	def move_speed(self, speed):
		self.mode = self.SPEED
		self.powered = True
		self.set_direction(speed)
		self.set_stepping(speed)
		self.set_speed(speed)
		self.next_step_tick = 0

	def set_direction(self, direction):
		direction = 1 if (direction>0) else 0 if (direction==0) else -1
		self.direction = direction

		if direction != 0:
			dir_control = GPIO.HIGH if (direction==1) else GPIO.LOW
			GPIO.output(self.dir_pin, dir_control)
 
	def set_stepping(self, speed):
		speed = abs(speed)
		print "moving", speed
		if speed > 50:
			#full step
			self.stepping_multiplier = 1
			GPIO.output(self.ms1_pin, GPIO.HIGH)
			GPIO.output(self.ms2_pin, GPIO.HIGH)
		elif speed > 25:
			#1/2 step
			self.stepping_multiplier = 2
			GPIO.output(self.ms1_pin, GPIO.LOW)
			GPIO.output(self.ms2_pin, GPIO.HIGH)
		elif speed > 12:
			#1/4 step
			self.stepping_multiplier = 4
			GPIO.output(self.ms1_pin, GPIO.HIGH)
			GPIO.output(self.ms2_pin, GPIO.LOW)
		else:
			#1/8 step
			self.stepping_multiplier = 8
			GPIO.output(self.ms1_pin, GPIO.LOW)
			GPIO.output(self.ms2_pin, GPIO.LOW)
		print "Stepping: ",self.stepping_multiplier
	def set_speed(self, speed):
		"""-100 - 100"""
		self.set_direction(speed)
		speed = abs(speed)
		speed = 100 if (speed > 100) else 0 if (speed < 0) else speed
		self.speed = maximum_step_speed	- ( (speed / 100.0 ) * (maximum_step_speed - minimum_step_speed))
		self.speed = self.speed/self.stepping_multiplier
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
		
LEFT_MOTOR = 0
RIGHT_MOTOR = 1
motors = [
	Motor('Left Wheel', 7, 11, 22, 24), #name, step pin, dir_pin, MS1 pin, MS2 pin
	Motor('Right Wheel', 13, 15, 16, 18)
]
#motors[LEFT_MOTOR] = Motor('Left Wheel', 7, 11)#name, step pin, dir_pin
#motors[RIGHT_MOTOR] =  Motor('Right Wheel', 13, 15)
	

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
	GPIO.setup(motor.ms1_pin,GPIO.OUT)
	GPIO.setup(motor.ms2_pin,GPIO.OUT)

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
def isData():
        return select.select([sys.stdin], [], [], 0) == ([sys.stdin], [], [])

def control(command, args):
	print "command:",command," args:",args
	obuffer = 'I'
	if command == 'D':
		try:
			wheel = args[0]
			inP = args[1:].split(',')
			degrees = int(inP[0])
			speed = int(inP[1])
			#print "spinning ", wheel, " at speed ", speed, " for ", degrees, " degrees"

			if wheel == 'L':
				motors[LEFT_MOTOR].rotate_degrees(degrees, speed)
			elif wheel == 'R':
				motors[RIGHT_MOTOR].rotate_degrees(degrees, -speed)
			elif wheel == 'B':
				motors[LEFT_MOTOR].rotate_degrees(degrees, speed)
				motors[RIGHT_MOTOR].rotate_degrees(degrees, -speed)
			obuffer += str(wheel) + str(degrees) + ',' + str(speed)
		except:
			obuffer = "E! Unknown D Command: ",args
			
	elif command == 'G':
		obuffer += 'G'
                try:
                        wheel = args[0]
                        speed = int(args[1:])
                        #print "spinning ", wheel, " at speed ", speed

                        if wheel == 'L':
                                motors[LEFT_MOTOR].move_speed(speed)
                        elif wheel == 'R':
                                motors[RIGHT_MOTOR].move_speed(-speed)
                        elif wheel == 'B':
                                motors[LEFT_MOTOR].move_speed(speed)
                                motors[RIGHT_MOTOR].move_speed(-speed)
                        obuffer += str(wheel) + str(speed)
                except Exception as e:
                        obuffer = "E! Unknown G Command: ",args, e

	elif command == '+':
		obuffer += '+'
	elif command == '-':
		obuffer += '-'
	elif command == 'S':
		obuffer += 'S'
	elif command == 'F':
		obuffer += 'F'
	elif command == 'N':
		obuffer += 'G'
	else:
		obuffer = "E! Unknown Command: ",command,args

	return obuffer

def execute_command(command):
	if command.startswith("C"): #all commands start with C
		c = command[1]
		arguments = command[2:]
		obuffer = control(c, arguments)
		print command
	else:
		print "E! Unknown Control: ",ibuffer


ibuffer = '';
old_settings = termios.tcgetattr(sys.stdin)
server = Server('', 5555)
try:
	tty.setcbreak(sys.stdin.fileno())
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
		execute = False
		if isData():
			c = sys.stdin.read(1)
			if c== '\x1b': #escape
				break
			elif c== "\n":
				execute = True
			ibuffer += c
		if (execute) :
			execute_command(ibuffer)
#			if ibuffer.startswith("C"): #all commands start with C
#				command = ibuffer[1]
#				arguments = ibuffer[2:]
#				obuffer = control(command, arguments)
#				print obuffer
#			else:
#				print "E! Unknown Control: ",ibuffer
			#inP = ibuffer.split();
			#degrees = int(inP[0])
			#speed = int(inP[1])
			#print degrees, " - ", speed
			#motors[0].rotate_degrees(degrees, -speed)
			#motors[1].rotate_degrees(degrees,  speed)

			ibuffer = ''
		if (not motors_moving and False) :
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

		asyncore.poll()
finally:
	GPIO.output(reset_pin, GPIO.LOW)
	GPIO.cleanup()
	termios.tcsetattr(sys.stdin, termios.TCSADRAIN, old_settings)



