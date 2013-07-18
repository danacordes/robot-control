package com.dhc.openglbasic;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.text.NumberFormat;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.WindowManager;

public class OpenGLES20Basic extends Activity implements SensorEventListener {

	private GLSurfaceView myGLView;
	private static Context context;
	private SensorManager mSensorManager;
	Sensor accelerometer;
	Sensor magnetometer;
	
	private static final String TAG = "OpenGLES20Basic";
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		OpenGLES20Basic.context = getApplicationContext();
		//setContentView(R.layout.activity_open_gles20_basic);
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		//create a GLSurfaceView instance and set it as the ContentVuew for this Activity
		myGLView = new MyGLSurfaceView(this);
		setContentView(myGLView);
		
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
	}
	
	public static Context getContext() {
		return OpenGLES20Basic.context;
	}	
	protected void onResume(){
		super.onResume();
		mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
		mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
	}
	
	protected void onPause(){
		super.onPause();
		mSensorManager.unregisterListener(this);
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.open_gles20_basic, menu);
		return true;
	}
	
	public void onAccuracyChanged(Sensor sensor, int accuracy){}
	
	float[] mGravity;
	float[] mGeomagnetic;
	public void onSensorChanged(SensorEvent event){
		if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
			mGravity = event.values;
		if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
			mGeomagnetic = event.values;
		
		if(mGravity != null && mGeomagnetic != null){
			float R[] = new float[9];
			float I[] = new float[9];
			boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
			if(success){
				float orientation[] = new float[3];
				SensorManager.getOrientation(R, orientation);
				//azimuth = orientation[0];  //orientation contains azimuth, pitch and roll or z, x, y
				//want to use z for accel and turning
				((MyGLSurfaceView)myGLView).updateMovement(orientation);
				((MyGLSurfaceView)myGLView).updateNetwork();
//				((MyGLSurfaceView)myGLView).updateMovement(R);
			}
		}
		
	}
	
	class MyGLSurfaceView extends GLSurfaceView{
		
		private final MyGL20Renderer mRenderer;
	
		public MyGLSurfaceView(Context context){
			super(context);
			
			//Create an OpenGL ES 2.0 context
			setEGLContextClientVersion(2);
			
			//set the Renderer for drawing on the GLSurfaceView
			mRenderer = new MyGL20Renderer();
			setRenderer(mRenderer);
			
			//render the view only when there is a change in the drawing data
			setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
		}

		ByteBuffer in;
		ByteBuffer out;
		public void updateNetwork(){
			if(socketChannel==null)
				//bail if we're not connected
				return;
			
			if(out==null)
				out = ByteBuffer.allocate(48);
			if(in==null)
				 in = ByteBuffer.allocate(48);

			//reading
			int bytesRead = -1;
			try {
				bytesRead = socketChannel.read(in);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.e(TAG,e.toString());
			}
			if(bytesRead==-1){
				//handle disconnect
			}
			
			//writing
//			String newData = "New String to write to file..." + System.currentTimeMillis();

			/*
				out.clear();
				out.put(newData.getBytes());
				out.flip();
			*/

			if(out.hasRemaining()){
			    try {
					socketChannel.write(out);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Log.e(TAG,e.toString());
				}
			}
			/*
			while(out.hasRemaining()) {
			    try {
					socketChannel.write(out);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Log.e(TAG,e1.toString());
				}
			}
			*/			
		}

		private float[] offsets = {0f, 0f, 0f};
		private boolean takeOffsetsFlag = true;
		public void setOffsets(){
			takeOffsetsFlag = true;
		}
		private void setOffsets(float[] initialOrientation){
			for(int i = 0; i<3; i++){
				offsets[i] = initialOrientation[i];
				System.out.println("Offset[" + i + "] " + offsets[i]);
			}
		}
		
		
		private int SMOOTHING = 10;
		private float REVERSE_TIPPING_POINT = 0.5f;
		private int REVERSE_ACCELERATION = -1;//set to 1 to have forward tilt go forward
		private float ACCELERATE_SENSITIVITY = 0.5f;
		private float ACCELERATION_DEADZONE = 0.1f; //percentage
		private float TURN_SENSIVITITY = 0.7f;
		private float TURN_DEADZONE = 0.07f; //percentage

		private ArrayList<Float> mTurnSmoothing = new ArrayList<Float>();
		private ArrayList<Float> mAccelerateSmoothing = new ArrayList<Float>();
		private float mTurnPercentage;
		private float mAcceleratePercentage;
		NumberFormat nf = NumberFormat.getPercentInstance();
		
		public void updateMovement(float[] orientation){
			if(takeOffsetsFlag){
				takeOffsetsFlag = false;
				setOffsets(orientation);
				//enable for instant stop.
				//mAccelerateSmoothing.clear();
				//mTurnSmoothing.clear();
			}
			if(mTurnSmoothing.size() >= SMOOTHING)
				mTurnSmoothing.remove(0);
			mTurnSmoothing.add(new Float(orientation[1] - offsets[1]));
			
			float tmp = 0.0f;
			for( Float f : mTurnSmoothing)
				tmp += f.floatValue();
			
			mTurnPercentage = (float) ((tmp / mTurnSmoothing.size()) / TURN_SENSIVITITY);
			//turn dead zone
			if(Math.abs(mTurnPercentage) < TURN_DEADZONE)
				mTurnPercentage = 0;
			else
				mTurnPercentage =  (mTurnPercentage - (mTurnPercentage>0?TURN_DEADZONE:-TURN_DEADZONE));

			if  ( mTurnPercentage > 1 )
				mTurnPercentage = 1.0f;
			else if  ( mTurnPercentage < -1 )
				mTurnPercentage = -1.0f;

			tmp = 0.0f;
			
			if(mAccelerateSmoothing.size() >= SMOOTHING)
				mAccelerateSmoothing.remove(0);
			//if we're in a stopping state, only add zeros
			if(buttonStates[MyGL20Renderer.BUTTON_STOP])
				mAccelerateSmoothing.add(new Float(0f));
			else
				mAccelerateSmoothing.add(new Float(orientation[2] - offsets[2]));

			for( Float f : mAccelerateSmoothing)
				tmp += f.floatValue();

			//take the average of the stored sensor values
			mAcceleratePercentage = (float) (tmp / mAccelerateSmoothing.size()); 
			//make it more/less sensitive
			mAcceleratePercentage = mAcceleratePercentage / ACCELERATE_SENSITIVITY; 
			//reverse the controls, if set (-1  or 1)
			mAcceleratePercentage = mAcceleratePercentage * REVERSE_ACCELERATION;
			
			//implement our deadzone
			if(Math.abs(mAcceleratePercentage) < ACCELERATION_DEADZONE)
				mAcceleratePercentage = 0;
			else
				mAcceleratePercentage =  (mAcceleratePercentage - (mAcceleratePercentage>0?ACCELERATION_DEADZONE:-ACCELERATION_DEADZONE));
//			Log.e(TAG, "DeadZone: " + DEAD_ZONE + "Actual: " + tmp + " Math.abs(mAcceleratePercentage):" + Math.abs(mAcceleratePercentage));
			
			//cap it out at 1.0 and bottom it out at -1
			if  ( mAcceleratePercentage > 1 )
				mAcceleratePercentage = 1.0f;
			else if  ( mAcceleratePercentage < -1 )
				mAcceleratePercentage = -1.0f;
			
			mRenderer.mAngle = -(mTurnPercentage * 180);
//			mRenderer.mAngle = -90;
			
			float leftPowerBalance = 0.0f, rightPowerBalance = 0.0f;
			
			if(mTurnPercentage <= 0)
				leftPowerBalance = 1.0F;
			else if(mTurnPercentage <= REVERSE_TIPPING_POINT)
				leftPowerBalance = 1 - (mTurnPercentage / REVERSE_TIPPING_POINT);
			else
				leftPowerBalance = -((mTurnPercentage-REVERSE_TIPPING_POINT) / REVERSE_TIPPING_POINT);
			mRenderer.leftPowerBalance = leftPowerBalance;
			mRenderer.leftPower = leftPowerBalance * mAcceleratePercentage;
			
			if(mTurnPercentage >= 0)
				rightPowerBalance = 1.0f;
			else if(mTurnPercentage >= -REVERSE_TIPPING_POINT)
				rightPowerBalance = 1 + (mTurnPercentage / REVERSE_TIPPING_POINT);
			else
				rightPowerBalance = ((mTurnPercentage+REVERSE_TIPPING_POINT) / REVERSE_TIPPING_POINT);
			mRenderer.rightPowerBalance = rightPowerBalance;
			mRenderer.rightPower = rightPowerBalance * mAcceleratePercentage;
			

//			System.out.println("turn=" + nf.format(mTurnPercentage) + "\t" + "  acceleration=" + nf.format(mAcceleratePercentage));
//			System.out.println(nf.format(leftPowerBalance) + ", " + nf.format(rightPowerBalance));
//			System.out.println(nf.format(mRenderer.leftPower) + ", " + nf.format(mRenderer.rightPower));
//			System.out.println(mRenderer.mAngle);
//			Log.e(TAG, "mAcceleratePercentage: " + mAcceleratePercentage);
//			Log.e(TAG, "mTurnPercentage: " + mTurnPercentage);
			requestRender();
		}
		
		private float mPreviousX;
		private float mPreviousY;
		private final float TOUCH_SCALE_FACTOR = 180.0f / 1024;
		
		private boolean[] buttonStates = {false, false, false, false};

		private SocketChannel socketChannel;
		public String SERVER_ADDRESS = "207.178.144.76"; //fandisti.nmbtc.com
		public int SERVER_PORT = 5555;

		@Override
		public boolean onTouchEvent(MotionEvent e){
			//MotionEvent reports input details from he touch screen
			//an other input controls.  In this case, you are only
			//interesting in events where the touch position changed.
			
			float x = e.getX();
			float y = e.getY();
			
			switch (e.getAction()) {
				case MotionEvent.ACTION_MOVE:
					float dx = x - mPreviousX;
					float dy = y - mPreviousY;
					
					//reverse direction of rotation above the mid-line
					if (y > getHeight() / 2)
						dx *= -1;
					
					//reverse direction of rotation to the left if the midline
					if (x < getWidth() / 2)
						dy *= -1;
					
//					mRenderer.mAngle += (dx + dy) * TOUCH_SCALE_FACTOR;  // = 180.0f / 320
//					requestRender();
					break;
				case MotionEvent.ACTION_UP:
					DisplayMetrics metrics = new DisplayMetrics();
					getWindowManager().getDefaultDisplay().getMetrics(metrics);
					int width = metrics.widthPixels;
					int height = metrics.heightPixels;
					
					
					float xP = x/width;
					float yP = y/height; 
					//Log.e(TAG, (xP) + ", " + (yP));
					
					for( Button b : mRenderer.getButtons()){
						float[] range = b.getTouchDetectionRange();
						if(
								xP > range[0] && xP < range[2] &&
								yP > range[1] && yP < range[3]
						)	
							switch (b.getId()) {
								case MyGL20Renderer.BUTTON_POWER:
									buttonStates[MyGL20Renderer.BUTTON_POWER] = ! buttonStates[MyGL20Renderer.BUTTON_POWER];
									b.setColor(buttonStates[MyGL20Renderer.BUTTON_POWER]?MyGL20Renderer.fgSquareColor:MyGL20Renderer.lightBlueColor);
									
									mRenderer.getInfo(MyGL20Renderer.INFO_ENABLED).updateMessage(buttonStates[MyGL20Renderer.BUTTON_POWER]?"Disabled":"Enabled");
									//break; //a power off will always trigger a stop.
								case MyGL20Renderer.BUTTON_STOP:
									buttonStates[MyGL20Renderer.BUTTON_STOP] = ! buttonStates[MyGL20Renderer.BUTTON_STOP];
									mRenderer.getButton(MyGL20Renderer.BUTTON_STOP).setColor(buttonStates[MyGL20Renderer.BUTTON_STOP]?MyGL20Renderer.fgSquareColor:MyGL20Renderer.lightBlueColor);
									if(! buttonStates[MyGL20Renderer.BUTTON_STOP]);
											setOffsets();
									break;
								case MyGL20Renderer.BUTTON_SPEAK:
									buttonStates[MyGL20Renderer.BUTTON_SPEAK] = ! buttonStates[MyGL20Renderer.BUTTON_SPEAK];
									b.setColor(buttonStates[MyGL20Renderer.BUTTON_SPEAK]?MyGL20Renderer.fgSquareColor:MyGL20Renderer.lightBlueColor);
									
									out.clear();
									out.put(("S" + System.currentTimeMillis()).getBytes());
									out.flip();
									

									break;
								case MyGL20Renderer.BUTTON_CONNECT:
									buttonStates[MyGL20Renderer.BUTTON_CONNECT] = ! buttonStates[MyGL20Renderer.BUTTON_CONNECT];

									//http://tutorials.jenkov.com/java-nio/socket-channel.html
									if(!buttonStates[MyGL20Renderer.BUTTON_CONNECT]){
										//already connected, so close it
										try {
											socketChannel.close();
											buttonStates[MyGL20Renderer.BUTTON_CONNECT] = false;
										} catch (IOException e1) {
											// TODO Auto-generated catch block
											e1.printStackTrace();
											Log.e(TAG,e1.toString());
										}  
									} else {
										//else connect
									
										try {
											//http://stackoverflow.com/questions/6976317/android-http-connection-exception
											socketChannel = SocketChannel.open();
											socketChannel.configureBlocking(false);
											socketChannel.connect(new InetSocketAddress(SERVER_ADDRESS, SERVER_PORT));
											
											while(! socketChannel.finishConnect() ){
											    //wait, or do something else...   
												b.setColor(MyGL20Renderer.whiteColor);
											}
											buttonStates[MyGL20Renderer.BUTTON_CONNECT] = true;
										} catch (IOException e1) {
											// TODO Auto-generated catch block
											e1.printStackTrace();
											Log.e(TAG,e1.toString());
										}
									}

									b.setColor(buttonStates[MyGL20Renderer.BUTTON_CONNECT]?MyGL20Renderer.fgSquareColor:MyGL20Renderer.lightBlueColor);
									
									break;
							}
					}

					
			}
			
			mPreviousX = x;
			mPreviousY = y;
//			Log.e(TAG, x + ", " + y);
			
			return true;
		}
	}
	

}
