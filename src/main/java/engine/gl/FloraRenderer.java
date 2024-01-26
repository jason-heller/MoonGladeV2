package engine.gl;

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.glDrawElements;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;

import java.util.Collection;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.lwjgl.opengl.GL13;

import engine.dev.DevFlags;
import engine.gl.data.GenericMesh;
import engine.gl.data.Texture2D;
import engine.world.Chunk;
import engine.world.WorldManager;

public class FloraRenderer {
	private Shader shader;
	
	private Texture2D diffuseMap;
	
	public FloraRenderer() {
		shader = new Shader("shaders/plant.vsh", "shaders/plant.fsh");
		
		diffuseMap = Texture2D.load("resources/textures/flora.png");
		
	}
	
	public void render(ICamera camera, WorldManager terrainManager, Matrix4f proj, Matrix4f view) {
		
		shader.bind();
		shader.setUniform("projection", proj);
		shader.setUniform("view", view);
		
		shader.setTexture("diffuseMap", 0, diffuseMap);
		
		Collection<Chunk> chunks = terrainManager.getChunkData().getAllChunks();

		for(Chunk chunk : chunks) {
			renderFlora(chunk, terrainManager.getRelativeCenters());
		}
		
		for(Chunk chunk : terrainManager.getChunksPendingDeletion()) {
			renderFlora(chunk, terrainManager.getRelativeCenters());
		}

		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		diffuseMap.unbind();
		shader.unbind();
	}
	
	private void renderFlora(Chunk chunk, Vector2f[] relativeCenter) {
		if (!chunk.isMeshed() || DevFlags.skipFlora)
			return;
		
		GenericMesh mesh = chunk.getFloraMesh();
		
		mesh.bind();
		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);
		glEnableVertexAttribArray(2);
		glDrawElements(GL_TRIANGLES, mesh.numIndices, GL_UNSIGNED_INT, 0);

		glDisableVertexAttribArray(2);
		glDisableVertexAttribArray(1);
		glDisableVertexAttribArray(0);
		mesh.unbind();
	}

	public void destroy() {
		shader.destroy();
		
		diffuseMap.destroy();
	}
}
