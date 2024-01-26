package engine.world;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.joml.Vector3f;
import org.joml.Vector3i;

import engine.data.chunk.IChunkData;
import engine.geo.MarchingCubes;
import engine.gl.meshing.transvoxel.ScalarField;
import engine.utils.math.Maths;

import static engine.world.Chunk.CHUNK_WIDTH;

public class ChunkDataHandler {


	private Map<ChunkKey, Chunk> chunkData; // 'local' meaning does not account for chunk position
	
	private Set<Chunk> unmeshedChunkForRemoval = new HashSet<>();
	private Map<ChunkKey, Chunk> meshedChunksForRemoval = new HashMap<>();
	
	private MarchingCubes mc;	// Used for referencing chunk data related to terrain
	private WorldManager worldManager;	// Pointer to owner

	public ChunkDataHandler(WorldManager worldManager) {
		Comparator<ChunkKey> comparator = Comparator.comparingInt(ChunkKey::getLOD).thenComparingInt(ChunkKey::getX)
				.thenComparingInt(ChunkKey::getZ);

		chunkData = new TreeMap<>(comparator);
		this.worldManager = worldManager;
		
		mc = new MarchingCubes(1f, 0f);
	}

	public Chunk createChunk(int x, int z, byte lod) {
		final Chunk chunk = new Chunk(x, z, lod);
		
		chunkData.put(getKey(x, z, lod), chunk);

		return chunk;
	}
	
	public Collection<Chunk> getAllChunks() {
		return chunkData.values();
	}

	public Chunk getChunkByCoords(int x, int z, byte lod) {
		return chunkData.get(getKey(x, z, lod));
	}
	
	// TODO: This is slow
	public Chunk getChunkContaining(int x, int z) {
		//int xVertexSpace = Maths.floor(x / Chunk.NUM_VERTICES_X);
		//int zVertexSpace = Maths.floor(z / Chunk.NUM_VERTICES_X);
		
		for(Chunk chunk : getAllChunks()) {
			if (chunk.getX() <= x && chunk.getZ() <= z
				&& chunk.getX() + chunk.getWidth() > x && chunk.getZ() + chunk.getWidth() > z)
				return chunk;
		}
		
		return null;
	}

	public List<Vector3f> getVerticesWithin(Vector3f min, Vector3f max) {
		final int MAX_HEIGHT = Chunk.NUM_VERTICES_Y - 1;
		final int maxX = Maths.ceil(max.x);
		final int maxY = Math.min((int)max.y, MAX_HEIGHT);
		final int maxZ = Maths.ceil(max.z);
		
		final int minX = Maths.floor(min.x);
		final int minY = Math.min((int)min.y - 1, MAX_HEIGHT);
		final int minZ = Maths.floor(min.z);

		final List<Vector3f> vertices = new ArrayList<>(((maxX - minX) * (maxY - minY) * (maxX - minX)) * 3);

		for (int x = minX; x < maxX; ++x) {
			for (int z = minZ; z < maxZ; ++z) {
				
				final Chunk chunk = getChunkContaining(x, z);
				if (chunk == null || chunk.getTileData() == null)
					return new ArrayList<>(0);

				final IChunkData data = chunk.getTileData();
				mc.setScalarField(new ScalarField() {
					public float getDensity(int x, int y, int z) {
						if (y <= 1)
							return 1;
						if (y >= MAX_HEIGHT)
							return 0;
						return data.getData(x - chunk.getX(), y, z - chunk.getZ());
					}
				});
				
				for (int y = minY; y <= maxY; ++y) {
					//final byte tile = chunk.getTileData().getData(tileX, y, tileZ);
					Vector3f[] verticesAtIndex = mc.getVertices(x, y, z);
					
					
					for(Vector3f vertex : verticesAtIndex)
						vertices.add(vertex);
					
				}
			}
		}
		
		return vertices;
	}

	public byte getTileAt(int x, int y, int z) {
		final Chunk chunk = getChunkContaining(x ,z);
		if (chunk == null || chunk.getTileData() == null) {
			return 0;
		}
		final int chunkX = chunk.getX();
		final int chunkZ = chunk.getZ();
		return chunk.getTileData().getData(x - chunkX, y, z - chunkZ);
	}
	
	public byte getTileAt(Vector3i position) {
		return getTileAt(position.x, position.y, position.z);
	}

	private ChunkKey getKey(int x, int z, byte lod) {
		return new ChunkKey(x, z, lod);
	}
	
	public void drain() {
		
		Iterator<Chunk> iter = unmeshedChunkForRemoval.iterator(); 
		while(iter.hasNext()) {
			Chunk chunk = iter.next();
		    if (chunk.isMeshed()) {
				chunk.destroy();
				iter.remove();
		    }
		}
	}
	
	public void drain(byte lod) {
		Iterator<Entry<ChunkKey, Chunk>> iter = meshedChunksForRemoval.entrySet().iterator();
		while(iter.hasNext()) {
			Chunk chunk = iter.next().getValue();
			if (chunk.getLOD() == lod) {
				chunk.destroy();
				iter.remove();
			}
		}
	}

	public void destroy() {
		for (Chunk chunk : getAllChunks()) {
			chunk.destroy();
		}
		
		for (Chunk chunk : meshedChunksForRemoval.values()) {
			chunk.destroy();
		}
	}
	
	public void markForDeletion(Chunk chunk) {
		if (chunk.isMeshed()) {
			meshedChunksForRemoval.put(getKey(chunk.getX(), chunk.getZ(), chunk.getLOD()), chunk);
			//chunk.clearData();
		} else {
			unmeshedChunkForRemoval.add(chunk);
		}
	}

	public boolean chunkReplace(int x, int z, byte lod) {
		Chunk chunk = chunkData.remove(getKey(x, z, lod));
		
		return (chunk != null);
	}
	
	public Collection<Chunk> getMeshedChunksForRemoval() {
		return meshedChunksForRemoval.values();
	}

	public Set<Chunk> getUnmeshedChunkForRemoval() {
		return unmeshedChunkForRemoval;
	}
	
	public void setTile(byte id, Chunk chunk, int localX, int localY, int localZ) {
		chunk.setTile(id, localX, localY, localZ);
		worldManager.remeshChunk(chunk);

		byte lod = chunk.getLOD();
		int cx = chunk.getX(), cz = chunk.getZ();
		int width = chunk.getWidth();
		
		// Edges
		if (localX == 0) {
			Chunk neighbor = getChunkByCoords(cx - width, cz, lod);
			if (neighbor != null && neighbor.isMeshed()) {
				neighbor.setFace(id, CHUNK_WIDTH, localY, localZ, 0, 1);
				worldManager.remeshChunk(neighbor);
			}
		}

		if (localX == CHUNK_WIDTH-1) {
			Chunk neighbor = getChunkByCoords(cx + width, cz, lod);
			if (neighbor != null && neighbor.isMeshed()) {
				neighbor.setFace(id, 0, localY, localZ, 0, 1);
				worldManager.remeshChunk(neighbor);
			}
		}

		if (localZ == 0) {
			Chunk neighbor = getChunkByCoords(cx, cz - width, lod);
			if (neighbor != null && neighbor.isMeshed()) {
				neighbor.setFace(id, localX, localY, CHUNK_WIDTH, 1, 0);
				worldManager.remeshChunk(neighbor);
			}
		}

		if (localZ == CHUNK_WIDTH-1) {
			Chunk neighbor = getChunkByCoords(cx, cz + width, lod);
			if (neighbor != null && neighbor.isMeshed()) {
				neighbor.setFace(id, localX, localY, 0, 1, 0);
				worldManager.remeshChunk(neighbor);
			}
		}
		
		// Corners
		if (localX == 0 && localZ == 0) {
			Chunk neighbor = getChunkByCoords(cx - width, cz - width, lod);
			if (neighbor != null && neighbor.isMeshed()) {
				neighbor.setVertex(id, CHUNK_WIDTH, localY, CHUNK_WIDTH);
				neighbor.setVertex(id, CHUNK_WIDTH, localY + 1, CHUNK_WIDTH);
				worldManager.remeshChunk(neighbor);
			}
		}

		if (localX == 0 && localZ == CHUNK_WIDTH-1) {
			Chunk neighbor = getChunkByCoords(cx - width, cz + width, lod);
			if (neighbor != null && neighbor.isMeshed()) {
				neighbor.setVertex(id, CHUNK_WIDTH, localY, 0);
				neighbor.setVertex(id, CHUNK_WIDTH, localY + 1, 0);
				worldManager.remeshChunk(neighbor);
			}
		}

		if (localX == CHUNK_WIDTH-1 && localZ == 0) {
			Chunk neighbor = getChunkByCoords(cx + width, cz - width, lod);
			if (neighbor != null && neighbor.isMeshed()) {
				neighbor.setVertex(id, 0, localY, CHUNK_WIDTH);
				neighbor.setVertex(id, 0, localY + 1, CHUNK_WIDTH);
				worldManager.remeshChunk(neighbor);
			}
		}

		if (localX == CHUNK_WIDTH-1 && localZ == CHUNK_WIDTH-1) {
			Chunk neighbor = getChunkByCoords(cx + width, cz -+ width, lod);
			if (neighbor != null && neighbor.isMeshed()) {
				neighbor.setVertex(id, 0, localY, 0);
				neighbor.setVertex(id, 0, localY + 1, 0);
				worldManager.remeshChunk(neighbor);
			}
		}
	}
}