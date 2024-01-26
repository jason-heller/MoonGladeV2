package engine.world;

public class ChunkKey {

	private final int x;
	private final int z;
	private final byte lod;

	public ChunkKey(int x, int z, byte lod) {
		this.x = x;
		this.z = z;
		this.lod = lod;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof ChunkKey))
			return false;
		ChunkKey key = (ChunkKey) o;
		return x == key.x && z == key.z && lod == key.lod;
	}

	@Override
	public int hashCode() {
		int result = x;
		result = (31 * result) + (163 * lod) + z;
		return result;
	}
	
	public int getLOD() {
		return lod;
	}
	
	public int getX() {
		return x;
	}
	
	public int getZ() {
		return z;
	}
}