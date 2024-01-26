#version 330 core

in vec3 worldPos;
in vec3 vertexColor;
in vec3 normal;
in vec2 atlasCoord;
in float visibility;

out vec4 outColor;

uniform sampler2D diffuseMap;

const vec3 GROUND_COLOR = vec3(173.0 / 255.0, 154.0 / 255.0, 127.0 / 255.0);
const vec3 skyColor = vec3(181.0/255.0, 154.0/255.0, 238.0/255.0);
const float ATLAS_SCALE = 0.125;

void main() {
	// Handle texture
    vec3 absNormal = abs(normal);
    vec4 albedo = vec4(0.0);
    vec2 uvCoords = vec2(0.0);
    vec3 terrainColor = vertexColor;
    float atlasOffsetY = 0.0;
    
    if (absNormal.x >= absNormal.z && absNormal.x >= absNormal.y) {
    	uvCoords = mod(worldPos.zy , 1.0);
    }
    else if (absNormal.y >= absNormal.x && absNormal.y >= absNormal.z) {
        uvCoords = mod(worldPos.xz, 1.0);
 		atlasOffsetY = -sign(normal.y);
 		
    }
    else if (absNormal.z >= absNormal.x && absNormal.z >= absNormal.y) {
        uvCoords = mod(worldPos.xy, 1.0);
    }
    
    terrainColor = mix(GROUND_COLOR, vertexColor, normal.y);
    
    albedo = texture(diffuseMap, ((uvCoords + vec2(atlasCoord.x, atlasCoord.y + atlasOffsetY)) * ATLAS_SCALE));
    albedo.rgb *= terrainColor;
	
	// Output color
	vec3 withFog = mix(albedo.rgb, skyColor, visibility);
	withFog *= (0.5 + (dot(vec3(.5,.5,0), normal) * 0.5));

	outColor = vec4(withFog, 1.0);
}