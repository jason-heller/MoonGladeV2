package engine.pg.gen;

import engine.data.chunk.IChunkData;
import engine.data.chunk.IByteLayerData;

public class TerrainGeneratorData {
	public final IChunkData tiles;
	public final IByteLayerData heights;
	
	public TerrainGeneratorData(IByteLayerData heights, IChunkData tiles) {
		this.heights = heights;
		this.tiles = tiles;
	}

	public IChunkData getTiles() {
		return tiles;
	}

	public IByteLayerData getHeights() {
		return heights;
	}
}
