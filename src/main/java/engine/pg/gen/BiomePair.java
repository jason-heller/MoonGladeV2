package engine.pg.gen;

import engine.pg.biome.Biome;

public class BiomePair {
	
	public Biome biome;
	public float influence;
	public int foliageColor;
	public float terrainEdgeScale;
	
	public BiomePair(Biome biome, float influence, float terrainEdgeScale, int foliageColor) {
		this.biome = biome;
		this.influence = influence;
		this.foliageColor = foliageColor;
		this.terrainEdgeScale = terrainEdgeScale;
	}
}