package engine.space;

import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import engine.Window;
import engine.data.chunk.IByteLayerData;
import engine.dev.DevFlags;
import engine.dev.DevInfo;
import engine.geo.VoxelRaycaster;
import engine.gl.FirstPersonCamera;
import engine.gl.FloraRenderer;
import engine.gl.ICamera;
import engine.gl.LineRenderer;
import engine.gl.TerrainRenderer;
import engine.gl.text.TextRenderer;
import engine.io.Input;
import engine.io.Keybinds;
import engine.pg.gen.WorldGenerator;
import engine.pg.gen.WorldMesher;
import engine.utils.math.Maths;
import engine.world.Chunk;
import engine.world.ChunkDataHandler;
import engine.world.WorldManager;

public class OverworldSpace implements ISpace {

	private FirstPersonCamera camera;
	private GenericPlayer player;
	
	private TextRenderer textRenderer;
	private TerrainRenderer terrainRenderer;
	private FloraRenderer floraRenderer;
	
    private WorldManager worldManager;
    
    private ChunkDataHandler chunkDataPtr;
    
    private IntersectionTestEntity intersector;
    
    private VoxelRaycaster voxelRaycaster;
  
	
	public OverworldSpace(float aspectRatio) {
		terrainRenderer = new TerrainRenderer();
		floraRenderer = new FloraRenderer();
		textRenderer = new TextRenderer();	
		LineRenderer.init();
		
		worldManager = new WorldManager(24);
		
		player = new GenericPlayer(.4f, 1f, .4f);
		player.setPosition(4,115,4);
		player.setTangible(true);
		
		camera = new FirstPersonCamera(aspectRatio);
		camera.attachTo((CameraInheritable) player);
		
		intersector = new IntersectionTestEntity(player);
		
		chunkDataPtr = worldManager.getChunkData();
		
		voxelRaycaster = new VoxelRaycaster(chunkDataPtr, camera.getPosition(), camera.getLookVector(), 6f, 0.5f);
	}

	public void destroy() {
		worldManager.destroy();
		textRenderer.destroy();
		terrainRenderer.destroy();
		floraRenderer.destroy();
		LineRenderer.destroy();
	}

	@Override
	public void updateTick() {
		LineRenderer.clear();
		player.tick(this);
		intersector.tick(this);
		camera.update();
		
		handleVoxelPicker();
		
		worldManager.update(camera);
		
		long totalMem = Runtime.getRuntime().totalMemory();
		long freeMem = Runtime.getRuntime().freeMemory();
		
		double usedMemPercent = (((double)(totalMem - freeMem)) / totalMem) * 100.0;
		
		double chunkTileMem = 0;
		for(Chunk c : this.worldManager.getChunkData().getAllChunks()) {
			chunkTileMem += c.getMemorySize();
		}
		chunkTileMem /= 1000.0;
		
		final int camX = Maths.floor(camera.getX());
		final int camY = Maths.floor(camera.getY());
		final int camZ = Maths.floor(camera.getZ());

		Vector3f look = camera.getLookVector();
		DevInfo.append("<va=BOTTOM>FPS", Window.fps);
		DevInfo.append("X", camX, "red");
		DevInfo.append("Y", camY, "green");
		DevInfo.append("Z", camZ, "blue");
		DevInfo.append("#C unload", "(" + WorldGenerator.numJobs + "/" + WorldMesher.numJobs + ")", "white");
		DevInfo.append("", WorldManager.numJobsPerLOD[0], "white");
		DevInfo.append("", WorldManager.numJobsPerLOD[1], "white");
		DevInfo.append("", WorldManager.numJobsPerLOD[2], "white");
		DevInfo.append("", WorldManager.numJobsPerLOD[3], "white");
		DevInfo.append("look", look.x + " "+ look.y + " " + look.z, "white");
		DevInfo.append("Mem", String.format("%.01f", usedMemPercent) + "% chunk mem: " + String.format("%.01f", chunkTileMem) + " kb");
		
		
		Chunk chunk = worldManager.getChunkData().getChunkContaining(camX, camZ);
		if (chunk != null && chunk.getTileData() != null) {
			final int chunkX = chunk.getX();
			final int chunkZ = chunk.getZ();
			final int chunkLocalX = (camX - chunkX) / chunk.getScale();
			final int chunkLocalZ = (camZ - chunkZ) / chunk.getScale();
			
			final int hOrigin = chunkLocalX + (chunkLocalZ * Chunk.NUM_VERTICES_X);
			int[] hIndices = {
				hOrigin, hOrigin+1, hOrigin+Chunk.NUM_VERTICES_X, hOrigin+Chunk.NUM_VERTICES_X+1
			};
			
			float height;
			IByteLayerData heights = chunk.getHeightData();
			
			float weightX = ((camera.getX() - chunkX) % chunk.getScale()) / chunk.getScale();
			float weightZ = ((camera.getX() - chunkZ) % chunk.getScale()) / chunk.getScale();

			if (weightX > 1f - weightZ) {
				height = Maths.barycentric(weightX, weightZ,
						new Vector3f(1, heights.get(hIndices[1]) * chunk.getScale(), 0),
						new Vector3f(0, heights.get(hIndices[2]) * chunk.getScale(), 1),
						new Vector3f(1, heights.get(hIndices[3]) * chunk.getScale(), 1));
			} else {
				height = Maths.barycentric(weightX, weightZ,
						new Vector3f(0, heights.get(hIndices[0]) * chunk.getScale(), 0),
						new Vector3f(1, heights.get(hIndices[1]) * chunk.getScale(), 0),
						new Vector3f(0, heights.get(hIndices[2]) * chunk.getScale(), 1));
			}
			
			DevInfo.append("height", height);
			DevInfo.append("wx", weightX);
			DevInfo.append("wz", weightZ);
			
		}
		//DevInfo.append("avg gen time", String.format("%.03f", WorldGenerator.getAvgGenerationTime()));
		//DevInfo.append("<scale=.75> biome", String.format("%.03f", WorldGenerator.biomeTime) + "<br> terrain=" + String.format("%.03f", WorldGenerator.terrainTime), "black");
		//DevInfo.append("avg mesh time", String.format("%.03f", WorldMesher.getAvgMeshingTime()), "white");
		DevInfo.drawInfo();
		
		if (DevFlags.wireframe) {
			GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
		}
		terrainRenderer.render(camera, worldManager, camera.getProjectionMatrix(), camera.getViewMatrix());
		floraRenderer.render(camera, worldManager, camera.getProjectionMatrix(), camera.getViewMatrix());
		LineRenderer.render(camera);
		
		if (DevFlags.wireframe) {
			GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
		}
		
		textRenderer.render(camera);
	}

	private void handleVoxelPicker() {
		voxelRaycaster.raycast();
		
		if (!voxelRaycaster.hasFoundVoxel())
			return;
		
		if (Input.isPressed(Keybinds.DESTROY)) {
			voxelRaycaster.setTarget((byte)0);
		}
		
		if (Input.isDown(Keybinds.BUILD)) {
			/*boolean success = */voxelRaycaster.addTarget((byte)1);
		}
	}

	@Override
	public void gameTick() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ICamera getCamera() {
		return camera;
	}

	@Override
	public IPlayer getPlayer() {
		return player;
	}
	
	public ChunkDataHandler getChunkData() {
		return chunkDataPtr;
	}
}
