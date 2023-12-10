#version 330 core

layout(location = 0) in float heights;
layout(location = 1) in uint eulerNormal;

uniform mat4 projMatrix;
uniform mat4 viewMatrix;

uniform vec2 offset;
uniform float scale;

out vec3 color;

const float VERTS_PER_RUN = 20;
const float VERTS_PER_RUN_CLAMPED = 17;

void main() {
	// Clamping
	float rowIndex = mod(gl_VertexID, VERTS_PER_RUN);
	float indexClamped = clamp(rowIndex - 1.0, 0.0, VERTS_PER_RUN_CLAMPED);

	vec3 position = vec3(floor(indexClamped / 2.0), heights, 1.0 - mod(indexClamped, 2.0));

	color = vec3(mod(position.x, 8)/8.0, mod(position.z + floor(gl_VertexID / VERTS_PER_RUN), 8)/8.0, 1.0);

	position.x *= scale;
	position.z *= scale;

	position.x += offset.x;
	position.z += offset.y + (floor(gl_VertexID / VERTS_PER_RUN) * scale);

	position.y -= scale;

	gl_Position = projMatrix * viewMatrix * vec4(position, 1.0);
}
