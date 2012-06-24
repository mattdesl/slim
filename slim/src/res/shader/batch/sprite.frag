#version 120

uniform sampler2D tex0;
uniform sampler2D tex1;

//varying vec2 vPosition;
varying vec4 vColor;
varying vec2 vTexCoord;

void main(void) {
	vec4 grid = texture2D(tex0, vTexCoord);
	vec4 terrain = texture2D(tex1, vTexCoord);
	 
	//GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
	
	vec4 color = terrain + grid.a + (1.0 - terrain.a);
	
	gl_FragColor = vColor * color;
} 

