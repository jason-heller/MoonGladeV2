package engine.data.chunk;

import org.joml.Vector3i;

public interface IChunkData {
	public byte getData(int localX, int localY, int localZ);

	public byte getData(int index);

	public byte getData(Vector3i localPosition);
	
	public void setData(byte id, int localX, int localY, int localZ);

	public void setData(byte id, Vector3i localPosition);
	
	boolean calcHomogeneous();

	public int getMemorySize();


}
