package engine.pg.biome.topography;

public interface ITopographyGenerator {
	
	public void setSeed(long seed);
	
	public float generate(int x, int y, int z, int lodScale, float biomeInfluence, float originalValue);
}
