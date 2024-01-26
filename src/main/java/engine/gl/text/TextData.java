package engine.gl.text;

public class TextData {
	
	public String string;
	public float x, y, scale;
	public TextColor color;	//  AARRGGBB

	private boolean isRightAligned = false;
	private boolean isBottomAligned = false;
	
	public TextData(String string, float x, float y, float scale, TextColor color) {
		this.string = string;
		this.x = x;
		this.y = y;
		this.scale = scale;
		this.color = color;
	}
	
	public void setRightAligned(boolean isRightAligned) {
		this.isRightAligned = isRightAligned;
	}
	
	public void setBottomAligned(boolean isBottomAligned) {
		this.isBottomAligned = isBottomAligned;
	}
	
	public void topAlign() {
		this.isBottomAligned = false;
	}
	
	public void rightAlign() {
		this.isRightAligned = true;
	}
	
	/* These are the defaults
	public void leftAlign() {
		this.isRightAligned = false;
	}
	
	public void bottomAlign() {
		this.isBottomAligned = true;
	}*/
	
	public boolean isRightAligned() {
		return this.isRightAligned;
	}
	
	public boolean isBottomAligned() {
		return this.isBottomAligned;
	}
}