package com.dhc.openglbasic;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

public class BroadcastNotifier {
	private LocalBroadcastManager mBroadcaster;
	
	public BroadcastNotifier(Context context){
		mBroadcaster = LocalBroadcastManager.getInstance(context);
	}
	
	public void broadcastCommand(int command){
		Intent localIntent = new Intent();
		
		localIntent.setAction(Constants.COMMAND_ACTION);
		localIntent.putExtra(Constants.EXTENDED_DATA_COMMAND, command);
		localIntent.addCategory(Intent.CATEGORY_DEFAULT);
		
		mBroadcaster.sendBroadcast(localIntent);
	}

		public void broadcastIntentWithState(int status){
		Intent localIntent = new Intent();
		
		localIntent.setAction(Constants.BROADCAST_ACTION);
		
		localIntent.putExtra(Constants.EXTENDED_DATA_STATUS, status);
		localIntent.addCategory(Intent.CATEGORY_DEFAULT);
		
		mBroadcaster.sendBroadcast(localIntent);
	}
	
	public void notifyProgress(String logData){
		Intent localIntent = new Intent();
		
		localIntent.setAction(Constants.BROADCAST_ACTION);
		
		localIntent.putExtra(Constants.EXTENDED_DATA_STATUS, -1);
		
		localIntent.putExtra(Constants.EXTENDED_STATUS_LOG, logData);
		localIntent.addCategory(Intent.CATEGORY_DEFAULT);
		
		mBroadcaster.sendBroadcast(localIntent);
	}

}
