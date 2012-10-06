#version 120

uniform sampler2D tex0;

varying vec2 TexCoord;
varying vec4 Color;

void main(void) {
	vec4 c = texture2D(tex0, TexCoord);
	gl_FragColor = Color * c;
} 

