uniform sampler2D tex0;
varying vec4 vColor;
varying vec2 vTexCoord;

void main(void) {
	vec4 tex = texture2D(tex0, vTexCoord);
	gl_FragColor = vColor * tex;
} 

