#!/usr/bin/python

import asyncore
import asynchat
import socket

class EchoHandler(asyncore.dispatcher_with_send):
	def handle_read(self):
		data = self.recv(8192)
		if data:
			self.send(data)
		print data

class RoboHandler(asynchat.async_chat):
	def __init__(self, sock, addr):
		asynchat.async_chat.__init__(self, sock)

		self.addr = addr
		self.ibuffer = []
		self.obuffer = ""
		self.set_terminator("\r\n")

	def collect_incoming_data(self, data):
		"""buffer the data"""
		self.ibuffer.append(data)

	def found_terminator(self):
		print self.ibuffer[0]
		self.ibuffer = []

	def handle_close(self):
		print 'Disconnecting connection from %s' % repr(self.addr)
		self.close()


class Server(asyncore.dispatcher):
	def __init__(self, host, port):
		asyncore.dispatcher.__init__(self)
		self.create_socket(socket.AF_INET, socket.SOCK_STREAM)
		self.set_reuse_addr()
		self.bind((host, port))
		self.listen(5)

	def handle_accept(self):
		pair = self.accept()
		if pair is not None:
			sock, addr = pair
			print 'Incoming connection from %s' % repr(addr)
			handler = RoboHandler(sock, addr)

#server = Server('localhost', 8080)
server = Server('r2d2', 8080)
asyncore.loop()
