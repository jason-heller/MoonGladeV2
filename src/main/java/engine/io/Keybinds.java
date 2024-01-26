package engine.io;

import org.lwjgl.glfw.GLFW;

public enum Keybinds {
	MOVE_FORWARD(GLFW.GLFW_KEY_W),
	MOVE_BACKWARD(GLFW.GLFW_KEY_S),
	STRAFE_LEFT(GLFW.GLFW_KEY_A),
	STRAFE_RIGHT(GLFW.GLFW_KEY_D),
	JUMP(GLFW.GLFW_KEY_SPACE),
	CROUCH(GLFW.GLFW_KEY_LEFT_CONTROL), 
	ESCAPE(GLFW.GLFW_KEY_ESCAPE),
	DESTROY(GLFW.GLFW_KEY_LAST + GLFW.GLFW_MOUSE_BUTTON_LEFT),
	BUILD(GLFW.GLFW_KEY_LAST + GLFW.GLFW_MOUSE_BUTTON_RIGHT);
	
	public static final int NO_KEY = 0;
	public static final int SCROLL_UP = GLFW.GLFW_KEY_LAST + GLFW.GLFW_MOUSE_BUTTON_LAST + 1;
	public static final int SCROLL_DOWN = SCROLL_UP + 1;
	public static final int LEFT_CLICK = GLFW.GLFW_KEY_LAST + GLFW.GLFW_MOUSE_BUTTON_LEFT;
	public static final int RIGHT_CLICK = GLFW.GLFW_KEY_LAST + GLFW.GLFW_MOUSE_BUTTON_RIGHT;
	public static final int MIDDLE_CLICK = GLFW.GLFW_KEY_LAST + GLFW.GLFW_MOUSE_BUTTON_MIDDLE;
	
	private int defaultBind, defaultAltBind;

	private Keybinds(int defaultBind) {
		this(defaultBind, NO_KEY);
	}
	
	private Keybinds(int defaultBind, int defaultAltBind) {
		this.defaultBind = defaultBind;
		this.defaultAltBind = defaultAltBind;
	}

	public int getDefaultBind() {
		return defaultBind;
	}
	
	public int getDefaultAltBind() {
		return defaultAltBind;
	}
}
