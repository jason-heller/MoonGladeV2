package engine.gl.meshing.transvoxel;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;

import static engine.gl.meshing.transvoxel.RegularTables.REGULAR_VERTEX_COUNT;
import static engine.gl.meshing.transvoxel.TransitionTables.TRANSITION_VERTEX_COUNT;

/*
 * Transvoxel
 * Lengyel, Eric. “Voxel-Based Terrain for Real-Time Virtual Simulations”. PhD diss., University of California at Davis, 2010.
 */
public class Transvoxel {
	
	private static final float SEAM_SIZE = 0.1f;

	private float voxelSize;
	private int gridSizeX;
	private int gridSizeY;
	private int gridSizeZ;
	
	private float[] cubeScalars;	// TODO: to byte
	
	private List<Float> vertices, normals;
	private List<Integer> indices;
	
	private ScalarField scalars;
	private ScalarField scalarsAdjacent;
	
	private float edgeOffsetX = 0, edgeOffsetZ = 0;
	
	private Seam seam;
	
	private float threshold;
	
	public Transvoxel(int gridSizeX, int gridSizeY, int gridSizeZ, float voxelSize, float threshold) {
		this.voxelSize = voxelSize;
		this.gridSizeX = gridSizeX;
		this.gridSizeY = gridSizeY;
		this.gridSizeZ = gridSizeZ;
		this.threshold = threshold;
	}
	
	public TransvoxelMesh createMesh(ScalarField scalars, byte[] upperBoundsY) {
		this.scalars = scalars;
		this.scalarsAdjacent = null;
		this.seam = Seam.NO_SEAM;

		return extract(upperBoundsY);
	}
	
	public TransvoxelMesh createMesh(ScalarField scalars, ScalarField scalarsAdjacent, Seam seam, byte[] upperBoundsY) {
		this.scalars = scalars;
		this.scalarsAdjacent = scalarsAdjacent;
		this.seam = seam;

		return extract(upperBoundsY);
	}

	private TransvoxelMesh extract(byte[] upperBoundsY) {
		vertices = new ArrayList<>();
		indices = new ArrayList<>();
		normals = new ArrayList<>();
		
		// Do regular cells
		extractRegularCells(upperBoundsY);
		
		// Do transition cells (U/V instead of X/Z)
		if (seam != Seam.NO_SEAM)
			extractTransitionCells(upperBoundsY);

		// Normals (temp)
		Vector3f p1 = new Vector3f(),
				p2 = new Vector3f(),
				p3 = new Vector3f(),
				normal = new Vector3f();
		for(int i = 0 ; i < indices.size(); i += 3) {
			
			int[] ids = {
					indices.get(i) * 3,
					indices.get(i+1) * 3,
					indices.get(i+2) * 3
			};
			
			p1.set(vertices.get(ids[0]), vertices.get(ids[0] + 1), vertices.get(ids[0] + 2));
			p2.set(vertices.get(ids[1]), vertices.get(ids[1] + 1), vertices.get(ids[1] + 2));
			p3.set(vertices.get(ids[2]), vertices.get(ids[2] + 1), vertices.get(ids[2] + 2));
			
			p2.sub(p1);
			p3.sub(p1);
			
			normal.set(p2).cross(p3).normalize();

			for(int j = 0; j < 3; j++) {
				normals.set(ids[j],     normal.x);
				normals.set(ids[j] + 1, normal.y);
				normals.set(ids[j] + 2, normal.z);
			}
		}
		
		return new TransvoxelMesh(vertices, normals, indices);
	}

	private void extractRegularCells(byte[] upperBoundsY) {
		// TODO: These seam IFs could just be built into the for loop
		for (int x = 0; x < gridSizeX; ++x) {
			edgeOffsetX = 0;
			
			if (seam == Seam.NEG_X && x == 0)
				edgeOffsetX = SEAM_SIZE;
			if (seam == Seam.POS_X && x == gridSizeX-1)
				edgeOffsetX = -SEAM_SIZE;
			
			for (int z = 0; z < gridSizeZ; ++z) {
				edgeOffsetZ = 0;
				
				if (seam == Seam.NEG_Z && z == 0)
					edgeOffsetZ = SEAM_SIZE;
				if (seam == Seam.POS_Z && z == gridSizeX-1)
					edgeOffsetZ = -SEAM_SIZE;
				
				for (int y = upperBoundsY[x + (z * gridSizeX)]; y < gridSizeY; ++y) {
					Vector3f[] cubeVertices = new Vector3f[REGULAR_VERTEX_COUNT];
					int caseID = getRegularCaseID(cubeVertices, x, y, z);
					int classID = RegularTables.EQUIV_CLASSES[caseID];
					
					byte[] vertexOrder = RegularTables.VERTEX_ORDER[classID];
					char componentCounts = RegularTables.VERT_AND_INDEX_COUNTS[classID]; // 2 nibbles: vertex #, tri #
					char[] edgeBitField = RegularTables.EDGE_DATA[caseID];	// 4 nibbles: E1 reuse, E2 reuse, E1, E2
					
					if (edgeBitField.length == 0)
						continue;
					
					// int[] outIndices = new int[(componentCounts & 0x0F) * 3];
					Vector3f[] terrainVertices = computeVertices((componentCounts & 0xF0) >> 4, cubeVertices, edgeBitField);
					
					int lastIndex = vertices.size() / 3;
					for(int i = 0 ; i < terrainVertices.length; ++i) {
						Vector3f vertex = terrainVertices[i];
						vertices.add(vertex.x);
						vertices.add(vertex.y);
						vertices.add(vertex.z);
						normals.add(0f);
						normals.add(0f);
						normals.add(0f);
					}
					
					for(int i = 0 ; i < vertexOrder.length; ++i) {
						int index = vertexOrder[i];
						indices.add(lastIndex + index);
					}
				}
			}
		}
	}
	
	private void extractTransitionCells(byte[] upperBoundsY) {
		for (int u = 0; u < Seam.SEAM_LENGTH; u ++) {
	
			final int x = (seam.initX * Seam.SEAM_LENGTH) + (u * seam.incX);
			final int z = (seam.initZ * Seam.SEAM_LENGTH) + (u * seam.incZ);
			
			edgeOffsetX = 0;
			edgeOffsetZ = 0;
			
			// These are flipped from the regular cells
			if (seam == Seam.NEG_X && x == 0)
				edgeOffsetX = SEAM_SIZE;
			if (seam == Seam.POS_X && x == gridSizeX)
				edgeOffsetX = -SEAM_SIZE;
			if (seam == Seam.NEG_Z && z == 0)
				edgeOffsetZ = SEAM_SIZE;
			if (seam == Seam.POS_Z && z == gridSizeX)
				edgeOffsetZ = -SEAM_SIZE;
			
			final int upperBoundsOffset = (x + (z * gridSizeX));
			for (int y = upperBoundsY[upperBoundsOffset]; y < gridSizeY; y ++) {
				Vector3f[] cubeVertices = new Vector3f[TRANSITION_VERTEX_COUNT];
				
				int caseID = getTransitionCaseID(cubeVertices, x, y, z);	// 0 to 512
				int classID = TransitionTables.EQUIV_CLASSES[caseID];
				boolean reverseOrder = (classID & 0x80) != 0;

				// Probably due to the points not flipping when rotated for other seams
				if (seam == Seam.NEG_X || seam == Seam.POS_Z)
					reverseOrder = !reverseOrder;
				
				classID = classID & 0x7F;	// Remove high bit
				
				byte[] vertexOrder = TransitionTables.VERTEX_ORDER[classID];
				char componentCounts = TransitionTables.VERT_AND_INDEX_COUNTS[classID]; // 2 nibbles: vertex #, tri #
				char[] edgeBitField = TransitionTables.EDGE_DATA[caseID];	// 4 nibbles: E1 reuse, E2 reuse, E1, E2
				
				if (edgeBitField.length == 0)
					continue;
				
				// int[] outIndices = new int[(componentCounts & 0x0F) * 3];
				final int numVertices = (componentCounts & 0xF0) >> 4;
				Vector3f[] terrainVertices = computeVertices(numVertices, cubeVertices, edgeBitField);
				
				int lastIndex = vertices.size() / 3;
				for(int i = 0 ; i < terrainVertices.length; ++i) {
					Vector3f vertex = terrainVertices[i];
					vertices.add(vertex.x);
					vertices.add(vertex.y);
					vertices.add(vertex.z);
					normals.add(0f);
					normals.add(0f);
					normals.add(0f);
				}
				
				final int numIndices = vertexOrder.length;

				for(int i = 0 ; i < numIndices; ++i) {
					int offset = reverseOrder ? (numIndices - i) - 1 : i;
					int index = vertexOrder[offset];

					indices.add(lastIndex + index);
				}
				
			}
		}
	}

	private int getRegularCaseID(Vector3f[] cubeVertices, int indexX, int indexY, int indexZ) {
		cubeScalars = new float[REGULAR_VERTEX_COUNT];
		int cubeVertexIndex = 0;
		int caseID = 0;
		int offset = 1;

		for (int i = 0; i < RegularTables.TRANSITION_POINTS_COURSE.length; ++i) {
			final int[] point = RegularTables.TRANSITION_POINTS_COURSE[i];
			final int vx = indexX + point[0], vy = indexY + point[1], vz = indexZ + point[2];
			float dx = 0, dz = 0;
			
			if (vx == 0 || vx == Seam.SEAM_LENGTH)
				dx = edgeOffsetX;
			if (vz == 0 || vz == Seam.SEAM_LENGTH)
				dz = edgeOffsetZ;
			
			cubeVertices[cubeVertexIndex] = new Vector3f((vx+dx) * voxelSize , vy * voxelSize, (vz+dz) * voxelSize);
			float scalar = scalars.getDensity(vx, vy, vz);
			cubeScalars[cubeVertexIndex++] = scalar;

			// If the density is solid
			if (scalar > threshold)
				caseID |= offset;
			
			offset <<= 1;
		}

		return caseID;
	}
	
	private int getTransitionCaseID(Vector3f[] cubeVertices, int indexX, int indexY, int indexZ) {
		cubeScalars = new float[TRANSITION_VERTEX_COUNT];
		int cubeVertexIndex = 0;
		int caseID = 0;
		
		// Index into the previous LOD
		int indexXLower = indexX * 2, indexYLower = indexY * 2, indexZLower = indexZ * 2;
		
		final float halfSize = voxelSize * 0.5f;		// Since the transition happens with the courser (larger) region, we scale it down to match the finer region

		for (int i = 0; i < TransitionTables.TRANSITION_POINTS_FINE.length; ++i) {

			final char[] point = TransitionTables.TRANSITION_POINTS_FINE[i];
			final int adjX, adjY, adjZ;	// Index into adjacent chunk's data
			final int vertOffX, vertOffZ;

			adjY = indexYLower + point[1];
			
			switch (seam) {
			case POS_X:
				adjX = 0;
				adjZ = indexZLower + point[0];
				vertOffX = 2 * Seam.SEAM_LENGTH;
				vertOffZ = adjZ;
				break;
			case NEG_X:
				adjX = Seam.SEAM_LENGTH;
				adjZ = indexZLower + point[0];
				vertOffX = 0;
				vertOffZ = adjZ;
				break;
			case POS_Z:
				adjX = indexXLower + point[0];
				adjZ = 0;
				vertOffX = adjX;
				vertOffZ = 2 * Seam.SEAM_LENGTH;
				break;
			case NEG_Z:
				adjX = indexXLower + point[0];
				adjZ = Seam.SEAM_LENGTH;
				vertOffX = adjX;
				vertOffZ = 0;
				break;
			default:
				throw new IllegalArgumentException("Cannot process a region with no seam.");
			}
			
			final Vector3f vertex = new Vector3f(vertOffX * halfSize, adjY * halfSize, vertOffZ * halfSize);
			float scalar = scalarsAdjacent.getDensity(adjX, adjY, adjZ);
			cubeVertices[cubeVertexIndex] = vertex;
			cubeScalars[cubeVertexIndex++] = scalar;

			// If the density is solid
			if (scalar > threshold)
				caseID |= point[2];
		}
		
		for(int i = 0; i < TransitionTables.TRANSITION_POINTS_COURSE.length; ++i) {
			final int pointIndex = i + 9;
			final char[] point = TransitionTables.TRANSITION_POINTS_COURSE[i];
			
			final int adjX, adjY, adjZ;
			adjY = indexY + point[1];
			
			switch (seam) {
			case POS_X:
				adjX = Seam.SEAM_LENGTH;
				adjZ = indexZ + point[0];
				break;
			case NEG_X:
				adjX = 0;
				adjZ = indexZ + point[0];
				break;
			case POS_Z:
				adjX = indexX + point[0];
				adjZ = Seam.SEAM_LENGTH;
				break;
			case NEG_Z:
				adjX = indexX + point[0];
				adjZ = 0;
				break;
			default:
				throw new IllegalArgumentException("Cannot process a region with no seam.");
			}
			
			final Vector3f vertex = new Vector3f(adjX + edgeOffsetX, adjY, adjZ + edgeOffsetZ);
			float scalar = scalars.getDensity(adjX, adjY, adjZ);
			cubeVertices[pointIndex] = vertex;
			cubeScalars[cubeVertexIndex++] = scalar;
		}

		return caseID;
	}

	private Vector3f[] computeVertices(int numVertices, Vector3f[] cubeVertices, char[] edgeBitField) {
		Vector3f[] terrainVertices = new Vector3f[numVertices];
		
		for(int i = 0; i < edgeBitField.length; ++i) {
			char edgeData = edgeBitField[i];
			int v2 = edgeData & 0x0F;
			int v1 = (edgeData & 0xF0) >> 4;
			//int r2 = (edgeData >> 8) & 0x0F;
			//int r1 = (edgeData >> 12) & 0x0F;
			
			terrainVertices[i] = lerpVertices(cubeVertices[v1], cubeVertices[v2], cubeScalars[v1], cubeScalars[v2]);
		}
		
		return terrainVertices;
	}
	
	private Vector3f lerpVertices(Vector3f firstVertex, Vector3f secondVertex, float firstScalar, float secondScalar) {
		float lerpFactor;

		if (firstScalar == secondScalar)
			lerpFactor = 0f;
		else
			lerpFactor = (0.5f - firstScalar) / (secondScalar - firstScalar);

		return new Vector3f(firstVertex).lerp(secondVertex, lerpFactor);
		/*if (Math.abs(firstScalar) < Math.ulp(1f))
			return firstVertex;
		if (Math.abs(secondScalar) < Math.ulp(1f))
			return secondVertex;
		if (Math.abs(firstScalar - secondScalar) < Math.ulp(1f))
			return firstVertex;

		float lerpFactor = (-firstScalar) / (secondScalar - firstScalar);

		return new Vector3f(firstVertex).lerp(secondVertex, lerpFactor);*/
	}
	
	public void setGridY(int gridSizeY) {
		this.gridSizeY = gridSizeY;
	}

	public int getGridSizeY() {
		return gridSizeY;
	}
}
