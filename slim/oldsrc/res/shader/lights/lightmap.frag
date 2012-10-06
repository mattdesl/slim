#version 120

uniform sampler2D tex0;
uniform float pixelBias; //e.g. 4 / 512 => px / renderTargetSize 
uniform vec4 tint; //lighting tint color
const float PI = 3.14159265358979323846264;

void main(void) {
	vec2 texCoord = gl_TexCoord[0].st;
	vec4 color = texture2D(tex0, texCoord);
	
	//distance from top
	float dist = texCoord.t;
	
	//give it a slight bias of a couple pixels, for a nicer effect
	dist -= pixelBias;
	
	//if we haven't yet reached our shadow caster, make it white
	float lit = dist < color.r ? 1.0 : 0.0;
	
	gl_FragColor = vec4(1, 1, 1, lit);
} 

