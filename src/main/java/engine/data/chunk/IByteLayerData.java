package engine.data.chunk;

public interface IByteLayerData {

	byte get(int localX, int localZ);
	byte get(int index);

	void set(byte id, int localX, int localZ);

	boolean calcHomogeneous();
	boolean isHomogeneous();
}
