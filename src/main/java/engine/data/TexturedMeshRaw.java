package engine.data;

import java.util.List;

public class TexturedMeshRaw {
	
	public final List<Float> vertices, texCoords, normals;
	public final List<Integer> indices;

	public TexturedMeshRaw(List<Float> vertices, List<Float> texCoords, List<Float> normals, List<Integer> indices) {
		this.vertices = vertices;
		this.texCoords = texCoords;
		this.normals = normals;
		this.indices = indices;
	}

}
