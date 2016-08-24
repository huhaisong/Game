uniform mat4 uMVPMatrix;
attribute vec3 aPosition;
attribute vec4 aColor;
varying vec4 vColor;
//attribute vec2 aTexCoor;
//varying vec2 vTextureCoord;
void main()     
{
   gl_Position =vec4(aPosition,1);//uMVPMatrix * vec4(aPosition,1);
   vColor = aColor;
  // vTextureCoord = aTexCoor;
}