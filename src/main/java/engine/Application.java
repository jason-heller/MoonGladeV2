package engine;

import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetErrorCallback;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

import engine.dev.DevFlags;
import engine.dev.DevTools;
import engine.dev.Level;
import engine.dev.Log;
import engine.dev.console.DeveloperConsole;
import engine.space.ISpace;
import engine.space.OverworldSpace;

public class Application {

    private GLFWErrorCallback errorCallback;

    private DeveloperConsole console;
    private Window window;
    private ISpace worldSpace;
    
    private static final String NAME = "Moonglade";

	private void init() {

        
        //Thread.setDefaultUncaughtExceptionHandler(uncaughtExceptionHandler);
	
		// OpenGL
		errorCallback = GLFWErrorCallback.createPrint(System.err);
        glfwSetErrorCallback(errorCallback);

        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");
        
        window = new Window(NAME, 1280, 720);
        
        GL.createCapabilities();
        glEnable(GL_DEPTH_TEST);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable( GL_BLEND );
        
        
        // Worldspace
        worldSpace = new OverworldSpace(Window.getAspectRatio());

        // Dev tools
        DevTools.setReferences(this);
        console = new DeveloperConsole();
    }
	
	private void loop() {
		
		while (!Window.shouldClose()) {
			if (DevFlags.cullFace)
		        glEnable(GL_CULL_FACE);
			else
		        glDisable(GL_CULL_FACE);
			
			GL11.glClearColor(181.0f/255.0f, 154.0f/255.0f, 238.0f/255.0f, 1);
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer
			glfwPollEvents();
			worldSpace.updateTick();
			window.update(worldSpace.getCamera());
			console.update();
			
			final Runtime runtime = Runtime.getRuntime();

            // Adjust the threshold based on your requirements
            if (runtime.freeMemory() < 1000000) {
                handleOutOfMemory();
                
                break;
            }
		}
	}
	
	private void handleOutOfMemory() {
		if (!DeveloperConsole.isVisible)
    		console.toggle();
    	
		long max = Runtime.getRuntime().maxMemory();
		long free = Runtime.getRuntime().freeMemory();
		
    	Log.setLevel(Level.FATAL);
    	Log.fatal(NAME + " does not have enough free memory to continue X_X");
    	Log.info("Max memory: " + (max / 1000) + " kb (" + (max / 1000000) + " mb)");
    	Log.info("Free memory: " + (max / 1000) + " kb (" + (free / 1000000) + " mb)");
	}

	private void close() {
		
		worldSpace.destroy();
		Window.destroy();
		console.close();
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

	public ISpace getSpace() {
		return worldSpace;
	}
	
	private Thread.UncaughtExceptionHandler uncaughtExceptionHandler = new Thread.UncaughtExceptionHandler() {
        @Override
        public void uncaughtException(Thread thread, Throwable ex) {
        	Window.close();
        	
        	if (!DeveloperConsole.isVisible)
        		console.toggle();
        	
        	Log.setLevel(Level.FATAL);
        	Log.fatal(NAME + " has crashed :(");
            Log.stackTrace(ex);
            DeveloperConsole.println("Use command 'save_logs' to save logs to file or 'exit' to exit");
        }
    };
}
