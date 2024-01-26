package engine.gl.text;

import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBTTAlignedQuad;
import org.lwjgl.stb.STBTTBakedChar;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTruetype;
import org.lwjgl.system.MemoryUtil;

import engine.gl.data.TextureAlpha;
import engine.io.utils.IOUtil;
 
// TODO: Orange451 bruh Global Anarchy 3 when

public class BitmapFont {
    private static final int BITMAP_WIDTH = 512;
    private static final int BITMAP_HEIGHT = 512;
    private static final int FIRST_CHAR = 32;
    private static final int NUM_CHARS   = 96;
 
    private int ascent;
    private int descent;
    private int lineGap;
 
    private float height;
    private float padding = 2f;
    private Glyph[] glyphs;
    private ByteBuffer bitmap;
 
    private TextureAlpha texture;
    
    public BitmapFont(String fontFile, float height) {
        this.height = height;
        this.glyphs = new Glyph[NUM_CHARS];
        STBTTBakedChar.Buffer cdata = STBTTBakedChar.malloc(NUM_CHARS);

        try {
            // Load the font
            ByteBuffer ttf = IOUtil.ioResourceToByteBuffer(fontFile, 512 * 1024);
            
            if (ttf == null)
                throw new FileNotFoundException("Font not found " + fontFile);
            
            // Generate font-bitmap data.
            bitmap = MemoryUtil.memAlloc(BITMAP_WIDTH * BITMAP_HEIGHT);
            STBTruetype.stbtt_BakeFontBitmap(ttf, getFontHeight(), bitmap, BITMAP_WIDTH, BITMAP_HEIGHT, FIRST_CHAR, cdata);
            
            //STBImageWrite.stbi_write_png("Resources/test.png", BITMAP_WIDTH, BITMAP_HEIGHT, 1, bitmap, 0);
 
            // Get font metrics
            final STBTTFontinfo info = STBTTFontinfo.create();
            
            if (!STBTruetype.stbtt_InitFont(info, ttf)) {
            	MemoryUtil.memFree(bitmap);
                throw new IllegalStateException("Failed to initialize font information.");
            }
            
            float scale = STBTruetype.stbtt_ScaleForPixelHeight(info, height);
            
            IntBuffer ascent = BufferUtils.createIntBuffer(1);
            IntBuffer descent = BufferUtils.createIntBuffer(1);
            IntBuffer lineGap = BufferUtils.createIntBuffer(1);
            STBTruetype.stbtt_GetFontVMetrics(info, ascent, descent, lineGap);
            
            this.ascent = (int) (ascent.get(0) * scale);
            this.descent = (int) (descent.get(0) * scale);
            this.lineGap = lineGap.get(0);
            
            texture = TextureAlpha.load(bitmap, BITMAP_WIDTH, BITMAP_HEIGHT, 1);
            
        	MemoryUtil.memFree(bitmap);
        	
            // Generate glyphs
            for (int i = 0; i < NUM_CHARS; i++) {
                char c = (char) (FIRST_CHAR + i);
                Glyph g = new Glyph();
                STBTTAlignedQuad stbQuad = STBTTAlignedQuad.create();
                float[] x = new float[] {0};
                float[] y = new float[] {0};
				STBTruetype.stbtt_GetBakedQuad(cdata, BITMAP_WIDTH, BITMAP_HEIGHT, c - FIRST_CHAR, x, y, stbQuad, true);

				g.setBufferData(stbQuad.x0(), -stbQuad.y0(), stbQuad.x1(), -stbQuad.y1(), stbQuad.s0(), stbQuad.t0(),
						stbQuad.s1(), stbQuad.t1());

                g.width = stbQuad.x1() - stbQuad.x0();
                glyphs[i] = g;
            }
            
            // Fudge space
            glyphs[' ' - FIRST_CHAR].width = glyphs['a' - FIRST_CHAR].width;
            glyphs[',' - FIRST_CHAR].width += 10;
            glyphs['.' - FIRST_CHAR].width += 10;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Return the defined size for this font.
     * @return
     */
    public float getFontHeight() {
        return height;
    }
    
    public TextureAlpha getTexture() {
    	return texture;
    }
 
    class Glyph {
        
    	float width;
    	public float x1, y1, x2, y2;
    	public float s1, t1, s2, t2;
        
        public void setBufferData(float x1, float y1, float x2, float y2, float s1, float t1, float s2, float t2) {
        	this.x1 = x1;
        	this.y1 = y1;
        	this.x2 = x2;
        	this.y2 = y2;
        	
        	this.s1 = s1;
        	this.t1 = t1;
        	this.s2 = s2;
        	this.t2 = t2;
        }
    }
 
    /**
     * Return glyph data for a specific character.
     * @param c
     * @return
     */
    public Glyph getGlyph(char c) {
        return glyphs[(int)c - FIRST_CHAR];
    }
 
    /**
     * Return the bitmap data that makes up this bitmap texture sheet.
     * @return
     */
    public ByteBuffer getBitmapData() {
        return bitmap;
    }
 
    /**
     * Return the width of this bitmaps texture sheet.
     * @return
     */
    public int getBitmapWidth() {
        return BITMAP_WIDTH;
    }
 
    /**
     * Return the height of this bitmaps texture sheet.
     * @return
     */
    public int getBitmapHeight() {
        return BITMAP_HEIGHT;
    }
 
    /**
     * Return the ascent of this font.
     * @return
     */
    public int getAscent() {
        return this.ascent;
    }
 
    /**
     * Return the descent of this font.
     * @return
     */
    public int getDescent() {
        return this.descent;
    }
 
    /**
     * Return the line-gap of this font.
     * @return
     */
    public int getLineGap() {
        return this.lineGap;
    }

	public void destroy() {
		texture.destroy();
	}

	public float getPadding() {
		return padding;
	}
}