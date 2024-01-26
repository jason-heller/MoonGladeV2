package engine.data.chunk;

import static engine.world.Chunk.NUM_VERTICES_XY;
import static engine.world.Chunk.NUM_VERTICES_Y;

import org.joml.Vector3i;

import static engine.world.Chunk.NUM_VERTICES_X;

@Deprecated
public class ByteChunkData implements IChunkData {

	private byte[] data = new byte[NUM_VERTICES_X * NUM_VERTICES_X * NUM_VERTICES_Y];

	
	@Override
	public byte getData(int localX, int localY, int localZ) {

		int index = localY + (localX * NUM_VERTICES_Y) + (localZ * NUM_VERTICES_XY);
		return data[index];
	}

	@Override
	public byte getData(int index) {
		return data[index];
	}

	@Override
	public void setData(byte id, int localX, int localY, int localZ) {
		int index = localY + (localX * NUM_VERTICES_Y) + (localZ * NUM_VERTICES_XY);
		data[index] = id;
	}

	@Override
	public boolean calcHomogeneous() {
		int id = data[0];
		
		for(int i = 1; i < data.length; ++i) {
			if (data[i] != id)
				return false;
		}
		
		return true;
	}

	@Override
	public int getMemorySize() {
		return data.length;
	}

	@Override
	public byte getData(Vector3i localPosition) {
		return this.getData(localPosition.x, localPosition.y, localPosition.z);
	}

	@Override
	public void setData(byte id, Vector3i localPosition) {
		this.setData(id, localPosition.x, localPosition.y, localPosition.z);
	}
}
