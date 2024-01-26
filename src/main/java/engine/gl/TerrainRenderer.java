package engine.gl;

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.glDrawElements;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;

import java.util.Collection;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL13;

import engine.gl.data.GenericMesh;
import engine.gl.data.Texture2D;
import engine.world.Chunk;
import engine.world.WorldManager;

public class TerrainRenderer {
	private Shader shader;
	
	private Texture2D diffuseMap;
	private Texture2D colorMap;
	
	public TerrainRenderer() {
		shader = new Shader("shaders/terrain.vsh", "shaders/terrain.fsh");
		
		diffuseMap = Texture2D.load("resources/textures/ground_diffuse.png");
		colorMap = Texture2D.load("resources/textures/ground_color.png");
		
	}
	
	public void render(ICamera camera, WorldManager terrainManager, Matrix4f proj, Matrix4f view) {
		
		shader.bind();
		shader.setUniform("projection", proj);
		shader.setUniform("view", view);
		shader.setUniform("cameraPosition", camera.getPosition());
		
		shader.setTexture("diffuseMap", 0, diffuseMap);
		shader.setTexture("colorMap", 1, colorMap);
		
		Collection<Chunk> chunks = terrainManager.getChunkData().getAllChunks();

		for(Chunk chunk : chunks) {
			renderChunk(chunk, terrainManager.getRelativeCenters());
		}
		
		for(Chunk chunk : terrainManager.getChunksPendingDeletion()) {
			renderChunk(chunk, terrainManager.getRelativeCenters());
		}

		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		colorMap.unbind();
		GL13.glActiveTexture(GL13.GL_TEXTURE1);
		diffuseMap.unbind();
		shader.unbind();
	}
	
	private void renderChunk(Chunk chunk, Vector2f[] relativeCenter) {
		if (!chunk.isMeshed())
			return;
		
		GenericMesh mesh = chunk.getTerrainMesh();
		
		int lod = chunk.getLOD();
		
		shader.setUniform("scale", (float)(1 << lod)); 
		shader.setUniform("offset", new Vector4f((float)chunk.getX(), (float)chunk.getZ(), relativeCenter[lod].x, relativeCenter[lod].y));
		
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
		
		colorMap.destroy();
		diffuseMap.destroy();
	}
}
