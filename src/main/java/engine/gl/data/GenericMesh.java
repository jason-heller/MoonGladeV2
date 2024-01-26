package engine.gl.data;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.lwjgl.opengl.GL30.glVertexAttribIPointer;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

// TODO: Remove at some point and replace with more extendable item
public class GenericMesh implements IMesh {
	private int vao;
	private int[] bufferObjects;
	
	public int numIndices;
	
	public GenericMesh() {}
	
	/**
	 * Creates the Mesh's VAO and binds it. Needs to be called before setting the buffer objects. This constructor is the functionally the same as instantiating this object then calling init()
	 * 
	 * @param numBufferObjects The number of buffer objects this VAO will hold
	 */
	public GenericMesh(int numBufferObjects) {
		init(numBufferObjects);
	}
	
	/**
	 * Creates the Mesh's VAO and binds it. Needs to be called before setting the buffer objects
	 * 
	 * @param numBufferObjects The number of buffer objects this VAO will hold
	 */
	public void init(int numBufferObjects) {
		vao = glGenVertexArrays();
		bind();
		
		bufferObjects = new int[numBufferObjects];
	}
	
	// For convenience
	public void setBuffer(int index, int numComponents, FloatBuffer data) {
		setBuffer(index, numComponents, data, GL_ARRAY_BUFFER);
	}
	
	public void setBuffer(int index, int numComponents, IntBuffer data) {
		setBuffer(index, numComponents, data, GL_ARRAY_BUFFER);
	}
	
	// glBufferData has to take in one type, so we have a bunch of buffers here
	public void setBuffer(int index, int numComponents, FloatBuffer data, int target) {
		int bufferObject = glGenBuffers();
		glBindBuffer(target, bufferObject);
		
		glBufferData(target, data, GL_STATIC_DRAW);
		glVertexAttribPointer(index, numComponents, GL_FLOAT, false, 0, 0);
		
		glBindBuffer(target, 0);
		bufferObjects[index] = bufferObject;
		
		
	}
	
	public void setBuffer(int index, int numComponents, IntBuffer data, int target) {
		int bufferObject = glGenBuffers();
		glBindBuffer(target, bufferObject);
		
		glBufferData(target, data, GL_STATIC_DRAW);
		glVertexAttribIPointer(index, numComponents, GL_UNSIGNED_INT, 0, 0);
		
		glBindBuffer(target, 0);
		bufferObjects[index] = bufferObject;
	}
	
	public void setIndexBuffer(int index, IntBuffer data) {
		int bufferObject = glGenBuffers();
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferObject);
		
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, data, GL_STATIC_DRAW);
		
		bufferObjects[index] = bufferObject;
	}
	
	public void bind() {
		glBindVertexArray(vao);
	}

	public void unbind() {
		glBindVertexArray(0);
	}

	
	public int getVAO() {
		return vao;
	}

	public void destroy() {
		for(int bufferObject : bufferObjects)
			glDeleteBuffers(bufferObject);
		
		glDeleteVertexArrays(vao);
	}
}
