package engine.pg.gen;

import static engine.world.Chunk.NUM_VERTICES_X;
import static engine.world.Chunk.NUM_VERTICES_XY;
import static engine.world.Chunk.NUM_VERTICES_Y;
import static engine.world.Chunk.VERTEX_NUM;

import engine.data.chunk.ByteLayerData;
import engine.data.chunk.HomogeneousByteLayerData;
import engine.data.chunk.HomogeneousChunkData;
import engine.data.chunk.IChunkData;
import engine.data.chunk.IByteLayerData;
import engine.data.chunk.LayeredChunkData;
import engine.pg.biome.Biome;
import engine.pg.noise.Simplex2S;
import engine.utils.math.Maths;
import engine.world.Chunk;

public class TerrainGenerator {
	
	//private static final float HEIGHTMAP_AMPLITUDE = 1f;
	//private static final float HEIGHTMAP_FREQUENCY = .001f;
	private static final int   HEIGHTMAP_NUM_OCTAVES = 1;
	public static final float HEIGHTMAP_SMOOTHNESS = 16f;		// density increases 8x the speed of the heightmap grid
	private static final float HEIGHTMAP_PERSISTANCE = .5f;
	//private static final float HEIGHTMAP_LACUNARITY = .5f;
	private static final float BASE_Y = 75;
	
	public static float terrainCourseness = 0;

	private static long terrainSeed;
	
	private byte[] upperBounds = new byte[NUM_VERTICES_X * NUM_VERTICES_X];
	
	public TerrainGenerator(long seed) {
		terrainSeed = 1111;//seed * 757577;
		
		float deltaScale = HEIGHTMAP_SMOOTHNESS;
		for(int i = 0; i < HEIGHTMAP_NUM_OCTAVES; ++i) {
			terrainCourseness += deltaScale;
			deltaScale *= HEIGHTMAP_PERSISTANCE;
		}
	}

	public TerrainGeneratorData generateChunk(Chunk chunk, BiomeGeneratorData biomeData) {
		
		//final byte[] tiles = new byte[VERTEX_NUM];
		IChunkData tiles = new LayeredChunkData();
		final byte[] heights = new byte[NUM_VERTICES_X * NUM_VERTICES_X];
		final int originX = chunk.getX();
		final int originZ = chunk.getZ();
		final int scale = 1 << chunk.getLOD();
		
		boolean uniformHeights = true;
		
		int x, y, z, localX, localY, localZ, biomeIndex, ubIndex;

		for (int i = 0; i < VERTEX_NUM; i++) {
			localX = (i / NUM_VERTICES_Y) % NUM_VERTICES_X;
			localY = i % NUM_VERTICES_Y;
			localZ = (i / NUM_VERTICES_XY);

			x = (localX * scale) + originX;
			y = localY * scale;
			z = (localZ * scale) + originZ;

			biomeIndex = localX + (localZ * NUM_VERTICES_X);
			float density = noiseAt(x, y, z, biomeData, biomeIndex, chunk.getLOD());
			tiles.setData((byte) density, localX, localY, localZ);
			
			// TODO: find better way to get height
			if (density >= 1f) {
				heights[localX + (localZ * NUM_VERTICES_X)] = (byte)localY;
				uniformHeights = false;
			}
			
			ubIndex = localX + (localZ * NUM_VERTICES_X);
			upperBounds[ubIndex] = 0;
			
			if (tiles.getData(i) >= 0)
				upperBounds[ubIndex] = (byte)localY;
		}

		if (tiles.calcHomogeneous())
			tiles = new HomogeneousChunkData(tiles.getData(0));
		
		IByteLayerData heightData = uniformHeights ? new HomogeneousByteLayerData((byte)0) : new ByteLayerData(heights);
		return new TerrainGeneratorData(heightData, tiles);
	}

	private float noiseAt(int x, int y, int z, BiomeGeneratorData biomeData, int biomeIndex, byte lod) {
		// Base heightmap for all terrain
		//float density = addHeightmap(x, y, z, lod);
		final float smoothness = HEIGHTMAP_SMOOTHNESS / (1 << lod);
		
		float density = ((BASE_Y - y) * smoothness);
		
		// Add biome specific terrain
		Biome biome = biomeData.getBiome(biomeIndex);
		float terrainEdgeScale = biomeData.getBiomeEdgeScale(biomeIndex);

		density = biome.getTopography().generate(x, y, z, 1 << lod, terrainEdgeScale, density);
		density = Maths.clamp(density, -127.9f, 126.9f);
		
		return density;
	}
	
	public byte[] getCurrentUpperBounds() {
		return upperBounds;
	}
}
