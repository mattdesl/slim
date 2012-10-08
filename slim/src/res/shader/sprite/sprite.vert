uniform mat4 projMatrix;

attribute vec4 Color;
attribute vec2 TexCoord;
attribute vec2 Position;

varying vec4 vColor;
varying vec2 vTexCoord; 

void main() {
	vColor = Color;
	vTexCoord = TexCoord;
	gl_Position = projMatrix * vec4(Position.xy, 0, 1);
} 