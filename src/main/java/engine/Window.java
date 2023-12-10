package engine;

import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MAJOR;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MINOR;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_CORE_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_FORWARD_COMPAT;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_RESIZABLE;
import static org.lwjgl.glfw.GLFW.GLFW_VISIBLE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDefaultWindowHints;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor;
import static org.lwjgl.glfw.GLFW.glfwGetVideoMode;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwSetWindowPos;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL11.GL_TRUE;
import static org.lwjgl.system.MemoryUtil.NULL;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;

import engine.io.Input;
import engine.io.Keybinds;
import engine.utils.Sync;

public class Window {

	private long handle;
	private int width, height;
	
	private int targetFramerate = 120;
	private int fps = 0, frameCounter = 0;
	private long lastFrameQueryTime, frameQueryTime;
	
    private Input input;
    
    private Sync sync;
	
	public Window(String title, int width, int height) {
		
		this.width = width;
		this.height = height;
		
		sync = new Sync();

		glfwDefaultWindowHints(); // optional, the current window hints are already the default
		glfwWindowHint(GLFW_VISIBLE, GL_FALSE); // the window will stay hidden after creation
		glfwWindowHint(GLFW_RESIZABLE, GL_TRUE); // the window will be resizable
		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
		glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
		glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);

		handle = glfwCreateWindow(width, height, title, NULL, NULL);
    
		if ( handle == NULL )
			throw new RuntimeException("Failed to create the GLFW window");
	
		// Get the resolution of the primary monitor
		GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
		
		// Center our window
		glfwSetWindowPos(
			handle,
			(vidmode.width() - width) / 2,
			(vidmode.height() - height) / 2
		);
		
		glfwMakeContextCurrent(handle);
		glfwSwapInterval(0);	// No Vsync be default
		glfwShowWindow(handle);

		input = new Input(handle);
		
		frameQueryTime = lastFrameQueryTime = System.currentTimeMillis();
	}
	
	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}
	
	public void close() {
		glfwDestroyWindow(handle);
		input.destroy();
	}

	public boolean shouldClose() {
		return glfwWindowShouldClose(handle);
	}

	public void update() {
		glfwSwapBuffers(handle);
		
		// Putting this here ties the mouse delta rates to the window update rate, may want to decouple
		int centerX = width / 2, centerY = height / 2;
		
		if (!Input.isDown(Keybinds.CROUCH)) {
			input.update(handle, centerX, centerY);
			GLFW.glfwSetCursorPos(handle, centerX, centerY);
		} else {

			input.update(handle, Input.getMouseX(), Input.getMouseY());
		}
		
		frameCounter++;
		
		frameQueryTime = System.currentTimeMillis();
		
		if (frameQueryTime >= lastFrameQueryTime + 1000) {
			lastFrameQueryTime = frameQueryTime;
			
			fps = frameCounter;
			frameCounter = 0;
			GLFW.glfwSetWindowTitle(handle, "fps: " + fps);	// TODO: remove me
		}
		
		sync.sync(targetFramerate);
	}

	public float getAspectRatio() {
		return width / (float)height;
	}
}
