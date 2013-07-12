package com.dhc.openglbasic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

//http://fw-geekycoder.blogspot.com/2011/05/creating-non-blocking-client-and-server.html
public class ControlClient {
	private Socket commandSocket = null;
	private PrintWriter out = null;
	private BufferedReader in = null;
	
	private int RETRY_DELAY = 5;// seconds 
	public int DEFAULT_PORT = 8778;
	
	ControlClient(String hostname) throws UnknownHostException, IOException{
		open(hostname);
	}
	
	public void open(String hostname) throws UnknownHostException, IOException{
		try{
			commandSocket = new Socket(hostname, DEFAULT_PORT);
			out = new PrintWriter(commandSocket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(commandSocket.getInputStream()));
		} catch (UnknownHostException e){
			System.err.println("Cannot find host: " + hostname);
		} catch (IOException e){
			System.err.println("Couldn't get I/O for connection to " + hostname);
		}
	}
	public void close() throws IOException{
		out.close();
		in.close();
		commandSocket.close();
	}
	
	public void write(String command){
		out.println(command);
	}
	
	public String[] read(){
	}

}
