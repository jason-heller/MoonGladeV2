package engine.rendering.data;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

public class HeightmapMesh implements IMesh {
	public static final int VERTEX_RUN = 9;
	public static final int TILE_RUN = VERTEX_RUN - 1;
	
	private float[] heights;
	private int[] eulerNormal;
	private int vao, pbo, normalBO;
	
	public HeightmapMesh(float[] heights, int[] eulerNormal) {
		this.heights = heights;
		this.eulerNormal = eulerNormal;
	}
	
	public void create() {
		vao = GL30.glGenVertexArrays();
		GL30.glBindVertexArray(vao);
		
		FloatBuffer floatBuffer = MemoryUtil.memAllocFloat(heights.length);
		floatBuffer.put(heights).flip();
		
		pbo = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, pbo);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, floatBuffer, GL15.GL_STATIC_DRAW);
		GL20.glVertexAttribPointer(0, 1, GL11.GL_FLOAT, false, 0, 0);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		
		IntBuffer shortBuffer = MemoryUtil.memAllocInt(eulerNormal.length);
		shortBuffer.put(eulerNormal).flip();
		
		normalBO = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, normalBO);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, shortBuffer, GL15.GL_STATIC_DRAW);
		GL20.glVertexAttribPointer(1, 1, GL11.GL_UNSIGNED_INT, false, 0, 0);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		
		shortBuffer.put(eulerNormal).flip();

	}

	public int getVAO() {
		return vao;
	}

	public int getPBO() {
		return pbo;
	}
	
	public int getNormalBO() {
		return normalBO;
	}


	public void destroy() {
		GL15.glDeleteBuffers(pbo);
		GL15.glDeleteBuffers(normalBO);
		GL30.glDeleteVertexArrays(vao);
	}

	public float[] getHeights() {
		return heights;
	}
}
