package engine.utils.math;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Maths {
	private static final int BIT_COUNT_EXCLUDING_SIGN_32 = 31;

	public static int clamp(int x, int min, int max) {
		return x > max ? max : x < min ? min : x;
	}
	
	public static float clamp(float x, float min, float max) {
		return x > max ? max : x < min ? min : x;
	}

	// Faster than Math.floor()
	public static int floor(float x) {
		int xi = (int) x;
		return x < xi ? xi - 1 : xi;
	}
	
	public static int ceil(float x) {
		int xi = (int) x;
		return x < xi ? xi : xi + 1;
	}
	
	public static float barycentric(float x, float y, Vector3f p1, Vector3f p2, Vector3f p3) {
		final float det = (p2.z - p3.z) * (p1.x - p3.x) + (p3.x - p2.x) * (p1.z - p3.z);
		final float l1 = ((p2.z - p3.z) * (x - p3.x) + (p3.x - p2.x) * (y - p3.z)) / det;
		final float l2 = ((p3.z - p1.z) * (x - p3.x) + (p1.x - p3.x) * (y - p3.z)) / det;
		final float l3 = 1.0f - l1 - l2;
		return l1 * p1.y + l2 * p2.y + l3 * p3.y;
	}

	public static float lerp(float s, float t, float amount) {
		return s * (1f - amount) + t * amount;
	}
	
	public static Vector3f getDirection(Matrix4f matrix) {
		final Matrix4f inverse = new Matrix4f();
		matrix.invert(inverse);

		return new Vector3f(inverse.m20(), inverse.m21(), inverse.m22());
	}

	/**
	 * Gets the sign of the supplied number. The method being "zero position" means that the sign of zero is 1.
	 *
	 * @param number The number to get the sign from.
	 * @return The number's sign.
	 */
	public static int getSignZeroPositive(float number) {
	    assert !Float.isNaN(number);
	    return getNegativeSign(number) | 1;
	}

	/**
	 * Gets the negative sign of the supplied number. So, in other words, if the number is negative, -1 is returned but if the number is positive or zero, then zero is returned.
	 * It does not check if the parameter is NaN.
	 *
	 * @param number The number to get its negative sign.
	 * @return -1 if the number is negative, 0 otherwise.
	 */
	public static int getNegativeSign(float value) {
	    assert !Float.isNaN(value);
	    return Float.floatToRawIntBits(value) >> BIT_COUNT_EXCLUDING_SIGN_32;
	}
}
