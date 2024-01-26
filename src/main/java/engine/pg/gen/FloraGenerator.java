package engine.pg.gen;

import engine.data.chunk.ByteLayerData;
import engine.data.chunk.HomogeneousByteLayerData;
import engine.data.chunk.IByteLayerData;
import engine.pg.biome.Biome;
import engine.pg.noise.Simplex2S;
import engine.world.Chunk;

public class FloraGenerator {
	
	private long seed;

	public FloraGenerator(long seed) {
		this.seed = seed;
	}

	public IByteLayerData generateChunk(Chunk chunk, BiomeGeneratorData biomeData) {
		int chunkScale = 1 << chunk.getLOD();
		int index = 0;
		int dx, dz;
		
		final int FLORA_PER_ROW = Chunk.CHUNK_WIDTH * chunkScale;
		boolean uniform = true;
		
		byte[] floraIDs = new byte[FLORA_PER_ROW * FLORA_PER_ROW];
		for(int z = 0; z < FLORA_PER_ROW; z ++) {
			for(int x = 0; x < FLORA_PER_ROW; x ++) {
				dx = chunk.getX() + x;
				dz = chunk.getZ() + z;
				
				int biomeIndex = (x / chunkScale) + ((z / chunkScale) * Chunk.NUM_VERTICES_X);

				floraIDs[index] = ecologyAt(dx, dz, biomeData, biomeIndex);
				
				// Determine scale and rotation
				if (floraIDs[index] != 0) {
					floraIDs[index] |= calculateTransform(floraIDs[index], Simplex2S.noise2(-seed, dx, dz));
					uniform = false;
				}
				
				++index;
			}
		}
		
		return uniform ? new HomogeneousByteLayerData((byte) 0) : new ByteLayerData(floraIDs);
	}
	
	private byte ecologyAt(int x, int z, BiomeGeneratorData biomeData, int biomeIndex) {
		// Add biome specific terrain
		Biome biome = biomeData.getBiome(biomeIndex);
		float terrainEdgeScale = biomeData.getBiomeInfluence(biomeIndex);

		return biome.getEcology().generate(x, z, terrainEdgeScale);
	}

	private int calculateTransform(float density, float variance) {
		boolean flipX = (int)(density * 10f) % 3 == 0;
		boolean flipZ = (int)(density * 10f) % 2 == 1;
		byte scale = (byte)(variance * 15f);
		
		return ((flipX ? 1 : 0) << 31) | ((flipZ ? 1 : 0) << 30) | (scale << 26);
	}

}
