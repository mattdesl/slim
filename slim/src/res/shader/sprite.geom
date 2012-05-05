#version 120

//in values from vert shader...
varying in vec2 vSpriteTransform[];
varying in vec4 vSpriteColor[];
varying in vec4 vSpriteTexCoords[];

//out values to frag shader...
varying out vec2 TexCoord;
varying out vec4 Color;

void main() {
	mat4 gVP = gl_ModelViewProjectionMatrix;
	
	vec2 pos = gl_PositionIn[0].xy;
	vec2 size = vSpriteTransform[0].xy;
	vec4 tex = vSpriteTexCoords[0]; 
		
	//out to frag...
	Color = vSpriteColor[0];
	
	gl_Position = gVP * vec4(pos.x, pos.y, 0, 1); //top left
	TexCoord = vec2(tex.x, tex.y);
	EmitVertex();
	
	gl_Position = gVP * vec4(pos.x+size.x, pos.y, 0, 1); //top right
	TexCoord = vec2(tex.x+tex.z, tex.y);
	EmitVertex();
	
	gl_Position = gVP * vec4(pos.x, pos.y+size.y, 0, 1); //bottom left
	TexCoord = vec2(tex.x, tex.y+tex.w);
	EmitVertex();
	
	gl_Position = gVP * vec4(pos.x+size.x, pos.y+size.y, 0, 1); //bottom right
	TexCoord = vec2(tex.x+tex.z, tex.y+tex.w);
	EmitVertex();
	
	EndPrimitive();
} 