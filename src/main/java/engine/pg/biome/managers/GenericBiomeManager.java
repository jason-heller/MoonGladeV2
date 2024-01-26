package engine.pg.biome.managers;

public class GenericBiomeManager implements IBiomeManager {

	@Override
	public void biomeGeneration() {
		
	}

	@Override
	public void biomeTick() {
		// TODO Auto-generated method stub

	}

	@Override
	public float terrainGeneration(int x, int y, int z, float biomeInfluence, float value) {
		return value;
	}

}
