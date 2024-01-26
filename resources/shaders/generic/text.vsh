#version 330 core

layout (location = 0) in vec4 Vertex;

out vec2 texCoords;
out vec4 textColor;

uniform mat4 projection;

float getColor(float v) {
	return float(floatBitsToInt(v) & 3);
}

void main() {
    gl_Position = vec4(Vertex.xy, 0.0, 1.0);
    texCoords = Vertex.zw;
    
    textColor = vec4(getColor(Vertex.y) / 3.0, getColor(Vertex.z) / 3.0, getColor(Vertex.w) / 3.0, (getColor(Vertex.x) / 4.0) + 0.25);
} 