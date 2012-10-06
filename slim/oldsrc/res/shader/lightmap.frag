#version 120

uniform sampler2D tex0;
uniform float renderTargetSize;
const float PI = 3.14159265358979323846264;

void main(void) {
	vec2 texCoord = gl_TexCoord[0].st;
	vec4 color = texture2D(tex0, texCoord);
	
	float dist = texCoord.t;
	dist -= 4/renderTargetSize; //4 pixel bias
	float lit = dist < color.r ? 1.0 : 0.0;
	gl_FragColor = vec4(1, 1, 1, lit);
} 

