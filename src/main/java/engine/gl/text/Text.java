package engine.gl.text;

import org.joml.Vector2f;

public class Text {
	
	// Wrapper class for convenience
	
	
	// Text can have additional commands in them to control how they render. Format is as such:
	// <command = arg0, arg1, arg2>
	//
	// Commands ignore whitespace, are not case sensitive, and will simply not run if the syntax is incorret
	// Here is the command list:

	/* 
	* <scale = X> or <s = X> 			: Sets the scale of the charactors following the command to X
	*
	* <alpha = X> or "<a = X> 			: Sets the alpha of the text, must be within the range of [0, 4], where 0 is 0% opacity, and 4 is 100% opacity
	* 
	* <position = X, Y> or <pos = X, Y> : Sets the insertion point of the following text to X, Y, where (0, 0) is the center of the screen, and (-1, -1) is the bottom left
	* 
	* <halign = X> or <ha = X>			: Sets the horizontal alignment of the text, permitted values are LEFT and RIGHT
	* 
	* <valign = X> or <va = X>			: Sets the horizontal alignment of the text, permitted values are TOP and BOTTOM
	*
	* <br> 								: Moves text to newline, functionally equivalent to '\n'
	* 
	* <X> 								: Sets the new color of the text to X, colors can be found under the TextColor enum. 
	*/
		
	
	

	public static TextData draw(String string, float x, float y) {
		return draw(string, x, y, TextColor.WHITE);
	}	
	
	public static TextData draw(String string, float x, float y, TextColor color) {
		return draw(string, new Vector2f(x, y), 1f, color);
	}	
	
	public static TextData draw(String string, float x, float y, float scale, TextColor color) {
		return draw(string, new Vector2f(x, y), scale, color);
	}
	
	public static TextData draw(String string, Vector2f pos, float scale, TextColor color) {
		return TextRenderer.putText(string, pos, scale, color);
	}
}

