package engine.gl.meshing;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;

import org.lwjgl.system.MemoryUtil;

public class MeshUtil {
	public static FloatBuffer toFloatBuffer(List<Float> list) {
		FloatBuffer buff = MemoryUtil.memAllocFloat(list.size());
		for(float f : list)
			buff.put(f);
		
		return buff;
	}
	
	public static IntBuffer toIntBuffer(List<Integer> list) {
			IntBuffer buff = MemoryUtil.memAllocInt(list.size());
			for(int i : list)
				buff.put(i);
			
			return buff;
	}
	
	public static FloatBuffer toFloatBuffer(float[] list) {
		FloatBuffer buff = MemoryUtil.memAllocFloat(list.length);
		for(float f : list)
			buff.put(f);
		
		return buff;
	}
	
	public static IntBuffer toIntBuffer(int[] list) {
			IntBuffer buff = MemoryUtil.memAllocInt(list.length);
			for(int i : list)
				buff.put(i);
			
			return buff;
	}
}
