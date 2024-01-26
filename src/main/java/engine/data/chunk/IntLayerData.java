package engine.data.chunk;

import engine.world.Chunk;

public class IntLayerData implements IIntLayerData {
	
	private int[] data = new int[Chunk.NUM_VERTICES_XZ];

	public IntLayerData(int[] data) {
		this.data = data;
	}

	@Override
	public int get(int localX, int localZ) {
		return data[localZ + (localX * Chunk.NUM_VERTICES_X)];
	}
	
	@Override
	public int get(int index) {
		return data[index];
	}

	@Override
	public void set(int id, int localX, int localZ) {
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
