package engine.gl.meshing;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;

import org.lwjgl.system.MemoryUtil;

import engine.data.ColoredMeshData;
import engine.data.chunk.IIntLayerData;
import engine.gl.meshing.transvoxel.ScalarField;
import engine.gl.meshing.transvoxel.Seam;
import engine.gl.meshing.transvoxel.Transvoxel;
import engine.gl.meshing.transvoxel.TransvoxelMesh;
import engine.pg.gen.UnprocessedMeshContainer;
import engine.utils.math.Maths;
import engine.world.Chunk;
import engine.world.ChunkDataHandler;

// Handles general meshing with terrain
public class TerrainMesher {

	private static final int TERRAIN_MAX = Chunk.NUM_VERTICES_Y-1, TERRAIN_MIN = 0;
	
	private Transvoxel tvMesher;
	// private TerrainMesher cmMesher;
	private ChunkDataHandler chunkData;

	public TerrainMesher(ChunkDataHandler chunkData, int chunkWidth, int chunkHeight,
			int chunkLength, float tileStride) {
		tvMesher = new Transvoxel(chunkWidth, chunkHeight, chunkLength, tileStride, 0f);
		// cmMesher = new TerrainMesher(chunkWidth, chunkHeight, chunkLength,
		// tileStride);
		this.chunkData = chunkData;
	}

	public void setGridY(int gridSizeY) {
		tvMesher.setGridY(gridSizeY);
		// cmMesher.setGridSizeY(gridSizeY);
	}

	public ColoredMeshData createMesh(UnprocessedMeshContainer umc) {
		final int x = umc.getX();
		final int z = umc.getZ();
		final byte lod = umc.getLOD();
		final Seam seam = umc.getSeam();
		
		ScalarField field = new ScalarField() {
			public float getDensity(int x, int y, int z) {
				if (y >= TERRAIN_MAX)
					return 0f;
				if (y <= TERRAIN_MIN)
					return 15f;

				// OK here
				return umc.getTileData().getData(x, y, z);
			}
		};

		TransvoxelMesh tvMesh = null;

		final byte[] upperBounds = umc.getTerrainUpperBounds();

		if (seam != Seam.NO_SEAM) {
			int dx = seam.signX * seam.incZ;
			int dz = seam.signZ * seam.incX;
			if (dx == 1)
				dx++;
			if (dz == 1)
				dz++;

			if (hasMissingNeighbors(x, z, lod, dx, dz, seam.incX, seam.incZ))
				return null;

			tvMesh = tvMesher.createMesh(field, createGlobalField(x, z, lod, seam, dx, dz), seam, upperBounds);
		} else {
			tvMesh = tvMesher.createMesh(field, upperBounds);
		}

		final int numVertices = tvMesh.positions.size() / 3;
		final int chunkScale = 1 << umc.getLOD();

		FloatBuffer posBuffer = MeshUtil.toFloatBuffer(tvMesh.positions).flip();
		IntBuffer colorBuffer = createColorBuffer(umc.getColors(), tvMesh.positions, chunkScale, seam);
		FloatBuffer normalBuffer = MeshUtil.toFloatBuffer(tvMesh.normals).flip();
		IntBuffer indBuffer = MeshUtil.toIntBuffer(tvMesh.indices).flip();

		ColoredMeshData mesh = new ColoredMeshData(posBuffer, colorBuffer, normalBuffer, indBuffer);
		mesh.numIndices = tvMesh.indices.size();
		mesh.numVertices = numVertices;

		// ColoredMeshData mesh = cmMesher.createMesh(umc);

		return mesh;
	}

	private ScalarField createGlobalField(int chunkX, int chunkZ, byte chunkLOD, Seam chunkSeam, int dx, int dz) {
		return new ScalarField() {

			public float getDensity(int x, int y, int z) {
				if (y >= TERRAIN_MAX)
					return 0f;
				if (y <= TERRAIN_MIN)
					return 15f;

				final byte lastLod = (byte) (chunkLOD - 1);
				final int lodScale = (1 << lastLod);

				final int lastLODChunkWidth = Chunk.CHUNK_WIDTH * lodScale;

				final int xOffset = dx * lastLODChunkWidth;
				final int zOffset = dz * lastLODChunkWidth;

				final int chunkXOffset = (Math.floorDiv(x, Chunk.NUM_VERTICES_X));
				final int chunkZOffset = (Math.floorDiv(z, Chunk.NUM_VERTICES_X));

				int cx = chunkX + xOffset + (chunkXOffset * lastLODChunkWidth);
				int cz = chunkZ + zOffset + (chunkZOffset * lastLODChunkWidth);

				Chunk neighbor = chunkData.getChunkByCoords(cx, cz, lastLod);
				float density;
				
				if (chunkSeam == Seam.NO_SEAM) {
					System.err.println("WRONG SEAM");
					throw new NullPointerException();
				}
				
				// Happens when a neighbor formly there was deleted before this one can load
				if (neighbor == null || neighbor.getTileData() == null) {
					//Log.warn("Missing neighbor" + " " + chunkSeam + " " + x + "/" + z + " " + (xOffset) + '/' + (zOffset)
					//		+ " lod: " + lodScale);
					return -1f;
				}

				density = neighbor.getTileData().getData(x - (chunkXOffset * Chunk.CHUNK_WIDTH), y,
						z - (chunkZOffset * Chunk.CHUNK_WIDTH));

				density = Maths.clamp(density, -128f, 127f);

				return density;
			}
		};
	}

	private boolean hasMissingNeighbors(int chunkX, int chunkZ, byte chunkLOD, int dx, int dz, int incX, int incZ) {
		final byte lastLod = (byte) (chunkLOD - 1);
		final int lodScale = (1 << lastLod);

		final int lastLODChunkWidth = Chunk.CHUNK_WIDTH * lodScale;

		final int xOffset = dx * lastLODChunkWidth;
		final int zOffset = dz * lastLODChunkWidth;

		int cx = chunkX + xOffset;// + (chunkXOffset * lastLODChunkWidth);
		int cz = chunkZ + zOffset;// + (chunkZOffset * lastLODChunkWidth);

		Chunk chunk = chunkData.getChunkByCoords(cx, cz, lastLod);
		
		if (chunk == null || chunk.getTileData() == null)
			return true;

		cx += (incX * lastLODChunkWidth);
		cz += (incZ * lastLODChunkWidth);

		chunk = chunkData.getChunkByCoords(cx, cz, lastLod);
		return (chunk == null || chunk.getTileData() == null);
	}

	private IntBuffer createColorBuffer(IIntLayerData biomeColors, List<Float> positions, int chunkScale, Seam chunkSeam) {
		IntBuffer colorBuffer = MemoryUtil.memAllocInt(positions.size() / 3);
		final int numPositions = positions.size();

		for (int i = 0; i < numPositions; i += 3) {
			float px = positions.get(i) / 1;
			float pz = positions.get(i + 2) / 1;
			int x = (int) (px), z = (int) (pz);

			x = Maths.clamp(x, 0, Chunk.CHUNK_WIDTH);
			z = Maths.clamp(z, 0, Chunk.CHUNK_WIDTH);

			colorBuffer.put(biomeColors.get(x + (z * Chunk.NUM_VERTICES_X)));
		}

		return colorBuffer.flip();
	}
}
