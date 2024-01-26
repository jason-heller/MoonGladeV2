#version 330 core

layout (location = 0) in vec3 Vertex;

out vec3 color;

uniform mat4 projection;
uniform mat4 view;
uniform vec3 A;
uniform vec3 B;
uniform vec3 AC;
uniform vec3 BC;

void main() {
	vec3 vertex = vec3(0.0);
	
	if (mod(gl_VertexID,2) == 0) {
		vertex += A;
		color = AC;
	} else {
		vertex += B;
		color = BC;
	}

    gl_Position = projection * view * vec4(vertex, 1.0);
} 