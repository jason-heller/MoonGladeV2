package engine.gl;

import engine.rendering.data.HeightmapMesh;

public class HeightmapBuilder {
	
	// Run is how many vertices there are per slice
	public static HeightmapMesh buildTriStripHeightmap(int run) {
		int size = (run * 2) * run;
		float[] heights = new float[size];
		int[] eulerNormal = new int[size];
		
		int i = 0;
		for(int x = 0; x < run; x++) {
			for(int z = 0; z < run; z++) {
				heights[i] = 0;
				eulerNormal[i] = 0;
				
				++i;
				
				heights[i] = 0;
				eulerNormal[i] = 0;
				
				++i;
	
			}
		}
		
		HeightmapMesh mesh = new HeightmapMesh(heights, eulerNormal);
		return mesh;
	}
}
