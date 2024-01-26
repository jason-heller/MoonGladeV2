package engine.pg.biome;

public enum Temperature {
	FREEZING(0), COLD(0), MILD(1), TEMPERATE(1), SEASONAL(1), WARM(1), HOT(2), SCORCHING(2), TROPICAL(2);
	
	public static final int TOTAL_VARIANCE = 3;
	
	private final int index;	// [0,2] - points to where on the temperature scale this enum falls, 0 is coldest, 2 is hottest

	Temperature(int index) {
		this.index = index;
	}
	
	public int getIndex() {
		return index;
	}
}
