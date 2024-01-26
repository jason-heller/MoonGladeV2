package engine.io;

import static engine.gl.meshing.MeshUtil.toFloatBuffer;
import static engine.gl.meshing.MeshUtil.toIntBuffer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import engine.data.TexturedMeshData;
import engine.dev.Log;

// A Simple OBJ loader, only accounts for triangulated meshes with only one UV map.
public class ObjLoader {
	
	private static float txOffset, tyOffset;
	
	public static TexturedMeshData loadMesh(String resource, float s, float t) {
		txOffset = s;
		tyOffset = t;
		List<String> lines = null;
		
		Path path = Paths.get(resource);
		if (path != null && Files.isReadable(path)) {
			try {
				lines = Files.readAllLines(path);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			return loadMesh(lines);
		}
		
		new FileNotFoundException("File not found: " + resource).printStackTrace();
		return null;
	}

	private static TexturedMeshData loadMesh(List<String> lines) {
		final List<Float> vertices = new ArrayList<>();
		final List<Float> texCoords = new ArrayList<>();
		final List<Float> normals = new ArrayList<>();
		final List<Integer> indices = new ArrayList<>();
		final Map<Int3, Integer> indexPairs = new HashMap<>();
		
		for(String line : lines) {
			parseLine(line, vertices, texCoords, normals, indexPairs, indices);
		}
		
		final FloatBuffer positionBuffer 	= toFloatBuffer(vertices);
		final FloatBuffer texCoordBuffer 	= toFloatBuffer(texCoords);
		final FloatBuffer normalBuffer 		= toFloatBuffer(normals);
		final IntBuffer   indexBuffer 		= toIntBuffer(indices);
		
		final TexturedMeshData meshData = new TexturedMeshData(positionBuffer, texCoordBuffer, normalBuffer, indexBuffer);
		
		meshData.numVertices = vertices.size() / 3;
		meshData.numIndices = indices.size();

		return meshData;
	}

	private static void parseLine(String line, List<Float> vertices, List<Float> texCoords, List<Float> normals,
			Map<Int3, Integer> indexPairs, List<Integer> indices) {

		String[] lineParts = line.split(" ");
		
		switch(lineParts[0]) {
		case "v":
			vertices.add(parseFloat(lineParts[1]));
			vertices.add(parseFloat(lineParts[2]));
			vertices.add(parseFloat(lineParts[3]));
			break;
		case "vt":
			texCoords.add(parseFloat(lineParts[1]) + txOffset);
			texCoords.add(parseFloat(lineParts[2]) + tyOffset);
			break;
		case "vn":
			normals.add(parseFloat(lineParts[1]));
			normals.add(parseFloat(lineParts[2]));
			normals.add(parseFloat(lineParts[3]));
			break;
		case "f":
			getIndex(lineParts[1], indexPairs, indices);
			getIndex(lineParts[2], indexPairs, indices);
			getIndex(lineParts[3], indexPairs, indices);
			break;
		default:
			Log.trace("Ignored line: "  + line);
		}
	}
	
	private static void getIndex(String linePart, Map<Int3, Integer> indexPairs, List<Integer> indices) {
		String[] lineSubParts = linePart.split("/");
		Int3 indexPair = new Int3();
		indexPair.x = parseInt(lineSubParts[0]);
		indexPair.y = parseInt(lineSubParts[1]);
		indexPair.z = parseInt(lineSubParts[2]);
		
		if (indexPairs.containsKey(indexPair)) {
			indices.add(indexPairs.get(indexPair));
		} else {
			int index = indexPairs.keySet().size();
			indices.add(index);
			indexPairs.put(indexPair, index);
		}
	}

	private static float parseFloat(String linePart) {
		return Float.parseFloat(linePart);
	}
	
	private static int parseInt(String linePart) {
		return Integer.parseInt(linePart);
	}
}

class Int3 {
	int x, y, z;

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Int3))
			return false;

		Int3 i = (Int3) o;
		return (i.x == x && i.y == y && i.z == z);
	}

	@Override
	public int hashCode() {
		return Integer.hashCode(x + y + z);
	}
}