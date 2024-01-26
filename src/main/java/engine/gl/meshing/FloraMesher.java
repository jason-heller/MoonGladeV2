package engine.gl.meshing;

import static engine.gl.meshing.MeshUtil.toFloatBuffer;
import static engine.gl.meshing.MeshUtil.toIntBuffer;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.Vector3f;

import engine.data.TexturedMeshData;
import engine.data.chunk.IByteLayerData;
import engine.io.ObjLoader;
import engine.pg.biome.Floras;
import engine.utils.math.Maths;
import engine.world.Chunk;

public class FloraMesher {

	public static final int NUM_FLORA_MESHES = Floras.values().length;

	private static final float MAX_SLOPE = 1f;
	
	private final List<Float> vertices = new ArrayList<>();
	private final List<Float> texCoords = new ArrayList<>();
	private final List<Float> normals = new ArrayList<>();
	private final List<Integer> indices = new ArrayList<>();

	private Map<Integer, TexturedMeshData> floraMeshes;
	
	public FloraMesher() {
		floraMeshes = new HashMap<>();
		
		for(int i = 1; i < Floras.values().length; ++i) {
			Floras mesh = Floras.values()[i];
			TexturedMeshData resource = ObjLoader.loadMesh(mesh.getResourcePath(), mesh.s, mesh.t);
			floraMeshes.put(i, resource);
		}
	}
	
	public TexturedMeshData createMesh(IByteLayerData floraIDs, IByteLayerData heights, int chunkX, int chunkZ, int chunkScale) {
		vertices.clear();
		texCoords.clear();
		normals.clear();
		indices.clear();
		
		int indexOffset = 0;
		int flipX, flipZ;
		float scale;

		final int FLORA_PER_ROW = Chunk.CHUNK_WIDTH * chunkScale;
		
		//int floraIndex = 0, heightIndex = 0;
		
		for (int x = 0; x < FLORA_PER_ROW; x ++) {
			for (int z = 0; z < FLORA_PER_ROW; z ++) {
				// Get heights
				float height;
				float weightX = (x % chunkScale) / (float)chunkScale;
				float weightZ = (z % chunkScale) / (float)chunkScale;
				final int hOrigin = (x / chunkScale) + ((z / chunkScale) * Chunk.NUM_VERTICES_X);
			
				// 0--1
				// |  |
				// 2--3
				final int[] localHeights = { heights.get(hOrigin) * chunkScale, heights.get(hOrigin + 1) * chunkScale,
						heights.get(hOrigin + Chunk.NUM_VERTICES_X) * chunkScale,
						heights.get(hOrigin + Chunk.NUM_VERTICES_X + 1) * chunkScale, };

				/*heights[hOrigin] & 0xFF,
				heights[hOrigin + 1] & 0xFF,
				heights[hOrigin + Chunk.NUM_VERTICES_X] & 0xFF,
				heights[hOrigin+Chunk.NUM_VERTICES_X + 1] & 0xFF,*/

				// Make sure we're not meshing off a cliff
				if (Math.abs(localHeights[3] - localHeights[0]) > MAX_SLOPE * chunkScale)
					continue;
				if (Math.abs(localHeights[2] - localHeights[1]) > MAX_SLOPE * chunkScale)
					continue;
				
				int floraIndex = x + (z * Chunk.CHUNK_WIDTH * chunkScale);
				int value = floraIDs.get(floraIndex);
				int floraID = value & 0xFFFFFF;
				flipX = (value & 0x40000000) != 0 ? -1 : 1;
				flipZ = (value & 0x20000000) != 0 ? -1 : 1;
				scale = 1 + (((value >> 26) & 0xF) / 0xF);
				
				if (floraID <= 0 || floraID > NUM_FLORA_MESHES)
					continue;

				TexturedMeshData meshData = floraMeshes.get(floraID);
				
				meshData.flip();
				
				if (weightX > 1f - weightZ) {
					height = Maths.barycentric(weightX, weightZ,
							new Vector3f(1, localHeights[1], 0),
							new Vector3f(0, localHeights[2], 1),
							new Vector3f(1, localHeights[3], 1));
				} else {
					height = Maths.barycentric(weightX, weightZ,
							new Vector3f(0, localHeights[0], 0),
							new Vector3f(1, localHeights[1], 0),
							new Vector3f(0, localHeights[2], 1));
				}
				
				float dx = chunkX + (x);
				float dy = height + (chunkScale / 2);// + (chunkScale == 2 ? -.5f : 0);
				float dz = chunkZ + (z);
	
				addData(vertices, texCoords, normals, indices, meshData, indexOffset, dx, dy, dz, chunkScale, flipX, flipZ, scale);
				
				indexOffset += meshData.numVertices;
				
			}
		}
		
		FloatBuffer positionBuffer = toFloatBuffer(vertices);
		FloatBuffer texCoordBuffer = toFloatBuffer(texCoords);
		FloatBuffer normalBuffer = toFloatBuffer(normals);
		IntBuffer   indexBuffer = toIntBuffer(indices);
		
		final TexturedMeshData meshData = new TexturedMeshData(positionBuffer, texCoordBuffer, normalBuffer, indexBuffer);
		meshData.flip();
		
		meshData.numVertices = vertices.size() / 3;
		meshData.numIndices = indices.size();
		
		return meshData;
	}

	private void addData(List<Float> vertices, List<Float> texCoords, List<Float> normals, List<Integer> indices, TexturedMeshData meshData, int indexOffset, float dx, float dy, float dz, float chunkScale,
			int flipX, int flipZ, float scale) {
		int i = 0;
		
		boolean flip = (flipX==-1 ^ flipZ==-1);
		
		for(; i < meshData.numVertices; ++i) {
			vertices.add(flipX * meshData.getPositionBuffer().get() * scale + dx);
			vertices.add(meshData.getPositionBuffer().get() * scale + dy);
			vertices.add(flipZ * meshData.getPositionBuffer().get() * scale + dz);
			
			texCoords.add(meshData.getTexCoordBuffer().get());
			texCoords.add(meshData.getTexCoordBuffer().get());
			
			normals.add(meshData.getNormalBuffer().get()*flipX);
			normals.add(meshData.getNormalBuffer().get());
			normals.add(meshData.getNormalBuffer().get()*flipZ);
		}
		
		if (!flip) {
			for(i = 0; i < meshData.numIndices; ++i) {
				final int index = meshData.getIndexBuffer().get();
				indices.add(indexOffset + index);
			}
		} else {
			for(i = 0; i < meshData.numIndices; i += 3) {
				final int i1 = meshData.getIndexBuffer().get();
				final int i2 = meshData.getIndexBuffer().get();
				final int i3 = meshData.getIndexBuffer().get();
				indices.add(indexOffset + i1);
				indices.add(indexOffset + i3);
				indices.add(indexOffset + i2);
			}
		}
	}
	
	private float lerpHeight(float firstVertex, float secondVertex, float firstScalar, float secondScalar) {
		float lerpFactor;

		if (firstScalar == secondScalar)
			lerpFactor = 0f;
		else
			lerpFactor = (0.5f - firstScalar) / (secondScalar - firstScalar);

		return Maths.lerp(firstVertex, secondVertex, lerpFactor);
	}
	
	public void destroy() {
		for(TexturedMeshData meshData : floraMeshes.values()) {
			meshData.free();
		}
	}
}
