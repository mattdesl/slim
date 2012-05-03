#version 120

uniform sampler2D tex0;
uniform float renderTargetSize;
const float PI = 3.14159265358979323846264;

void main(void) {
	vec2 texCoord = gl_TexCoord[0].st;
	vec2 norm = texCoord * 2.0 - 1.0;
	
	// center = 0, because cords are -1 to 1 so we remove it
	float theta = PI + atan(norm.x, norm.y);
	float r = length(norm);
	
	vec2 polar = vec2((theta/(2.0*PI)), (r));
	vec4 color = texture2D(tex0, polar);
	
	float c = color.r;
	
	//float distance = length(texCoord - 0.5);
	gl_FragColor = vec4(1, 1, 1, color.a);
} 

