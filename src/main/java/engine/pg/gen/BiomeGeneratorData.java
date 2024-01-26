package engine.pg.gen;

import engine.data.chunk.HomogeneousIntLayerData;
import engine.data.chunk.IIntLayerData;
import engine.data.chunk.IntLayerData;
import engine.pg.biome.Biome;

public class BiomeGeneratorData {
	private final BiomePair[] biomeData;				// Hold onto the last generated biome matrix for use by the terrain generator
	
	public BiomeGeneratorData(BiomePair[] biomeData) {
		this.biomeData = biomeData;
	}
	
	public BiomePair[] getBiomeData() {
		return biomeData;
	}
	
	public Biome getBiome(int index) {
		return biomeData[index].biome;
	}
	
	public float getBiomeInfluence(int index) {
		return biomeData[index].influence;
	}
	
	public float getBiomeEdgeScale(int index) {
		return biomeData[index].terrainEdgeScale;
	}
	
	public IIntLayerData getBiomeColors() {
		final int len = biomeData.length;
		int[] colors = new int[len];
		
		int firstColor = biomeData[0].foliageColor;
		boolean uniform = true;
		
		for(int i = 0; i < len; ++i) {
			colors[i] = biomeData[i].foliageColor;
			if (colors[i] != firstColor) 
				uniform = false;
		}
		
		return uniform ? new HomogeneousIntLayerData(firstColor) : new IntLayerData(colors);
	}
}
