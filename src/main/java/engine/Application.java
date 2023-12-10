package engine;

import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetErrorCallback;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

import engine.space.ISpace;
import engine.space.WorldSpace;

public class Application {

    private GLFWErrorCallback errorCallback;

    private Window window;
    private ISpace worldSpace;

	private void close() {
		worldSpace.destroy();
		window.close();
	}

	private void init() {

		errorCallback = GLFWErrorCallback.createPrint(System.err);
        glfwSetErrorCallback(errorCallback);

        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");
        
        window = new Window("Moonglade", 1280, 720);
        
        GL.createCapabilities();
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_CULL_FACE);
        
        worldSpace = new WorldSpace(window.getAspectRatio());
    }
	
	private void loop() {
		
		while (!window.shouldClose()) {
			GL11.glClearColor(0, 0, .1f, 1);
			//GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer
			glfwPollEvents();
			
			worldSpace.updateTick();
			
			window.update();
		}
	}
	
	public void run() {

        try {
        	
            init();
            loop();
            close();
            
        } finally {
        	
            glfwTerminate();
            errorCallback.close();
        }
    }
	
	public static void main(String[] args) {
		new Application().run();
	}
}
