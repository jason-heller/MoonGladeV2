package engine.gl.text;

import static org.lwjgl.opengl.GL30.glBindVertexArray;

import org.lwjgl.opengl.GL11;

import engine.gl.data.PersistentMesh;

public class TextMesh extends PersistentMesh {

	private static final int MAX_CHARS = 800;
	public static final int NUM_VERTICES = MAX_CHARS * 6;
	public static final int NUM_BYTES = NUM_VERTICES * 3;

	public TextMesh() {
		build();
	}
	
	@Override
	public void build() {
		this.build(NUM_BYTES, 1);
	}

	@Override
	protected void addBufferObjects(int numVerticesPerMesh) {
		this.addImmutableBufferObject(0, NUM_BYTES, 4, GL11.GL_FLOAT);
	}

	public void bind() {
		glBindVertexArray(vao);
	}

	public void unbind() {
		glBindVertexArray(0);
	}

}
