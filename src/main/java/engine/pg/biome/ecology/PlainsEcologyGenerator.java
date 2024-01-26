package engine.pg.biome.ecology;

import engine.pg.biome.Floras;

public class PlainsEcologyGenerator extends EcologyGenerator {

	public PlainsEcologyGenerator() {
		super(.9f);
		addFlora(95, Floras.NONE);
		addFlora(5, Floras.SNOWY_PINE);
	}
}
