#version 330 core

in vec4 textColor;
in vec2 texCoords;

out vec4 outputColor;

uniform sampler2D glyphMap;


void main() {    
	outputColor = vec4(textColor.rgb, texture(glyphMap, texCoords).r * textColor.a);
}  