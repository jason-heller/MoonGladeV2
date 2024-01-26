#version 330 core

in vec3 normal;
in vec2 atlasCoord;
in float visibility;

out vec4 outColor;

uniform sampler2D diffuseMap;


const vec3 skyColor = vec3(181.0/255.0, 154.0/255.0, 238.0/255.0);

void main() {
	// Handle texture
    vec4 albedo = texture(diffuseMap, atlasCoord);
    
    if(albedo.a < 0.8)
    	discard;
	
	// Output color
	vec3 withFog = mix(albedo.rgb, skyColor, visibility);
	withFog *= (0.5 + (dot(vec3(.5,.5,0), normal) * 0.5));

	outColor = vec4(withFog, 1.0);
}