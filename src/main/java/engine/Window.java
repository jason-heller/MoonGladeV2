package engine;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL11.GL_TRUE;
import static org.lwjgl.system.MemoryUtil.NULL;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;

import engine.dev.console.DeveloperConsole;
import engine.gl.ICamera;
import engine.io.Input;
import engine.io.Keybinds;
import engine.utils.Sync;

public class Window {

	private static long handle;
	public static int width, height;
	
	public static float deltaTime;
	public static float timeScale = 1f;
	
	private static int targetFramerate = 120;
	private int frameCounter = 0;
	private long lastFrameQueryTime, frameQueryTime;
	
	public static int fps = 0;
	private static boolean isFocused = true;
	
    private static Input input;
    private static boolean shouldClose = false;
    
    private Sync sync;
	private long lastFrameTime;
	
	public Window(String title, int w, int h) {
		
		width = w;
		height = h;
		
		sync = new Sync();

		glfwDefaultWindowHints();
		glfwWindowHint(GLFW_VISIBLE, GL_FALSE);
		glfwWindowHint(GLFW_RESIZABLE, GL_TRUE);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 4);		// 4.4 Released in 2013, Should be fine now
		glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
		glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);
		//glfwWindowHint(GLFW_FOCUS_ON_SHOW, GL_FALSE);
		
		handle = glfwCreateWindow(width, height, title, NULL, NULL);
    
		if ( handle == NULL )
			throw new RuntimeException("Failed to create the GLFW window");
	
		glfwSetWindowFocusCallback(handle, (long handle, boolean glfwIsFocused)->{
			isFocused = glfwIsFocused;
		});
		
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
		
		lastFrameTime = System.currentTimeMillis();
		frameQueryTime = lastFrameQueryTime = lastFrameTime;
	}
	
	public static int getWidth() {
		return width;
	}

	public static int getHeight() {
		return height;
	}
	
	public static <T> void close() {
		shouldClose = true;
	}
	
	public static void destroy() {
		input.destroy();
		GLFW.glfwDestroyWindow(handle);
	}

	public static boolean shouldClose() {
		return shouldClose || glfwWindowShouldClose(handle);
	}

	public void update(ICamera c) {
		glfwSwapBuffers(handle);
		
		// Putting this here ties the mouse delta rates to the window update rate, may want to decouple
		int centerX = width / 2, centerY = height / 2;
		
		input.update(handle, centerX, centerY);
		if (!DeveloperConsole.isVisible())
			GLFW.glfwSetCursorPos(handle, centerX, centerY);
		
		if (Input.isDown(Keybinds.ESCAPE)) {
			close();
			return;
		}
		
		frameCounter++;
		
		frameQueryTime = System.currentTimeMillis();
		deltaTime = (frameQueryTime - lastFrameTime) / 1000f * timeScale;
		lastFrameTime = frameQueryTime;
		
		if (frameQueryTime >= lastFrameQueryTime + 1000) {
			lastFrameQueryTime = frameQueryTime;
			
			fps = frameCounter;
			frameCounter = 0;
		}
		
		sync.sync(targetFramerate);
	}
	
	public static void setTargetFramerate(int target) {
		targetFramerate = target;
	}
	
	public static boolean isFocused() {
		return isFocused;
	}

	public static float getAspectRatio() {
		return width / (float)height;
	}
}
