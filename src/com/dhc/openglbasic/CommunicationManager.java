package com.dhc.openglbasic;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Arrays;

import android.util.Log;

public class CommunicationManager {
	public static final String TAG = "ConnectionManager";
	private static CommunicationManager networkManager;
	private static SocketChannel connection;
	
	private static ArrayList<String> pendingSends = new ArrayList<String>();
	
	public synchronized static CommunicationManager getInstance(){
		if(networkManager == null){
			networkManager = new CommunicationManager();
		}
		
		return networkManager;
	}
	
	private CommunicationManager(){
	}
	
	private SocketChannel getConnection(String hostname, int port) throws IOException{
		Log.e(TAG, "Opening connection " + hostname + ":" + port);

		SocketChannel sc = SocketChannel.open();
		sc.configureBlocking(false);
		sc.connect(new InetSocketAddress(hostname, port));
		
		while(!sc.finishConnect()){
		}

		return sc;
	}

	public ArrayList sendReceive() throws IOException{
		if(!isConnected())
			return null;

		sendMessages();
		return readMessages();
	}
	
	private static final String delimiter = "\n";
	private void sendMessages() throws IOException{
		if(!isConnected() || pendingSends.size()==0)
			return;
		
		StringBuffer sb = new StringBuffer();
		for(String send: pendingSends){
			sb.append(send)
				.append(delimiter);
		}
		Log.e(TAG, "Sending: " + sb);
		pendingSends.clear();
		
		ByteBuffer bb = ByteBuffer.allocate(sb.length()*2);
		bb.clear();
		bb.put(sb.toString().getBytes());
		
		bb.flip();
		
		while(bb.hasRemaining())
			connection.write(bb);
		
	}

	private ArrayList<String> readMessages() throws IOException{
		if(!isConnected())
			return null;

		ArrayList<String> reads = null;
		
		ByteBuffer bb = ByteBuffer.allocate(48);
		StringBuffer sb = null;

		if(connection.read(bb) > 0){
			if(sb == null)
				sb = new StringBuffer();
			String read = new String(bb.array());

			sb.append(read);
			Log.e(TAG, "Reading: " + read);
		}
		if(sb != null && sb.length() > 0)
			reads = new ArrayList<String>(Arrays.asList(sb.toString().split("\n")));

//		if(reads != null && reads.size()>0){
//			Log.e(TAG,"Responses:");
//			for(String r: reads)
//				Log.e(TAG,"\t" + r);
//		}

		return reads;
	}
	
	public void queueSend(String send){
		if(isConnected())
			pendingSends.add(send);
	}
	
	public void connect(String hostname, int port) throws IOException{
		connection = getConnection(hostname, port);
	}
	public void disconnect() throws IOException{
		if(isConnected()){
			connection.close();
		}
	}
	
	public boolean isConnected(){
		return connection!=null && connection.isConnected();
	}
}
