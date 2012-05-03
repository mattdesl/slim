//define our sampler2D object, i.e. texture
uniform sampler2D tex0;
uniform vec2 texSize; 
uniform float scale;

void main() {
	float textureSize = texSize.x * scale;
	float texelSize = texSize.y * scale;
	vec2 uv = gl_TexCoord[0].st;
    vec4 tl = texture2D(tex0, uv);
    vec4 tr = texture2D(tex0, uv + vec2(texelSize, 0));
    vec4 bl = texture2D(tex0, uv + vec2(0, texelSize));
    vec4 br = texture2D(tex0, uv + vec2(texelSize , texelSize));
    vec2 f = fract( uv.xy * textureSize ); // get the decimal part
    vec4 tA = mix( tl, tr, f.x ); // will interpolate the red dot in the image
    vec4 tB = mix( bl, br, f.x ); // will interpolate the blue dot in the image
	gl_FragColor = mix( tA, tB, f.y ); // will interpolate the green dot in the image
	
}