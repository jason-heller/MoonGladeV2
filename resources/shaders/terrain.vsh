#version 330 core

layout(location = 0) in vec3 Position;
layout(location = 1) in int TexCoord;
layout(location = 2) in vec3 Normal;

out vec3 normal;
out vec3 worldPos;
out vec2 atlasCoord;
out vec3 vertexColor;
out float visibility;

uniform sampler2D colorMap;
uniform mat4 projection;
uniform mat4 view;
uniform vec3 cameraPosition;
uniform vec4 offset;
uniform float scale;

const float DIP_SIZE = 16 * 2.001;

void main() {
	
	// Apply transform
	worldPos = vec3(Position);
	
	worldPos.x = worldPos.x * scale + offset.x;
	worldPos.y = worldPos.y * scale;
	worldPos.z = worldPos.z * scale + offset.y;
	
	// Dip under earlier terrain
	//float dipRange = (DIP_SIZE * scale) + scale;
	//if (scale != 1.0 && abs(worldPos.x - offset.z) <= dipRange && abs(worldPos.z - offset.w) <= dipRange)
	//	worldPos.y -= 0.25;

	normal = Normal;
	
	// Handle fog
	vec4 posRelativeToCamera = view * vec4(worldPos, 1.0);
	
	gl_Position = projection * posRelativeToCamera;	
	
	float distance = length(posRelativeToCamera.xyz);
	float visibility = exp(-pow(distance * .001, 100.0));
	visibility = clamp(visibility, 0.0, 1.0);
	
	atlasCoord.y = 1;
	
	//vertexColor = texelFetch(colorMap, ivec2(((TexCoord & 0xFF00) >> 8) / 4, ((TexCoord & 0xFF)) / 4), 0).rgb;
	vertexColor = vec3(((TexCoord & 0xFF0000) >> 16) / 255.0, ((TexCoord & 0xFF00) >> 8) / 255.0, ((TexCoord & 0xFF)) / 255.0);

	
}
