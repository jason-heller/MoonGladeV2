package engine.pg.biome;

//TODO: Replace with a JSON file eventually
public enum Floras {
	NONE(""),
	OAK("resources/meshes/oak.obj"),
	BUSH("resources/meshes/bush.obj"),
	BIG_BUSH("resources/meshes/big_bush.obj"),
	CACTUS("resources/meshes/cactus.obj"),
	DEAD_TREE("resources/meshes/dead_tree.obj"),
	PINE("resources/meshes/pine.obj"),
	SNOWY_PINE("resources/meshes/pine.obj", 0, 4),
	ROCK("resources/meshes/rock.obj"),
	CATTAIL("resources/meshes/cattail.obj"),
	THIN_TREE("resources/meshes/thin_tree.obj");
	
	private static final float ATLAS_SIZE = 16f;
	
	private final String resourcePath;
	public final float s, t;

	Floras(String resourcePath) {
		this.resourcePath = resourcePath;
		this.s = 0f;
		this.t = 0f;
	}
	
	Floras(String resourcePath, float s, float t) {
		this.resourcePath = resourcePath;
		this.s = s / ATLAS_SIZE;
		this.t = t / ATLAS_SIZE;
	}
	
	public String getResourcePath() {
		return resourcePath;
	}
}
