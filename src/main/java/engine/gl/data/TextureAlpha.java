package engine.gl.data;

import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_RED;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glDeleteTextures;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.stb.STBImage.stbi_failure_reason;
import static org.lwjgl.stb.STBImage.stbi_info_from_memory;
import static org.lwjgl.stb.STBImage.stbi_load_from_memory;
import static org.lwjgl.system.MemoryStack.stackPush;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.system.MemoryStack;

import engine.io.utils.IOUtil;

public class TextureAlpha implements ITexture {

	private final ByteBuffer pixelData;
	private final int w;
	private final int h;

	private int textureID;
	
	public static TextureAlpha load(String path) {
		return new TextureAlpha(IOUtil.ioResourceToByteBuffer(path, 8 * 1024));
	}
	
	public static TextureAlpha load(ByteBuffer pixelData, int w, int h, int comp) {
		return new TextureAlpha(pixelData, w, h, comp);
	}

	private TextureAlpha(ByteBuffer imageBuffer) {
		try (MemoryStack stack = stackPush()) {
			IntBuffer w = stack.mallocInt(1);
			IntBuffer h = stack.mallocInt(1);
			IntBuffer comp = stack.mallocInt(1);

			if (!stbi_info_from_memory(imageBuffer, w, h, comp))
				throw new RuntimeException("Failed to read image information: " + stbi_failure_reason());

			pixelData = stbi_load_from_memory(imageBuffer, w, h, comp, 0);

			if (pixelData == null)
				throw new RuntimeException("Failed to load image: " + stbi_failure_reason());
			
			if (comp.get(0) != 1)
				throw new RuntimeException("Failed to load image: too many components");

			this.w = w.get(0);
			this.h = h.get(0);
		}

		textureID = createTexture();
	}
	
	private TextureAlpha(ByteBuffer pixelData, int w, int h, int comp) {
		if (pixelData == null)
			throw new NullPointerException("Texture data cannot be null");

		try (MemoryStack stack = stackPush()) {
			// Decode the image
			this.pixelData = pixelData;

			this.w = w;
			this.h = h;
		}

		textureID = createTexture();
	}

	public void bind() {
		glBindTexture(GL_TEXTURE_2D, textureID);
	}

	public void unbind() {
		glBindTexture(GL_TEXTURE_2D, 0);
	}

	public void destroy() {
		glDeleteTextures(textureID);
	}
	
	private int createTexture() {
        int texID = glGenTextures();
        
		glBindTexture(GL_TEXTURE_2D, texID);

		glTexImage2D(GL_TEXTURE_2D, 0, GL_RED, w, h, 0, GL_RED, GL_UNSIGNED_BYTE, pixelData);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        
        return texID;
	}

	public int getID() {
		return textureID;
	}
}
