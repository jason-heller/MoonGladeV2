package engine.utils;

import org.joml.Vector3f;

public class ColorUtil {

	public static Vector3f hexToColor(String hexCode) {
		int i = (hexCode.charAt(0) == '#') ? 1 : 0;

		float r = Integer.valueOf(hexCode.substring(i, i + 2), 16) / 255f;
		float g = Integer.valueOf(hexCode.substring(i + 2, i + 4), 16) / 255f;
		float b = Integer.valueOf(hexCode.substring(i + 4, i + 6), 16) / 255f;

		return new Vector3f(r, g, b);
	}

	public static int hexToRGB8(String hexCode) {
		int i = (hexCode.charAt(0) == '#') ? 1 : 0;

		int rgb = Integer.valueOf(hexCode.substring(i, i + 2), 16) << 16;
		rgb += Integer.valueOf(hexCode.substring(i + 2, i + 4), 16) << 8;
		rgb += Integer.valueOf(hexCode.substring(i + 4, i + 6), 16);

		return rgb;
	}

	// blends RGB of two colors, S, T by the ratio given
	public static int blend(int s, int t, float ratio) {
		float inverseRatio = 1f - ratio;

		int sR = ((s & 0xFF0000) >> 16);
		int sG = ((s & 0x00FF00) >> 8);
		int sB =  (s & 0x0000FF);

		int tR = ((t & 0xFF0000) >> 16);
		int tG = ((t & 0x00FF00) >> 8);
		int tB =  (t & 0x0000FF);
		
		int R = (int)((sR * inverseRatio) + (tR * ratio));
		int G = (int)((sG * inverseRatio) + (tG * ratio));
		int B = (int)((sB * inverseRatio) + (tB * ratio));

		return R << 16 | G << 8 | B;
	}
}
