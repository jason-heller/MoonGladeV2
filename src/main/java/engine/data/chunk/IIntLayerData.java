package engine.data.chunk;

public interface IIntLayerData {

	int get(int localX, int localZ);
	int get(int index);

	void set(int id, int localX, int localZ);

	boolean calcHomogeneous();
	boolean isHomogeneous();
}
