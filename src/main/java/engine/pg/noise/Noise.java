package engine.pg.noise;

public abstract class Noise {
	
	public abstract float noise(float x, float y);
	public abstract float noise(float x, float y, float z);
	
	/** Generates Fractional Brownian motion at the given coordinates
	 * @param x				\
	 * @param y				/ The coordinates of the noise
	 * @param octaves		The number of 'octaves' or cycles of the noise function
	 * @param roughness		How 'rough' to render the noise
	 * @param scale			The scale of the noise
	 * @return	a noise value [0,1] of the FBM noise
	 */
	public float fbm(float x, float y, int octaves, float roughness, float scale) {
		float noiseSum = 0;
		float layerFrequency = scale;
		float layerWeight = 1;
		float weightSum = 0;

		for (int octave = 0; octave < octaves; octave++) {
			noiseSum += noise(x * layerFrequency, y * layerFrequency) * layerWeight;
			layerFrequency *= 2;
			weightSum += layerWeight;
			layerWeight *= roughness;
		}
		return noiseSum / weightSum;
	}
	
	/** Generates Fractional Brownian motion at the given coordinates
	 * @param x				\
	 * @param y				|
	 * @param y				/ The coordinates of the noise
	 * @param octaves		The number of 'octaves' or cycles of the noise function
	 * @param roughness		How 'rough' to render the noise
	 * @param scale			The scale of the noise
	 * @return	a noise value [0,1] of the FBM noise
	 */
	public float fbm(float x, float y, float z, int octaves, float roughness, float scale) {
		float noiseSum = 0;
		float layerFrequency = scale;
		float layerWeight = 1;
		float weightSum = 0;

		for (int octave = 0; octave < octaves; octave++) {
			noiseSum += noise(x * layerFrequency, y * layerFrequency, z * layerFrequency) * layerWeight;
			layerFrequency *= 2;
			weightSum += layerWeight;
			layerWeight *= roughness;
		}
		return noiseSum / weightSum;
	}
}