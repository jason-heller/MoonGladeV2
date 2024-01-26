package engine.world;

import engine.gl.data.GenericMesh;

public class ChunkMeshContainer {
	public final int x, z;
	public final byte lod;
	private GenericMesh terrainMesh, floraMesh;
	
	public ChunkMeshContainer(int x, int z, byte lod) {
		this.x = x;
		this.z = z;
		this.lod = lod;
	}

	public void destroy() {
		if (this.terrainMesh != null)
			terrainMesh.destroy();
		if (this.terrainMesh != null)
			floraMesh.destroy();
	}

	public GenericMesh getTerrainMesh() {
		return terrainMesh;
	}

	public GenericMesh getFloraMesh() {
		return floraMesh;
	}

	public void setTerrainMesh(GenericMesh terrainMesh) {
		if (this.terrainMesh != null)
			this.terrainMesh.destroy();
		
		this.terrainMesh = terrainMesh;
	}

	public void setFloraMesh(GenericMesh floraMesh) {
		if (this.floraMesh != null)
			this.floraMesh.destroy();
		
		this.floraMesh = floraMesh;
	}
}
