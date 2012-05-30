#version 120

uniform mat4 viewMatrix;
uniform mat4 projMatrix;

//in
attribute vec3 Position;  //location=0
attribute vec4 Color;     //location=1
attribute vec2 TexCoord0; //location=2

//out
varying vec3 vPosition;
varying vec4 vColor;
varying vec2 vTexCoord0; 

void main() {
	vPosition = Position;
	vColor = Color;
	vTexCoord0 = TexCoord0;
	gl_Position = projMatrix * viewMatrix * vec4(Position.xyz, 1);
} 