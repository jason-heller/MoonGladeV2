package engine.pg.biome.topography;

import engine.pg.gen.TerrainGenerator;

public class MesaTopographyGenerator implements ITopographyGenerator {

	//private long seed;
	
	@Override
	public float generate(int x, int y, int z, int lodScale, float terrainEdgeScale, float vertexDensity) {
		// density - ChunkMesher.ISO_LEVEL is negative if above ground, negative if above. The bigger the more further away
		float cliffHeight = (TerrainGenerator.terrainCourseness * 40f * terrainEdgeScale);
		if (vertexDensity >= -cliffHeight) {
			return vertexDensity + cliffHeight - TerrainGenerator.terrainCourseness;
		}
		
		return vertexDensity;
	}

	@Override
	public void setSeed(long seed) {
		//this.seed = seed;
	}

}
