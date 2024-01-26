package engine.pg.biome.topography;

import engine.pg.noise.Simplex2S;

public class BadlandsTopographyGenerator implements ITopographyGenerator {

	private long seed;
	
	@Override
	public float generate(int x, int y, int z, int lodScale, float biomeInfluence, float originalValue) {
		return Math.min(originalValue, addSimplex3D(seed, x, y, z, 2000f, .01f, 4, .75f, .01f) * biomeInfluence);
	}
	
	private static float addSimplex3D(long seed, int x, int y, int z, float amplitude, float frequency, int numOctaves, float persistence, float lacunarity) {

		float value = 0f;

		for (int i = 0; i < numOctaves; i++) {
			double noise = Simplex2S.noise3_ImproveXZ(seed, x * frequency, y * frequency, z * frequency);
			
			value += amplitude * (float)noise;
			amplitude *= persistence;
			frequency *= lacunarity;
		}
	
		return value;
	}

	@Override
	public void setSeed(long seed) {
		this.seed = seed;
	}

}
