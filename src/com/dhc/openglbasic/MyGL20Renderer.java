package com.dhc.openglbasic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;

import android.opengl.GLES20;
import android.os.SystemClock;
import android.util.Log;

public class MyGL20Renderer implements GLSurfaceView.Renderer{
	private Triangle myTriangle;

	private Square leftFG, leftBG ;
	private Triangle leftForward, leftBackard;
	private Triangle leftBalanceBG, leftBalanceFG;
	
	private Square rightFG, rightBG;
	private Triangle rightForward, rightBackard;
	private Triangle rightBalanceBG, rightBalanceFG;
	
	private Button power, speak, stop, spin360;
	
	private final float[] mMVPMatrix = new float[16];
	private final float[] mProjMatrix = new float[16];
	private final float[] mVMatrix = new float[16];
	private final float[] mRotationMatrix = new float[16];

	public static float leftSquare[] = {		
		1.25f,	 0.7f,	0.0f,	//top left
		1.25f,	-0.7f,	0.0f,	//bottom left
		 0.75f,-0.7f,	0.0f,	//bottom right
		 0.75f, 0.7f,	0.0f	};	//top right
	static float leftForwardTriangleCoords[] = { //in counterclockwise order:
		1.26f, 0.61f, 0.0f, 	//top
		1.40f, 0.01f, 0.0f,		//bottom left
		1.26f, 0.01f, 0.0f	//bottom right
	};
	static float leftBackwardTriangleCoords[] = { //in counterclockwise order:
		1.26f, -0.61f, 0.0f, 	//top
		1.40f, -0.01f, 0.0f,		//bottom left
		1.26f, -0.01f, 0.0f	//bottom right
	};
	static float leftBalanceTriangleCoords[] = { //in counterclockwise order:
		0.74f, 0.50f, 0.0f, 	//top
		0.74f, -0.50f, 0.0f,		//bottom left
		0.0f, 0.0f, 0.0f	//bottom right
	};

	public static float rightSquare[] = {		
		-1.25f,	 0.7f,	0.0f,	//top left
		-1.25f,	-0.7f,	0.0f,	//bottom left
		 -0.75f,-0.7f,	0.0f,	//bottom right
		 -0.75f, 0.7f,	0.0f	};	//top right
	static float rightForwardTriangleCoords[] = { //in counterclockwise order:
		-1.26f, 0.61f, 0.0f, 	//top
		-1.40f, 0.01f, 0.0f,		//bottom left
		-1.26f, 0.01f, 0.0f	//bottom right
	};
	static float rightBackwardTriangleCoords[] = { //in counterclockwise order:
		-1.26f, -0.61f, 0.0f, 	//top
		-1.40f, -0.01f, 0.0f,		//bottom left
		-1.26f, -0.01f, 0.0f	//bottom right
	};
	static float rightBalanceTriangleCoords[] = { //in counterclockwise order:
		-0.74f, 0.50f, 0.0f, 	//top
		-0.74f, -0.50f, 0.0f,		//bottom left
		0.0f, 0.0f, 0.0f	//bottom right
	};

	
	public static float upperRightButton[] = {		
		-2.10f,	0.9f,	0.0f,	//top left
		-2.10f,	0.3f,	0.0f,	//bottom left
		-1.50f, 0.3f,	0.0f,	//bottom right
		-1.50f, 0.9f,	0.0f	//top right	
	};	
	public static float midRightButton[] = {		
		-2.10f,	0.2f,	0.0f,	//top left
		-2.10f,	-0.4f,	0.0f,	//bottom left
		-1.50f, -0.4f,	0.0f,	//bottom right
		-1.50f, 0.2f,	0.0f	//top right	
	};	
	
	public static float upperLeftButton[] = {		
		2.10f,	0.9f,	0.0f,	//top left
		2.10f,	0.3f,	0.0f,	//bottom left
		1.50f, 0.3f,	0.0f,	//bottom right
		1.50f, 0.9f,	0.0f	//top right	
	};	
	public static float midLeftButton[] = {		
		2.10f,	0.2f,	0.0f,	//top left
		2.10f,	-0.4f,	0.0f,	//bottom left
		1.50f, -0.4f,	0.0f,	//bottom right
		1.50f, 0.2f,	0.0f	//top right	
	};	
	
//	float bgSquareColor [] = { 0.2f, 0.709803922f, 0.898039216f, 1.0f };
	float bgSquareColor [] = { 0.2f, 0.2f, 0.2f, 1.0f };
	float fgSquareColor [] = { 0.6f, 0f, 0f, 1.0f };
	float blueColor [] = { 0.2f, 0.2f, 1.0f, 1.0f };
	float lightBlueColor [] = { 0.5f, 0.6f, .8f, 1.0f };
	
	//Declare as volatile because we are updated it from another thread
	public volatile float mAngle;
	public volatile float leftPower = 0.0f;
	public volatile float rightPower = 0.0f;
	public volatile float leftPowerBalance = 0.0f;
	public volatile float rightPowerBalance = 0.0f;
	
	
	public void onSurfaceCreated(GL10 unused, EGLConfig config){
		//set the frame bg color
		GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);
		
		power = new Button(upperRightButton, R.drawable.button_transparent, "Power");
		stop = new Button(midRightButton, R.drawable.button_transparent, "Stop");
		speak = new Button(upperLeftButton, R.drawable.button_transparent, "Speak");
		spin360 = new Button(midLeftButton, R.drawable.button_transparent, "Spin 360");

		myTriangle = new Triangle(null);
		//mySquare = new Square();
		leftFG = new Square(leftSquare);
		leftBG = new Square(leftSquare);
		leftForward = new Triangle(leftForwardTriangleCoords);
		leftBackard= new Triangle(leftBackwardTriangleCoords);
		leftBalanceFG = new Triangle(leftBalanceTriangleCoords);
		leftBalanceBG = new Triangle(leftBalanceTriangleCoords);
		
		
		rightFG = new Square(rightSquare);
		rightBG = new Square(rightSquare);
		rightForward = new Triangle(rightForwardTriangleCoords);
		rightBackard= new Triangle(rightBackwardTriangleCoords);
		rightBalanceFG = new Triangle(rightBalanceTriangleCoords);
		rightBalanceBG = new Triangle(rightBalanceTriangleCoords);
	}
	
	public void onDrawFrame(GL10 unused){
		//redraw bg color
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
		
		//set up camera position (View matrix)
		Matrix.setLookAtM(mVMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
		
		//calc the projection and view transformation
		Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mVMatrix, 0);
		
		float[] leftBarMatrix = new float[16];
		System.arraycopy( mMVPMatrix, 0, leftBarMatrix, 0, mMVPMatrix.length );
		float[] leftBalanceMatrix = new float[16];
		System.arraycopy( mMVPMatrix, 0, leftBalanceMatrix, 0, mMVPMatrix.length );
		float[] rightBarMatrix = new float[16];
		System.arraycopy( mMVPMatrix, 0, rightBarMatrix, 0, mMVPMatrix.length );
		float[] rightBalanceMatrix = new float[16];
		System.arraycopy( mMVPMatrix, 0, rightBalanceMatrix, 0, mMVPMatrix.length );
		float[] pointerMatrix = new float[16];
		System.arraycopy( mMVPMatrix, 0, pointerMatrix, 0, mMVPMatrix.length );

		power.draw(mMVPMatrix, lightBlueColor);
		stop.draw(mMVPMatrix, lightBlueColor);
		speak.draw(mMVPMatrix, lightBlueColor);
		spin360.draw(mMVPMatrix, lightBlueColor);
		
		leftBG.draw(mMVPMatrix, bgSquareColor);
		leftForward.draw(mMVPMatrix, leftPower>=0?fgSquareColor:bgSquareColor);
		leftBackard.draw(mMVPMatrix, leftPower<0?blueColor:bgSquareColor);
		leftBalanceBG.draw(mMVPMatrix, bgSquareColor);

		rightBG.draw(mMVPMatrix, bgSquareColor);
		rightForward.draw(mMVPMatrix, rightPower>=0?fgSquareColor:bgSquareColor);
		rightBackard.draw(mMVPMatrix, rightPower<0?blueColor:bgSquareColor);
		rightBalanceBG.draw(mMVPMatrix, bgSquareColor);
									     
		Matrix.scaleM(leftBarMatrix, 0, 1.0f, leftPower, 1.0f); //width, height,  ?
		leftFG.draw(leftBarMatrix,  leftPower>=0?fgSquareColor:blueColor);
		Matrix.scaleM(rightBarMatrix, 0, 1.0f, rightPower, 1.0f); //width, height,  ?
		rightFG.draw(rightBarMatrix, rightPower>=0?fgSquareColor:blueColor);

//		float leftScale = leftPowerBalance>0?leftPowerBalance:0.0f;
		Matrix.scaleM(leftBalanceMatrix, 0, Math.abs(leftPowerBalance), Math.abs(leftPowerBalance), 1.0f); //width, height,  ?
		leftBalanceFG.draw(leftBalanceMatrix, leftPowerBalance>=0?fgSquareColor:blueColor);
		Matrix.scaleM(rightBalanceMatrix, 0, Math.abs(rightPowerBalance), Math.abs(rightPowerBalance), 1.0f); //width, height,  ?
		rightBalanceFG.draw(rightBalanceMatrix, rightPowerBalance>=0?fgSquareColor:blueColor);

		//create a rotation transformation for the triangle
		//long time = SystemClock.uptimeMillis() % 4000L;
		//float angle = 0.090f * ((int) time);
		Matrix.setRotateM(mRotationMatrix, 0, mAngle, 0, 0, -1.0f);
		
		//combine the rotation matrix with the projection and camera view
		Matrix.multiplyMM(pointerMatrix, 0, mRotationMatrix, 0, pointerMatrix, 0);
		
		myTriangle.draw(pointerMatrix, null);


	}
	
	public void onSurfaceChanged(GL10 unused, int width, int height){
		GLES20.glViewport(0, 0, width, height);
		
		float ratio = (float) width / height;
		
		//this porjection amtrix is applied to object coords
		// in the onDrawFrame() method
		
		Matrix.frustumM(mProjMatrix,  0,  -ratio,  ratio, -1, 1, 3, 7);
	}
	
	public static int loadShader(int type, String shaderCode){
		//create a vertex shader type
		//or a fragment shader type
		int shader = GLES20.glCreateShader(type);
		
		//add the code to the shader and compile
		GLES20.glShaderSource(shader,  shaderCode);
		GLES20.glCompileShader(shader);
		
		return shader;
	}
/*	
	public static int loadTexture2(final Context context, final int resourceId){
		final int[] textureHandle = new int[1];
		
		GLES20.glGenTextures(1, textureHandle, 0);
		
		if(textureHandle[0] != 0){
			final BitmapFactory.Options options = new BitmapFactory.Options();
//			options.inScaled = false; //no Pre-scaling
			
			//Read in
			final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);

			Canvas canvas = new Canvas(bitmap);
*/		

/*			
			bitmap.eraseColor(0);
			
			Drawable background = context.getResources().getDrawable(R.drawable.background);
			background.setBounds(0, 0, 256, 256);
			background.draw(canvas);
*/		
	/*			
			//draw text
			Paint textPaint = new Paint();
			textPaint.setTextSize(32);
			textPaint.setAntiAlias(true);
			textPaint.setARGB(0xff,  0x00, 0x00, 0x00);
			canvas.drawText("BUTTON",  16,  111,  textPaint);
			
			//Bin to texture in OpenGL
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);
			
			//set filtering
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,  GLES20.GL_TEXTURE_MIN_FILTER,  GLES20.GL_NEAREST);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,  GLES20.GL_TEXTURE_MAG_FILTER,  GLES20.GL_LINEAR);

			//different possible texture parameters
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);

			
			//Load the bitmap into the bound texture
			GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
			
			//recycle the bitmap, since it's data has been loaded already
			bitmap.recycle();
		} 
		
		if(textureHandle[0] == 0)
				throw new RuntimeException("Error loading texture.");
		
		return textureHandle[0];
	}
*/	
/*	
	public static int loadTextTexture(final Context context, final int resourceId){
		final int[] textureHandle = new int[1];
		GLES20.glGenTextures(1, textureHandle, 0);
		
		if(textureHandle[0] != 0){

			final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId);
			
			bitmap.eraseColor(0);
			
			//get the bg image 
	
			Canvas canvas = new Canvas(bitmap);
			bitmap.eraseColor(0);
			
			Drawable background = context.getResources().getDrawable(R.drawable.background);
			background.setBounds(0, 0, 256, 256);
			background.draw(canvas);
			
			//draw text
			Paint textPaint = new Paint();
			textPaint.setTextSize(32);
			textPaint.setAntiAlias(true);
			textPaint.setARGB(0xff,  0x00, 0x00, 0x00);
			canvas.drawText("BUTTON",  16,  111,  textPaint);
			
			//gen on text pointer
			GLES20.glGenTextures(1, textureHandle, 0);
			//bing to array
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,  textureHandle[0]);
			
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
			
			//different possible texture parameters
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
		
			GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
			
			bitmap.recycle();
		
	}
*/
}


class Triangle{
	public final static String vertexShaderCode = 
			"uniform mat4 uMVPMatrix;" + 
			"attribute vec4 vPosition;" +
			"void main() {" +
				"gl_Position = vPosition * uMVPMatrix;" + 
			"}";
		
	public final static String fragmentShaderCode = 
			"precision mediump float;" + 
			"uniform vec4 vColor;" + 
			"void main() {" + 
				"gl_FragColor = vColor;" + 
			"}"; 
	private FloatBuffer vertexBuffer;
	
	//number of coordinates per vertex in this array
	static final int COORDS_PER_VERTEX = 3;
	
	//By default, OpenGL ES assumes a coordinate system where [0,0,0] (X,Y,Z) 
	//specifies the center of the GLSurfaceView frame, [1,1,0] is the top right 
	//corner of the frame and [-1,-1,0] is bottom left corner of the frame. 
	static float triangleCoords[] = { //in counterclockwise order:
		0.0f, 0.25f, 0.0f, 	//top
		-0.15f, -0.15f, 0.0f,	//bottom left
		0.15f, -0.15f, 0.0f	//bottom right
	};
	private final int vertexCount = triangleCoords.length / COORDS_PER_VERTEX;
	private final int vertexStride = COORDS_PER_VERTEX * 4; //4 bytes per vertex
	
	//set the color with r,g,b,a values
	float defaultColor[] = { 0.63671875f, 0.76953125f, 0.22265625f, 0.6f };
	
	private final int mProgram;
	private int mPositionHandle;
	private int mColorHandle;
	private int mMVPMatrixHandle;
	
	public Triangle(float[] coords){
		if(coords==null)
			coords = triangleCoords;
		
		//initialize vertex by buffer for shape coordinates
		ByteBuffer bb = ByteBuffer.allocateDirect(
				// (number of coordinate values * 4 bytes per float)
				triangleCoords.length * 4);
		//use the device hardware's native byte order
		bb.order(ByteOrder.nativeOrder());
		
		//create a floating point buffer from the ByteBuffer
		vertexBuffer = bb.asFloatBuffer();
		//add the coords 
		vertexBuffer.put(coords);
		//set the buffer to read the first coord
		vertexBuffer.position(0);
		
		int vertexShader = MyGL20Renderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
		int fragmentShader = MyGL20Renderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
		
		mProgram = GLES20.glCreateProgram();	//create an empty OpenGL ES Program
		GLES20.glAttachShader(mProgram, vertexShader);	//add the vertex shader
		GLES20.glAttachShader(mProgram, fragmentShader);	//add the fragment shader
		GLES20.glLinkProgram(mProgram);	//create OpenGL ES program execs
	}
	
	public void draw(float[] mvpMatrix, float color[]){ //pass in the calc'd transofrmation matrix
		if(color==null)
			color = defaultColor;
		//add program to OpenGL ES env
		GLES20.glUseProgram(mProgram);
		
		//get handle to vertex shader's vPosition member
		mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
		
		//enable a hangle to the triangle vertices
		GLES20.glEnableVertexAttribArray(mPositionHandle);
		
		//prep the triangle coord data
		GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);
		
		//get handle to grament's shader vColor member
		mColorHandle = GLES20.glGetUniformLocation(mProgram,  "vColor");
		
		//set color for drawing
		GLES20.glUniform4fv(mColorHandle, 1, color, 0);
		
		//get handle to shape's transofrmation matrix
		mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
		
		//Apply the projection and view transormation
		GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
		
		//Draw it!
		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);
		
		//disable the vertex array
		GLES20.glDisableVertexAttribArray(mPositionHandle);
		
	}
}

class Square {
	private FloatBuffer vertexBuffer;
	private ShortBuffer drawListBuffer;
	private final int mProgram;
	private int mPositionHandle;
	private int mColorHandle;
	private int mMVPMatrixHandle;

	//numer of coords per vertex
	static final int COORDS_PER_VERTEX = 3;
	public static float defaultCoords[] = {		-0.5f,	0.5f,	0.0f,	//top left
										-0.5f,	-0.5f,	0.0f,	//bottom left
										0.5f,	-0.5f,	0.0f,	//bottom right
										0.5f,	0.5f,	0.0f	};	//top right

	private short drawOrder[]  = { 0, 1, 2, 0, 2, 3 }; //order to draw vertices

	//private final int vertexCount = squareCoords.length / COORDS_PER_VERTEX;
	private final int vertexStride = COORDS_PER_VERTEX * 4; //4 bytes per vertex
	
	//set the color with r,g,b,a values
	float defaultColor[] = { 0.2f, 0.709803922f, 0.898039216f, 1.0f };
	
	
	public Square(float[] squareCoords){
		int vertexCount = squareCoords.length / COORDS_PER_VERTEX;
		//init vertex bb for shape coords
		ByteBuffer bb = ByteBuffer.allocateDirect( squareCoords.length * 4 );  //4 byte per float
		bb.order(ByteOrder.nativeOrder());
		
		vertexBuffer = bb.asFloatBuffer();
		vertexBuffer.put(squareCoords);
		vertexBuffer.position(0);
		
		//iit bb for draw list
		ByteBuffer dlb = ByteBuffer.allocateDirect(
				//(# of coord values * 2 bytes per short)
				drawOrder.length *2 );
		dlb.order(ByteOrder.nativeOrder());
		drawListBuffer = dlb.asShortBuffer();
		drawListBuffer.put(drawOrder);
		drawListBuffer.position(0);
		
		int vertexShader = MyGL20Renderer.loadShader(GLES20.GL_VERTEX_SHADER, Triangle.vertexShaderCode);
		int fragmentShader = MyGL20Renderer.loadShader(GLES20.GL_FRAGMENT_SHADER, Triangle.fragmentShaderCode);
		
		mProgram = GLES20.glCreateProgram();	//create an empty OpenGL ES Program
		GLES20.glAttachShader(mProgram, vertexShader);	//add the vertex shader
		GLES20.glAttachShader(mProgram, fragmentShader);	//add the fragment shader
		GLES20.glLinkProgram(mProgram);	//create OpenGL ES program execs		
	}
	public void draw(float[] mvpMatrix, float color[]){ //pass in the calc'd transofrmation matrix
		//add program to OpenGL ES env
		GLES20.glUseProgram(mProgram);
		
		//get handle to vertex shader's vPosition member
		mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
		
		//enable a hangle to the triangle vertices
		GLES20.glEnableVertexAttribArray(mPositionHandle);
		
		//prep the trianle coord data
		GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
			GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);
		
		//get handle to grament's shader vColor member
		mColorHandle = GLES20.glGetUniformLocation(mProgram,  "vColor");
		
		//set color for drawing
		GLES20.glUniform4fv(mColorHandle, 1, color, 0);
		
		//get handle to shape's transformation matrix
		mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
		
		//Apply the projection and view transformation
		GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
		
		//Draw it!
		//GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);
		GLES20.glDrawElements(GLES20.GL_TRIANGLES,  drawOrder.length, GLES20.GL_UNSIGNED_SHORT, drawListBuffer);
		
		//disable the vertex array
		GLES20.glDisableVertexAttribArray(mPositionHandle);
		
	}	
}

	class Button {
		private FloatBuffer vertexBuffer;
		private ShortBuffer drawListBuffer;
		private final int mProgram;
		private int mPositionHandle;
		private int mColorHandle;
		private int mMVPMatrixHandle;

		private int mTextureDataHandle;
		private int mTextureUniformHandle;
		private int mTextureCoordinateHandle;

		private FloatBuffer textureBuffer;
		private float texture[] = {
				// Mapping coordinates for the vertices
				0.0f, 1.0f,		// top left		(V2)
				0.0f, 0.0f,		// bottom left	(V1)
				1.0f, 0.0f,		// bottom right	(V3)
				1.0f, 1.0f,		// top right	(V4)
		};				

		//numer of coords per vertex
		static final int COORDS_PER_VERTEX = 3;
		public static float defaultCoords[] = {
			-0.5f,	 0.5f,	0.0f,	//top left
			-0.5f,	-0.5f,	0.0f,	//bottom left
			 0.5f,	-0.5f,	0.0f,	//bottom right
			 0.5f,	 0.5f,	0.0f	};	//top right

//		private short drawOrder[]  = { 0, 1, 2, 0, 2, 3 }; //order to draw vertices
		private short drawOrder[]  = { 0, 1, 2, 0, 2, 3 }; //order to draw vertices

		//private final int vertexCount = squareCoords.length / COORDS_PER_VERTEX;
		private final int vertexStride = COORDS_PER_VERTEX * 4; //4 bytes per vertex
		
		//set the color with r,g,b,a values
		float defaultColor[] = { 0.2f, 0.709803922f, 0.898039216f, 1.0f };
		
		
		public Button(float[] squareCoords, int background, String buttonText){
			int vertexCount = squareCoords.length / COORDS_PER_VERTEX;
			//init vertex bb for shape coords
			ByteBuffer bb = ByteBuffer.allocateDirect( squareCoords.length * 4 );  //4 byte per float
			bb.order(ByteOrder.nativeOrder());
			
			vertexBuffer = bb.asFloatBuffer();
			vertexBuffer.put(squareCoords);
			vertexBuffer.position(0);
			
			//iit bb for draw list
			ByteBuffer dlb = ByteBuffer.allocateDirect(
					//(# of coord values * 2 bytes per short)
					drawOrder.length *2 );
			dlb.order(ByteOrder.nativeOrder());
			drawListBuffer = dlb.asShortBuffer();
			drawListBuffer.put(drawOrder);
			drawListBuffer.position(0);
			
			//adding texture buffer
			bb = ByteBuffer.allocateDirect(texture.length * 4);
			bb.order(ByteOrder.nativeOrder());
			textureBuffer = bb.asFloatBuffer();
			textureBuffer.put(texture);
			textureBuffer.position(0);			
			
			int vertexShader = MyGL20Renderer.loadShader(GLES20.GL_VERTEX_SHADER, getVertexShader());
			int fragmentShader = MyGL20Renderer.loadShader(GLES20.GL_FRAGMENT_SHADER, getFragmentShader());
			
			mProgram = GLES20.glCreateProgram();	//create an empty OpenGL ES Program
			GLES20.glAttachShader(mProgram, vertexShader);	//add the vertex shader
			GLES20.glAttachShader(mProgram, fragmentShader);	//add the fragment shader
			GLES20.glLinkProgram(mProgram);	//create OpenGL ES program execs		
			
			mTextureDataHandle = loadTransparentTextureWithText(OpenGLES20Basic.getContext(), R.drawable.button_transparent, buttonText, squareCoords[0]<0);
/*			
			int vertexCount = squareCoords.length / COORDS_PER_VERTEX;
			//init vertex bb for shape coords
			ByteBuffer bb = ByteBuffer.allocateDirect( squareCoords.length * 4 );  //4 byte per float
			bb.order(ByteOrder.nativeOrder());
			
			vertexBuffer = bb.asFloatBuffer();
			vertexBuffer.put(squareCoords);
			vertexBuffer.position(0);

			bb = ByteBuffer.allocateDirect(texture.length * 4);
			bb.order(ByteOrder.nativeOrder());
			textureBuffer = bb.asFloatBuffer();
			textureBuffer.put(texture);
			textureBuffer.position(0);
			
			//it bb for draw list
			ByteBuffer dlb = ByteBuffer.allocateDirect(drawOrder.length *2 );//(# of coord values * 2 bytes per short)
			dlb.order(ByteOrder.nativeOrder());
			drawListBuffer = dlb.asShortBuffer();
			drawListBuffer.put(drawOrder);
			drawListBuffer.position(0);
			
			final String vertexShaderCode = getVertexShader();   		
	 		final String fragmentShaderCode = getFragmentShader();	
			
	 		final int vertexShader = MyGL20Renderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
System.out.println("Button vertexShader: " + vertexShader);

	 		final int fragmentShader = MyGL20Renderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
System.out.println("Button fragmentShader: " + fragmentShader);
	 		
			mProgram = GLES20.glCreateProgram();	//create an empty OpenGL ES Program
			GLES20.glAttachShader(mProgram, vertexShader);	//add the vertex shader
			GLES20.glAttachShader(mProgram, fragmentShader);	//add the fragment shader
			GLES20.glLinkProgram(mProgram);	//create OpenGL ES program execs
*/			
			
		}
		
/*		
		public void loadGLTexture(Context context, int background){
			// loading texture
			Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), background);

			// generate one texture pointer
			GLES20.glGenTextures(1, textures, 0);
			// ...and bind it to our array
			GLES20.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);
			
			// create nearest filtered texture
			GLES20.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
			GLES20.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
			
			// Use Android GLUtils to specify a two-dimensional texture image from our bitmap 
			GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
			
			// Clean up
			bitmap.recycle();			
		}
*/
		private int mMVMatrixHandle;
		private int mNormalHandle;
		private int maTextureHandle;
		private int msTextureHandle;
		private int muTMatrixHandle;
		private int mvTextureCoord;
		private int maPositionHandle;
		public void draw(float[] mvpMatrix, float color[]){ //pass in the calc'd transformation matrix
			//add program to OpenGL ES env
			GLES20.glUseProgram(mProgram);
			
			//get handle to vertex shader's vPosition member
			mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
			
			//enable a hangle to the triangle vertices//prep the triangle coord data
			GLES20.glEnableVertexAttribArray(mPositionHandle);
			GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);
			
			//get handle to grament's shader vColor member//set color for drawing
			mColorHandle = GLES20.glGetUniformLocation(mProgram,  "vColor");
			GLES20.glUniform4fv(mColorHandle, 1, color, 0);
			
			//get handle to shape's transformation matrix//Apply the projection and view transformation
			mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
			GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
			
			//TEXTURE STUFF
			maTextureHandle = GLES20.glGetAttribLocation(mProgram,  "aTextureCoord");
			msTextureHandle = GLES20.glGetUniformLocation(mProgram, "sTexture");
			GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle);
			GLES20.glUniform1i(msTextureHandle, 0);
            GLES20.glEnableVertexAttribArray(maTextureHandle);
            GLES20.glVertexAttribPointer(maTextureHandle, 2, GLES20.GL_FLOAT, false, 0, textureBuffer);
			
            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
			//Draw it!
            GLES20.glEnable(GLES20.GL_BLEND); 
			GLES20.glDrawElements(GLES20.GL_TRIANGLES,  drawOrder.length, GLES20.GL_UNSIGNED_SHORT, drawListBuffer);
            GLES20.glDisable(GLES20.GL_BLEND); 
			
			//disable the vertex array
			GLES20.glDisableVertexAttribArray(mPositionHandle);	
            GLES20.glDisableVertexAttribArray(maTextureHandle);
			
/*			
			//add program to OpenGL ES env
			GLES20.glUseProgram(mProgram);
			
			//vertex shader
			mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
			muTMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uTMatrix");
			maTextureHandle = GLES20.glGetAttribLocation(mProgram,  "aTextureCoord");
			mvTextureCoord = GLES20.glGetAttribLocation(mProgram,  "vTextureCoord");
			maPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
			//frag shader
			msTextureHandle = GLES20.glGetUniformLocation(mProgram, "sTexture");
			mvTextureCoord = GLES20.glGetAttribLocation(mProgram, "vTextureCoord");

			GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer);
			GLES20.glEnableVertexAttribArray(maPositionHandle);
			
			GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
//			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, msTextureHandle);
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle);
            GLES20.glUniform1i(msTextureHandle, 0);

            GLES20.glVertexAttribPointer(maTextureHandle, 2, GLES20.GL_FLOAT, false, 0, textureBuffer);
            GLES20.glEnableVertexAttribArray(maTextureHandle);

            GLES20.glUniformMatrix4fv(muTMatrixHandle, 1, false, mvpMatrix, 0);
            
            GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_SHORT, drawListBuffer);
            
            GLES20.glDisableVertexAttribArray(mPositionHandle);
            GLES20.glDisableVertexAttribArray(maTextureHandle);
*/            
/*			
			//add program to OpenGL ES env
			GLES20.glUseProgram(mProgram);
			
			//get handle to vertex shader's vPosition member
			mPositionHandle = GLES20.glGetAttribLocation(mProgram, "a_Position");
			mColorHandle = GLES20.glGetUniformLocation(mProgram,  "a_Color");
			mTextureUniformHandle = GLES20.glGetUniformLocation(mProgram, "u_Texture");
			mTextureCoordinateHandle = GLES20.glGetAttribLocation(mProgram, "a_TexCoordinate");
			mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "u_MVPMatrix");

	        mMVMatrixHandle = GLES20.glGetUniformLocation(mProgram, "u_MVMatrix"); 
	        mNormalHandle = GLES20.glGetAttribLocation(mProgram, "a_Normal"); 

			//set the active texture unto to texture unit 0
			GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

			//Bind the texture to the unit
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle);
			
			//tell the texture uniform sampler to use this texutre in the shader by binding tho texture unit 0
			GLES20.glUniform1i(mTextureUniformHandle,  0);

			//enable a hangle to the  vertices
			GLES20.glEnableVertexAttribArray(mPositionHandle);
			
			//prep the trianle coord data
			GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);
			
			//set color for drawing
			GLES20.glUniform4fv(mColorHandle, 1, color, 0);
			
			//Apply the projection and view transformation
			GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
			
			//Draw it!
			//GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);
			GLES20.glDrawElements(GLES20.GL_TRIANGLES,  drawOrder.length, GLES20.GL_UNSIGNED_SHORT, drawListBuffer);
			
			//disable the vertex array
			GLES20.glDisableVertexAttribArray(mPositionHandle);
			
			//drawLight(mvpMatrix);
 */
			
		}
/*	
		public static int loadTexture(final Context context, final int resourceId) {
			final int[] textureHandle = new int[1];

			GLES20.glGenTextures(1, textureHandle, 0);

            // Create Nearest Filtered Texture
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

            // Different possible texture parameters, e.g.
            // GLES20.GL_CLAMP_TO_EDGE
            //GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
            //GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

			final BitmapFactory.Options options = new BitmapFactory.Options();
			options.inScaled = false;	// No pre-scaling
            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);;
            if (bitmap == null) {
            	System.err.println("Could not load texture:" + resourceId);
            	Bitmap.Config conf = Bitmap.Config.ARGB_8888;
            	bitmap = Bitmap.createBitmap(100, 100, conf);
            } else
            	System.out.println(resourceId + " texture loaded successfully");
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
            
//			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);
			
			bitmap.recycle();
            return textureHandle[0];
        }
*/
		public static int loadTransparentTextureWithText(final Context context, final int resourceId, String text, boolean flipHorizontal) {
			final int[] textureHandle = new int[1];

			GLES20.glGenTextures(1, textureHandle, 0);

			if (textureHandle[0] != 0)
			{
				final BitmapFactory.Options options = new BitmapFactory.Options();
				options.inScaled = false;	// No pre-scaling

				// Read in the resource
				//Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);
				
				Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888 );
				
			    Canvas canvas = new Canvas(bitmap);
			    bitmap.eraseColor(0);
			    
				Drawable background = context.getResources().getDrawable(resourceId);
				background.setBounds(0, 0, 100, 100);
				background.draw(canvas);

			    // Draw the text
			    Paint textPaint = new Paint();
			    textPaint.setTextAlign(Paint.Align.CENTER);
			    textPaint.setAntiAlias(true);

			    int fontSize = 16;

			    textPaint.setTextSize(fontSize);
			    textPaint.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
			    textPaint.setARGB(0xff, 0xff, 0xff, 0xff);
			    canvas.drawText(text, 50,50+(fontSize/3), textPaint);

			    textPaint.setTextSize(fontSize);
			    textPaint.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
			    textPaint.setARGB(0xff, 0x00, 0x00, 0x00);
			    canvas.drawText(text, 50,50+(fontSize/3), textPaint);

/*
			    byte[] buffer = new byte[bitmap.getWidth() * bitmap.getHeight() * 4];
				for ( int y = 0; y < bitmap.getHeight(); y++ )
				    for ( int x = 0; x < bitmap.getWidth(); x++ ){
				        int pixel = bitmap.getPixel(x, y);
				        buffer[(y * bitmap.getWidth() + x) * 4 + 0] = (byte)((pixel >> 16) & 0xFF);
				        buffer[(y * bitmap.getWidth() + x) * 4 + 1] = (byte)((pixel >> 8) & 0xFF);
				        buffer[(y * bitmap.getWidth() + x) * 4 + 2] = (byte)((pixel >> 0) & 0xFF);
				        buffer[(y * bitmap.getWidth() + x) * 4 + 3] = (byte)((pixel >> 24) & 0xFF);
				    }
				
				ByteBuffer byteBuffer = ByteBuffer.allocateDirect(bitmap.getWidth() * bitmap.getHeight() * 4);
			    byteBuffer.put(buffer).position(0);
	*/		    
				// Bind to the texture in OpenGL
				GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);

				//flip it
				android.graphics.Matrix flip = new android.graphics.Matrix();
				flip.postScale(flipHorizontal?-1f:1f, -1f);
				bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(),flip, true);
				
				// Load the bitmap into the bound texture.
				GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
				//GLES20.glTexImage2D ( GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, bitmap.getWidth(), bitmap.getHeight(), 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, byteBuffer );

				// Set filtering
				GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
				GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
				GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE); 
				GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

				
				// Recycle the bitmap, since its data has been loaded into OpenGL.
				bitmap.recycle();						
			}

			if (textureHandle[0] == 0)
			{
				throw new RuntimeException("Error loading texture.");
			}

			return textureHandle[0];
		}
		public static int loadTransparentTexture(final Context context, final int resourceId) {
			final int[] textureHandle = new int[1];

			GLES20.glGenTextures(1, textureHandle, 0);

			if (textureHandle[0] != 0)
			{
				final BitmapFactory.Options options = new BitmapFactory.Options();
				options.inScaled = false;	// No pre-scaling

				// Read in the resource
				final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);
				
				byte[] buffer = new byte[bitmap.getWidth() * bitmap.getHeight() * 4];
				for ( int y = 0; y < bitmap.getHeight(); y++ )
				    for ( int x = 0; x < bitmap.getWidth(); x++ ){
				        int pixel = bitmap.getPixel(x, y);
				        buffer[(y * bitmap.getWidth() + x) * 4 + 0] = (byte)((pixel >> 16) & 0xFF);
				        buffer[(y * bitmap.getWidth() + x) * 4 + 1] = (byte)((pixel >> 8) & 0xFF);
				        buffer[(y * bitmap.getWidth() + x) * 4 + 2] = (byte)((pixel >> 0) & 0xFF);
				        buffer[(y * bitmap.getWidth() + x) * 4 + 3] = (byte)((pixel >> 24) & 0xFF);
				    }
				
				ByteBuffer byteBuffer = ByteBuffer.allocateDirect(bitmap.getWidth() * bitmap.getHeight() * 4);
			    byteBuffer.put(buffer).position(0);
			    
				// Bind to the texture in OpenGL
				GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);

				// Load the bitmap into the bound texture.
				//GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
				GLES20.glTexImage2D ( GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, bitmap.getWidth(), bitmap.getHeight(), 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, byteBuffer );

				// Set filtering
				GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
				GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
				GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE); 
				GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

				
				// Recycle the bitmap, since its data has been loaded into OpenGL.
				bitmap.recycle();						
			}

			if (textureHandle[0] == 0)
			{
				throw new RuntimeException("Error loading texture.");
			}

			return textureHandle[0];
		}
		public static int loadTexture(final Context context, final int resourceId) {
			final int[] textureHandle = new int[1];

			GLES20.glGenTextures(1, textureHandle, 0);

			if (textureHandle[0] != 0)
			{
				final BitmapFactory.Options options = new BitmapFactory.Options();
				options.inScaled = false;	// No pre-scaling

				// Read in the resource
				final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);

				// Bind to the texture in OpenGL
				GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);

				// Set filtering
				GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
				GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
				
				GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE); 
				GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
				
				// Load the bitmap into the bound texture.
				GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

				// Recycle the bitmap, since its data has been loaded into OpenGL.
				bitmap.recycle();						
			}

			if (textureHandle[0] == 0)
			{
				throw new RuntimeException("Error loading texture.");
			}

			return textureHandle[0];
		}
		protected String getVertexShader(){
			return readTextFileFromRawResource(OpenGLES20Basic.getContext(), R.raw.button_vertex_shader);
		}

		protected String getFragmentShader(){
			return readTextFileFromRawResource(OpenGLES20Basic.getContext(), R.raw.button_fragment_shader);
		}
		public static String readTextFileFromRawResource(final Context context, final int resourceId){
			final InputStream inputStream = context.getResources().openRawResource(resourceId);
			final InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
			final BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

			String nextLine;
			final StringBuilder body = new StringBuilder();

			try{
				while ((nextLine = bufferedReader.readLine()) != null){
					body.append(nextLine);
					body.append('\n');
				}
			}
			catch (IOException e){
				return null;
			}

			return body.toString();
		}		
		public static int compileShader(final int shaderType, final String shaderSource){
			
			int shaderHandle = GLES20.glCreateShader(shaderType);

			if (shaderHandle != 0){
				// Pass in the shader source.
				GLES20.glShaderSource(shaderHandle, shaderSource);

				// Compile the shader.
				GLES20.glCompileShader(shaderHandle);

				// Get the compilation status.
				final int[] compileStatus = new int[1];
				GLES20.glGetShaderiv(shaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

				// If the compilation failed, delete the shader.
				if (compileStatus[0] == 0){
					System.err.println("Error compiling shader: " + GLES20.glGetShaderInfoLog(shaderHandle));
					GLES20.glDeleteShader(shaderHandle);
					shaderHandle = 0;
				}
			}

			if (shaderHandle == 0){			
				throw new RuntimeException("Error creating shader.");
			}

			return shaderHandle;
		}	
	
}
