#version 330 core

layout(location = 0) in vec3 Position;
layout(location = 1) in vec2 TexCoord;
layout(location = 2) in vec3 Normal;

out vec3 normal;
out vec2 atlasCoord;
out float visibility;

uniform mat4 projection;
uniform mat4 view;
uniform vec4 offset;
uniform float scale;

void main() {
	normal = Normal;
	atlasCoord = TexCoord;
	
	// Handle fog
	vec4 posRelativeToCamera = view * vec4(Position, 1.0);
	
	gl_Position = projection * posRelativeToCamera;	
	
	float distance = length(posRelativeToCamera.xyz);
	float visibility = exp(-pow(distance * .001, 100.0));
	visibility = clamp(visibility, 0.0, 1.0);

	
}
