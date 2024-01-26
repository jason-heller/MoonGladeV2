package engine.world;

import static engine.world.Chunk.CHUNK_WIDTH;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.joml.Vector2f;

import engine.data.ColoredMeshData;
import engine.data.TexturedMeshData;
import engine.dev.DevFlags;
import engine.gl.ICamera;
import engine.gl.data.GenericMesh;
import engine.gl.meshing.transvoxel.Seam;
import engine.pg.gen.ProcessedMeshContainer;
import engine.pg.gen.WorldGenerator;
import engine.pg.gen.WorldMesher;
import engine.utils.math.Maths;

public class WorldManager {
	
	private static final int MAX_TIME_MESHING_MS = 5;
		
	private static int firstLODWidthInChunks = 8;				// How wide the terrain is, in chunks, specifically referring to the smallest LOD
	public static int totalLODLevels = 4;						// How many levels of detail to render the terrain in

	private WorldGenerator worldGenerator;
	private WorldMesher meshGenerator;
	
	private ChunkDataHandler chunkData;							// Handler for all chunk data
	
	private int[] lodUpdateCoords;								// The X,Z position of each level of detail's last update, used to confirm when a new update is needed
	
	private Vector2f[] relativeCenter;
	
	private boolean allowMeshing = true;
	public static int[] numJobsPerLOD = new int[WorldManager.totalLODLevels];
	
	public WorldManager(long seed) {
		setLevelOfDetail(totalLODLevels);
		
		worldGenerator = new WorldGenerator(seed, totalLODLevels, firstLODWidthInChunks);
		chunkData = new ChunkDataHandler(this);
		meshGenerator = new WorldMesher(chunkData, worldGenerator.getTerrainGenerator());
	}
	
	public void setLevelOfDetail(int lod) {
		totalLODLevels = lod;
		lodUpdateCoords = new int[totalLODLevels * 2];
		relativeCenter = new Vector2f[lod];
		
		for(int i = 0; i < lod; ++i)
			relativeCenter[i] = new Vector2f();
		
		for(int i = 0; i < lodUpdateCoords.length; ++i)
			lodUpdateCoords[i] = Integer.MAX_VALUE;
	}
	
	public void update(ICamera camera) {
		
		updateRunnables(camera);
		
		if (!DevFlags.lockTerrainGeneration)
			updateChunkLocations(camera);
		
		chunkData.drain();
		
		int numEmpty = 0;
		for(int i = 0; i < totalLODLevels; ++i) {
			if (numJobsPerLOD[i] != 0)
				break;

			final byte lod = worldGenerator.getCurrentLOD();
			
			if (i != 0 && numEmpty == i)
				chunkData.drain((byte) (i-1));
			
			if (lod != i) {
				++numEmpty;
				continue;
			}
			
			int numQueued = worldGenerator.startLOD();
			numJobsPerLOD[i] = numQueued;
			numEmpty += (numJobsPerLOD[i] == 0) ? 1 : 0;
		}
		
		if (numEmpty == totalLODLevels)
			chunkData.drain((byte) (totalLODLevels-1));
	}

	private void updateRunnables(ICamera camera) {
		
		meshGenerator.update();
		worldGenerator.update(camera);
		
		if (!allowMeshing)
			return;
		
		// Load meshes to openGL
		long time = System.currentTimeMillis();
		
		while(meshGenerator.hasMeshes() && (System.currentTimeMillis() - time) < MAX_TIME_MESHING_MS) {
			final ProcessedMeshContainer processedMeshes = meshGenerator.popMeshContainer();
			
			final ColoredMeshData terrainMeshData = processedMeshes.terrainMesh;
			final TexturedMeshData floraMeshData = processedMeshes.floraMesh;
			
			GenericMesh terrainMesh = new GenericMesh(4);
			terrainMesh.setBuffer(0, 3, terrainMeshData.getPositionBuffer());
			terrainMesh.setBuffer(1, 1, terrainMeshData.getColorBuffer());
			terrainMesh.setBuffer(2, 3, terrainMeshData.getNormalBuffer());
			terrainMesh.setIndexBuffer(3, terrainMeshData.getIndexBuffer());
			terrainMesh.numIndices = terrainMeshData.numIndices;
			terrainMesh.unbind(); 
			
			terrainMeshData.free();
			
			GenericMesh floraMesh = new GenericMesh(4);
			floraMesh.setBuffer(0, 3, floraMeshData.getPositionBuffer());
			floraMesh.setBuffer(1, 2, floraMeshData.getTexCoordBuffer());
			floraMesh.setBuffer(2, 3, floraMeshData.getNormalBuffer());
			floraMesh.setIndexBuffer(3, floraMeshData.getIndexBuffer());
			floraMesh.numIndices = floraMeshData.numIndices;
			floraMesh.unbind();
			
			floraMeshData.free();
			
			Chunk chunk = processedMeshes.chunk;
			
			chunk.setTerrainMesh(terrainMesh);
			chunk.setFloraMesh(floraMesh);

			--numJobsPerLOD[chunk.getLOD()];
		}
		
		while(worldGenerator.hasChunks()) {
			meshGenerator.addJob(worldGenerator.popChunk());
		}
	}
	
	private void updateChunkLocations(ICamera camera) {
		
		final int cameraX = Maths.floor(camera.getX());
		final int cameraZ = Maths.floor(camera.getZ());
		
		int chunkX, chunkZ;
		int lodChunkWidth = CHUNK_WIDTH;
		
		// The width of the entire LOD, halved
		int lodWidth = (lodChunkWidth * firstLODWidthInChunks);
		int halfLODWidth = lodWidth / 2;

		// Top left corner (the origin) of the terrain's LOD, as well as the prior origin
		int lodOriginX = Math.floorDiv(cameraX - halfLODWidth, lodChunkWidth) * lodChunkWidth;
		int lodOriginZ = Math.floorDiv(cameraZ - halfLODWidth, lodChunkWidth) * lodChunkWidth;

		int lastLODOriginX = 0, lastLODOriginZ = 0, lastLODWidth = 0;
		int lodScale = 1;

		boolean hasShifted = (lodOriginX != lodUpdateCoords[0] || lodOriginZ != lodUpdateCoords[1]);
		
		// Fast out: If we haven't moved enough to trigger an update on the smallest LOD, then there are no updates at all, skip
		if (!hasShifted)
			return;

		for(byte i = 0; i < totalLODLevels; i++) {
			int lodCoordIndex = i * 2;
			
			relativeCenter[i].set(lodOriginX + (lodWidth / 2f), lodOriginZ + (lodWidth / 2f));
			
			lodChunkWidth = lodScale * CHUNK_WIDTH;
			lodWidth = (lodChunkWidth * firstLODWidthInChunks);
			halfLODWidth = (lodWidth / 2);

			// Get LOD origin and floor by the chunk width
			lodOriginX = Math.floorDiv(cameraX - halfLODWidth, lodChunkWidth) * lodChunkWidth;
			lodOriginZ = Math.floorDiv(cameraZ - halfLODWidth, lodChunkWidth) * lodChunkWidth;
			
			HashSet<Chunk> reusableChunks = new HashSet<>();
			boolean lodHasUpdated = false;
			
			lodUpdateCoords[lodCoordIndex] = lodOriginX;
			lodUpdateCoords[lodCoordIndex + 1] = lodOriginZ;

			for(int z = 0; z < firstLODWidthInChunks; z++) {
				for(int x = 0; x < firstLODWidthInChunks; x++) {
					
					// Skip chunk locations in LOD that overlap previous LODs
					chunkX = lodOriginX + (lodChunkWidth * x);
					chunkZ = lodOriginZ + (lodChunkWidth * z);

					Seam seam = Seam.NO_SEAM;
						
					if (i != 0) {
						int lastLODRight = (lastLODOriginX + lastLODWidth - lodChunkWidth / 2);
						int lastLODBottom = (lastLODOriginZ + lastLODWidth - lodChunkWidth / 2);

						boolean pastL = chunkX >= lastLODOriginX;
						boolean beforeR = chunkX < lastLODRight;
						boolean pastT = chunkZ >= lastLODOriginZ;
						boolean beforeB = chunkZ < lastLODBottom;
						
						int flagsSet = (pastL ? 1 : 0) + (beforeR ? 1 : 0) + (pastT ? 1 : 0) + (beforeB ? 1 : 0);
						
						if (flagsSet == 4)
							continue;

						if (flagsSet == 3) {
							if (!pastL && chunkX + lodChunkWidth >= lastLODOriginX)	// If we are left of the gap
								seam = Seam.POS_X;
							else if (!pastT && chunkZ + lodChunkWidth >= lastLODOriginZ)
								seam = Seam.POS_Z;
							else if (!beforeR && chunkX - lodChunkWidth < lastLODRight)
								seam = Seam.NEG_X;
							else if (!beforeB && chunkZ - lodChunkWidth < lastLODBottom)
								seam = Seam.NEG_Z;
						}
					}
					
					lodHasUpdated = true;
					Chunk preExistingChunk = chunkData.getChunkByCoords(chunkX, chunkZ, i);
					
					if (preExistingChunk != null) {
						// If chunk already exists, mark it for re-use
						reusableChunks.add(preExistingChunk);
						
						// If the seam has changes, redraw it with the new seam
						if (seam != preExistingChunk.getSeam() && preExistingChunk.isMeshed() && i != 0) {
							preExistingChunk.setSeam(seam);
							worldGenerator.addJob(preExistingChunk);
						}
						
						continue;
					} else {
						// If it does not, create a new chunk
						Chunk chunk = chunkData.createChunk(chunkX, chunkZ, i);
						chunk.setSeam(seam);
						worldGenerator.addJob(chunk);
						
						reusableChunks.add(chunk);
					}
				}
			}
			
			// Delete unmarked chunks, reset marked status
			if (lodHasUpdated) {
				
				Iterator<Chunk> iter = chunkData.getAllChunks().iterator();
				while(iter.hasNext()) {
					Chunk chunk = iter.next();
					
					// Prevent LODs from overlapping and deleting an earlier LOD
					if (chunk.getLOD() != i)
						continue;
					
					if (!reusableChunks.contains(chunk)) {
						iter.remove();
						chunkData.markForDeletion(chunk);
					}
				}
			}
			
			lastLODOriginX = lodOriginX;
			lastLODOriginZ = lodOriginZ;
			lastLODWidth = lodWidth;
			
			lodScale *= 2f;
		}
	}

	public ChunkDataHandler getChunkData() {
		return chunkData;
	}
	
	public void destroy() {
		worldGenerator.destroy();
		meshGenerator.destroy();
		chunkData.destroy();
		allowMeshing = false;
		meshGenerator.destroy();
		
		for(Chunk chunk : chunkData.getUnmeshedChunkForRemoval()) {
			chunk.destroy();
		}
	}

	public Collection<Chunk> getChunksPendingDeletion() {
		return chunkData.getMeshedChunksForRemoval();
	}

	public Vector2f[] getRelativeCenters() {
		return this.relativeCenter;
	}

	public WorldGenerator getWorldGenerator() {
		return this.worldGenerator;
	}

	public void remeshChunk(Chunk chunk) {
		meshGenerator.addJob(chunk);
		++numJobsPerLOD[chunk.getLOD()];
	}
}
