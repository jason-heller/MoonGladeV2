package engine.pg.biome.ecology;

import engine.pg.biome.Floras;

public class MarshEcologyGenerator extends EcologyGenerator {

	public MarshEcologyGenerator() {
		super(.9f);
		addFlora(78, Floras.NONE);
		addFlora(15, Floras.CATTAIL);
		addFlora(10, Floras.ROCK);
		addFlora(2, Floras.THIN_TREE);
	}
}
