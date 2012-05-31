#version 120

uniform mat4 viewMatrix;
uniform mat4 projMatrix;

//in
attribute vec3 Position;
attribute vec4 Color;
attribute vec2 TexCoord;
attribute vec4 MyAttrib;
attribute vec4 RealAttrib;

//out
varying vec3 vPosition;
varying vec4 vColor;
varying vec2 vTexCoord; 

void main() {
	//vPosition = Position;
	vColor = Color * RealAttrib;
	vTexCoord = TexCoord;
	gl_Position = projMatrix * viewMatrix * vec4(Position.xyz, 1);
} 