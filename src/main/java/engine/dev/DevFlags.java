package engine.dev;

import java.lang.reflect.Field;

public class DevFlags {

	public static boolean 
	cheats = true,
	wireframe,
	cullFace = true,
	noclip,
	showBiomeBorders,
	lockTerrainGeneration,
	skipFlora,
	los;
	
	public static void setFlag(String fieldName, boolean value)  {
	    try {
		    Field field = DevFlags.class.getDeclaredField(fieldName);
			field.setBoolean(DevFlags.class, value);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

