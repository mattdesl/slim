#version 120

uniform sampler2D tex0;

void main(void) {
	vec4 c = texture2D(tex0, gl_TexCoord[0].st);
	gl_FragColor = vec4(1-c.r, 1-c.g, 1.-c.b, c.a);
} 

