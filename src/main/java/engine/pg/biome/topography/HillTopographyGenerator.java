package engine.pg.biome.topography;

import engine.pg.gen.TerrainGenerator;
import engine.pg.noise.Simplex2S;
import engine.world.Chunk;

public class HillTopographyGenerator implements ITopographyGenerator {

	private long seed = 234234;
	
	private float amplitude, frequency;
	private int numOctaves;
	
	public HillTopographyGenerator(float amplitude, float frequency, int numOctaves) {
		this.amplitude = amplitude;
		this.frequency = frequency;
		this.numOctaves = numOctaves;
	}
	
	@Override
	public float generate(int x, int y, int z, int lodScale, float terrainEdgeScale, float vertexDensity) {
		// density - ChunkMesher.ISO_LEVEL is negative if above ground, negative if above. The bigger the more further away
		float height = TopographyUtil.addHeightmapPositive(seed, x, y, z, lodScale, amplitude, frequency, numOctaves, .5f, .5f);
		
		return vertexDensity + (height * terrainEdgeScale);
	}
	
	@Override
	public void setSeed(long seed) {
		this.seed = seed;
	}

}
