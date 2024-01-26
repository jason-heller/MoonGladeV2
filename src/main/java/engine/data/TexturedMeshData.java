package engine.data;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.system.MemoryUtil;

public class TexturedMeshData implements IMeshData {

	private final FloatBuffer positionBuffer;
	private final FloatBuffer texCoordBuffer;
	private final FloatBuffer normalBuffer;
	private final IntBuffer indexBuffer;

	public int numVertices, numIndices;
	
	public TexturedMeshData(FloatBuffer positionBuffer, FloatBuffer texCoordBuffer, FloatBuffer normalBuffer, IntBuffer indexBuffer) {
		this.positionBuffer = positionBuffer;
		this.texCoordBuffer = texCoordBuffer;
		this.normalBuffer = normalBuffer;
		this.indexBuffer = indexBuffer;
	}
	
	public void flip() {
		flip(positionBuffer);
		flip(texCoordBuffer);
		flip(normalBuffer);
		flip(indexBuffer);
	}
	
	public void free() {
		free(positionBuffer);
		free(texCoordBuffer);
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

	public FloatBuffer getTexCoordBuffer() {
		return texCoordBuffer;
	}

	public FloatBuffer getNormalBuffer() {
		return normalBuffer;
	}

	public IntBuffer getIndexBuffer() {
		return indexBuffer;
	}

}
