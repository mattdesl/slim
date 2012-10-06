#version 120

uniform sampler2D tex0;
uniform float renderTargetSize;
const float PI = 3.14159265358979323846264;

void main(void) {
	vec2 texCoord = gl_TexCoord[0].st;
	
	float dist = length(texCoord - 0.5f);
	
	vec2 norm = texCoord * 2.0 - 1.0;
	
	float theta = PI + norm.x * PI;
	float r = (1.0 + norm.y) * 0.5 ;
	
	vec2 cart = vec2(-r * sin(theta), -r * cos(theta));
	cart = ( (cart/2.0) + 0.5);
	
	vec4 color = texture2D(tex0, cart);
	float distColor = color.a>0.3f ? texCoord.t : 1.0f;
	
	gl_FragColor = vec4(distColor, distColor, distColor, 1.0);
} 







	
	