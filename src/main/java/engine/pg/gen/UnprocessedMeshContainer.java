package engine.pg.gen;

import engine.data.chunk.HomogeneousByteLayerData;
import engine.data.chunk.HomogeneousIntLayerData;
import engine.data.chunk.IByteLayerData;
import engine.data.chunk.IChunkData;
import engine.data.chunk.IIntLayerData;
import engine.gl.meshing.transvoxel.Seam;
import engine.world.Chunk;

@Deprecated
public class UnprocessedMeshContainer {
	
	// This carries data from world gen over to the meshing thread, sharing useful data
	private Chunk chunk;
	
	private IIntLayerData colors;
	private IByteLayerData heights;
	private IByteLayerData floraIDs;
	
	private Seam seam;
	
	//private IIntData terrainLowerBounds;	<-- This can replace"hasAscendingDensities" but likely uneeded
	private byte[] terrainUpperBounds;

	public int failedAttempts;
	
	public UnprocessedMeshContainer(Chunk chunk, Seam seam, byte[] terrainUpperBounds) {
		this.chunk = chunk;
		this.terrainUpperBounds = new byte[terrainUpperBounds.length];
		System.arraycopy(terrainUpperBounds, 0, this.terrainUpperBounds, 0, terrainUpperBounds.length);
		this.seam = seam;
	}
	
	public UnprocessedMeshContainer(Chunk chunk) {
		this.chunk = chunk;
		this.heights = chunk.getHeightData();
		this.colors = chunk.getColorData();
		this.floraIDs = chunk.getFloraData();
		this.terrainUpperBounds = new byte[Chunk.NUM_VERTICES_XZ];
		this.seam = chunk.getSeam();
	}

	public void setColors(IIntLayerData colors) {
		this.colors = colors;
	}
	
	public void setFloras(IByteLayerData floraIDs) {
		if (floraIDs == null) {
			this.floraIDs = new HomogeneousByteLayerData((byte) 0);
		} else {
			this.floraIDs = floraIDs;
		}
	}
	
	public byte getLOD() {
		return chunk.getLOD();
	}
	
	public int getX() {
		return chunk.getX();
	}
	
	public int getZ() {
		return chunk.getZ();
	}
	
	public IChunkData getTileData() {
		return chunk.getTileData();
	}
	
	public IIntLayerData getColors() {
		return colors;
	}
	
	public byte[] getTerrainUpperBounds() {
		return terrainUpperBounds;
	}
	
	public IByteLayerData getHeights() {
		return heights;
	}

	public IByteLayerData getFloraData() {
		return floraIDs;
	}

	public void setHeights(IByteLayerData heights) {
		this.heights = heights;
	}

	public Seam getSeam() {
		return seam;
	}

	public void setSeam(Seam seam) {
		this.seam = seam;
	}

	/**
	 * @deprecated This method is not thread safe, should not be used over the getters in this object
	 */
	@Deprecated
	public Chunk getChunk() {
		return chunk;
	}

	public Chunk passToProcessedChunks() {
		chunk.setSeam(seam);
		return chunk;
	}
}
