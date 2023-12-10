package engine.rendering;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import engine.rendering.data.HeightmapMesh;

public class Renderer {
	private Shader shader;
	
	private int chunkRun = 8;
	private int numLODs = 5;
	
	public Renderer() {
		shader = new Shader("/shaders/terrainVert.glsl", "/shaders/terrainFrag.glsl");
	}
	
	public void renderMesh(ICamera camera, HeightmapMesh mesh, Matrix4f proj, Matrix4f view) {
		shader.bind();
		shader.setUniform("projMatrix", proj);
		shader.setUniform("viewMatrix", view);
		
		float originX = 0, lastOriginX = 0;
		float originZ = 0, lastOriginZ = 0;
		
		Vector2f offset = new Vector2f();
		float scale = 1;
		
		int chunkSize;
		float lodSize;
		
		int chunkDispL, chunkDispT;

		GL30.glEnableVertexAttribArray(0);
		GL30.glEnableVertexAttribArray(1);
		
		for(int i = 0; i < numLODs; i++) {
			
			chunkSize = (int) (scale * HeightmapMesh.TILE_RUN);
			lodSize = chunkSize * chunkRun;

			originX = camera.getPosition().x - (lodSize / 2f);
			originZ = camera.getPosition().z - (lodSize / 2f);
			originX = (float) (Math.floor(originX / chunkSize) * chunkSize);
			originZ = (float) (Math.floor(originZ / chunkSize) * chunkSize);
			
			chunkDispL = (lastOriginX % chunkSize == 0) ? 1 : 2;
			chunkDispT = (lastOriginZ % chunkSize == 0) ? 1 : 2;
			
			shader.setUniform("scale", scale);
			
			for(int x = 0; x < chunkRun; x++) {
				for(int z = 0; z < chunkRun; z++) {
					
					offset.x = originX + (scale * (HeightmapMesh.TILE_RUN * x));
					offset.y = originZ + (scale * (HeightmapMesh.TILE_RUN * z));
					
					// Cull overlapping faces
					if (i != 0 && x > chunkDispL && x < chunkRun - 2 && z > chunkDispT && z < chunkRun - 2)
						continue;
					
					shader.setUniform("offset", offset);
					
					GL30.glBindVertexArray(mesh.getVAO());
					GL11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, mesh.getHeights().length);
				}
			}
			
			lastOriginX = originX;
			lastOriginZ = originZ;
			scale *= 2f;
		}

		GL30.glDisableVertexAttribArray(1);
		GL30.glDisableVertexAttribArray(0);

		GL30.glBindVertexArray(0);
		shader.unbind();
	}
	
	public void destroy() {
		shader.destroy();
	}
}
