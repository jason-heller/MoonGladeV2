package engine.gl.data;

import static org.lwjgl.opengl.GL11.GL_CLAMP;
import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_NEAREST_MIPMAP_NEAREST;
import static org.lwjgl.opengl.GL11.GL_RGB;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL11.GL_UNPACK_ALIGNMENT;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glDeleteTextures;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glPixelStorei;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.stb.STBImage.stbi_failure_reason;
import static org.lwjgl.stb.STBImage.stbi_image_free;
import static org.lwjgl.stb.STBImage.stbi_info_from_memory;
import static org.lwjgl.stb.STBImage.stbi_load_from_memory;
import static org.lwjgl.stb.STBImageResize.STBIR_ALPHA_CHANNEL_NONE;
import static org.lwjgl.stb.STBImageResize.STBIR_COLORSPACE_SRGB;
import static org.lwjgl.stb.STBImageResize.STBIR_EDGE_CLAMP;
import static org.lwjgl.stb.STBImageResize.STBIR_FILTER_MITCHELL;
import static org.lwjgl.stb.STBImageResize.STBIR_FLAG_ALPHA_PREMULTIPLIED;
import static org.lwjgl.stb.STBImageResize.stbir_resize_uint8_generic;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.memAlloc;
import static org.lwjgl.system.MemoryUtil.memFree;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.system.MemoryStack;

import engine.io.utils.IOUtil;

public class Texture2D implements ITexture {

	private final ByteBuffer pixelData;
	private final int w;
	private final int h;
	private final int comp; // Components

	private int textureID;
	
	public static Texture2D load(String path) {
		return new Texture2D(IOUtil.ioResourceToByteBuffer(path, 8 * 1024));
	}
	
	public static Texture2D load(ByteBuffer pixelData, int w, int h, int comp) {
		return new Texture2D(pixelData, w, h, comp);
	}

	private Texture2D(ByteBuffer imageBuffer) {
		try (MemoryStack stack = stackPush()) {
			IntBuffer w = stack.mallocInt(1);
			IntBuffer h = stack.mallocInt(1);
			IntBuffer comp = stack.mallocInt(1);

			if (!stbi_info_from_memory(imageBuffer, w, h, comp))
				throw new RuntimeException("Failed to read image information: " + stbi_failure_reason());

			// Decode the image
			pixelData = stbi_load_from_memory(imageBuffer, w, h, comp, 0);

			if (pixelData == null)
				throw new RuntimeException("Failed to load image: " + stbi_failure_reason());

			this.w = w.get(0);
			this.h = h.get(0);
			this.comp = comp.get(0);
		}

		textureID = createTexture();
	}
	
	private Texture2D(ByteBuffer pixelData, int w, int h, int comp) {
		if (pixelData == null)
			throw new NullPointerException("Texture data cannot be null");

		try (MemoryStack stack = stackPush()) {
			// Decode the image
			this.pixelData = pixelData;

			this.w = w;
			this.h = h;
			this.comp = comp;
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
	
	private void premultiplyAlpha() {
		int stride = w * 4;
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				int i = y * stride + x * 4;

				float alpha = (pixelData.get(i + 3) & 0xFF) / 255.0f;
				pixelData.put(i + 0, (byte) Math.round(((pixelData.get(i + 0) & 0xFF) * alpha)));
				pixelData.put(i + 1, (byte) Math.round(((pixelData.get(i + 1) & 0xFF) * alpha)));
				pixelData.put(i + 2, (byte) Math.round(((pixelData.get(i + 2) & 0xFF) * alpha)));
			}
		}
	}

	private int createTexture() {
        int texID = glGenTextures();
        
        int magFilter = GL_NEAREST;
        int minFilter = GL_NEAREST_MIPMAP_NEAREST;
        int wrap = GL_CLAMP;

		glBindTexture(GL_TEXTURE_2D, texID);
		int format;
		if (comp == 3) {
			if ((w & 3) != 0)
				glPixelStorei(GL_UNPACK_ALIGNMENT, 2 - (w & 1));
			format = GL_RGB;
		} else {
			// TODO: This makes world explode
			premultiplyAlpha();

			format = GL_RGBA;
		}
		
		glTexImage2D(GL_TEXTURE_2D, 0, format, w, h, 0, format, GL_UNSIGNED_BYTE, pixelData);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, magFilter);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, minFilter);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, wrap);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, wrap);

		ByteBuffer input_pixels = pixelData;
		int input_w = w;
		int input_h = h;
		int mipmapLevel = 0;
		while (1 < input_w || 1 < input_h) {
			int output_w = Math.max(1, input_w >> 1);
			int output_h = Math.max(1, input_h >> 1);

			ByteBuffer output_pixels = memAlloc(output_w * output_h * comp);
			stbir_resize_uint8_generic(input_pixels, input_w, input_h, input_w * comp, output_pixels, output_w,
					output_h, output_w * comp, comp, comp == 4 ? 3 : STBIR_ALPHA_CHANNEL_NONE,
					STBIR_FLAG_ALPHA_PREMULTIPLIED, STBIR_EDGE_CLAMP, STBIR_FILTER_MITCHELL, STBIR_COLORSPACE_SRGB);

			if (mipmapLevel == 0) {
				stbi_image_free(pixelData);
			} else {
				memFree(input_pixels);
			}

			glTexImage2D(GL_TEXTURE_2D, ++mipmapLevel, format, output_w, output_h, 0, format, GL_UNSIGNED_BYTE,
					output_pixels);

			input_pixels = output_pixels;
			input_w = output_w;
			input_h = output_h;
		}
		if (mipmapLevel == 0) {
			stbi_image_free(pixelData);
		} else {
			memFree(input_pixels);
		}
		return texID;

	}

	public int getID() {
		return textureID;
	}
}
