#version 120

//in values...
attribute vec2 SpriteTransform;
attribute vec4 SpriteTexCoords;
attribute vec4 SpriteColor;

//out values...
varying vec2 vSpriteTransform;
varying vec4 vSpriteColor;
varying vec4 vSpriteTexCoords;

void main() {
	vSpriteTransform = SpriteTransform;
	vSpriteTexCoords = SpriteTexCoords;
	vSpriteColor = SpriteColor;
	gl_Position = gl_Vertex;
} 