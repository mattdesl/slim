#version 120

uniform sampler2D tex0;

//varying vec2 vPosition;
varying vec4 vColor;
varying vec2 vTexCoord;

void main(void) {
	vec4 c = texture2D(tex0, vTexCoord);
	gl_FragColor = vColor * c;
} 

