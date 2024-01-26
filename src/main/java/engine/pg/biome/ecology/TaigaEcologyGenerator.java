package engine.pg.biome.ecology;

import engine.pg.biome.Floras;

public class TaigaEcologyGenerator extends EcologyGenerator {

	public TaigaEcologyGenerator() {
		super(.9f);
		addFlora(160, Floras.NONE);
		addFlora(15, Floras.PINE);
		addFlora(10, Floras.THIN_TREE);
	}
}
