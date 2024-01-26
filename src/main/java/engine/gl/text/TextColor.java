package engine.gl.text;

public enum TextColor {
	RED((byte)0xF0, (char)-1), 
	MAROON((byte)0xE0, (char)-1), 
	BROWN((byte)0xD0, (char)-1), 
	LIGHT_GREEN((byte)0xDC, (char)-1), 
	GREEN((byte)0xCC, (char)-1), 
	EMERALD((byte)0xC8, (char)-1), 
	LIME((byte)0xD8, (char)-1), 
	DARK_GREEN((byte)0xC4, (char)-1), 
	BLUE((byte)0xC3, (char)-1), 
	TEAL((byte)0xC6, (char)-1), 
	DARK_BLUE((byte)0xC2, (char)-1), 
	STRATOS_BLUE((byte)0xC1, (char)-1), 
	ORANGE((byte)0xF8, (char)-1), 
	DARK_ORANGE((byte)0xE4, (char)-1), 
	YELLOW((byte)0xFC, (char)-1), 
	GOLD((byte)0xE8, (char)-1), 
	
	CYAN((byte)0xCF, (char)-1), 
	LIGHT_BLUE((byte)0xCB, (char)-1), 
	PINK((byte)0xF6, (char)-1), 
	MAGENTA((byte)0xF3, (char)-1), 
	PURPLE((byte)0xD2, (char)-1), 
	VIOLET((byte)0xF2, (char)-1), 
	JAZZBERRY_JAM((byte)0xE1, (char)-1), 		// Apparently what this one is called, (char)-1),  keeping it because the name's cool
	
	BLACK((byte)0xC0, (char)-1), 
	GRAY((byte)0xD5, (char)-1), 
	SILVER((byte)0xEA, (char)-1), 
	WHITE((byte)0xFF, (char)-1);

	private byte argb;
	private char reference;

	TextColor(byte argb, char reference) {
		this.argb = argb;
		this.reference = reference;
	}
	
	public byte getARGB() {
		return argb;
	}
	
	public char getReference() {
		return reference;
	}
}
