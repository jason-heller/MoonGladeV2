#version 330 core

in vec3 color;

out vec4 outputColor;

void main() {    
	outputColor = vec4(color.xyz, 1.0);
}  