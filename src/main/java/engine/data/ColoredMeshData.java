package engine.data;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.system.MemoryUtil;

public class ColoredMeshData implements IMeshData {

	private final FloatBuffer positionBuffer;
	private final IntBuffer colorBuffer;
	private final FloatBuffer normalBuffer;
	private final IntBuffer indexBuffer;

	public int numVertices, numIndices;

	public ColoredMeshData(FloatBuffer positionBuffer, IntBuffer colorBuffer, FloatBuffer normalBuffer, IntBuffer indexBuffer) {
		this.positionBuffer = positionBuffer;
		this.colorBuffer = colorBuffer;
		this.normalBuffer = normalBuffer;
		this.indexBuffer = indexBuffer;
	}
	
	public void flip() {
		flip(positionBuffer);
		flip(colorBuffer);
		flip(normalBuffer);
		flip(indexBuffer);
	}
	
	public void free() {
		free(positionBuffer);
		free(colorBuffer);
		free(normalBuffer);
		free(indexBuffer);
	}

	private void free(Buffer buff) {
		MemoryUtil.memFree(buff);
	}
	
	private void flip(Buffer buff) {
		buff.flip();
	}

	public FloatBuffer getPositionBuffer() {
		return positionBuffer;
	}

	public IntBuffer getColorBuffer() {
		return colorBuffer;
	}

	public FloatBuffer getNormalBuffer() {
		return normalBuffer;
	}

	public IntBuffer getIndexBuffer() {
		return indexBuffer;
	}

}
