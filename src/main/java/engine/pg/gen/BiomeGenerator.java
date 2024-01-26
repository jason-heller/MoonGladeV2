package engine.pg.gen;

import static engine.world.Chunk.CHUNK_WIDTH;
import static engine.world.Chunk.NUM_VERTICES_X;

import java.util.LinkedList;
import java.util.List;

import engine.dev.DevFlags;
import engine.gl.ICamera;
import engine.pg.biome.Biome;
import engine.pg.biome.BiomeData;
import engine.pg.biome.Topography;
import engine.pg.noise.Simplex2S;
import engine.pg.noise.TranslatableVoronoi;
import engine.pg.noise.VoronoiDataPoint;
import engine.utils.ColorUtil;
import engine.utils.math.Maths;
import engine.world.Chunk;

public class BiomeGenerator {
	
	private BiomeData biomeData = new BiomeData();
	
	private static final float CLIMATE_SCALE = 1f;
	
	private static final int TRANSITON_SIZE = 8;
	private static final int TRANSITION_SQR = TRANSITON_SIZE * TRANSITON_SIZE;
	
	private static final float BIOME_DISTORTION_FREQ = 1f;		// 5
	private static final float BIOME_DISTORTION_SCALE = .5f;	// .1f

	private static final int BIOME_VORONOI_SIZE = 32;
	
	private TranslatableVoronoi biomeVoronoi;
	
	private static long tempSeed, humidSeed, varianceSeed;
	
	private long[] biomeDistortionSeeds;

	public BiomeGenerator(long seed, int numLODs, int firstLODWidthInChunks) {
		tempSeed = seed * 668265263;
		humidSeed = seed * 2246822519l + 374761393;
		varianceSeed = seed * 3266489917l ^ tempSeed >> 13;
		biomeDistortionSeeds = new long[2];
		biomeDistortionSeeds[0] = seed * 1049l;
		biomeDistortionSeeds[1] = seed * 100049l;
		
		int mapWidth = firstLODWidthInChunks;
		for(int i = 1; i < numLODs; i++) {
			mapWidth += (2<<i) * 4;
		}
		
		biomeVoronoi = new TranslatableVoronoi(seed, BIOME_VORONOI_SIZE * CHUNK_WIDTH, mapWidth * CHUNK_WIDTH);

		Topography.setSeeds(seed);
	}
	
	public void update(ICamera camera) {
		biomeVoronoi.update(camera.getX(), camera.getZ());
	}
	
	public BiomeGeneratorData generateChunk(Chunk chunk) {
		final BiomePair[] biomeData = new BiomePair[NUM_VERTICES_X * NUM_VERTICES_X];
		int originX = chunk.getX();
		int originZ = chunk.getZ();
		int scale = 1 << chunk.getLOD();
		

		for (int i = 0; i < biomeData.length; i++) {
			int localX = i % NUM_VERTICES_X;
			int localZ = i / NUM_VERTICES_X;

			int x = (localX * scale) + originX;
			int z = (localZ * scale) + originZ;

			BiomePair biomePointData = getBiomeAt(x, z);
			biomeData[i] = biomePointData;
		}
		
		return new BiomeGeneratorData(biomeData);
	}

	
	// TODO: Awful old code, optimize
	public BiomePair getBiomeAt(int x, int y) {
		final int arraySize = biomeVoronoi.getArraySize();
		float closestDist = Float.POSITIVE_INFINITY, secondClosestDist = Float.POSITIVE_INFINITY;

		Biome[] biomes = new Biome[arraySize * arraySize];
		float[] distances = new float[biomes.length];
		float[] influences = new float[biomes.length];

		int inflID = 0;
		int biomeID = 0, secondClosestBiomeID = 0;
		float terrainEdgeScale = 1f; // Slightly different than influence.. this one controls the terrain's influence
										// scale at the biome's edge. This helps to create smoother transitions, and,
										// for example, shorelines

		// Get and apply border distortion
		float scaledX = x / biomeVoronoi.getScale();
		float scaledY = y / biomeVoronoi.getScale();
		
		// TODO: bijective A->B instead of two noise functs
		float[] noise = new float[] {
				Simplex2S.noise2(biomeDistortionSeeds[0], scaledX * BIOME_DISTORTION_FREQ, scaledY * BIOME_DISTORTION_FREQ),
				Simplex2S.noise2(biomeDistortionSeeds[1], scaledX * BIOME_DISTORTION_FREQ, scaledY * BIOME_DISTORTION_FREQ)
		};
		
		scaledX += noise[0] * BIOME_DISTORTION_SCALE;
		scaledY += noise[1] * BIOME_DISTORTION_SCALE;

		// Sample all nearby biomes, as well as their influence
		for(int i = 0; i < arraySize; i++) {
			for(int j = 0; j < arraySize; j++) {
				VoronoiDataPoint point = biomeVoronoi.getPoint(i, j);
				float dx = scaledX - point.x;
				float dy = scaledY - point.y;

				float distanceSqr = (dx * dx) + (dy * dy);

				if (distanceSqr < closestDist) {
					biomeID = inflID;
					closestDist = distanceSqr;
				}
				
				float temp = getTemperatureAt(point.x, point.y);
				float humid = getHumidityAt(point.x, point.y);
				float variance = ((float)Simplex2S.noise2(varianceSeed, point.x * CLIMATE_SCALE, point.y * CLIMATE_SCALE)) * 0.5f + 0.5f;
				biomes[inflID] = biomeData.getBiomeAt(temp, humid, variance);
				distances[inflID] = distanceSqr;
				inflID++;
			}
		}
		
		// Find the next closest biome distance (1st closest is our target cell - we don't want the distance to ourself!)
		for(int i = 0; i < biomes.length; i++) {
			if (biomes[i] != biomes[biomeID] && distances[i] != closestDist && distances[i] < secondClosestDist) {
				secondClosestDist = distances[i];
				secondClosestBiomeID = i;
			}
		}
		
		influences[biomeID] = 1f;
		
		Topography topo = biomes[biomeID].getTopography();
		Topography topoOther = biomes[secondClosestBiomeID].getTopography();
		
		if (topo == topoOther) {
			terrainEdgeScale = 1;
		} else {
			terrainEdgeScale = (secondClosestDist - closestDist);
			terrainEdgeScale *= topo.getBorderScale();
			terrainEdgeScale -= topo.getBorderStart();
			terrainEdgeScale = Maths.clamp(terrainEdgeScale, 0, 1);
		}

		// Find all intersecting biomes
		List<Integer> borderBiomeIDs = new LinkedList<Integer>();	
		
		for(int i = 0; i < biomes.length; i++) {
			if (i == biomeID)
				continue;
			if (distances[i] - distances[biomeID] > TRANSITION_SQR) {
				biomes[i] = null;
				continue;
			}
			
			influences[i] = (distances[i] - distances[biomeID]) * TRANSITON_SIZE;
			influences[i] = Maths.clamp(influences[i], 0f, 1f);
			influences[i] = 1f - influences[i];
			
			if (influences[i] != 0 && influences[i] != 1)
				borderBiomeIDs.add(i);
		}
		
		// Determine the influence per biome
		if (borderBiomeIDs.size() == 1) {
			if (biomes[borderBiomeIDs.get(0)] != biomes[biomeID]) {
				influences[borderBiomeIDs.get(0)] /= 2;
				influences[biomeID] -= influences[borderBiomeIDs.get(0)];
			}
		} else {
			float foreignInfluence = 0;
			for(Integer borderBiomeID : borderBiomeIDs) {
				influences[borderBiomeID] /= borderBiomeIDs.size() + .75f;
				foreignInfluence += influences[borderBiomeID];
			}
			
			influences[biomeID] -= foreignInfluence;
		}

		// Calculate foliage color
		int foliageColor = biomes[biomeID].getFoliageColor();
		for(int i = 0; i < borderBiomeIDs.size(); i++)
			foliageColor = ColorUtil.blend(biomes[biomeID].getFoliageColor(), biomes[borderBiomeIDs.get(i)].getFoliageColor(), influences[borderBiomeIDs.get(i)]);

		if (DevFlags.showBiomeBorders && influences[biomeID] <= .6f) {
			foliageColor = 0;
		}
		
		return new BiomePair(biomes[biomeID], influences[biomeID], terrainEdgeScale, foliageColor);
	}

	public static float getHumidityAt(float x, float z) {
		return (((float)Simplex2S.noise2(humidSeed, x * CLIMATE_SCALE, z * CLIMATE_SCALE)) * 0.5f) + 0.5f;
	}

	public static float getTemperatureAt(float x, float z) {
		return (((float)Simplex2S.noise2(tempSeed, x * CLIMATE_SCALE, z * CLIMATE_SCALE)) * 0.5f) + 0.5f;
	}
}
