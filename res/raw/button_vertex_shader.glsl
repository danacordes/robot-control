/*
uniform mat4 uTMatrix;
uniform mat4 uMVPMatrix;
attribute vec4 aPosition;
attribute vec2 aTextureCoord;
varying vec2 vTextureCoord;

void main() {
    gl_Position = uMVPMatrix * uTMatrix * aPosition;
    vTextureCoord = aTextureCoord;
}
*/
uniform mat4 uMVPMatrix; 
attribute vec4 vPosition;
attribute vec2 aTextureCoord;
varying vec2 vTextureCoord;
void main() {
	gl_Position = vPosition * uMVPMatrix; 
    vTextureCoord = aTextureCoord;
}
