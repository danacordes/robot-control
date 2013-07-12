package com.dhc.openglbasic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

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
	private static final String TAG = "MyGL20Renderer";

	private Square leftFG, leftBG ;
	private Triangle leftForward, leftBackard;
	private Triangle leftBalanceBG, leftBalanceFG;
	
	private Square rightFG, rightBG;
	private Triangle rightForward, rightBackard;
	private Triangle rightBalanceBG, rightBalanceFG;
	
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
		-0.74f, -0.50f, 0.0f,	//bottom left
		0.0f, 0.0f, 0.0f	//bottom right
	};

	
	public static float upperRightButtonTouch[] = {.85f, 0f, 1.0f, 0.27f};
	public static float upperRightButton[] = {		
		-2.10f,	0.9f,	0.0f,	//top left
		-2.10f,	0.3f,	0.0f,	//bottom left
		-1.50f, 0.3f,	0.0f,	//bottom right
		-1.50f, 0.9f,	0.0f	//top right	
	};	
	
	public static float midRightButtonTouch[] = {.85f, .33f, 1.0f, 0.6f};
	public static float midRightButton[] = {		
		-2.10f,	0.2f,	0.0f,	//top left
		-2.10f,	-0.4f,	0.0f,	//bottom left
		-1.50f, -0.4f,	0.0f,	//bottom right
		-1.50f, 0.2f,	0.0f	//top right	
	};	
	
	public static float upperLeftButtonTouch[] = {.0f, 0f, 0.15f, 0.27f};
	public static float upperLeftButton[] = {		
		2.10f,	0.9f,	0.0f,	//top left
		2.10f,	0.3f,	0.0f,	//bottom left
		1.50f, 0.3f,	0.0f,	//bottom right
		1.50f, 0.9f,	0.0f	//top right	
	};	

	public static float midLeftButtonTouch[] = {.0f, .33f, 0.15f, 0.6f};
	public static float midLeftButton[] = {		
		2.10f,	0.2f,	0.0f,	//top left
		2.10f,	-0.4f,	0.0f,	//bottom left
		1.50f, -0.4f,	0.0f,	//bottom right
		1.50f, 0.2f,	0.0f	//top right	
	};
	
	
	public static float infoTemplate[] = {		
		-0.25f,	-0.25f,	0.0f,	//top left
		-0.25f,	 0.25f,	0.0f,	//bottom left
		 0.25f,  0.25f,	0.0f,	//bottom right
		 0.25f, -0.25f,	0.0f	//top right	
	};	

	
//	float bgSquareColor [] = { 0.2f, 0.709803922f, 0.898039216f, 1.0f };
	public static float bgSquareColor [] = { 0.2f, 0.2f, 0.2f, 1.0f };
	public static float fgSquareColor [] = { 0.6f, 0f, 0f, 1.0f };
	public static float blueColor [] = { 0.2f, 0.2f, 1.0f, 1.0f };
	public static float lightBlueColor [] = { 0.5f, 0.6f, .8f, 1.0f };
	public static float whiteColor [] = { 1f, 1f, 1f, 1.0f };
	public static float greyColor [] = { 0.7f, 0.7f, 0.7f, 1.0f };
	
	//Declare as volatile because we are updated it from another thread
	public volatile float mAngle;
	public volatile float leftPower = 0.0f;
	public volatile float rightPower = 0.0f;
	public volatile float leftPowerBalance = 0.0f;
	public volatile float rightPowerBalance = 0.0f;
	
	private volatile ArrayList<Button> buttons = new ArrayList<Button>();
	public static final int BUTTON_POWER = 0;
	public static final int BUTTON_STOP = 1;
	public static final int BUTTON_SPEAK = 2;
	public static final int BUTTON_SPIN360 = 3;
	
	private volatile ArrayList<Info> infos = new ArrayList<Info>();
	public static final int INFO_CONNECTED = 0;
	public static final int INFO_ENABLED = 1;
	public static final int INFO_PLACEHOLDER1 = 2;
	public static final int INFO_PLACEHOLDER2 = 3;
	public static final int INFO_PLACEHOLDER3 = 4;
	public static final int INFO_PLACEHOLDER4 = 5;
	
	public void onSurfaceCreated(GL10 unused, EGLConfig config){
		//set the frame bg color
		GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);

		//set up the buttons
		buttons.add(new Button(upperRightButton, R.drawable.button_transparent, "Power", upperRightButtonTouch, BUTTON_POWER));
		buttons.add(new Button(midRightButton, R.drawable.button_transparent, "Stop", midRightButtonTouch, BUTTON_STOP));
		buttons.add(new Button(upperLeftButton, R.drawable.button_transparent, "Speak", upperLeftButtonTouch, BUTTON_SPEAK));
		buttons.add(new Button(midLeftButton, R.drawable.button_transparent, "Spin 360", midLeftButtonTouch, BUTTON_SPIN360));

		//set up the info displays
		infos.add(new Info(infoTemplate, R.drawable.info, "Connected", INFO_CONNECTED));
		infos.add(new Info(infoTemplate, R.drawable.info, "Enabled", INFO_ENABLED));
		infos.add(new Info(infoTemplate, R.drawable.info, "P1", INFO_PLACEHOLDER1));
		infos.add(new Info(infoTemplate, R.drawable.info, "P2", INFO_PLACEHOLDER2));
		infos.add(new Info(infoTemplate, R.drawable.info, "P3", INFO_PLACEHOLDER3));
		infos.add(new Info(infoTemplate, R.drawable.info, "P4", INFO_PLACEHOLDER4));

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
		float[] infoMatrix = new float[16];
		System.arraycopy( mMVPMatrix, 0, infoMatrix, 0, mMVPMatrix.length );

		for( Button b : getButtons())
			b.draw(mMVPMatrix);
		
		int max = getInfos().size();
		for( Info i : getInfos())
			i.draw(infoMatrix.clone(), i.getId(), max);

		leftBG.draw(mMVPMatrix, bgSquareColor);
		leftForward.draw(mMVPMatrix, leftPower>0?fgSquareColor:bgSquareColor);
		leftBackard.draw(mMVPMatrix, leftPower<0?blueColor:bgSquareColor);
		leftBalanceBG.draw(mMVPMatrix, bgSquareColor);

		rightBG.draw(mMVPMatrix, bgSquareColor);
		rightForward.draw(mMVPMatrix, rightPower>0?fgSquareColor:bgSquareColor);
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
	
	public ArrayList<Button> getButtons(){
		return buttons;
	}
	public Button getButton(int index){
		for( Button b : getButtons())
			if(b.getId() == index)
				return b;

		return null;
	}
	
	public ArrayList<Info> getInfos(){
		return infos;
	}
	public Info getInfo(int index){
		for( Info i : getInfos())
			if(i.getId() == index)
				return i;

		return null;
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

	class Info extends Button{
		
		private final float[] mTranslationMatrix = new float[16];
		private static final String TAG = "Info";
		private static final boolean flipTexture = false;

		public Info(float[] squareCoords, int background, String messageText, int id){
			super(squareCoords, background, messageText, null, id, flipTexture);
			setColor(MyGL20Renderer.greyColor);
		}
		
		public static final int NUMBER_OF_ROWS = 2;
		public static final float ROW_OFFSET = 1.45f;
		public static final float SPACING_OFFSET = 0.22f;
		public void draw(float[] mvpMatrix, int index, int max){
//			Matrix.translateM(mTranslationMatrix, 0, mvpMatrix, 0, 0.0f, 0.0f, 0.0f);
			int itemsPerRow = max / NUMBER_OF_ROWS;
			int rowNumber = (int) Math.floor( (float)index / itemsPerRow);
			int rowIndex = index - (rowNumber * itemsPerRow);
			Matrix.setIdentityM(mTranslationMatrix, 0);
			Matrix.translateM(mTranslationMatrix, 0, 
					(-(SPACING_OFFSET) + (rowIndex * SPACING_OFFSET)), // x axis 
					(-(ROW_OFFSET/NUMBER_OF_ROWS) + (rowNumber * ROW_OFFSET)) // y axis
					, 0.0f);
//			Log.e(TAG,itemsPerRow + ", " + rowNumber + ", " + rowIndex + ", " + index + ", " + max);

			Matrix.multiplyMM(mvpMatrix, 0, mTranslationMatrix, 0, mvpMatrix, 0);

			Matrix.setRotateM(mTranslationMatrix, 0, 180, 0, 0, -1.0f);
			
			//combine the rotation matrix with the projection and camera view
			
			Matrix.multiplyMM(mvpMatrix, 0, mTranslationMatrix, 0, mvpMatrix, 0);
			super.draw(mvpMatrix);
		}
		
		public void updateMessage(String message){
			this.updateMessage(message, false);
		}
	}
	
	class Button {
		private FloatBuffer vertexBuffer;
		private ShortBuffer drawListBuffer;
		private final int mProgram;
		private int mPositionHandle;
		private int mColorHandle;
		private int mMVPMatrixHandle;
		private static final String TAG = "Button";

		protected String messageText;
		protected int background;
		protected int mTextureDataHandle;

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

		private short drawOrder[]  = { 0, 1, 2, 0, 2, 3 }; //order to draw vertices

		//private final int vertexCount = squareCoords.length / COORDS_PER_VERTEX;
		private final int vertexStride = COORDS_PER_VERTEX * 4; //4 bytes per vertex
		
		//set the color with r,g,b,a values
		private float defaultColor[] = { 0.2f, 0.709803922f, 0.898039216f, 1.0f };
		
		private float touchDetection[];
		
		private int buttonId = -1;
		
		public float[] getTouchDetectionRange(){
			return touchDetection;
		}
		public int getId(){
			return buttonId;
		}
		
		protected void updateMessage(String message, boolean flipTexture){
			if(message != null && !message.equals(this.messageText)){
				this.messageText = message;
				this.updateText = true; 
				Log.e(TAG, "Updating Message: " + message);
			}
		}
		
		public Button(float[] squareCoords, int background, String buttonText, float[] touches, int id){
				this(squareCoords, background, buttonText, touches, id, squareCoords[0]<=0);
		}
		
		public Button(float[] squareCoords, int background, String buttonText, float[] touches, int id, boolean flipTexure){
			if(touches!=null)
				this.touchDetection = touches;
			this.buttonId = id;
			this.messageText = buttonText;
			this.background = background;
			
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
			
			mTextureDataHandle = loadTransparentTextureWithText(OpenGLES20Basic.getContext(), background, buttonText, flipTexure);
		}
		
		private int maTextureHandle;
		private int msTextureHandle;
		private float[] color;
		public void setColor(float[] c){
			color = c;
		}
		private boolean updateText = false;
		public void draw(float[] mvpMatrix){ //pass in the calc'd transformation matrix
			if(color == null)
				color = MyGL20Renderer.lightBlueColor;
			
			//add program to OpenGL ES env
			GLES20.glUseProgram(mProgram);
			
			if(this.updateText){
				this.updateText = false;
				this.mTextureDataHandle = loadTransparentTextureWithText(OpenGLES20Basic.getContext(), this.background, this.messageText, false, this.mTextureDataHandle);
			}
			
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
		}

		public static int loadTransparentTextureWithText(final Context context, final int resourceId, String text, boolean flipHorizontal) {
			return loadTransparentTextureWithText(context, resourceId,  text,  flipHorizontal, -1);
		}
		public static int loadTransparentTextureWithText(final Context context, final int resourceId, String text, boolean flipHorizontal, int existingHandle) {
			Log.e(TAG, "Loading texture: " + resourceId + ": " + text);

			final int[] textureHandle = new int[1];

			if(existingHandle != -1)
				textureHandle[0] = existingHandle;
			else
				GLES20.glGenTextures(1, textureHandle, 0);

			Log.e(TAG, "New texture: " + textureHandle[0]);
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

				if(text!=null){
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
				}
				    
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
				//flip it
				android.graphics.Matrix flip = new android.graphics.Matrix();
				flip.postScale(flipHorizontal?-1f:1f, -1f);
				bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(),flip, true);
				
				// Bind to the texture in OpenGL
				Log.e(TAG, "Binding texture" + textureHandle[0]);
				GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);

				if(existingHandle != -1){
					GLUtils.texSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, bitmap);
				} else {
					// Set filtering
					GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
					GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
					GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE); 
					GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

					// Load the bitmap into the bound texture.
					GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
					//GLES20.glTexImage2D ( GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, bitmap.getWidth(), bitmap.getHeight(), 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, byteBuffer );
				}
				
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
