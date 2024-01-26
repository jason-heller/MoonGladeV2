package engine.pg.gen;

import static engine.world.Chunk.CHUNK_HEIGHT;
import static engine.world.Chunk.CHUNK_WIDTH;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import engine.data.ColoredMeshData;
import engine.data.TexturedMeshData;
import engine.gl.meshing.FloraMesher;
import engine.gl.meshing.TerrainMesher;
import engine.gl.meshing.transvoxel.Seam;
import engine.world.Chunk;
import engine.world.ChunkDataHandler;

public class WorldMesher implements Runnable {
	private ConcurrentLinkedQueue<UnprocessedMeshContainer> queuedChunks = new ConcurrentLinkedQueue<>();
	private ConcurrentLinkedQueue<ProcessedMeshContainer> finishedMeshes = new ConcurrentLinkedQueue<>();
	
	private static final float TILE_STRIDE = 1f;					// The distance between consecutive vertices in a heightmap grid
	
	private ArrayList<Thread> threads = new ArrayList<>();
	private ExecutorService executorService;
	
	//private TerrainMesher terrainMesher;
	private TerrainMesher chunkMesher;
	private FloraMesher floraMesher;
	
	private double numChunksMeshed;
	private double timeSpentMeshing;
	private static double avgMeshingTimeMS;
	
	public static int numJobs;
	
	public WorldMesher(ChunkDataHandler chunkData, TerrainGenerator terrainGenerator) {
		//terrainMesher = new TerrainMesher(CHUNK_WIDTH, CHUNK_HEIGHT, CHUNK_WIDTH, TILE_STRIDE);
		chunkMesher = new TerrainMesher(chunkData, CHUNK_WIDTH, CHUNK_HEIGHT, CHUNK_WIDTH, TILE_STRIDE);
		floraMesher = new FloraMesher();

		executorService = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
	}

	public void update() {
		
		/*for (short i = 0; i < threads.size(); i++) {
			if (!threads.get(i).isAlive()) {
				threads.remove(i);
				i--;
			}
		}
		
		if (hasJob() && threads.size() < 1) {
			Thread t = new Thread(this);
			threads.add(threads.size(), t);
			t.start();
		}*/
		
		if (hasJob())
			executorService.execute(this);
	}
	
	@Override
	public void run() {
		while (!queuedChunks.isEmpty()) {
			long time = System.currentTimeMillis();
			if (buildTerrainMesh())
				++numChunksMeshed;
			timeSpentMeshing += System.currentTimeMillis() - time;
			
			avgMeshingTimeMS = timeSpentMeshing / numChunksMeshed;
		}
	}

	private boolean buildTerrainMesh() {
		UnprocessedMeshContainer unprocessedMeshes = queuedChunks.remove();
		ProcessedMeshContainer processedMeshes = new ProcessedMeshContainer();
		
		//Chunk chunk = unprocessedMeshes.getChunk();
		final int lod = 1 << unprocessedMeshes.getLOD();
		
		// TODO: Could just pass the chunk...
		int gridSizeY = CHUNK_HEIGHT / lod;
		//terrainMesher.setGridSizeY(gridSizeY);
		chunkMesher.setGridY(gridSizeY);
		
		
        ColoredMeshData terrainMesh = chunkMesher.createMesh(unprocessedMeshes);
        
        if (terrainMesh == null) {
        	// Failed
        	unprocessedMeshes.setSeam(Seam.NO_SEAM);
        	terrainMesh = chunkMesher.createMesh(unprocessedMeshes);
        }
        
		//ColoredMeshData terrainMesh = terrainMesher.createMesh(unprocessedMeshes);
		TexturedMeshData floraMesh = floraMesher.createMesh(unprocessedMeshes.getFloraData(), unprocessedMeshes.getHeights(), unprocessedMeshes.getX(), unprocessedMeshes.getZ(), lod);
		
		// Put into container
		processedMeshes.chunk = unprocessedMeshes.passToProcessedChunks();
		processedMeshes.terrainMesh = terrainMesh;
		processedMeshes.floraMesh = floraMesh;
		
		finishedMeshes.add(processedMeshes);
		--numJobs;
		return true;
	}
	
	public void addJob(UnprocessedMeshContainer chunk) {
		if (!queuedChunks.contains(chunk)) {
			queuedChunks.add(chunk);
			++numJobs;
		} else {
			System.err.println("WTF");
		}
	}
	
	public void addJob(Chunk chunk) {
		UnprocessedMeshContainer umc = new UnprocessedMeshContainer(chunk);
		queuedChunks.add(umc);
	}
	
	public void destroy() {
		floraMesher.destroy();
		
		executorService.shutdown();
		try {
		    if (!executorService.awaitTermination(800, TimeUnit.MILLISECONDS)) {
		        executorService.shutdownNow();
		    } 
		} catch (InterruptedException e) {
		    executorService.shutdownNow();
		}
	}
	
	public boolean hasMeshes() {
		return finishedMeshes.size() != 0;
	}
	
	public boolean hasJob() {
		return !queuedChunks.isEmpty();
	}
	
	public ProcessedMeshContainer popMeshContainer() {
		return finishedMeshes.remove();
	}
	
	public static double getAvgMeshingTime() {
		return avgMeshingTimeMS;
	}
}
