package engine.geo;

import static engine.gl.meshing.transvoxel.RegularTables.EDGE_DATA;
import static engine.gl.meshing.transvoxel.RegularTables.EQUIV_CLASSES;
import static engine.gl.meshing.transvoxel.RegularTables.REGULAR_VERTEX_COUNT;
import static engine.gl.meshing.transvoxel.RegularTables.TRANSITION_POINTS_COURSE;
import static engine.gl.meshing.transvoxel.RegularTables.VERTEX_ORDER;
import static engine.gl.meshing.transvoxel.RegularTables.VERT_AND_INDEX_COUNTS;

import org.joml.Vector3f;

import engine.gl.meshing.transvoxel.ScalarField;

// As apposed to Transvoxel, this is more used as a util class for marching cube related maths
public class MarchingCubes {

	private final Vector3f[] cubeVertices;
	private final float[] scalars;

	private final float voxelSize;
	private final float threshold;

	private ScalarField field;

	public MarchingCubes(float voxelSize, float threshold) {
		cubeVertices = new Vector3f[REGULAR_VERTEX_COUNT];
		scalars = new float[REGULAR_VERTEX_COUNT];

		this.voxelSize = voxelSize;
		this.threshold = threshold;
	}

	public Vector3f[] getVertices(int x, int y, int z) {
		int caseID = getRegularCaseID(x, y, z);

		int classID = EQUIV_CLASSES[caseID];
		byte[] vertexOrder = VERTEX_ORDER[classID];
		int numVertices = (VERT_AND_INDEX_COUNTS[classID] & 0xF0) >> 4;
		char[] edgeBitField = EDGE_DATA[caseID];

		if (edgeBitField.length == 0)
			return new Vector3f[] {};

		Vector3f[] terrainVertices = computeVertices(numVertices, cubeVertices, edgeBitField);
		Vector3f[] triangles = new Vector3f[vertexOrder.length];

		for (int i = 0; i < vertexOrder.length; ++i) {
			int index = vertexOrder[i];
			triangles[i] = terrainVertices[index];
		}

		return triangles;
	}

	private int getRegularCaseID(int indexX, int indexY, int indexZ) {

		final int len = TRANSITION_POINTS_COURSE.length;

		int cubeVertexIndex = 0;
		int caseID = 0;
		int offset = 1;

		for (int i = 0; i < len; ++i) {
			final int[] point = TRANSITION_POINTS_COURSE[i];
			final int vx = indexX + point[0], vy = indexY + point[1], vz = indexZ + point[2];
			float dx = 0, dz = 0;

			cubeVertices[cubeVertexIndex] = new Vector3f((vx + dx) * voxelSize, vy * voxelSize, (vz + dz) * voxelSize);
			float scalar = field.getDensity(vx, vy, vz);
			scalars[cubeVertexIndex++] = scalar;

			if (scalar > threshold)
				caseID |= offset;

			offset <<= 1;
		}

		return caseID;
	}

	private Vector3f[] computeVertices(int numVertices, Vector3f[] cubeVertices, char[] edgeBitField) {
		Vector3f[] outVertices = new Vector3f[numVertices];

		for (int i = 0; i < edgeBitField.length; ++i) {
			char edgeData = edgeBitField[i];
			int v2 = edgeData & 0x0F;
			int v1 = (edgeData & 0xF0) >> 4;

			outVertices[i] = lerpVertices(cubeVertices[v1], cubeVertices[v2], scalars[v1], scalars[v2]);
		}

		return outVertices;
	}

	private Vector3f lerpVertices(Vector3f firstVertex, Vector3f secondVertex, float firstScalar, float secondScalar) {
		float lerpFactor;

		if (firstScalar == secondScalar)
			lerpFactor = 0f;
		else
			lerpFactor = (0.5f - firstScalar) / (secondScalar - firstScalar);

		return new Vector3f(firstVertex).lerp(secondVertex, lerpFactor);
	}
	
	public void setScalarField(ScalarField field) {
		this.field = field;
	}
}
