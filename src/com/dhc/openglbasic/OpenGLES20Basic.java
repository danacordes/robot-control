package com.dhc.openglbasic;

import java.text.NumberFormat;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.Menu;
import android.view.MotionEvent;

public class OpenGLES20Basic extends Activity implements SensorEventListener {

	private GLSurfaceView myGLView;
	private static Context context;
	private SensorManager mSensorManager;
	Sensor accelerometer;
	Sensor magnetometer;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		OpenGLES20Basic.context = getApplicationContext();
		//setContentView(R.layout.activity_open_gles20_basic);
		
		//create a GLSurfaceView instance and set it as the ContentVuew for this Activity
		myGLView = new MyGLSurfaceView(this);
		setContentView(myGLView);
		
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

/*		
		SensorManager sensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
		
		final float[] mValuesAccel = new float[3];
		final float[] mRotationMatrix = new float[9];
		
		final SensorEventListener mEventListener = new SensorEventListener(){
			public void onAccuracyChanged(Sensor sensor, int accuracy){
			}
			
			public void onSensorChanged(SensorEvent event){
				//handle the events for which we are reg'd
				switch(event.sensor.getType()){
					case Sensor.TYPE_ACCELEROMETER:
						System.arraycopy(event.values,  0,  mValuesAccel,  0,  3);
						break;
				}
			}
		};
		
		//register it with the manager
		setListeners(sensorManager, mEventListener);
*/	
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
/*	
	@SuppressWarnings("deprecation")
	public void setListeners(SensorManager sensorManager, SensorEventListener mEventListener){
		sensorManager.registerListener(mEventListener,  
				sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), 
				SensorManager.SENSOR_DELAY_NORMAL);
	}
*/
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
		
		private float mPreviousX;
		private float mPreviousY;
		private final float TOUCH_SCALE_FACTOR = 180.0f / 1024;
		
		private int SMOOTHING = 10;
		private float TURN_SENSIVITITY = 0.5f;
		private float ACCELERATE_SENSITIVITY = 0.5f;
		private float REVERSE_TIPPING_POINT = 0.5f;
		private int REVERSE_ACCELERATION = -1;//set to 1 to have forward tilt go forward

		private ArrayList<Float> mTurnSmoothing = new ArrayList<Float>();
		private ArrayList<Float> mAccelerateSmoothing = new ArrayList<Float>();
		private float mTurnPercentage;
		private float mAcceleratePercentage;
		NumberFormat nf = NumberFormat.getPercentInstance();
		

		public void updateMovement(float[] orientation){
			if(mTurnSmoothing.size() >= SMOOTHING)
				mTurnSmoothing.remove(0);
			mTurnSmoothing.add(new Float(orientation[1]));
			
			float tmp = 0.0f;
			for( Float f : mTurnSmoothing)
				tmp += f.floatValue();
			
			mTurnPercentage = (float) ((tmp / mTurnSmoothing.size()) / TURN_SENSIVITITY);
			if  ( mTurnPercentage > 1 )
				mTurnPercentage = 1.0f;
			else if  ( mTurnPercentage < -1 )
				mTurnPercentage = -1.0f;

			tmp = 0.0f;
			
			if(mAccelerateSmoothing.size() >= SMOOTHING)
				mAccelerateSmoothing.remove(0);
			mAccelerateSmoothing.add(new Float(orientation[2]));

			for( Float f : mAccelerateSmoothing)
				tmp += f.floatValue();

			mAcceleratePercentage = REVERSE_ACCELERATION * (float) ((tmp / mAccelerateSmoothing.size()) / ACCELERATE_SENSITIVITY);
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
//			System.out.println(nf.format(leftPower) + ", " + nf.format(rightPower));
//			System.out.println(mRenderer.mAngle);
			requestRender();
		}
		
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
					
					mRenderer.mAngle += (dx + dy) * TOUCH_SCALE_FACTOR;  // = 180.0f / 320
					requestRender();
			}
			
			mPreviousX = x;
			mPreviousY = y;
			
			return true;
		}
	}

}
