#version 120

uniform sampler2D tex0;
uniform float renderTargetSize;

vec4 distance(vec4 color, vec2 texCoord) {
	float distance = color.a>0.3f ? length(texCoord - 0.5f) : 1.0f;
	distance *= renderTargetSize;
	return vec4(distance, 0, 0, 1);
}



vec4 distort(vec2 texCoord) {
	  //translate u and v into [-1 , 1] domain
	  float u0 = texCoord.x * 2 - 1;
	  float v0 = texCoord.y * 2 - 1;
	  
	  //then, as u0 approaches 0 (the center), v should also approach 0 
	  v0 = v0 * abs(u0);

      //convert back from [-1,1] domain to [0,1] domain
	  v0 = (v0 + 1) / 2;

	  //we now have the coordinates for reading from the initial image
	  vec2 newCoords = vec2(texCoord.x, v0);
		
	  //read for both horizontal and vertical direction and store them in separate channels
	  float horizontal = texture2D(tex0, newCoords).x;
	  float vertical = texture2D(tex0, newCoords.yx).x;
      return vec4(horizontal, vertical, 0, 1);
}

vec4 distort2(vec2 texCoord) {
	  //translate u and v into [-1 , 1] domain
	  
	  vec2 newCoords = vec2(texCoord.x, texCoord.y);
	  float horizontal = texture2D(tex0, newCoords).r;
	  float vertical = texture2D(tex0, newCoords.yx).r;
      return vec4(horizontal, 0, 0, 1);
}

void main(void) {
	vec2 texCoord = gl_TexCoord[0].st;
	
	//translate u and v into [-1 , 1] domain
	float u0 = texCoord.x * 2 - 1;
	float v0 = texCoord.y * 2 - 1;
	  
	//then, as u0 approaches 0 (the center), v should also approach 0 
	v0 = v0 * abs(u0);
	
	//convert back from [-1,1] domain to [0,1] domain
	v0 = (v0 + 1) / 2;
	
	//we now have the coordinates for reading from the initial image
	vec2 newCoords = vec2(texCoord.x, v0);
	
	float dist = length(texCoord - 0.5f);
	
	//grab the distance
	vec4 colorH = texture2D(tex0, newCoords);
	float distanceH = colorH.a>0.3f ? dist : 1.0f;
	vec4 colorV = texture2D(tex0, newCoords.yx);
	float distanceV = colorV.a>0.3f ? dist : 1.0f;
	
	gl_FragColor = vec4(distanceH, distanceV, 0, 1);
} 

