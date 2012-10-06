#version 120

uniform mat4 projMatrix;
uniform mat4 viewMatrix;

//in
attribute vec4 Color;
attribute vec2 TexCoord;
attribute vec2 Position;

//out
//varying vec2 vPosition;
varying vec4 vColor;
varying vec2 vTexCoord; 

void main() {
	//vPosition = Position;
	vColor = Color;
	vTexCoord = TexCoord;
	gl_Position = projMatrix * viewMatrix * vec4(Position.xy, 0, 1);
} 