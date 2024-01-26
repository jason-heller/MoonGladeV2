package engine.pg.biome;

public enum Humidity {
	DRY(0), ARID(0), MODERATE(1), MILD(1), HUMID(2), DAMP(2), WET(2);
	
	public static final int TOTAL_VARIANCE = 3;
	
	private final int index;	// [0,2] - points to where on the humidity scale this enum falls, 0 is driest, 2 is dampest

	Humidity(int index) {
		this.index = index;
	}
	
	public int getIndex() {
		return index;
	}
}
