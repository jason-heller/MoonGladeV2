package engine.io.utils;

import org.joml.Vector3f;

import com.google.gson.JsonObject;

import engine.utils.ColorUtil;

public class JsonUtil {
	
	public static Vector3f getHexCode(JsonObject json, String key) {
		String hexCode = json.get(key).getAsString();
		
		return ColorUtil.hexToColor(hexCode);
	}
	
	public static int getHexCodeRGB8(JsonObject json, String key) {
		String hexCode = json.get(key).getAsString();
		
		return ColorUtil.hexToRGB8(hexCode);
	}
	
	/** Reads the element with the given key as a natural number: [0, inf)
	 * @param json The json object
	 * @param key the element key to read from
	 * @return
	 */
	public static int getNaturalNumber(JsonObject json, String key) {
		int value = json.get(key).getAsInt();
		
		if (value < 0)
			throw new NumberFormatException("The value contained is negative");
		
		return value;
	}
	
	public static <T extends Enum<T>> T getEnum(JsonObject json, Class<T> classOfEnum, String key) {
		String valueUpper = json.get(key).getAsString().toUpperCase();
		
		T result;
		try {
			result = Enum.valueOf(classOfEnum, valueUpper);
		} catch (IllegalArgumentException e) {
			// Failed to get the right enum, default to the first value
			result = classOfEnum.getEnumConstants()[0];
			e.printStackTrace();
		}
		
		return result;
	}

}
