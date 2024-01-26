package engine.data.chunk;

import org.joml.Vector3i;

public class HomogeneousChunkData implements IChunkData {

	private byte id;
	
	public HomogeneousChunkData(byte id) {
		this.id = id;
	}
	
	@Override
	public byte getData(int localX, int localY, int localZ) {
		return id;
	}

	@Override
	public byte getData(Vector3i localPosition) {
		return this.getData(localPosition.x, localPosition.y, localPosition.z);
	}
	
	@Override
	public void setData(byte id, int localX, int localY, int localZ) {
		this.id = id;
	}

	@Override
	public void setData(byte id, Vector3i localPosition) {
		this.setData(id, localPosition.x, localPosition.y, localPosition.z);
	}

	@Override
	public boolean calcHomogeneous() {
		return true;
	}

	@Override
	public byte getData(int index) {
		return id;
	}

	@Override
	public int getMemorySize() {
		return 1;
	}
}
