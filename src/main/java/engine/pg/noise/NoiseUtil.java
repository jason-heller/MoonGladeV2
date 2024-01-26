package engine.pg.noise;

public class NoiseUtil {

	public static double valueNoise1d(long seed, int x) {
		x += seed;
		int nn = (x * (x * x * 60493 + 19990303) + 1376312589) & 0x7fffffff;
		return 1.0 - ((double) nn / 1073741824.0);
	}

	// Returns output between [0, 1)
	public static float valueNoise2d(long seed, long x, long y) {
		x += seed;
		x *= seed;
		y *= seed * 60493;

		x = x * 3266489917l + 374761393;
		x = (x << 17) | (x >> 15);

		x += y * 3266489917l;
		x *= 668265263;
		x ^= x >> 15;
		x *= 2246822519l;
		x ^= x >> 13;
		x *= 3266489917l;
		x ^= x >> 16;

		return (x & 0x00ffffff) * (1.0f / 0x1000000);
	}

	public static double interpNoise1d(long seed, double x) {
		int intX = (int) (Math.floor(x));
		double n0 = valueNoise1d(seed, intX);
		double n1 = valueNoise1d(seed, intX + 1);
		double weight = x - Math.floor(x);
		double noise = lerp(n0, n1, curve(weight));
		return noise;
	}

	public static double interpNoise2d(long seed, double x, double y) {
		return interpNoise1d(seed, szudzik(x, y));
	}

	private static double curve(double weight) {
		return -2 * (weight * weight * weight) + 3 * (weight * weight);
	}

	private static double lerp(double start, double end, double amount) {
		return start + amount * (end - start);
	}

	public static long szudzik(long x, long y) {
		return y > x ? y * y + x : x * x + x + y;
	}

	public static double szudzik(double x, double y) {
		return pairNxZ(szudzik(pairZxN(x), pairZxN(y)));
	}

	private static long pairZxN(double z) {
		return (long) ((z >= 0) ? 2 * z : (-2 * z) - 1);
	}

	private static double pairNxZ(long n) {
		return ((n % 2 == 0) ? n : -(n + 1)) / 2f;
	}
}
