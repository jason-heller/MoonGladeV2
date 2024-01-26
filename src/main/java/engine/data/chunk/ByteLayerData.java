package engine.data.chunk;

import engine.world.Chunk;

public class ByteLayerData implements IByteLayerData {
	
	private byte[] data;

	public ByteLayerData(byte[] data) {
		this.data = data;
	}

	public ByteLayerData() {
		data = new byte[Chunk.NUM_VERTICES_XZ];
	}

	@Override
	public byte get(int localX, int localZ) {
		return data[localZ + (localX * Chunk.NUM_VERTICES_X)];
	}
	
	@Override
	public byte get(int index) {
		return data[index];
	}

	@Override
	public void set(byte id, int localX, int localZ) {
		data[localZ + (localX * Chunk.NUM_VERTICES_X)] = id;
	}

	@Override
	public boolean calcHomogeneous() {
		int id = data[0];
		
		for(int i = 1; i < Chunk.NUM_VERTICES_XZ; ++i) {
			if (data[i] != id)
				return false;
		}
		
		return true;
	}

	@Override
	public boolean isHomogeneous() {
		return false;
	}
}
