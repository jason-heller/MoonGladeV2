package engine.gl;

import java.util.ArrayList;

import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import engine.gl.data.GenericMesh;
import engine.gl.meshing.MeshUtil;

public class LineRenderer {

	private static Shader shader;
	private static GenericMesh mesh;
	
	private static ArrayList<Vector3f> pts = new ArrayList<>();
	private static ArrayList<Vector3f> col = new ArrayList<>();
	
	public static void add(Vector3f v, Vector3f c) {
		pts.add(v);
		col.add(c);
	}
	
	public static void init() {
		float[] f = {
			0,0,0,0,0,0	
		};
		
		shader = new Shader("shaders/generic/line.vsh", "shaders/generic/line.fsh");
		mesh = new GenericMesh();
		mesh.init(1);
		mesh.setBuffer(0, 3, MeshUtil.toFloatBuffer(f));
		mesh.unbind();
	}
	
	public static void render(ICamera camera) {
		if (pts.size() < 2 || pts.size() % 2 != 0)
			return;
		
		//GL11.glDisable(GL11.GL_DEPTH_TEST);
		
		shader.bind();
		shader.setUniform("projection", camera.getProjectionMatrix());
		shader.setUniform("view", camera.getViewMatrix());
		mesh.bind();
		for(int i = 0; i < pts.size()-1; i += 2) {
			shader.setUniform("A", pts.get(i));
			shader.setUniform("B", pts.get(i+1));
			shader.setUniform("AC", col.get(i));
			shader.setUniform("BC", col.get(i+1));
			GL11.glDrawArrays(GL11.GL_LINE_STRIP, 0, 2);
		}
		mesh.unbind();
		shader.unbind();
		
		//GL11.glEnable(GL11.GL_DEPTH_TEST);
	}
	
	public static void destroy() {
		mesh.destroy();
		shader.destroy();
	}

	public static void clear() {
		pts.clear();
		col.clear();
	}
	public static void box(Vector3f min, Vector3f max) {
		box(min,max,new Vector3f(1,1,0));
	}
	public static void box(Vector3f min, Vector3f max, Vector3f c) {
		Vector3f[] p = {
				min,
				new Vector3f(max.x, min.y, min.z),
				new Vector3f(max.x, min.y, max.z),
				new Vector3f(min.x, min.y, max.z),
				
				max,
				new Vector3f(max.x, max.y, min.z),
				new Vector3f(min.x, max.y, min.z),
				new Vector3f(min.x, max.y, max.z)
			};
		
		pts.add(p[0]);
		pts.add(p[1]);
		pts.add(p[1]);
		pts.add(p[2]);
		pts.add(p[2]);
		pts.add(p[3]);
		pts.add(p[3]);
		pts.add(p[0]);
		
		pts.add(p[4]);
		pts.add(p[5]);
		pts.add(p[5]);
		pts.add(p[6]);
		pts.add(p[6]);
		pts.add(p[7]);
		pts.add(p[7]);
		pts.add(p[4]);
		
		pts.add(p[0]);
		pts.add(p[6]);
		pts.add(p[1]);
		pts.add(p[5]);
		pts.add(p[2]);
		pts.add(p[4]);
		pts.add(p[3]);
		pts.add(p[7]);

		for(int i = 0; i < 24; i++) {
			col.add(c);
		}

	}
}
