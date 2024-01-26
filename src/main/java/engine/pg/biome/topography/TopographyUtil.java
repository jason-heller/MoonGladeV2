package engine.pg.biome.topography;

import engine.pg.noise.Simplex2S;

public class TopographyUtil {

	public static final float HEIGHTMAP_SMOOTHNESS = 16f;		// density increases 8x the speed of the heightmap grid
	
	public static float addHeightmapPositive(long seed, int x, int y, int z, int lodScale, float amplitude, float frequency, int numOctaves, float persistance, float lacunarity) {
		double amp = amplitude;
		double freq = frequency;
		double height = 0f;
		
		double vertexDensity = 0f;
		
		for (int i = 0; i < numOctaves; i++) {
			double heightNoise = Simplex2S.noise2(seed, x * freq, z * freq) + 1.0;
			height += amp * heightNoise;
			amp *= persistance;
			freq *= lacunarity;
		}
		
		final double smoothness = HEIGHTMAP_SMOOTHNESS / lodScale;
		
		vertexDensity = ((height - y) * smoothness);
		
		return (float) vertexDensity;
	}
	
	public static float addHeightmap(long seed, int x, int y, int z, int lodScale, float amplitude, float frequency, int numOctaves, float persistance, float lacunarity) {
		double amp = amplitude;
		double freq = frequency;
		double height = 0f;
		
		double vertexDensity = 0f;
		
		for (int i = 0; i < numOctaves; i++) {
			double heightNoise = Simplex2S.noise2(seed, x * freq, z * freq);
			height += amp * heightNoise;
			amp *= persistance;
			freq *= lacunarity;
		}
		
		final double smoothness = HEIGHTMAP_SMOOTHNESS / lodScale;
		
		vertexDensity = ((height - y) * smoothness);
		
		return (float) vertexDensity;
	}

	public static float addHeightmap(long seed, int x, int y, int z, int lodScale, float amplitude, float frequency,
			int numOctaves) {
		return addHeightmap(seed, x, y, z, lodScale, amplitude, frequency, numOctaves, .5f, .5f);
	}
}
