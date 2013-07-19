package com.dhc.openglbasic;

public final class Constants{
	public static final String BROADCAST_ACTION = "com.dhc.openglbasic.BROADCAST";
	public static final String COMMAND_ACTION = "com.dhc.openglbasic.COMMAND";
	
	public static final int STATE_ACTION_STARTED = 		0;
	public static final int STATE_ACTION_CONNECTING = 	1;
	public static final int STATE_ACTION_TX = 			2;
	public static final int STATE_ACTION_RX = 			3;
	public static final int STATE_ACTION_CLOSING = 		5;
	public static final int STATE_ACTION_COMPLETE = 	4;
	public static final int STATE_ACTION_CONNECTED = 	6;

	public static final int COMMAND_DEGREES = 	0;
	public static final int COMMAND_GO = 		1;
	public static final int COMMAND_DISABLE = 	2;
	public static final int COMMAND_ENABLE = 	3;
	public static final int COMMAND_FUNCTION = 	4;
	public static final int COMMAND_SENSOR = 	5;
	public static final int COMMAND_CONNECT = 	6;
	
	public static final String EXTENDED_DATA_COMMAND = "com.dhc.openglbasic.DATA_COMMAND";
	public static final String EXTENDED_DATA_STATUS = "com.dhc.openglbasic.STATUS";
	public static final String EXTENDED_STATUS_LOG = "com.dhc.openglbasic.LOG";
	public static final String EXTENDED_DATA_DEGREES = "com.dhc.openglbasic.DEGREES";
	public static final String EXTENDED_DATA_SPEED = "com.dhc.openglbasic.SPEED";
	public static final String EXTENDED_DATA_LEFT_POWER = "com.dhc.openglbasic.LEFT";
	public static final String EXTENDED_DATA_RIGHT_POWER = "com.dhc.openglbasic.RIGHT";
}
