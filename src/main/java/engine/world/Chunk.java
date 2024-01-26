package engine.world;

import engine.data.chunk.IByteLayerData;
import engine.data.chunk.IChunkData;
import engine.data.chunk.IIntLayerData;
import engine.gl.data.GenericMesh;
import engine.gl.meshing.transvoxel.Seam;

public class Chunk {
	public static final int CHUNK_WIDTH = 15; // \ In tiles
	public static final int CHUNK_HEIGHT = 254; // /

	public static final int NUM_VERTICES_X = CHUNK_WIDTH + 1;
	public static final int NUM_VERTICES_Y = CHUNK_HEIGHT + 1;
	public static final int NUM_VERTICES_XZ = NUM_VERTICES_X * NUM_VERTICES_X;
	public static final int NUM_VERTICES_XY = NUM_VERTICES_X * NUM_VERTICES_Y;

	public static final int LAYER_SIZE = CHUNK_WIDTH * CHUNK_WIDTH;

	// public static final int TILE_NUM = CHUNK_WIDTH * CHUNK_WIDTH * CHUNK_HEIGHT;
	public static final int VERTEX_NUM = NUM_VERTICES_XZ * NUM_VERTICES_Y;

	private final int x, z;
	private final byte lod;

	private final ChunkMeshContainer meshContainer;
	private boolean isMeshed;

	private IChunkData tileData;
	private IByteLayerData heightData;
	private IByteLayerData floraData;
	private IIntLayerData colorData; // TODO : This could be packed into shorts im sure
	private Seam seam = Seam.NO_SEAM;

	// Can probably remove this at some point - its only in here to carry to the
	// mesher, which can be abstracted out into an intermediary class instead of
	// passing the chunk class
	// private Biome[] biomes;

	public Chunk(int x, int z, byte lod) {
		this.x = x;
		this.z = z;
		this.lod = lod;
		meshContainer = new ChunkMeshContainer(x, z, lod);
	}

	public IChunkData getTileData() {
		return tileData;
	}

	public IByteLayerData getHeightData() {
		return heightData;
	}

	public IIntLayerData getColorData() {
		return colorData;
	}

	public IByteLayerData getFloraData() {
		return floraData;
	}

	public void setTileData(IChunkData chunkData) {
		this.tileData = chunkData;
	}

	public void setHeightData(IByteLayerData heightData) {
		this.heightData = heightData;
	}

	public void setColorData(IIntLayerData colorData) {
		this.colorData = colorData;
	}

	public void setFloraData(IByteLayerData floraData) {
		this.floraData = floraData;
	}

	public GenericMesh getTerrainMesh() {
		return meshContainer.getTerrainMesh();
	}

	public GenericMesh getFloraMesh() {
		return meshContainer.getFloraMesh();
	}

	public void setTerrainMesh(GenericMesh terrainMesh) {
		meshContainer.setTerrainMesh(terrainMesh);
	}

	public void setFloraMesh(GenericMesh floraMesh) {
		meshContainer.setFloraMesh(floraMesh);
		isMeshed = true;
	}

	public boolean isMeshed() {
		return isMeshed;
	}

	public int getX() {
		return x;
	}

	public int getZ() {
		return z;
	}

	public int getMemorySize() {
		return tileData == null ? 0 : tileData.getMemorySize();
	}

	public byte getLOD() {
		return lod;
	}

	public void destroy() {
		meshContainer.destroy();
	}

	public ChunkMeshContainer getMeshContainer() {
		return meshContainer;
	}

	public void setSeam(Seam seam) {
		this.seam = seam;
	}

	public Seam getSeam() {
		return seam;
	}

	public void setMeshed(boolean b) {
		this.isMeshed = b;
	}

	public int getWidth() {
		return Chunk.CHUNK_WIDTH * getScale();
	}

	public int getHeight() {
		return Chunk.CHUNK_HEIGHT * getScale();
	}

	public int getScale() {
		return (1 << lod);
	}

	public void setTile(byte id, int localX, int localY, int localZ) {
		setVertex(id, localX,     localY, localZ);
		setVertex(id, localX + 1, localY, localZ);
		setVertex(id, localX,     localY, localZ + 1);
		setVertex(id, localX + 1, localY, localZ + 1);
		
		setVertex(id, localX,     localY + 1, localZ);
		setVertex(id, localX + 1, localY + 1, localZ);
		setVertex(id, localX,     localY + 1, localZ + 1);
		setVertex(id, localX + 1, localY + 1, localZ + 1);
	}
	
	public void setFace(byte id, int localX, int localY, int localZ, int dx, int dz) {
		setVertex(id, localX, localY, localZ);
		setVertex(id, localX + dx, localY, localZ + dz);
		setVertex(id, localX, localY + 1, localZ);
		setVertex(id, localX + dx, localY + 1, localZ + dz);
	}
	
	public void setVertex(byte id, int localX, int localY, int localZ) {
		tileData.setData(id, localX, localY, localZ);
	}
}
