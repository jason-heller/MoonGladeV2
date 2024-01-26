package engine.dev;

import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glDisable;

import java.util.HashMap;
import java.util.Map;

import org.joml.Vector3f;

import engine.Application;
import engine.data.IMeshData;
import engine.gl.FirstPersonCamera;
import engine.gl.ICamera;
import engine.space.IPlayer;
import engine.space.ISpace;
import engine.space.OverworldSpace;

public class DevTools {
	
	private static Application app;
	
	public static void setReferences(Application application) {
		app = application;
	}

	public static boolean noclip(boolean b) {
		if (!firstPersonCheck()) return false;
		if (!playerExistsCheck()) return false;
		
		DevFlags.noclip = b;
		player().setTangible(!b);
		
		return true;
	}
	
	public static boolean teleport(Vector3f v) {
		if (!playerExistsCheck()) return false;
		
		player().setPosition(v);
		return true;
	}
	
	public static void cullFace(boolean b) {
		if (b) {
			glEnable(GL_CULL_FACE);
		} else {
			glDisable(GL_CULL_FACE);
		}
	}

	// In case any of our references go stale, we re-get them every time.
	private static ISpace space() {
		return app.getSpace();
	} 
	
	private static ICamera camera() {
		return space().getCamera();
	}
	
	private static IPlayer player() {
		return space().getPlayer();
	}
	
	// Helper functions
	private static boolean inMainSpaceCheck() {
		boolean require = (space() instanceof OverworldSpace);
		return require;
	}
	
	private static boolean firstPersonCheck() {
		boolean require = (camera() instanceof FirstPersonCamera);
		return require;
	}
	
	private static boolean playerExistsCheck() {
		boolean require = player() != null;
		return require;
	}
}
