package engine.data.chunk;

import static engine.world.Chunk.NUM_VERTICES_X;
import static engine.world.Chunk.NUM_VERTICES_XY;
import static engine.world.Chunk.NUM_VERTICES_Y;

import org.joml.Vector3i;

import engine.world.Chunk;

public class LayeredChunkData implements IChunkData {

	private IByteLayerData[] layers = new IByteLayerData[Chunk.NUM_VERTICES_Y];
	
	public LayeredChunkData() {
		for(int i = 0; i < Chunk.NUM_VERTICES_Y; ++i) {
			layers[i] = new ByteLayerData();
		}
	}
	
	@Override
	public byte getData(int localX, int localY, int localZ) {
		return layers[localY].get(localX, localZ);
	}
	
	@Override
	public byte getData(int index) {
		int localX = (index / NUM_VERTICES_Y) % NUM_VERTICES_X;
		int localY = index % NUM_VERTICES_Y;
		int localZ = (index / NUM_VERTICES_XY);
		
		return layers[localY].get(localX, localZ);
	}
	
	@Override
	public byte getData(Vector3i localPosition) {
		return this.getData(localPosition.x, localPosition.y, localPosition.z);
	}

	@Override
	public void setData(byte id, int localX, int localY, int localZ) {
		IByteLayerData data = layers[localY];
		if (data.isHomogeneous() && data.get(0) != id) {
			
			byte[] newData = new byte[Chunk.NUM_VERTICES_XZ];
			byte oldID = data.get(0);
			for(int i = 0; i < Chunk.NUM_VERTICES_XZ; ++i)
				newData[i] = oldID;
			
			layers[localY] = new ByteLayerData(newData);
		}
		
		layers[localY].set(id, localX, localZ);
	}

	@Override
	public void setData(byte id, Vector3i localPosition) {
		this.setData(id, localPosition.x, localPosition.y, localPosition.z);
	}

	// Memoization may be better
	@Override
	public boolean calcHomogeneous() {
		boolean homogeneous = true;
		byte firstTile = layers[0].get(0);
		
		for(int i = 0; i < Chunk.NUM_VERTICES_Y; ++i) {
			if (layers[i].calcHomogeneous()) {
				final byte tile = layers[i].get(0);
				layers[i] = new HomogeneousByteLayerData(tile);
				homogeneous &= (firstTile == tile);
			} else {
				homogeneous = false;
			}
		}
		
		return homogeneous;
	}


	@Override
	public int getMemorySize() {
		int mem = 0;
		for(int i = 0; i < Chunk.NUM_VERTICES_Y; ++i) {
			mem += (layers[i] instanceof HomogeneousByteLayerData) ? 1 : Chunk.NUM_VERTICES_XZ;
		}
		return mem;
	}
}
