package engine.data.chunk;

public class HomogeneousByteLayerData implements IByteLayerData {
	
	private byte id;

	public HomogeneousByteLayerData(byte id) {
		this.id = id;
	}

	@Override
	public byte get(int localX, int localZ) {
		return id;
	}

	@Override
	public byte get(int index) {
		return id;
	}

	@Override
	public void set(byte id, int localX, int localZ) {
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
