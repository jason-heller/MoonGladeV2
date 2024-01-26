package engine.io;

import static engine.io.utils.JsonUtil.getEnum;
import static engine.io.utils.JsonUtil.getHexCodeRGB8;
import static engine.io.utils.JsonUtil.getNaturalNumber;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.Map;

import com.google.gson.JsonObject;

import engine.io.utils.IOUtil;
import engine.pg.biome.Biome;
import engine.pg.biome.BiomeList;
import engine.pg.biome.Ecology;
import engine.pg.biome.Humidity;
import engine.pg.biome.Temperature;
import engine.pg.biome.Topography;
import engine.pg.biome.managers.IBiomeManager;

public class BiomeLoader {
	
	private static final String BIOME_DATA_PATH = "data/biome.json";
	
	@SuppressWarnings("unchecked")
	public static Biome readBiomes(BiomeList[][] biomes, Map<String, IBiomeManager> biomeManagers) {
		
		Biome defaultBiome = null;
		
		JsonObject json = IOUtil.readJson(BIOME_DATA_PATH);
		
		JsonObject jsonBiomes = json.getAsJsonObject("biomes");
		JsonObject jsonBiome;
		
		Iterator<String> iter = jsonBiomes.keySet().iterator();
		while(iter.hasNext()) {
			String name = iter.next();
			jsonBiome = jsonBiomes.getAsJsonObject(name);
		    
			Temperature temperature = getEnum(jsonBiome, Temperature.class, "temperature");
			Humidity humidity = getEnum(jsonBiome, Humidity.class, "humidity");
			Topography topography = getEnum(jsonBiome, Topography.class, "topography");
			Ecology ecology = getEnum(jsonBiome, Ecology.class, "ecology");
			
			int foliageColor = getHexCodeRGB8(jsonBiome, "foliage_color");
			int rarity = getNaturalNumber(jsonBiome, "occurance");
			
			// Perhaps this is better as an enum?
			IBiomeManager biomeManager = null;
			String manager = jsonBiome.get("manager").getAsString();
			Class<IBiomeManager> managerClass;
			try {
				managerClass = (Class<IBiomeManager>) Class.forName("engine.pg.biome.managers." + manager + "BiomeManager");
				biomeManager = (IBiomeManager) managerClass.getConstructors()[0].newInstance();
				biomeManagers.put(manager, biomeManager);
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | SecurityException e) {
				e.printStackTrace();
			}
			
			// Take data and store it
			Biome biome = new Biome(name, temperature, humidity, topography, ecology, foliageColor, biomeManager);

			int x = temperature.getIndex(), y = humidity.getIndex();
			if (biomes[x][y] == null)
				biomes[x][y] = new BiomeList(biome, rarity);
			else
				biomes[x][y].add(biome, rarity);
			
			if (defaultBiome == null)
				defaultBiome = biome;
			
		}
		

		
		return defaultBiome;
	}
}
