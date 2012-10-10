uniform sampler2D tex0;
uniform sampler2D tex1;

uniform vec3 light;

vec3 ambientColor = vec3(1.0, 1.0, 1.0); 
float ambientIntensity = 0.1; 
vec2 resolution = vec2(1024.0, 600.0);
vec3 lightColor = vec3(1.0, 1.0, 1.0);

void main() {
	vec4 color = texture2D(tex0, gl_TexCoord[0].st);
	vec3 normal = normalize(texture2D(tex1, gl_TexCoord[0].st).rgb * 2.0 - 1.0);
	vec3 light_pos = normalize(light);
	float lambert = max(dot(normal, light_pos), 0.0);
	
	//now let's get a nice little falloff
	float d = distance(gl_FragCoord.xy, light.xy * resolution);
	d *= light.z;
	
	float aConst = 0.5;
	float aLin = 0.0;
	float aQuad = 0.00001;
	
	float att = 1.0 / ( aConst + (aLin*d) + (aQuad*d*d) );
	
	//float att = max(1.0-dist/300.0, 0.0);
	
	vec3 result = (ambientColor * ambientIntensity) + (lightColor.rgb * lambert) * att;
	result *= color.rgb;
	
	// Color = Ambient + Diffuse * Attenuation
		//Ambient = (AmbientColor * AmbientIntensity)
		//Diffuse = (DiffuseMaterial * DiffuseLight * LambertFactor)
		//LambertFactor = dot(Normals, LightPos)
		//Attenuation = [not realistic]
		
	gl_FragColor = vec4(result, color.a);
} 