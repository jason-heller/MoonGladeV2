package engine.gl.text;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.PatternSyntaxException;

import org.joml.Vector2f;

import engine.Window;
import engine.gl.ICamera;
import engine.gl.Shader;
import engine.gl.data.PersistentMesh;
import engine.gl.data.TextureAlpha;
import engine.gl.text.BitmapFont.Glyph;

public class TextRenderer {
	
	private static final List<TextData> text = new LinkedList<>();	// All text to draw
	
	private Shader shader;
	private BitmapFont bitmapFont;
	
	private byte[] vertexData;
	private PersistentMesh textMesh;
	
	private int lastBufferSize = 0;
	
	private int vPos;
	
	private static final byte[] opacities = new byte[] {(byte)0x00, (byte)0x3F, (byte)0x7F, (byte)0xBF, (byte)0xFF};

	private static final char CMD_REGEX_START = '<';
	private static final char CMD_REGEX_END = '>';
	private static final String ARGS_REGEX = "=";
	private static final String ARG_REGEX = ",";
	
	private static final Logger logger = Logger.getLogger(TextRenderer.class.getName());
	
	public TextRenderer() {
		shader = new Shader("shaders/generic/text.vsh", "shaders/generic/text.fsh");
		bitmapFont = new BitmapFont("data/Lucida Console.ttf", 40);
		
		vertexData = new byte[4 * TextMesh.NUM_BYTES];
		textMesh = new TextMesh();
	}
	
	public static TextData putText(String string, Vector2f pos, float scale, TextColor color) {
		TextData tr = new TextData(string, pos.x, pos.y, scale, color);
		text.add(tr);
		return tr;
	}

	public void render(ICamera camera) {
		TextureAlpha fontTexture = bitmapFont.getTexture();

		updateVertexData();
		
        glEnable(GL_BLEND);
		
		shader.bind();
		shader.setUniform("projection", camera.getProjectionMatrix());
		shader.setTexture("glyphMap", 0, fontTexture);
		
		textMesh.setBufferSubData(0, 0, vertexData);
		textMesh.update();

		textMesh.bind();
		glEnableVertexAttribArray(0);
		glDrawArrays(GL_TRIANGLES, 0, TextMesh.NUM_VERTICES);
		glDisableVertexAttribArray(0);
		textMesh.unbind();
		
		fontTexture.unbind();
		shader.unbind();
		
		glDisable(GL_BLEND);
		
		text.clear();
    }
	
	private void updateVertexData() {
		vPos = 0;
		
		for(TextData tr : text) {
			boolean isRightAligned = tr.isRightAligned();
			boolean isBottomAligned = tr.isBottomAligned();
			float x = tr.x, y = tr.y;
			float dx = 0;
			float dy = isBottomAligned ? -bitmapFont.getFontHeight() / Window.height : 0;
			String string = tr.string;
			float scale = tr.scale;
			
			byte color = tr.color.getARGB();
			byte opacity = opacities[4];
			boolean ignoreRegex = false;		// Allows for typing command regex without starting a command
			
			int vPosStart = vPos;
			
			for(int i = 0; i < string.length(); i++) {
				char c = string.charAt(i);
				
				if (x + dx > 1f && !isRightAligned) {
					dx = 0;
					dy -= bitmapFont.getFontHeight() * scale / Window.height;
					vPosStart = vPos;
				} else if (x - dx < -1f && isRightAligned) {
					alignRight(vPosStart, x, dx);
					
					dx = 0;
					dy -= bitmapFont.getFontHeight() * scale / Window.height;
					vPosStart = vPos;
				}
				
				if (c == '\n') {
					if (isRightAligned)
						alignRight(vPosStart, x, dx);
					
					dx = 0;
					dy -= bitmapFont.getFontHeight() * scale / Window.height;
					vPosStart = vPos;
					ignoreRegex = false;
					continue;
				} else if (c == '\\') {
					ignoreRegex = true;
					continue;
				} else if (c == '\t' || c == '\b') {
					continue;
				} if (c == CMD_REGEX_START && !ignoreRegex) {
					// Command
					++i;
					String cmd = "";
					
					while(i < string.length() && (c = string.charAt(i)) != CMD_REGEX_END) {
						if (c != ' ')
							cmd += Character.toUpperCase(c);
						++i;
					}
					
					if (cmd.length() == 0)
						continue;
					
					int regexLoc = cmd.indexOf(ARGS_REGEX);
					String[] args = getArgs(cmd);
					String cmdName = regexLoc == -1 ? cmd : cmd.substring(0, regexLoc);
					
					switch(cmdName) {
					case "BR":
						if (isRightAligned)
							alignRight(vPosStart, x, dx);
						
						dx = 0;
						dy -= bitmapFont.getFontHeight() * scale / Window.height;
						vPosStart = vPos;
						ignoreRegex = false;
						
						break;
					case "ALPHA":
					case "A":
						if (!checkArgs(cmdName, args, 1))
							break;
						
						if (args[0].length() != 1) {
							textCommandErr("Illegal argument for <ALPHA>: " + args[0] + ", Text opacity must be a number within the range [0, 4]");
							break;
						}
						
						int opacityIndex = args[0].charAt(0) - '0';
						if (opacityIndex < 0 || opacityIndex > 4) {
							textCommandErr("Illegal argument for <ALPHA>: " + args[0] + ", Text opacity must be a number within the range [0, 4]");
							break;
						}
						
						opacity = opacities[opacityIndex];
						break;
					case "SCALE":
					case "S":
						if (!checkArgs(cmdName, args, 1))
							break;
						
						float newScale = getFloatArg(cmdName, args[0]);
						scale = Float.isNaN(newScale) ? scale : newScale;
						break;
					case "POSITION":
					case "POS":
						if (!checkArgs(cmdName, args, 2))
							break;
						
						float newX = getFloatArg(cmdName, args[0]);
						float newY = getFloatArg(cmdName, args[1]);
						
						if (!Float.isNaN(newX) && !Float.isNaN(newY)) {
							x = newX;
							y = newY;
							dx = 0;
							dy = 0;	// tr.isBottomAligned ? 0 : -bitmapFont.getFontHeight() / Window.height
						}
						break;
					/*case "DECIMAL":
						if (!checkArgs(cmdName, args, 2))
							break;
						try {
							double d = Double.valueOf(args[0]);
							string = string.substring(0, i + 1) + String.format("%.0"+Integer.parseInt(args[1])+"f", d) + string.substring(i + 1);
						} catch (NumberFormatException e)  {
							textCommandErr("Illegal argument for <DECIMAL>: " + args[0] + ", must be a double, " + args[1] + " must be an int");
						}
						break;*/
					case "HALIGN":
					case "HA":
						if (!checkArgs(cmdName, args, 1))
							break;
						if (args[0].equals("RIGHT"))
							isRightAligned = true;
						else if (args[0].equals("LEFT"))
							isRightAligned = false;
						else 
							textCommandErr("Unknown argument for <HALIGN>: " + args[0] + ", only RIGHT and LEFT are permitted");
						break;
					case "VALIGN":
					case "VA":
						if (!checkArgs(cmdName, args, 1))
							break;
						if (args[0].equals("BOTTOM")) {
							dy -= !isBottomAligned ? bitmapFont.getFontHeight() / Window.height : 0f;
							isBottomAligned = true;
						} else if (args[0].equals("TOP")) {
							dy += isBottomAligned ? bitmapFont.getFontHeight() / Window.height : 0f;
							isBottomAligned = false;
						} else {
							textCommandErr("Unknown argument for <VALIGN>: " + args[0] + ", only TOP and BOTTOM are permitted");
						}
						break;
					default:
						// Assume its a color
						TextColor newColor = null;
						
						try {
							newColor = TextColor.valueOf(cmd);
						} catch(IllegalArgumentException e) {
							textCommandErr("Unknown text command: " + cmd);
						}
						
						if (newColor != null) {
							color = newColor.getARGB();
						} else {
							// Unknown argument
							textCommandErr("Unknown text command: " + cmd);
						}
					}
					
					continue;
				}
				
				ignoreRegex = false;
				Glyph glyph = bitmapFont.getGlyph(c);
				
				if (opacity != 0)
					addGlyph(glyph, x + dx, y + dy, scale, (byte)(color & opacity));
				
				dx += ((glyph.width + bitmapFont.getPadding()) * scale) / Window.width;
			}
			
			if (isRightAligned)
				alignRight(vPosStart, x, dx);
		}

		int bufferSize = vPos;
		
		// If we are smaller than last time, zero the remainder out
		for(int i = lastBufferSize - vPos; i > 0; i--)
			vertexData[vPos++] = 0;
		
		lastBufferSize = bufferSize;
	}

	/** Takes the last line generated and shifts it to be right aligned, can be a bit expensive. Try to limit this happening
	 * @param vertexOffset Offset into the origin of this line in the vertex data
	 * @param xOrigin the insertion point's origin
	 * @param xOffset the insertion point's offset
	 */
	private void alignRight(int vertexOffset, float xOrigin, float xOffset) {
		shiftVertexData(vertexOffset, -xOffset);
	}

	private void shiftVertexData(int vertexOffset, float offset) {
		for (int v = vertexOffset; v < vPos; v += 16) {
			byte[] bytes = { vertexData[v], vertexData[v + 1], vertexData[v + 2], vertexData[v + 3] };
			float origValue = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getFloat();
			origValue += offset;

			int colorComponent = vertexData[v] & 3;
			int intBits = Float.floatToIntBits(origValue);

			vertexData[v] = (byte) ((intBits & ~3) | colorComponent);
			vertexData[v + 1] = (byte) (intBits >> 8);
			vertexData[v + 2] = (byte) (intBits >> 16);
			vertexData[v + 3] = (byte) (intBits >> 24);
		}
	}

	private boolean checkArgs(String cmd, String[] args, int i) {
		if (args == null || args.length != i) {
			textCommandErr("<" + cmd + "> requires " + i + " argument(s)");
			return false;
		}
		return true;
	}

	private float getFloatArg(String cmd, String arg) {
		float f = Float.NaN;
		try {
			f = Float.parseFloat(arg);
		} catch (Exception e) {
			textCommandErr("Illegal argument for <" + cmd + ">: " + arg + " must be a real number");

		}
		return f;
	}

	private void textCommandErr(String string) {
		logger.warning(string);
	}

	private String[] getArgs(String cmd) {
		String[] args;
		try {
			String[] cmdSplit = cmd.split(ARGS_REGEX);
			String rhs = (cmdSplit.length == 1) ? cmd : cmdSplit[1];
			args = rhs.split(ARG_REGEX);
		} catch(PatternSyntaxException e) {
			return null;
		}
		
		return args;
	}

	private void addGlyph(Glyph glyph, float x, float y, float scale, byte color) {
		
		final float x1 = ((glyph.x1 / Window.width) * scale) + x;
		final float y1 = ((glyph.y1 / Window.height) * scale) + y;
		final float x2 = ((glyph.x2 / Window.width) * scale) + x;
		final float y2 = ((glyph.y2 / Window.height) * scale) + y;
		
		final float s1 = glyph.s1, t1 = glyph.t1;
		final float s2 = glyph.s2, t2 = glyph.t2;
		
		addVertex(x1, y1, s1, t1, color);
		addVertex(x2, y2, s2, t2, color);
		addVertex(x2, y1, s2, t1, color);
		
		addVertex(x2, y2, s2, t2, color);
		addVertex(x1, y1, s1, t1, color);
		addVertex(x1, y2, s1, t2, color);
	}

	private void addVertex(float x, float y, float s, float t, byte color) {
		addFloat(x, (color & 0xC0) >> 6);	// A
		addFloat(y, (color & 0x30) >> 4);	// R
		addFloat(s, (color & 0x0C) >> 2);	// G
		addFloat(t, color & 0x03);		// B
	}
	
	private void addFloat(float value, int colorComponent) {
		int intBits =  Float.floatToIntBits(value);

		vertexData[vPos++] = (byte) ((intBits & ~3) | colorComponent);
		vertexData[vPos++] = (byte) (intBits >> 8);
		vertexData[vPos++] = (byte) (intBits >> 16);
		vertexData[vPos++] = (byte)  (intBits >> 24);
	}

	public void destroy() {
		bitmapFont.destroy();
		shader.destroy();
	}
}