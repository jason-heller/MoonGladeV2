package engine.gl.meshing;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

import engine.data.ColoredMeshData;
import engine.data.chunk.IIntLayerData;
import engine.pg.gen.UnprocessedMeshContainer;
import engine.world.Chunk;;

/**
 * Based on the implementation here: https://www.gamedev.net/blogs/entry/2264024-marching-cubes/
 * Lengyel, Eric. “Voxel-Based Terrain for Real-Time Virtual Simulations”. PhD diss., University of California at Davis, 2010.
 *
 */

@Deprecated
public final class TerrainMesherMC {

	private static final int VERTEX_COMPONENT_COUNT = 3;
	private static final int COLOR_COMPONENT_COUNT = 8;
	private static final int NORMAL_COMPONENT_COUNT = 3;

	private static final int VERTICES_PER_CUBE = 8;

	private Chunk chunk;
	
	private float voxelSize;
	private int gridSizeX;
	private int gridSizeY;
	private int gridSizeZ;
	
	private float[] cubeScalars;	// TODO: to byte
	
	private Vector3f origin;
	private List<Vector3f> vertices;
	
	// public static final float ISO_LEVEL = 0f;
	private static final Vector3f DEFAULT_OFFSET = new Vector3f();
	
	private int chunkScale;
	
	/**
	 * Constructs a new MarchingCubesMeshFactory for generating meshes out of a
	 * scalar field with the marching cubes algorithm.
	 * 
	 * @param chunkDensities	   	Contains the density of each position.
	 * @param gridSize	    Contains the dimensions of the data
	 * @param isoLevel     	The minimum density needed for a position to be
	 *                     considered solid.
	 * @param cubeDiameter The diameter of a single voxel.
	 */
	public TerrainMesherMC(int gridSize, float cubeDiameter) {
		this(gridSize, gridSize, gridSize, cubeDiameter);
	}
	
	/**
	 * Constructs a new MarchingCubesMeshFactory for generating meshes out of a
	 * scalar field with the marching cubes algorithm.
	 * 
	 * @param chunkDensities	   		Contains the density of each position.
	 * @param gridSizeX/Y/Z	    Contains the dimensions of the data
	 * @param isoLevel     		The minimum density needed for a position to be
	 *                     considered solid.
	 * @param cubeDiameter The diameter of a single voxel.
	 */
	public TerrainMesherMC(int gridSizeX, int gridSizeY, int gridSizeZ, float voxelSize) {
		this.voxelSize = voxelSize;
		this.gridSizeX = gridSizeX;
		this.gridSizeY = gridSizeY;
		this.gridSizeZ = gridSizeZ;
		this.origin = DEFAULT_OFFSET;
	}
	
	public void setOrigin(Vector3f origin) {
		this.origin = origin;
	}
	
	public ColoredMeshData createMesh(UnprocessedMeshContainer cmh) {
		this.chunk = cmh.getChunk();
		this.chunkScale = 1;// << (chunk.getLOD() * 2);
		FloatBuffer positionBuffer 		= createPositionBuffer(cmh.getTerrainUpperBounds(), false);
		IntBuffer   colorBuffer 		= createColorBuffer(cmh.getColors());
		FloatBuffer normalBuffer 		= createNormalBuffer();
		IntBuffer   indexBuffer 		= createIndexBuffer();		// Useless
		
		ColoredMeshData meshData = new ColoredMeshData(positionBuffer, colorBuffer, normalBuffer, indexBuffer);
		meshData.numIndices = vertices.size();

		System.out.println(meshData.numVertices + " " + meshData.numIndices);
		return meshData;
	}

	private FloatBuffer createPositionBuffer(byte[] upperBoundsY, boolean densitiesDescending) {
		vertices = new ArrayList<>();
		for (int x = 0; x < gridSizeX; ++x)
			for (int z = 0; z < gridSizeZ; ++z)
				for (int y = upperBoundsY[x + (z * gridSizeX)]; y < gridSizeY; ++y) {
					Vector3f[] cubeVertices = new Vector3f[VERTICES_PER_CUBE];
					int cubeIndex = computeCubeIndex(cubeVertices, x, y, z);
					
					// No more to generate here
					if (densitiesDescending && cubeIndex == 0)
						break;
					
					int edgeBitField = MarchingCubesTables.EDGE_TABLE[cubeIndex];
					
					if (edgeBitField == 0)
						continue;
					
					Vector3f[] lerpedVertices = computeVertices(cubeVertices, edgeBitField);
					addVerticesToList(lerpedVertices, cubeIndex);
				}

		return addVerticesToPositionBuffer();
	}
	
	private IntBuffer createColorBuffer(IIntLayerData biomeColors) {
		IntBuffer colorBuffer = MemoryUtil.memAllocInt(vertices.size() * COLOR_COMPONENT_COUNT);

		
		for (int i = 0; i < vertices.size(); ++i) {
			Vector3f v = vertices.get(i);
			int x = ((int)v.x / chunkScale), z = ((int)v.z / chunkScale);

			colorBuffer.put(biomeColors.get((x % Chunk.NUM_VERTICES_X) + (z * Chunk.NUM_VERTICES_X)));
		}

		return colorBuffer.flip();
	}
	
	private FloatBuffer createNormalBuffer() {
		FloatBuffer normalBuffer = MemoryUtil.memAllocFloat(vertices.size() * NORMAL_COMPONENT_COUNT);

		for (int i = 0; i < vertices.size(); i += 3) {
			Vector3f normal = computeTriangleNormal(vertices.get(i), vertices.get(i + 1), vertices.get(i + 2));

			for (int j = 0; j < NORMAL_COMPONENT_COUNT; ++j)
				normalBuffer.put(normal.x).put(normal.y).put(normal.z);
		}

		return normalBuffer.flip();
	}

	private IntBuffer createIndexBuffer() {
			IntBuffer indexBuffer = MemoryUtil.memAllocInt(vertices.size());
	
			for (int vertexIndex = 0; vertexIndex < vertices.size(); vertexIndex ++) {
				indexBuffer.put(vertexIndex);
			}
			
			return indexBuffer.flip();
	}

	private FloatBuffer addVerticesToPositionBuffer() {
		FloatBuffer positionBuffer = MemoryUtil.memAllocFloat(vertices.size() * VERTEX_COMPONENT_COUNT);

		for (int i = 0; i < vertices.size(); ++i) {
			Vector3f position = vertices.get(i);
			positionBuffer.put(position.x).put(position.y).put(position.z);
		}
		
		return positionBuffer.flip();
	}

	/**
	 * Add the generated vertices by the marching cubes algorithm to a list. The
	 * added vertices are modified so that they respect the origin.
	 * 
	 * @param vertrexList The list where to add the marching cubes vertices.
	 * @param vertices  The marching cubes vertices.
	 * @param cubeIndex   The cubeIndex.
	 */
	private void addVerticesToList(Vector3f[] localVertices, int cubeIndex) {
		int vertexCount = MarchingCubesTables.TRIANGLE_TABLE[cubeIndex].length;
		for (int i = 0; i < vertexCount; ++i) {
			int vertIndex = MarchingCubesTables.TRIANGLE_TABLE[cubeIndex][i];
			vertices.add(new Vector3f(localVertices[vertIndex]).add(origin));
		}
	}

	/**
	 * Computes the marching cubes vertices. Those are the lerped vertices that can
	 * later be used to form triangles.
	 * 
	 * @param cubeVertices The vertices of a cube, i.e. the 8 corners.
	 * @param edgeBitField The bit field representing all the edges that should be
	 *                     drawn.
	 * @param isoLevel     The minimum density needed for a position to be
	 *                     considered solid.
	 * @return The lerped vertices of a cube to form the marching cubes shape.
	 */
	private Vector3f[] computeVertices(Vector3f[] cubeVertices, int edgeBitField) {
		Vector3f[] lerpedVertices = new Vector3f[MarchingCubesTables.EDGE_BITS];

		for (int i = 0; i < MarchingCubesTables.EDGE_BITS; ++i) {
			if ((edgeBitField & (1 << i)) != 0) {
				int e1 = MarchingCubesTables.EDGE_FIRST_VERTEX[i];
				int e2 = MarchingCubesTables.EDGE_SECOND_VERTEX[i];

				lerpedVertices[i] = lerpVertices(cubeVertices[e1], cubeVertices[e2], cubeScalars[e1], cubeScalars[e2]);
			}
		}

		return lerpedVertices;
	}

	/** TODO: This is kinda useless now, only matters when scalar = 0, optimize this
	 * Lerps two vertices of a cube along their shared designed edge according to
	 * their densities.
	 * 
	 * @param firstVertex  The edge's first vertex.
	 * @param secondVertex The edge's second vertex.
	 * @param firstScalar  The first vertex's density.
	 * @param secondScalar The second vertex's density.
	 * @return The lerped resulting vertex along the edge.
	 */
	private Vector3f lerpVertices(Vector3f firstVertex, Vector3f secondVertex, float firstScalar, float secondScalar) {
		if (Math.abs(firstScalar) < Math.ulp(1f))
			return firstVertex;
		if (Math.abs(secondScalar) < Math.ulp(1f))
			return secondVertex;
		if (Math.abs(firstScalar - secondScalar) < Math.ulp(1f))
			return firstVertex;

		float lerpFactor = (-firstScalar) / (secondScalar - firstScalar);

		return new Vector3f(firstVertex).lerp(secondVertex, lerpFactor);
		
		//return new Vector3f(firstVertex).lerp(secondVertex, 0.5f);
	}

	/**
	 * Computes the cubeIndex, which represents the adjacent voxels' densities.
	 * 
	 * @param cubeVertices The 8 corners of a cube.
	 * @param indexX       The X position of the marching cube in the grid.
	 * @param indexY       The Y position of the marching cube in the grid.
	 * @param indexZ       The Z position of the marching cube in the grid.
	 * @return The cubeIndex.
	 */
	private int computeCubeIndex(Vector3f[] cubeVertices, int indexX, int indexY, int indexZ) {
		cubeScalars = new float[VERTICES_PER_CUBE];
		final int edgeLength = 2;
		int cubeVertexIndex = 0;
		int cubeIndex = 0;
		int cubeIndexRHS = 1;

		/*- Vertex indices
		                4  ___________________  5
		                  /|                 /|
		                 / |                / |
		                /  |               /  |
		           7   /___|______________/6  |
		              |    |              |   |
		              |    |              |   |
		              |  0 |______________|___| 1
		              |   /               |   /
		              |  /                |  /
		              | /                 | /
		              |/__________________|/
		             3                     2
		*/

		for (int y = 0; y < edgeLength; ++y)
			for (int z = 0; z < edgeLength; ++z)
				for (int x = z % edgeLength; x >= 0 && x < edgeLength; x += (z == 0 ? 1 : -1)) {
					
					final int vx = indexX + x, vy = indexY + y, vz = indexZ + z;
					
					cubeVertices[cubeVertexIndex] = new Vector3f(vx * voxelSize, vy * voxelSize, vz * voxelSize);
					float scalar = queryGridScalar(vx, vy, vz);
					cubeScalars[cubeVertexIndex++] = scalar;

					// If the density is solid
					if (scalar > 0f)
						cubeIndex |= cubeIndexRHS;

					cubeIndexRHS <<= 1;
				}

		return cubeIndex;
	}

	/**
	 * Queries the grid scalar at the given point and manages the boundaries, i.e.
	 * it's ok if x = -1 or is bigger than the gridLengthX.
	 * 
	 * @param x The scalar X position in the grid.
	 * @param y The scalar Y position in the grid.
	 * @param z The scalar Z position in the grid.
	 * @return The grid scalar at the (x, y, z) position.
	 */
	private float queryGridScalar(int x, int y, int z) {
		if (y >= gridSizeY)
			return 0;
		if (y <= 0)
			return 1f; // <= , 15 f

		return chunk.getTileData().getData(x, y, z);
	}

	public static Vector3f computeTriangleNormal(Vector3f[] vertices) {
		return computeTriangleNormal(vertices[0], vertices[1], vertices[2]);
	}

	public static Vector3f computeTriangleNormal(Vector3f p1, Vector3f p2, Vector3f p3) {
		Vector3f e1 = new Vector3f(p2).sub(p1);
		Vector3f e2 = new Vector3f(p3).sub(p1);
		return e1.cross(e2).normalize();
	}
	
	public void setGridSizeX(int gridSizeX) {
		this.gridSizeX = gridSizeX;
	}
	
	public void setGridSizeY(int gridSizeY) {
		this.gridSizeY = gridSizeY;
	}
	
	public void setGridSizeZ(int gridSizeZ) {
		this.gridSizeZ = gridSizeZ;
	}
}

// Big ugly utility class

class MarchingCubesTables {

	public static final int EDGE_BITS = 12;

	public static final int[] EDGE_TABLE = { 0x0, 0x109, 0x203, 0x30a, 0x406, 0x50f, 0x605, 0x70c, 0x80c, 0x905, 0xa0f,
			0xb06, 0xc0a, 0xd03, 0xe09, 0xf00, 0x190, 0x99, 0x393, 0x29a, 0x596, 0x49f, 0x795, 0x69c, 0x99c, 0x895,
			0xb9f, 0xa96, 0xd9a, 0xc93, 0xf99, 0xe90, 0x230, 0x339, 0x33, 0x13a, 0x636, 0x73f, 0x435, 0x53c, 0xa3c,
			0xb35, 0x83f, 0x936, 0xe3a, 0xf33, 0xc39, 0xd30, 0x3a0, 0x2a9, 0x1a3, 0xaa, 0x7a6, 0x6af, 0x5a5, 0x4ac,
			0xbac, 0xaa5, 0x9af, 0x8a6, 0xfaa, 0xea3, 0xda9, 0xca0, 0x460, 0x569, 0x663, 0x76a, 0x66, 0x16f, 0x265,
			0x36c, 0xc6c, 0xd65, 0xe6f, 0xf66, 0x86a, 0x963, 0xa69, 0xb60, 0x5f0, 0x4f9, 0x7f3, 0x6fa, 0x1f6, 0xff,
			0x3f5, 0x2fc, 0xdfc, 0xcf5, 0xfff, 0xef6, 0x9fa, 0x8f3, 0xbf9, 0xaf0, 0x650, 0x759, 0x453, 0x55a, 0x256,
			0x35f, 0x55, 0x15c, 0xe5c, 0xf55, 0xc5f, 0xd56, 0xa5a, 0xb53, 0x859, 0x950, 0x7c0, 0x6c9, 0x5c3, 0x4ca,
			0x3c6, 0x2cf, 0x1c5, 0xcc, 0xfcc, 0xec5, 0xdcf, 0xcc6, 0xbca, 0xac3, 0x9c9, 0x8c0, 0x8c0, 0x9c9, 0xac3,
			0xbca, 0xcc6, 0xdcf, 0xec5, 0xfcc, 0xcc, 0x1c5, 0x2cf, 0x3c6, 0x4ca, 0x5c3, 0x6c9, 0x7c0, 0x950, 0x859,
			0xb53, 0xa5a, 0xd56, 0xc5f, 0xf55, 0xe5c, 0x15c, 0x55, 0x35f, 0x256, 0x55a, 0x453, 0x759, 0x650, 0xaf0,
			0xbf9, 0x8f3, 0x9fa, 0xef6, 0xfff, 0xcf5, 0xdfc, 0x2fc, 0x3f5, 0xff, 0x1f6, 0x6fa, 0x7f3, 0x4f9, 0x5f0,
			0xb60, 0xa69, 0x963, 0x86a, 0xf66, 0xe6f, 0xd65, 0xc6c, 0x36c, 0x265, 0x16f, 0x66, 0x76a, 0x663, 0x569,
			0x460, 0xca0, 0xda9, 0xea3, 0xfaa, 0x8a6, 0x9af, 0xaa5, 0xbac, 0x4ac, 0x5a5, 0x6af, 0x7a6, 0xaa, 0x1a3,
			0x2a9, 0x3a0, 0xd30, 0xc39, 0xf33, 0xe3a, 0x936, 0x83f, 0xb35, 0xa3c, 0x53c, 0x435, 0x73f, 0x636, 0x13a,
			0x33, 0x339, 0x230, 0xe90, 0xf99, 0xc93, 0xd9a, 0xa96, 0xb9f, 0x895, 0x99c, 0x69c, 0x795, 0x49f, 0x596,
			0x29a, 0x393, 0x99, 0x190, 0xf00, 0xe09, 0xd03, 0xc0a, 0xb06, 0xa0f, 0x905, 0x80c, 0x70c, 0x605, 0x50f,
			0x406, 0x30a, 0x203, 0x109, 0x0 };
	
	/*- Edge indices
         _________4_________   
        /|                 /|
       7 |                5 |
      /  |               /  |
     /___|______6_______/   9
    |    8              |   |
    |    |              |   |
    11   |_______0______|___|  
    |   /              10   /
    |  3                |  1
    | /                 | /
    |/_________2________|/
                        
*/
	
	// These two are the indices per edge (into the cube vertices) <- 
	public static final int[] EDGE_FIRST_VERTEX =  { 0, 1, 2, 3, 4, 5, 6, 7, 0, 1, 2, 3 };
	public static final int[] EDGE_SECOND_VERTEX = { 1, 2, 3, 0, 5, 6, 7, 4, 4, 5, 6, 7 };

	// Vertex order **this indexes into the edges!!
	public static final int[][] TRIANGLE_TABLE = { {}, { 0, 8, 3 }, { 0, 1, 9 }, { 1, 8, 3, 9, 8, 1 }, { 1, 2, 10 },
			{ 0, 8, 3, 1, 2, 10 }, { 9, 2, 10, 0, 2, 9 }, { 2, 8, 3, 2, 10, 8, 10, 9, 8 }, { 3, 11, 2 },
			{ 0, 11, 2, 8, 11, 0 }, { 1, 9, 0, 2, 3, 11 }, { 1, 11, 2, 1, 9, 11, 9, 8, 11 }, { 3, 10, 1, 11, 10, 3 },
			{ 0, 10, 1, 0, 8, 10, 8, 11, 10 }, { 3, 9, 0, 3, 11, 9, 11, 10, 9 }, { 9, 8, 10, 10, 8, 11 }, { 4, 7, 8 },
			{ 4, 3, 0, 7, 3, 4 }, { 0, 1, 9, 8, 4, 7 }, { 4, 1, 9, 4, 7, 1, 7, 3, 1 }, { 1, 2, 10, 8, 4, 7 },
			{ 3, 4, 7, 3, 0, 4, 1, 2, 10 }, { 9, 2, 10, 9, 0, 2, 8, 4, 7 }, { 2, 10, 9, 2, 9, 7, 2, 7, 3, 7, 9, 4 },
			{ 8, 4, 7, 3, 11, 2 }, { 11, 4, 7, 11, 2, 4, 2, 0, 4 }, { 9, 0, 1, 8, 4, 7, 2, 3, 11 },
			{ 4, 7, 11, 9, 4, 11, 9, 11, 2, 9, 2, 1 }, { 3, 10, 1, 3, 11, 10, 7, 8, 4 },
			{ 1, 11, 10, 1, 4, 11, 1, 0, 4, 7, 11, 4 }, { 4, 7, 8, 9, 0, 11, 9, 11, 10, 11, 0, 3 },
			{ 4, 7, 11, 4, 11, 9, 9, 11, 10 }, { 9, 5, 4 }, { 9, 5, 4, 0, 8, 3 }, { 0, 5, 4, 1, 5, 0 },
			{ 8, 5, 4, 8, 3, 5, 3, 1, 5 }, { 1, 2, 10, 9, 5, 4 }, { 3, 0, 8, 1, 2, 10, 4, 9, 5 },
			{ 5, 2, 10, 5, 4, 2, 4, 0, 2 }, { 2, 10, 5, 3, 2, 5, 3, 5, 4, 3, 4, 8 }, { 9, 5, 4, 2, 3, 11 },
			{ 0, 11, 2, 0, 8, 11, 4, 9, 5 }, { 0, 5, 4, 0, 1, 5, 2, 3, 11 }, { 2, 1, 5, 2, 5, 8, 2, 8, 11, 4, 8, 5 },
			{ 10, 3, 11, 10, 1, 3, 9, 5, 4 }, { 4, 9, 5, 0, 8, 1, 8, 10, 1, 8, 11, 10 },
			{ 5, 4, 0, 5, 0, 11, 5, 11, 10, 11, 0, 3 }, { 5, 4, 8, 5, 8, 10, 10, 8, 11 }, { 9, 7, 8, 5, 7, 9 },
			{ 9, 3, 0, 9, 5, 3, 5, 7, 3 }, { 0, 7, 8, 0, 1, 7, 1, 5, 7 }, { 1, 5, 3, 3, 5, 7 },
			{ 9, 7, 8, 9, 5, 7, 10, 1, 2 }, { 10, 1, 2, 9, 5, 0, 5, 3, 0, 5, 7, 3 },
			{ 8, 0, 2, 8, 2, 5, 8, 5, 7, 10, 5, 2 }, { 2, 10, 5, 2, 5, 3, 3, 5, 7 }, { 7, 9, 5, 7, 8, 9, 3, 11, 2 },
			{ 9, 5, 7, 9, 7, 2, 9, 2, 0, 2, 7, 11 }, { 2, 3, 11, 0, 1, 8, 1, 7, 8, 1, 5, 7 },
			{ 11, 2, 1, 11, 1, 7, 7, 1, 5 }, { 9, 5, 8, 8, 5, 7, 10, 1, 3, 10, 3, 11 },
			{ 5, 7, 0, 5, 0, 9, 7, 11, 0, 1, 0, 10, 11, 10, 0 }, { 11, 10, 0, 11, 0, 3, 10, 5, 0, 8, 0, 7, 5, 7, 0 },
			{ 11, 10, 5, 7, 11, 5 }, { 10, 6, 5 }, { 0, 8, 3, 5, 10, 6 }, { 9, 0, 1, 5, 10, 6 },
			{ 1, 8, 3, 1, 9, 8, 5, 10, 6 }, { 1, 6, 5, 2, 6, 1 }, { 1, 6, 5, 1, 2, 6, 3, 0, 8 },
			{ 9, 6, 5, 9, 0, 6, 0, 2, 6 }, { 5, 9, 8, 5, 8, 2, 5, 2, 6, 3, 2, 8 }, { 2, 3, 11, 10, 6, 5 },
			{ 11, 0, 8, 11, 2, 0, 10, 6, 5 }, { 0, 1, 9, 2, 3, 11, 5, 10, 6 },
			{ 5, 10, 6, 1, 9, 2, 9, 11, 2, 9, 8, 11 }, { 6, 3, 11, 6, 5, 3, 5, 1, 3 },
			{ 0, 8, 11, 0, 11, 5, 0, 5, 1, 5, 11, 6 }, { 3, 11, 6, 0, 3, 6, 0, 6, 5, 0, 5, 9 },
			{ 6, 5, 9, 6, 9, 11, 11, 9, 8 }, { 5, 10, 6, 4, 7, 8 }, { 4, 3, 0, 4, 7, 3, 6, 5, 10 },
			{ 1, 9, 0, 5, 10, 6, 8, 4, 7 }, { 10, 6, 5, 1, 9, 7, 1, 7, 3, 7, 9, 4 }, { 6, 1, 2, 6, 5, 1, 4, 7, 8 },
			{ 1, 2, 5, 5, 2, 6, 3, 0, 4, 3, 4, 7 }, { 8, 4, 7, 9, 0, 5, 0, 6, 5, 0, 2, 6 },
			{ 7, 3, 9, 7, 9, 4, 3, 2, 9, 5, 9, 6, 2, 6, 9 }, { 3, 11, 2, 7, 8, 4, 10, 6, 5 },
			{ 5, 10, 6, 4, 7, 2, 4, 2, 0, 2, 7, 11 }, { 0, 1, 9, 4, 7, 8, 2, 3, 11, 5, 10, 6 },
			{ 9, 2, 1, 9, 11, 2, 9, 4, 11, 7, 11, 4, 5, 10, 6 }, { 8, 4, 7, 3, 11, 5, 3, 5, 1, 5, 11, 6 },
			{ 5, 1, 11, 5, 11, 6, 1, 0, 11, 7, 11, 4, 0, 4, 11 }, { 0, 5, 9, 0, 6, 5, 0, 3, 6, 11, 6, 3, 8, 4, 7 },
			{ 6, 5, 9, 6, 9, 11, 4, 7, 9, 7, 11, 9 }, { 10, 4, 9, 6, 4, 10 }, { 4, 10, 6, 4, 9, 10, 0, 8, 3 },
			{ 10, 0, 1, 10, 6, 0, 6, 4, 0 }, { 8, 3, 1, 8, 1, 6, 8, 6, 4, 6, 1, 10 }, { 1, 4, 9, 1, 2, 4, 2, 6, 4 },
			{ 3, 0, 8, 1, 2, 9, 2, 4, 9, 2, 6, 4 }, { 0, 2, 4, 4, 2, 6 }, { 8, 3, 2, 8, 2, 4, 4, 2, 6 },
			{ 10, 4, 9, 10, 6, 4, 11, 2, 3 }, { 0, 8, 2, 2, 8, 11, 4, 9, 10, 4, 10, 6 },
			{ 3, 11, 2, 0, 1, 6, 0, 6, 4, 6, 1, 10 }, { 6, 4, 1, 6, 1, 10, 4, 8, 1, 2, 1, 11, 8, 11, 1 },
			{ 9, 6, 4, 9, 3, 6, 9, 1, 3, 11, 6, 3 }, { 8, 11, 1, 8, 1, 0, 11, 6, 1, 9, 1, 4, 6, 4, 1 },
			{ 3, 11, 6, 3, 6, 0, 0, 6, 4 }, { 6, 4, 8, 11, 6, 8 }, { 7, 10, 6, 7, 8, 10, 8, 9, 10 },
			{ 0, 7, 3, 0, 10, 7, 0, 9, 10, 6, 7, 10 }, { 10, 6, 7, 1, 10, 7, 1, 7, 8, 1, 8, 0 },
			{ 10, 6, 7, 10, 7, 1, 1, 7, 3 }, { 1, 2, 6, 1, 6, 8, 1, 8, 9, 8, 6, 7 },
			{ 2, 6, 9, 2, 9, 1, 6, 7, 9, 0, 9, 3, 7, 3, 9 }, { 7, 8, 0, 7, 0, 6, 6, 0, 2 }, { 7, 3, 2, 6, 7, 2 },
			{ 2, 3, 11, 10, 6, 8, 10, 8, 9, 8, 6, 7 }, { 2, 0, 7, 2, 7, 11, 0, 9, 7, 6, 7, 10, 9, 10, 7 },
			{ 1, 8, 0, 1, 7, 8, 1, 10, 7, 6, 7, 10, 2, 3, 11 }, { 11, 2, 1, 11, 1, 7, 10, 6, 1, 6, 7, 1 },
			{ 8, 9, 6, 8, 6, 7, 9, 1, 6, 11, 6, 3, 1, 3, 6 }, { 0, 9, 1, 11, 6, 7 },
			{ 7, 8, 0, 7, 0, 6, 3, 11, 0, 11, 6, 0 }, { 7, 11, 6 }, { 7, 6, 11 }, { 3, 0, 8, 11, 7, 6 },
			{ 0, 1, 9, 11, 7, 6 }, { 8, 1, 9, 8, 3, 1, 11, 7, 6 }, { 10, 1, 2, 6, 11, 7 },
			{ 1, 2, 10, 3, 0, 8, 6, 11, 7 }, { 2, 9, 0, 2, 10, 9, 6, 11, 7 },
			{ 6, 11, 7, 2, 10, 3, 10, 8, 3, 10, 9, 8 }, { 7, 2, 3, 6, 2, 7 }, { 7, 0, 8, 7, 6, 0, 6, 2, 0 },
			{ 2, 7, 6, 2, 3, 7, 0, 1, 9 }, { 1, 6, 2, 1, 8, 6, 1, 9, 8, 8, 7, 6 }, { 10, 7, 6, 10, 1, 7, 1, 3, 7 },
			{ 10, 7, 6, 1, 7, 10, 1, 8, 7, 1, 0, 8 }, { 0, 3, 7, 0, 7, 10, 0, 10, 9, 6, 10, 7 },
			{ 7, 6, 10, 7, 10, 8, 8, 10, 9 }, { 6, 8, 4, 11, 8, 6 }, { 3, 6, 11, 3, 0, 6, 0, 4, 6 },
			{ 8, 6, 11, 8, 4, 6, 9, 0, 1 }, { 9, 4, 6, 9, 6, 3, 9, 3, 1, 11, 3, 6 }, { 6, 8, 4, 6, 11, 8, 2, 10, 1 },
			{ 1, 2, 10, 3, 0, 11, 0, 6, 11, 0, 4, 6 }, { 4, 11, 8, 4, 6, 11, 0, 2, 9, 2, 10, 9 },
			{ 10, 9, 3, 10, 3, 2, 9, 4, 3, 11, 3, 6, 4, 6, 3 }, { 8, 2, 3, 8, 4, 2, 4, 6, 2 }, { 0, 4, 2, 4, 6, 2 },
			{ 1, 9, 0, 2, 3, 4, 2, 4, 6, 4, 3, 8 }, { 1, 9, 4, 1, 4, 2, 2, 4, 6 },
			{ 8, 1, 3, 8, 6, 1, 8, 4, 6, 6, 10, 1 }, { 10, 1, 0, 10, 0, 6, 6, 0, 4 },
			{ 4, 6, 3, 4, 3, 8, 6, 10, 3, 0, 3, 9, 10, 9, 3 }, { 10, 9, 4, 6, 10, 4 }, { 4, 9, 5, 7, 6, 11 },
			{ 0, 8, 3, 4, 9, 5, 11, 7, 6 }, { 5, 0, 1, 5, 4, 0, 7, 6, 11 }, { 11, 7, 6, 8, 3, 4, 3, 5, 4, 3, 1, 5 },
			{ 9, 5, 4, 10, 1, 2, 7, 6, 11 }, { 6, 11, 7, 1, 2, 10, 0, 8, 3, 4, 9, 5 },
			{ 7, 6, 11, 5, 4, 10, 4, 2, 10, 4, 0, 2 }, { 3, 4, 8, 3, 5, 4, 3, 2, 5, 10, 5, 2, 11, 7, 6 },
			{ 7, 2, 3, 7, 6, 2, 5, 4, 9 }, { 9, 5, 4, 0, 8, 6, 0, 6, 2, 6, 8, 7 },
			{ 3, 6, 2, 3, 7, 6, 1, 5, 0, 5, 4, 0 }, { 6, 2, 8, 6, 8, 7, 2, 1, 8, 4, 8, 5, 1, 5, 8 },
			{ 9, 5, 4, 10, 1, 6, 1, 7, 6, 1, 3, 7 }, { 1, 6, 10, 1, 7, 6, 1, 0, 7, 8, 7, 0, 9, 5, 4 },
			{ 4, 0, 10, 4, 10, 5, 0, 3, 10, 6, 10, 7, 3, 7, 10 }, { 7, 6, 10, 7, 10, 8, 5, 4, 10, 4, 8, 10 },
			{ 6, 9, 5, 6, 11, 9, 11, 8, 9 }, { 3, 6, 11, 0, 6, 3, 0, 5, 6, 0, 9, 5 },
			{ 0, 11, 8, 0, 5, 11, 0, 1, 5, 5, 6, 11 }, { 6, 11, 3, 6, 3, 5, 5, 3, 1 },
			{ 1, 2, 10, 9, 5, 11, 9, 11, 8, 11, 5, 6 }, { 0, 11, 3, 0, 6, 11, 0, 9, 6, 5, 6, 9, 1, 2, 10 },
			{ 11, 8, 5, 11, 5, 6, 8, 0, 5, 10, 5, 2, 0, 2, 5 }, { 6, 11, 3, 6, 3, 5, 2, 10, 3, 10, 5, 3 },
			{ 5, 8, 9, 5, 2, 8, 5, 6, 2, 3, 8, 2 }, { 9, 5, 6, 9, 6, 0, 0, 6, 2 },
			{ 1, 5, 8, 1, 8, 0, 5, 6, 8, 3, 8, 2, 6, 2, 8 }, { 1, 5, 6, 2, 1, 6 },
			{ 1, 3, 6, 1, 6, 10, 3, 8, 6, 5, 6, 9, 8, 9, 6 }, { 10, 1, 0, 10, 0, 6, 9, 5, 0, 5, 6, 0 },
			{ 0, 3, 8, 5, 6, 10 }, { 10, 5, 6 }, { 11, 5, 10, 7, 5, 11 }, { 11, 5, 10, 11, 7, 5, 8, 3, 0 },
			{ 5, 11, 7, 5, 10, 11, 1, 9, 0 }, { 10, 7, 5, 10, 11, 7, 9, 8, 1, 8, 3, 1 },
			{ 11, 1, 2, 11, 7, 1, 7, 5, 1 }, { 0, 8, 3, 1, 2, 7, 1, 7, 5, 7, 2, 11 },
			{ 9, 7, 5, 9, 2, 7, 9, 0, 2, 2, 11, 7 }, { 7, 5, 2, 7, 2, 11, 5, 9, 2, 3, 2, 8, 9, 8, 2 },
			{ 2, 5, 10, 2, 3, 5, 3, 7, 5 }, { 8, 2, 0, 8, 5, 2, 8, 7, 5, 10, 2, 5 },
			{ 9, 0, 1, 5, 10, 3, 5, 3, 7, 3, 10, 2 }, { 9, 8, 2, 9, 2, 1, 8, 7, 2, 10, 2, 5, 7, 5, 2 },
			{ 1, 3, 5, 3, 7, 5 }, { 0, 8, 7, 0, 7, 1, 1, 7, 5 }, { 9, 0, 3, 9, 3, 5, 5, 3, 7 }, { 9, 8, 7, 5, 9, 7 },
			{ 5, 8, 4, 5, 10, 8, 10, 11, 8 }, { 5, 0, 4, 5, 11, 0, 5, 10, 11, 11, 3, 0 },
			{ 0, 1, 9, 8, 4, 10, 8, 10, 11, 10, 4, 5 }, { 10, 11, 4, 10, 4, 5, 11, 3, 4, 9, 4, 1, 3, 1, 4 },
			{ 2, 5, 1, 2, 8, 5, 2, 11, 8, 4, 5, 8 }, { 0, 4, 11, 0, 11, 3, 4, 5, 11, 2, 11, 1, 5, 1, 11 },
			{ 0, 2, 5, 0, 5, 9, 2, 11, 5, 4, 5, 8, 11, 8, 5 }, { 9, 4, 5, 2, 11, 3 },
			{ 2, 5, 10, 3, 5, 2, 3, 4, 5, 3, 8, 4 }, { 5, 10, 2, 5, 2, 4, 4, 2, 0 },
			{ 3, 10, 2, 3, 5, 10, 3, 8, 5, 4, 5, 8, 0, 1, 9 }, { 5, 10, 2, 5, 2, 4, 1, 9, 2, 9, 4, 2 },
			{ 8, 4, 5, 8, 5, 3, 3, 5, 1 }, { 0, 4, 5, 1, 0, 5 }, { 8, 4, 5, 8, 5, 3, 9, 0, 5, 0, 3, 5 }, { 9, 4, 5 },
			{ 4, 11, 7, 4, 9, 11, 9, 10, 11 }, { 0, 8, 3, 4, 9, 7, 9, 11, 7, 9, 10, 11 },
			{ 1, 10, 11, 1, 11, 4, 1, 4, 0, 7, 4, 11 }, { 3, 1, 4, 3, 4, 8, 1, 10, 4, 7, 4, 11, 10, 11, 4 },
			{ 4, 11, 7, 9, 11, 4, 9, 2, 11, 9, 1, 2 }, { 9, 7, 4, 9, 11, 7, 9, 1, 11, 2, 11, 1, 0, 8, 3 },
			{ 11, 7, 4, 11, 4, 2, 2, 4, 0 }, { 11, 7, 4, 11, 4, 2, 8, 3, 4, 3, 2, 4 },
			{ 2, 9, 10, 2, 7, 9, 2, 3, 7, 7, 4, 9 }, { 9, 10, 7, 9, 7, 4, 10, 2, 7, 8, 7, 0, 2, 0, 7 },
			{ 3, 7, 10, 3, 10, 2, 7, 4, 10, 1, 10, 0, 4, 0, 10 }, { 1, 10, 2, 8, 7, 4 }, { 4, 9, 1, 4, 1, 7, 7, 1, 3 },
			{ 4, 9, 1, 4, 1, 7, 0, 8, 1, 8, 7, 1 }, { 4, 0, 3, 7, 4, 3 }, { 4, 8, 7 }, { 9, 10, 8, 10, 11, 8 },
			{ 3, 0, 9, 3, 9, 11, 11, 9, 10 }, { 0, 1, 10, 0, 10, 8, 8, 10, 11 }, { 3, 1, 10, 11, 3, 10 },
			{ 1, 2, 11, 1, 11, 9, 9, 11, 8 }, { 3, 0, 9, 3, 9, 11, 1, 2, 9, 2, 11, 9 }, { 0, 2, 11, 8, 0, 11 },
			{ 3, 2, 11 }, { 2, 3, 8, 2, 8, 10, 10, 8, 9 }, { 9, 10, 2, 0, 9, 2 },
			{ 2, 3, 8, 2, 8, 10, 0, 1, 8, 1, 10, 8 }, { 1, 10, 2 }, { 1, 3, 8, 9, 1, 8 }, { 0, 9, 1 }, { 0, 3, 8 },
			{} };
}