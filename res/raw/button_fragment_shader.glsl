/*
precision mediump float;
varying vec2 vTextureCoord;
uniform sampler2D sTexture;
void main() {
    gl_FragColor = texture2D(sTexture, vTextureCoord);
}
*/
precision mediump float; 
uniform vec4 vColor; 
varying vec2 vTextureCoord;
uniform sampler2D sTexture;
void main() { 
    gl_FragColor = vColor * texture2D(sTexture, vTextureCoord);
//	gl_FragColor = vColor; 
} 
