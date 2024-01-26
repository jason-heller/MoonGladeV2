package engine.pg.biome;

import engine.pg.biome.managers.IBiomeManager;

public class Biome {
	
	private final String name;
	
	private final Temperature temperature;
	private final Humidity humidity;
	private final Topography topography;
	private final Ecology ecology;
	
	private final int foliageColor;
	private final IBiomeManager manager;
	
	public Biome(String name, Temperature temperature, Humidity humidity, Topography topography, Ecology ecology, int foliageColor, IBiomeManager manager) {
		this.name = name;
		this.temperature = temperature;
		this.humidity = humidity;
		this.topography = topography;
		this.ecology = ecology;
		this.foliageColor = foliageColor;
		this.manager = manager;
	}
	
	public String getName() {
		return name;
	}

	public Temperature getTemperature() {
		return temperature;
	}

	public Humidity getHumidity() {
		return humidity;
	}

	public Topography getTopography() {
		return topography;
	}

	public Ecology getEcology() {
		return ecology;
	}
	
	public int getFoliageColor() {
		return foliageColor;
	}

	public IBiomeManager getManager() {
		return manager;
	}
	
	@Override
	public String toString() {
		return name + ":\n\tTemperature: " + temperature
				+ "\n\tHumidity: " + humidity
				+ "\n\ttopography: " + topography
				+ "\n\tecology: " + ecology
				+ "\n\tcolor: " + Integer.toString(foliageColor, 16)
				+ "\n\tmanager: " + manager.toString();
	}
}
