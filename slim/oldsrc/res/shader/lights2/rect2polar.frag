#version 120

uniform sampler2D tex0;
const float PI = 3.14159265358979323846264;

void main(void) {
	vec2 texCoord = gl_TexCoord[0].st;
	
	//rectangular to polar
	vec2 norm = texCoord * 2.0 - 1.0;
	float theta = PI + atan(norm.x, norm.y);
	float r = length(norm);
	vec2 polar = vec2(theta/(2.0*PI), r);
	
	//sample like usual
	vec4 color = texture2D(tex0, polar);
	
	//just make it all white (for tinting)
	//anything not lit will be transparent
	gl_FragColor = vec4(1, 1, 1, color.a);
} 

