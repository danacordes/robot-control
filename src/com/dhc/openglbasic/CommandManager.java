package com.dhc.openglbasic;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Set;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class CommandManager extends IntentService {
	public static final String TAG = "CommandManager";

	private BroadcastNotifier mBroadcaster = new BroadcastNotifier(this);

	public CommandManager() {
		super(TAG);
	}
	
	private static final int ERROR_INT = -200;
	

	@Override
	protected void onHandleIntent(Intent intent) {
		// get data from incoming intent
		// String dataString = workIntent.getDataString();
		
		try {
			
//			if(connection == null || !connection.isConnected())
//				connection = ;
//				connection = getConnection(SERVER_ADDRESS, SERVER_PORT);
//			NioClient client = new NioClient(InetAddress.getByName(SERVER_ADDRESS), SERVER_PORT);
//			Thread t = new Thread(client);
//			t.setDaemon(true);
//			t.start();
//			ResponseHandler handler = new ResponseHandler();

/*			
			Bundle b = intent.getExtras();
			if(b != null){
				Log.d(TAG, "Extras:");
				Set<String> keys = b.keySet();
				for (String key: keys){
					Object o = b.get(key);
					Log.d(TAG, "\t" + key + ": " + o);
				}
			}
*/
			switch (intent.getIntExtra(Constants.EXTENDED_DATA_COMMAND,
					Constants.COMMAND_DISABLE)) {
				case Constants.COMMAND_DEGREES:
					Log.d(TAG, "State: Degrees");
	
					break;
				case Constants.COMMAND_GO:
					Log.d(TAG, "State: Go");
					int left = intent.getIntExtra(Constants.EXTENDED_DATA_LEFT_POWER, ERROR_INT);
					if(left != ERROR_INT){
						Log.d(TAG, "State: Go.left: " + left);
						queueSend("CGL" + left);
					}
					int right = intent.getIntExtra(Constants.EXTENDED_DATA_RIGHT_POWER, ERROR_INT);
					if(right != ERROR_INT){
						Log.d(TAG, "State: Go.right: " + right);
						queueSend("CGR" + right);
					}
					break;
				case Constants.COMMAND_DISABLE:
					Log.d(TAG, "State: Disable");
					queueSend("C-");
	
					break;
				case Constants.COMMAND_ENABLE:
					Log.d(TAG, "State: Enable");
					queueSend("C+");
	
					break;
				case Constants.COMMAND_FUNCTION:
					Log.d(TAG, "State: Function");
					int index  = intent.getIntExtra(Constants.EXTENDED_DATA_INDEX, ERROR_INT);

					queueSend("S" + Integer.toString(index));
	
					break;
				case Constants.COMMAND_SENSOR:
					Log.d(TAG, "State: Sensor");
					break;
				case Constants.COMMAND_CONNECT:
					Log.d(TAG, "State: Connect");
					
					String SERVER_ADDRESS = "207.178.144.76"; // fandisti.nmbtc.com
					int SERVER_PORT = 5555;
					if(!CommunicationManager.getInstance().isConnected()){
						CommunicationManager.getInstance().connect(SERVER_ADDRESS, SERVER_PORT);
						Intent localIntent = new Intent(Constants.BROADCAST_ACTION).putExtra(Constants.EXTENDED_DATA_STATUS, Constants.STATE_ACTION_CONNECTED);
					    LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
					} else {
						CommunicationManager.getInstance().disconnect();

						Intent localIntent = new Intent(Constants.BROADCAST_ACTION).putExtra(Constants.EXTENDED_DATA_STATUS, Constants.STATE_ACTION_DISCONNECTED);
					    LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
					}
					
	
					break;
				default:
					break;
			}
			
			handleResponses(CommunicationManager.getInstance().sendReceive());
//			Intent localIntent = new Intent(Constants.BROADCAST_ACTION);
//			localIntent.putExtra(Constants.EXTENDED_DATA_STATUS, Constants.STATE_ACTION_COMPLETE);
//		    LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
			//if(handler!=null)
				//handler.waitForResponse();
			//sendMessages();
			//readMessages();
			
//				if(responses != null){
//				Log.e(TAG,"Responses:");
//				for(String r: responses)
//					Log.e(TAG,"\t" + r);
//			}

		} catch (Exception e1) {
			e1.printStackTrace();
		}

		// do work here based on contents of datastring
		// /...

		// mBroadcaster.broadcastIntentWithState(Constants.STATE_ACTION_TX);
		// mBroadcaster.broadcastIntentWithState(Constants.STATE_ACTION_RX);
		// mBroadcaster.broadcastIntentWithState(Constants.STATE_ACTION_COMPLETE);
	}
	

	private void queueSend(String message) throws IOException{
		CommunicationManager.getInstance().queueSend(message);
	}
	

	private void handleResponses(ArrayList<String> responses){
		if(responses == null || responses.size()==0)
			return;
		
		Log.e(TAG,"Responses:");
		for(String r: responses)
			Log.e(TAG,"\t-" + r);
		
		for(String response: responses)
			switch(response.charAt(0)){
				case 'S':
					Intent localIntent = new Intent(Constants.BROADCAST_ACTION).putExtra(Constants.EXTENDED_DATA_STATUS, Constants.STATE_ACTION_FUNCTION_COMPLETE);
			    	LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
					
					break;
				default:
					break;
			}
		
	}		
}

/*
 * private class StatusReceiver extends BroadcastReceiver{ private
 * StatusReceiver(){ }
 * 
 * public void onReceive(Context context, Intent intent){ //handle intents
 * switch(intent.getIntExtra(Constants.EXTENDED_DATA_STATUS,
 * Constants.STATE_ACTION_COMPLETE)){ case Constants.STATE_ACTION_STARTED:
 * Log.d(TAG, "State: Started"); break; case Constants.STATE_ACTION_CONNECTING:
 * Log.d(TAG, "State: Connecting"); break; case Constants.STATE_ACTION_TX:
 * Log.d(TAG, "State: Transmitting"); break; case Constants.STATE_ACTION_RX:
 * Log.d(TAG, "State: Receiving"); break; case Constants.STATE_ACTION_CLOSING:
 * Log.d(TAG, "State: Closing"); break; case Constants.STATE_ACTION_COMPLETE:
 * Log.d(TAG, "State: Complete"); break; default: break; } } }
 */