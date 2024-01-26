package engine.pg.biome.managers;

public interface IBiomeManager {
	// This will control biome specific generation and updates
	
	public enum BiomeManagers {
		GENERIC;
	}
	
	public void biomeGeneration();
	
	public void biomeTick();

	public float terrainGeneration(int x, int y, int z, float biomeInfluence, float value);
}
