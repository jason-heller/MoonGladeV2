package engine.pg.biome;

import java.util.HashMap;
import java.util.Map;

import engine.io.BiomeLoader;
import engine.pg.biome.managers.IBiomeManager;

public class BiomeData {
	
	private BiomeList[][] biomes;
	
	private Biome defaultBiome;
	
	public static Map<String, IBiomeManager> biomeManagers;
	
	public BiomeData() {
		biomes = new BiomeList[Temperature.TOTAL_VARIANCE][Humidity.TOTAL_VARIANCE];
		biomeManagers = new HashMap<>();
		defaultBiome = null;
		
		defaultBiome = BiomeLoader.readBiomes(biomes, biomeManagers);
	}
	
	public Biome getBiomeAt(float tempPercent, float humidityPercent, float variation) {
		int x = (int)(tempPercent * (Temperature.TOTAL_VARIANCE));
		int y = (int)(humidityPercent * (Humidity.TOTAL_VARIANCE));
		
		BiomeList biomeList = biomes[x][y];
		
		if (biomeList == null)
			return defaultBiome;
		
		Biome biome = biomeList.get(variation);
		
		if (biome == null)
			return defaultBiome;
		
		return biome;
	}
	
	public static float getBiomeInfluence(float temp, float humid) {
		
		float tempInf = 0.5f - Math.abs((temp % 1) - 0.5f);
		float humidInf = 0.5f - Math.abs((humid % 1) - 0.5f);

		return tempInf + humidInf;
	}
}
