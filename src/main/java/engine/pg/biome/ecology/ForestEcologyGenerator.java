package engine.pg.biome.ecology;

import engine.pg.biome.Floras;

public class ForestEcologyGenerator extends EcologyGenerator {

	public ForestEcologyGenerator() {
		super(.9f);
		addFlora(88, Floras.NONE);
		addFlora(7, Floras.PINE);
		addFlora(3, Floras.BUSH);
		addFlora(2, Floras.BIG_BUSH);
	}
	

}
