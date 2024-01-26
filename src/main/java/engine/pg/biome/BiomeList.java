package engine.pg.biome;

import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.Map.Entry;

import engine.utils.math.Maths;

public class BiomeList {
	private final NavigableMap<Integer, Biome> biomes = new TreeMap<>();
    private int size = 0;
	
	public BiomeList(Biome biome, int weight) {
		add(biome, weight);
	}

	/** Returns a weighted value based on range
	 * @param range The range to sample from, [-1, 1] is expected.
	 * @return
	 */
	public Biome get(float range) {
		int index = (int) ((range + 1.0f) * 0.5f * size);
		index = Maths.clamp(index, 0, size);
		
		Entry<Integer, Biome> biome = biomes.higherEntry(index);
		if (biome == null)
			return null;
		
		return biome.getValue();
	}

	public void add(Biome biome, int weight) {
		size += weight;
		biomes.put(size, biome);
	}
}
