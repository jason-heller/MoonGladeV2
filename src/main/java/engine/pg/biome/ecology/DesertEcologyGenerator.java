package engine.pg.biome.ecology;

import engine.pg.biome.Floras;

public class DesertEcologyGenerator extends EcologyGenerator {

	public DesertEcologyGenerator() {
		super(.9f);
		addFlora(95, Floras.NONE);
		addFlora(5, Floras.CACTUS);
	}
}
