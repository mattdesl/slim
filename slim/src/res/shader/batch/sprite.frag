#version 120

uniform sampler2D tex0;
varying vec3 vPosition;
varying vec4 vColor;
varying vec2 vTexCoord0;

void main(void) {
	vec4 c = texture2D(tex0, vTexCoord0);
	gl_FragColor = vColor;
} 

