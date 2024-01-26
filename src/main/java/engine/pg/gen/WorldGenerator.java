package engine.pg.gen;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import engine.data.chunk.IByteLayerData;
import engine.data.chunk.IChunkData;
import engine.gl.ICamera;
import engine.world.Chunk;
import engine.world.WorldManager;

public class WorldGenerator implements Runnable {
	
	private List<Chunk> batchedChunks = new LinkedList<>();
	
	private ConcurrentLinkedQueue<Chunk> queuedChunks = new  ConcurrentLinkedQueue<>();
	private ConcurrentLinkedQueue<UnprocessedMeshContainer> finishedChunks = new ConcurrentLinkedQueue<>();
	//private ArrayList<Thread> threads = new ArrayList<>();
	
	private ExecutorService executorService;
	
	private BiomeGenerator biomeGenerator;
	private TerrainGenerator terrainGenerator;
	private FloraGenerator floraGenerator;
	
	private long seed;
	
	private double numChunksGenerated;
	private double timeSpentGenerating;
	private static double avgGenerationTime;
	public static double biomeTime, terrainTime;
	
	public static int numJobs = 0;
	
	public WorldGenerator(long seed, int numLODs, int firstLODWidthInChunks) {
		this.seed = seed;
		
		biomeGenerator = new BiomeGenerator(seed, numLODs, firstLODWidthInChunks);
		floraGenerator = new FloraGenerator(seed);
		terrainGenerator = new TerrainGenerator(seed);
		
		int threadCount = WorldManager.totalLODLevels;
		executorService = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
	}
	
	public void update(ICamera camera) {
		
		biomeGenerator.update(camera);
		
		if (hasJob())
			executorService.execute(this);
	}
	
	public byte getCurrentLOD() {
		final int size = batchedChunks.size();
		return size == 0 ? Byte.MAX_VALUE : batchedChunks.get(0).getLOD();
	}
	
	public int startLOD() {
		final int len = batchedChunks.size();
		
		if (len == 0) {
			return 0;
		}
		
		byte currentLOD = batchedChunks.get(0).getLOD();
		
		Chunk chunk;
		for(int i = 0; i < len; ++i) {
			chunk = batchedChunks.get(0);
			
			if (chunk.getLOD() != currentLOD)
				return i;
			
			queuedChunks.add(chunk);
			batchedChunks.remove(0);
		}
		
		return len;
	}

	@Override
	public void run() {
		while (!queuedChunks.isEmpty()) {
			long time = System.currentTimeMillis();
			generateChunk();
			
			++numChunksGenerated;
			timeSpentGenerating += System.currentTimeMillis() - time;
			
			avgGenerationTime = timeSpentGenerating / numChunksGenerated;
		}
	}

	private void generateChunk() {
		// TODO: the hell does this happen?
		if (queuedChunks.size() == 0)
			return;
		
		Chunk chunk = queuedChunks.remove();
		
		// Biome data
		long time = System.currentTimeMillis();
		BiomeGeneratorData biomeData = biomeGenerator.generateChunk(chunk);
		biomeTime = (biomeTime + (System.currentTimeMillis() - time)) / 2f;
		
		// Create terrain data
		time = System.currentTimeMillis();
		TerrainGeneratorData terrainData = terrainGenerator.generateChunk(chunk, biomeData);
		
		// Create floral data
		IByteLayerData floraIDs = floraGenerator.generateChunk(chunk, biomeData);
		
		IChunkData chunkData = terrainData.getTiles();
		
		chunk.setTileData(chunkData);
		chunk.setHeightData(terrainData.getHeights());
		chunk.setColorData(biomeData.getBiomeColors());
		chunk.setFloraData(floraIDs);
		terrainTime = (terrainTime + (System.currentTimeMillis() - time))/2f;
		
		UnprocessedMeshContainer unprocessedMesh = new UnprocessedMeshContainer(chunk, chunk.getSeam(), terrainGenerator.getCurrentUpperBounds());
		unprocessedMesh.setHeights(terrainData.getHeights());
		unprocessedMesh.setColors(biomeData.getBiomeColors());
		unprocessedMesh.setFloras(floraIDs);
		finishedChunks.add(unprocessedMesh);
		--numJobs;
	}
	
	public boolean addJob(Chunk chunk) {
		/*if (!batchedChunks.contains(chunk)) {
			batchedChunks.add(chunk);
			++numJobs;
			return true;
		}*/
		
		Chunk other;
		int insert = 0;
		for(int i = 0; i < batchedChunks.size(); ++i) {
			other = batchedChunks.get(i);
			if (other == chunk)
				return false;
			
			if (other.getLOD() <= chunk.getLOD())
				++insert;
			
		}
		
		batchedChunks.add(insert, chunk);
		++numJobs;
		
		return true;
	}
	
	public boolean hasChunks() {
		return finishedChunks.size() != 0;
	}
	
	public boolean hasJob() {
		return !queuedChunks.isEmpty();
	}
	
	public UnprocessedMeshContainer popChunk() {
		return finishedChunks.remove();
	}

	public TerrainGenerator getTerrainGenerator() {
		return terrainGenerator;
	}
	
	public static double getAvgGenerationTime() {
		return avgGenerationTime;
	}
	
	public void destroy() {
		executorService.shutdown();
		try {
		    if (!executorService.awaitTermination(800, TimeUnit.MILLISECONDS)) {
		        executorService.shutdownNow();
		    } 
		} catch (InterruptedException e) {
		    executorService.shutdownNow();
		}
	}
}
