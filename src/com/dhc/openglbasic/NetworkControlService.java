package com.dhc.openglbasic;

import java.net.InetAddress;
import java.util.Set;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class NetworkControlService extends IntentService {
	public static final String TAG = "NetworkControlService";

	private BroadcastNotifier mBroadcaster = new BroadcastNotifier(this);
	private NioClient client;
	private ResponseHandler handler;
	public static boolean connected = true;

	public NetworkControlService() {
		super(TAG);
	}
	
	private static final int ERROR_INT = -200;

	@Override
	protected void onHandleIntent(Intent intent) {
		// get data from incoming intent
		// String dataString = workIntent.getDataString();
		
		Log.d(TAG, "Connected?: " + connected);
		try {
			String SERVER_ADDRESS = "207.178.144.76"; // fandisti.nmbtc.com
			int SERVER_PORT = 5555;
			NioClient client = new NioClient(InetAddress.getByName(SERVER_ADDRESS), SERVER_PORT);
			Thread t = new Thread(client);
			t.setDaemon(true);
			t.start();
			ResponseHandler handler = new ResponseHandler();
			 
			Bundle b = intent.getExtras();
			if(b != null){
				Log.d(TAG, "Extras:");
				Set<String> keys = b.keySet();
				for (String key: keys){
					Object o = b.get(key);
					Log.d(TAG, "\t" + key + ": " + o);
				}
			}

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
						if(connected)
							client.send(("CGL" + left).getBytes(), handler);
					}
					int right = intent.getIntExtra(Constants.EXTENDED_DATA_RIGHT_POWER, ERROR_INT);
					if(right != ERROR_INT){
						Log.d(TAG, "State: Go.right: " + right);
						if(connected)
							client.send(("CGR" + right).getBytes(), handler);
					}
					break;
				case Constants.COMMAND_DISABLE:
					Log.d(TAG, "State: Disable");
					if(connected)
						client.send("C-".getBytes(), handler);
	
					break;
				case Constants.COMMAND_ENABLE:
					Log.d(TAG, "State: Enable");
					if(connected)
						client.send("C+".getBytes(), handler);
	
					break;
				case Constants.COMMAND_FUNCTION:
					Log.d(TAG, "State: Function");
					if(connected)
						client.send("S1".getBytes(), handler);
	
					break;
				case Constants.COMMAND_SENSOR:
					Log.d(TAG, "State: Sensor");
					break;
				case Constants.COMMAND_CONNECT:
					Log.d(TAG, "State: Connect");
					
					connected = true;
					Intent localIntent = new Intent(Constants.BROADCAST_ACTION).putExtra(Constants.EXTENDED_DATA_STATUS, Constants.STATE_ACTION_CONNECTED);
				    // Broadcasts the Intent to receivers in this app.
				    LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
	
					break;
				default:
					break;
			}
			if(handler!=null)
				handler.waitForResponse();
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		// do work here based on contents of datastring
		// /...

		// mBroadcaster.broadcastIntentWithState(Constants.STATE_ACTION_CONNECTING);
		// mBroadcaster.broadcastIntentWithState(Constants.STATE_ACTION_TX);
		// mBroadcaster.broadcastIntentWithState(Constants.STATE_ACTION_RX);
		// mBroadcaster.broadcastIntentWithState(Constants.STATE_ACTION_COMPLETE);
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
 * Log.d(TAG, "State: Recieving"); break; case Constants.STATE_ACTION_CLOSING:
 * Log.d(TAG, "State: Closing"); break; case Constants.STATE_ACTION_COMPLETE:
 * Log.d(TAG, "State: Complete"); break; default: break; } } }
 */