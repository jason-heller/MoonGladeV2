package engine.io;

import java.nio.DoubleBuffer;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;

public class Input {
	private static boolean[] inputs = new boolean[GLFW.GLFW_KEY_LAST + GLFW.GLFW_MOUSE_BUTTON_LAST + 3];
	// The + 3 is for mouse wheel up and mouse wheel down (and +1 for last key), to be impemented
	
	private static Map<Keybinds, KeyPair> keybinds = new HashMap<>();
	
	private static double mouseX, mouseY;
	private static double mouseDeltaX, mouseDeltaY;
	private static double scrollX, scrollY;

	private GLFWKeyCallback keyboard;
	private GLFWMouseButtonCallback mouseButtons;
	private GLFWScrollCallback mouseScroll;

	public Input(long handle) {
		keyboard = new GLFWKeyCallback() {
			public void invoke(long window, int key, int scancode, int action, int mods) {
				inputs[key] = (action != GLFW.GLFW_RELEASE);
			}
		};

		mouseButtons = new GLFWMouseButtonCallback() {
			public void invoke(long window, int button, int action, int mods) {
				inputs[GLFW.GLFW_KEY_LAST + button] = (action != GLFW.GLFW_RELEASE);
			}
		};

		mouseScroll = new GLFWScrollCallback() {
			public void invoke(long window, double offsetx, double offsety) {
				scrollX += offsetx;
				scrollY += offsety;
				
				if (offsety > 0) {
					inputs[Keybinds.SCROLL_UP] = true;
				} else if (offsety < 0) {
					inputs[Keybinds.SCROLL_DOWN] = true;
				}
			}
		};
		
		GLFW.glfwSetKeyCallback(handle, keyboard);
		GLFW.glfwSetMouseButtonCallback(handle, mouseButtons);
		GLFW.glfwSetScrollCallback(handle, mouseScroll);
		
		resetBinds();
	}
	
	/** Sets the main key for the bind. 
	 * @param bind The keybind to assign this key to
	 * @param key the key to be assigned as the bind
	 */
	public static void setBind(Keybinds bind, int key) {
		setBind(bind, key, Keybinds.NO_KEY);
	}
	
	/** Sets the main key for the bind. 
	 * @param bind The keybind to assign this key to
	 * @param key the main key to be assigned as the bind
	 * @param alt the alternative key to be assigned as the bind
	 */
	public static void setBind(Keybinds bind, int key, int alt) {
		if (keybinds.containsKey(bind)) {
			keybinds.put(bind, new KeyPair(key, alt));
		} else {
			KeyPair mapping = keybinds.get(bind);
			mapping.key = key;
			mapping.alt = alt;
		}
	}
	
	public static int getBind(Keybinds bind) {
		return keybinds.get(bind).key;
	}
	
	public static int getAltBind(Keybinds bind) {
		return keybinds.get(bind).alt;
	}
	
	public static void resetBinds() {
		keybinds.clear();
		
		for(Keybinds bind : Keybinds.values()) {
			keybinds.put(bind, new KeyPair(bind.getDefaultBind(), bind.getDefaultAltBind()));
		}
	}

	public static boolean isDown(Keybinds bind) {
		return inputs[keybinds.get(bind).key] | inputs[keybinds.get(bind).alt];
	}
	
	public void update(long window, double centerX, double centerY) {
		DoubleBuffer xPos = BufferUtils.createDoubleBuffer(1);
		DoubleBuffer yPos = BufferUtils.createDoubleBuffer(1);
		GLFW.glfwGetCursorPos(window, xPos, yPos);
		
		mouseX = xPos.get();
		mouseY = yPos.get();
		
		mouseDeltaX = mouseX - centerX;
		mouseDeltaY = mouseY - centerY;

		inputs[Keybinds.SCROLL_UP] = inputs[Keybinds.SCROLL_DOWN] = false;
	}

	public void destroy() {
		keyboard.free();
		
		mouseButtons.free();
		mouseScroll.free();
	}

	public static double getMouseX() {
		return mouseX;
	}

	public static double getMouseY() {
		return mouseY;
	}
	
	public static double getMouseDeltaX() {
		return mouseDeltaX;
	}

	public static double getMouseDeltaY() {
		return mouseDeltaY;
	}
	
	public static double getScrollX() {
		return scrollX;
	}

	public static double getScrollY() {
		return scrollY;
	}
	
}

class KeyPair {
	
	public int key, alt;
	
	public KeyPair(int key, int alt) {
		this.key = key;
		this.alt = alt;
	}
}
