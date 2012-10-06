#version 120

uniform sampler2D tex0;

void main(void) {
	vec2 tc = gl_TexCoord[0].st;
	vec4 c = texture2D(tex0, tc);
	gl_FragColor = c;
}