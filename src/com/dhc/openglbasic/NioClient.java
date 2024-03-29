package com.dhc.openglbasic;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.*;

//http://www.javafaq.nu/java-example-code-926.html

public class NioClient implements Runnable {
	// The host:port combination to connect to
	private InetAddress hostAddress;
	private int port;

	// The selector we'll be monitoring
	private Selector selector;

	// The buffer into which we'll read data when it's available
	private ByteBuffer readBuffer = ByteBuffer.allocate(8192);

	// A list of PendingChange instances
	private List pendingChanges = new LinkedList();

	// Maps a SocketChannel to a list of ByteBuffer instances
	private Map pendingData = new HashMap();

	// Maps a SocketChannel to a responseHandler
	private Map responseHandlers = Collections.synchronizedMap(new HashMap());

	public NioClient(InetAddress hostAddress, int port) throws IOException {
		this.hostAddress = hostAddress;
		this.port = port;
		this.selector = this.initSelector();
	}

	public void send(byte[] data, ResponseHandler handler) throws IOException {
		// Start a new connection
		SocketChannel socket = this.initiateConnection();

		// Register the response handler
		this.responseHandlers.put(socket, handler);

		// And queue the data we want written
		synchronized (this.pendingData) {
			List queue = (List) this.pendingData.get(socket);
			if (queue == null) {
				queue = new ArrayList();
				this.pendingData.put(socket, queue);
			}
			queue.add(ByteBuffer.wrap(data));
		}

		// Finally, wake up our selecting thread so it can make the required
		// changes
		this.selector.wakeup();
	}

	public void run() {
		while (true) {
			try {
				// Process any pending changes
				synchronized (this.pendingChanges) {
					Iterator changes = this.pendingChanges.iterator();
					while (changes.hasNext()) {
						ChangeRequest change = (ChangeRequest) changes.next();
						switch (change.type) {
						case ChangeRequest.CHANGEOPS:
							SelectionKey key = change.socket
									.keyFor(this.selector);
							key.interestOps(change.ops);
							break;
						case ChangeRequest.REGISTER:
							change.socket.register(this.selector, change.ops);
							break;
						}
					}
					this.pendingChanges.clear();
				}

				// Wait for an event one of the registered channels
				this.selector.select();

				// Iterate over the set of keys for which events are available
				Iterator selectedKeys = this.selector.selectedKeys().iterator();
				while (selectedKeys.hasNext()) {
					SelectionKey key = (SelectionKey) selectedKeys.next();
					selectedKeys.remove();

					if (!key.isValid()) {
						continue;
					}

					// Check what event is available and deal with it
					if (key.isConnectable()) {
						this.finishConnection(key);
					} else if (key.isReadable()) {
						this.read(key);
					} else if (key.isWritable()) {
						this.write(key);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void read(SelectionKey key) throws IOException {
		SocketChannel socketChannel = (SocketChannel) key.channel();

		// Clear out our read buffer so it's ready for new data
		this.readBuffer.clear();

		// Attempt to read off the channel
		int numRead;
		try {
			numRead = socketChannel.read(this.readBuffer);
		} catch (IOException e) {
			// The remote forcibly closed the connection, cancel
			// the selection key and close the channel.
			key.cancel();
			socketChannel.close();
			return;
		}

		if (numRead == -1) {
			// Remote entity shut the socket down cleanly. Do the
			// same from our end and cancel the channel.
			key.channel().close();
			key.cancel();
			return;
		}

		// Handle the response
		this.handleResponse(socketChannel, this.readBuffer.array(), numRead);
	}

	private void handleResponse(SocketChannel socketChannel, byte[] data,
			int numRead) throws IOException {
		// Make a correctly sized copy of the data before handing it
		// to the client
		byte[] responseData = new byte[numRead];
		System.arraycopy(data, 0, responseData, 0, numRead);

		// Look up the handler for this channel
		ResponseHandler handler = (ResponseHandler) this.responseHandlers
				.get(socketChannel);

		// And pass the response to it
		if (handler.handleResponse(responseData)) {
			// The handler has seen enough, close the connection
//			socketChannel.close();
//			socketChannel.keyFor(this.selector).cancel();
		}
	}

	private void write(SelectionKey key) throws IOException {
		SocketChannel socketChannel = (SocketChannel) key.channel();

		synchronized (this.pendingData) {
			List queue = (List) this.pendingData.get(socketChannel);

			// Write until there's not more data ...
			while (!queue.isEmpty()) {
				ByteBuffer buf = (ByteBuffer) queue.get(0);
				socketChannel.write(buf);
				if (buf.remaining() > 0) {
					// ... or the socket's buffer fills up
					break;
				}
				queue.remove(0);
			}

			if (queue.isEmpty()) {
				// We wrote away all data, so we're no longer interested
				// in writing on this socket. Switch back to waiting for
				// data.
				key.interestOps(SelectionKey.OP_READ);
			}
		}
	}

	private void finishConnection(SelectionKey key) throws IOException {
		SocketChannel socketChannel = (SocketChannel) key.channel();

		// Finish the connection. If the connection operation failed
		// this will raise an IOException.
		try {
			socketChannel.finishConnect();
		} catch (IOException e) {
			// Cancel the channel's registration with our selector
			System.out.println(e);
			key.cancel();
			return;
		}

		// Register an interest in writing on this channel
		key.interestOps(SelectionKey.OP_WRITE);
	}

	private static SocketChannel socketChannel;
	
	private SocketChannel initiateConnection() throws IOException {
		//check to see if we have a connection already.  if so, return it.
		if(socketChannel != null && socketChannel.isConnected())
			return socketChannel;
		
		// Create a non-blocking socket channel
		socketChannel = SocketChannel.open();
		socketChannel.configureBlocking(false);

		// Kick off connection establishment
		socketChannel
				.connect(new InetSocketAddress(this.hostAddress, this.port));

		// Queue a channel registration since the caller is not the
		// selecting thread. As part of the registration we'll register
		// an interest in connection events. These are raised when a channel
		// is ready to complete connection establishment.
		synchronized (this.pendingChanges) {
			this.pendingChanges.add(new ChangeRequest(socketChannel,
					ChangeRequest.REGISTER, SelectionKey.OP_CONNECT));
		}

		return socketChannel;
	}

	private Selector initSelector() throws IOException {
		// Create a new selector
		return SelectorProvider.provider().openSelector();
	}

	public static void main(String[] args) {
		try {
			String SERVER_ADDRESS = "207.178.144.76"; // fandisti.nmbtc.com
			int SERVER_PORT = 5555;
			NioClient client = new NioClient(
					InetAddress.getByName(SERVER_ADDRESS), SERVER_PORT);
			Thread t = new Thread(client);
			t.setDaemon(true);
			t.start();
			ResponseHandler handler = new ResponseHandler();
			client.send("Hello World".getBytes(), handler);
			handler.waitForResponse();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

/*
 * import java.io.BufferedReader; import java.io.IOException; import
 * java.io.InputStreamReader; import java.io.PrintWriter; import
 * java.net.Socket; import java.net.UnknownHostException;
 * 
 * //http://fw-geekycoder.blogspot.com/2011/05/creating-non-blocking-client-and-
 * server.html public class ControlClient { private Socket commandSocket = null;
 * private PrintWriter out = null; private BufferedReader in = null;
 * 
 * private int RETRY_DELAY = 5;// seconds public int DEFAULT_PORT = 8778;
 * 
 * ControlClient(String hostname) throws UnknownHostException, IOException{
 * open(hostname); }
 * 
 * public void open(String hostname) throws UnknownHostException, IOException{
 * try{ commandSocket = new Socket(hostname, DEFAULT_PORT); out = new
 * PrintWriter(commandSocket.getOutputStream(), true); in = new
 * BufferedReader(new InputStreamReader(commandSocket.getInputStream())); }
 * catch (UnknownHostException e){ System.err.println("Cannot find host: " +
 * hostname); } catch (IOException e){
 * System.err.println("Couldn't get I/O for connection to " + hostname); } }
 * public void close() throws IOException{ out.close(); in.close();
 * commandSocket.close(); }
 * 
 * public void write(String command){ out.println(command); }
 * 
 * // public String[] read(){ //retu // }
 * 
 * }
 */