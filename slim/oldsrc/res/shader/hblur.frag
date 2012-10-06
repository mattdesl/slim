#version 120
uniform sampler2D tex0;
uniform float renderTargetSize;
const float minBlur = 0.0;
const float maxBlur = 0.0;

void main() {
	vec4 sum = vec4(0.0);
	vec2 tc = gl_TexCoord[0].st;
	
	float distance = length(tc - 0.5f);
	float blur = mix(minBlur, maxBlur, distance) / renderTargetSize; 
	
	sum += texture2D(tex0, vec2(tc.x - 4.0*blur, tc.y)) * 0.05;
	sum += texture2D(tex0, vec2(tc.x - 3.0*blur, tc.y)) * 0.09;
	sum += texture2D(tex0, vec2(tc.x - 2.0*blur, tc.y)) * 0.12;
	sum += texture2D(tex0, vec2(tc.x - 1.0*blur, tc.y)) * 0.15;
	
	vec4 color = texture2D(tex0, vec2(tc.x, tc.y));
	sum += color * 0.16;
	
	sum += texture2D(tex0, vec2(tc.x + 1.0*blur, tc.y)) * 0.15;
	sum += texture2D(tex0, vec2(tc.x + 2.0*blur, tc.y)) * 0.12;
	sum += texture2D(tex0, vec2(tc.x + 3.0*blur, tc.y)) * 0.09;
	sum += texture2D(tex0, vec2(tc.x + 4.0*blur, tc.y)) * 0.05;
	
	gl_FragColor = vec4(sum);
}