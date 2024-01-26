package engine.pg.biome;

import engine.pg.biome.topography.BadlandsTopographyGenerator;
import engine.pg.biome.topography.HillTopographyGenerator;
import engine.pg.biome.topography.ITopographyGenerator;
import engine.pg.biome.topography.MesaTopographyGenerator;
import engine.world.Chunk;

public enum Topography {
	
	// MOUNTAINOUS, PLATEAU, VALLEY, PLAIN, HILLY, COASTAL, CANYON, DELTA, FOOTHILLS, MESA, RIDGE, BASIN, ESCARPMENT, GLACIAL, LAGOON, BADLANDS;
	PLAIN(null, 0),
	HILLY(new HillTopographyGenerator(Chunk.CHUNK_HEIGHT / 3f, .001f, 3), 0f, .75f, 23423403),
	BADLANDS(new BadlandsTopographyGenerator(), 3951371),
	MESA(new MesaTopographyGenerator(), 0f, .5f, 3951371);

	private static final float DEF_BORDER_START = 0f;	// No delay to transition
	private static final float DEF_BORDER_SCALE = 1f;	// Default 1:1 scale

	private ITopographyGenerator topoGen;
	private float borderStart; 		// How long to recede the border (higher = longer until terrain
									// transitions into the new topography generator). The purpose
									// is to add shorelines
	
	private float borderScale; 		// The scale of the topography's transition. Lower numbers =
									// longer transitions. If you want a less abrupt transition into
									// a biome, lower this number
	private long offset;
	
	private Topography(ITopographyGenerator topoGen, long offset) {
		this(topoGen, DEF_BORDER_START, DEF_BORDER_SCALE, offset);
	}
	
	private Topography(ITopographyGenerator topoGen, float borderStart, float borderScale, long offset) {
		 this.topoGen = topoGen;
		 this.borderStart = borderStart;
		 this.borderScale = borderScale;
		 this.offset = offset;
	}
	
	public float generate(int x, int y, int z, int lodScale, float terrainEdgeScale, float originalValue) {
		if (this.topoGen == null)
			return originalValue;
		
		return topoGen.generate(x, y, z, lodScale, terrainEdgeScale, originalValue);
	}
	
	public static void setSeeds(long seed) {
		for(Topography t : values()) {
			if (t.topoGen != null)
				t.setSeed(seed);
		}
	}
	
	public void setSeed(long seed) {
		long x = seed * offset;

		// Fuckery
		x = x * 3266489917l + 374761393;
		x = (x << 17) | (x >> 15);

		x *= 668265263;
		x ^= x >> 15;
		x *= 2246822519l;
		x ^= x >> 13;
		
		topoGen.setSeed(x);
	}

	public float getBorderScale() {
		return borderScale;
	}
	
	public float getBorderStart() {
		return borderStart;
	}
}

// https://t5k.org/curios/index.php?start=7&stop=7 for primes

/*

    Mountainous: Characterized by steep slopes and high elevations, often with rugged terrain.

    Plateau: A flat-topped elevated area with steep sides.

    Valley: A low area between hills or mountains, typically with a river running through it.

    Plain: A broad, flat area of land with minimal elevation changes.

    Hill: A raised area of land that is lower and more rounded than a mountain.

    Coastal: Relating to the interface between land and sea, characterized by various features such as cliffs, beaches, and dunes.

    Canyon: A deep gorge typically with a river flowing through it, often created by erosion.

    Delta: A landform where a river meets a larger body of water, characterized by sediment deposition and a fan-like shape.
    
    Foothills: The transition zone between plains or valleys and higher mountain ranges.

    Mesa: A flat-topped elevation with steep sides, smaller than a plateau.

    Ridge: A long, narrow elevated landform, often with steep sides, which forms a continuous elevated crest.

    Basin: A depression or dip in the land where water or sediment collects, often surrounded by higher land.

    Escarpment: A long cliff or steep slope separating two flat areas of differing elevations.

    Glacial: Topography shaped by the movement and melting of glaciers, characterized by U-shaped valleys, moraines, and other glacial features.

    Lagoon: A shallow body of water separated from a larger body of water by barrier islands or reefs.

    Badlands: A type of dry terrain where soft rock formations are extensively eroded into intricate and often bizarre shapes.

*/