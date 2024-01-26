package engine.pg.biome.ecology;

import java.util.NavigableMap;
import java.util.TreeMap;

import engine.pg.biome.Floras;
import engine.pg.noise.Simplex2S;
import engine.utils.math.Maths;

public abstract class EcologyGenerator {
	
	protected long seed;
	
	protected float density;		// Frequency of the noise
	
	protected NavigableMap<Integer, Floras> floraWeights = new TreeMap<>();
	protected int totalWeight = 0;
	
	public EcologyGenerator(float density) {
		this.density = density;
	}

	protected void addFlora(int weight, Floras flora) {
		floraWeights.put(totalWeight, flora);
		totalWeight += weight;
	}
	
	public void setSeed(long seed) {
		this.seed = seed;
	}
	
	public Floras generate(int x, int z, float biomeInfluence) {
		float noise = ((Simplex2S.noise2(seed, x * density, z * density) * 0.5f) + 0.5f) * biomeInfluence;
		
		int index = (int) (noise * totalWeight);
		index = Maths.clamp(index, 1, totalWeight);
		
		return floraWeights.lowerEntry(index).getValue();
	}
}
