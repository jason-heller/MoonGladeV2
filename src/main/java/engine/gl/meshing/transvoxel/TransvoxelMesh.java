package engine.gl.meshing.transvoxel;

import java.util.List;

public class TransvoxelMesh {
	public final List<Float> positions, normals;
	public final List<Integer> indices;
	
	public TransvoxelMesh(List<Float> positions, List<Float> normals, List<Integer> indices) {
		this.positions = positions;
		this.normals = normals;
		this.indices = indices;
	}
}
