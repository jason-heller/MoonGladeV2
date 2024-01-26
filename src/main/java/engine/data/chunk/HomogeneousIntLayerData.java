package engine.data.chunk;

public class HomogeneousIntLayerData implements IIntLayerData {
	
	private int id;

	public HomogeneousIntLayerData(int id) {
		this.id = id;
	}

	@Override
	public int get(int localX, int localZ) {
		return id;
	}

	@Override
	public int get(int index) {
		return id;
	}

	@Override
	public void set(int id, int localX, int localZ) {
		this.id = id;
	}

	@Override
	public boolean calcHomogeneous() {
		return true;
	}

	@Override
	public boolean isHomogeneous() {
		return true;
	}
}
