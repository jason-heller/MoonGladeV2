package engine.pg.biome;

import engine.pg.biome.ecology.DesertEcologyGenerator;
import engine.pg.biome.ecology.EcologyGenerator;
import engine.pg.biome.ecology.ForestEcologyGenerator;
import engine.pg.biome.ecology.MarshEcologyGenerator;
import engine.pg.biome.ecology.PlainsEcologyGenerator;
import engine.pg.biome.ecology.TaigaEcologyGenerator;

public enum Ecology {
	
	DEAD(null, 0),
	STEPPE(new PlainsEcologyGenerator(), 1290312),
	DESERT(new DesertEcologyGenerator(), 323245),
	FOREST(new ForestEcologyGenerator(), 3951371),
	MARSH(new MarshEcologyGenerator(), 3951371),
	TAIGA(new TaigaEcologyGenerator(), 3951371);
	
	private EcologyGenerator ecology;
	private long offset;
	
	private Ecology(EcologyGenerator ecology, long offset) {
		this.ecology = ecology;
		this.offset = offset;
	}
	
	public byte generate(int x, int z, float terrainEdgeScale) {
		if (this.ecology == null)
			return 0;
		
		return (byte)ecology.generate(x, z, terrainEdgeScale).ordinal();
	}
	
	public static void setSeeds(long seed) {
		for(Ecology t : values()) {
			if (t.ecology != null)
				t.setSeed(seed);
		}
	}
	
	public void setSeed(long seed) {
		long x = seed * offset;
		
		ecology.setSeed(x);
	}
}