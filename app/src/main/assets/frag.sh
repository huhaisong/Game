
precision mediump float;
varying vec4 vColor;
//uniform sampler2D sTexture;
void main()                         
{           
   gl_FragColor =vColor; //texture2D(sTexture, vTextureCoord);
}              