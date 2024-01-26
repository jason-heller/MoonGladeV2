package engine.pg.noise;

public class TranslatableVoronoi {
	
	private VoronoiDataPoint[][] points;
	private long seed;
	
	private int x, y;
	private float scale;
	private int arraySize;
	
	private final float SPREAD = 1f;
	
	private VoronoiDataPoint closestPointToOrigin;
	
	public TranslatableVoronoi(long seed, float biomeWidth, float mapWidth) {
		this.seed = seed;
		this.scale = biomeWidth;
		this.arraySize = (int) (Math.ceil(mapWidth / biomeWidth));
		this.points = new VoronoiDataPoint[this.arraySize][this.arraySize];
		
		populate(0, 0);
	}

	private void populate(int x, int y) {
		int halfArraySize = arraySize / 2;
		
		for (int i = 0; i < arraySize; i++) {
			for (int j = 0; j < arraySize; j++) {
				points[i][j] = calculatePoint(x + i - halfArraySize, y + j - halfArraySize);
			}
		}
	}
	
	public void update(float originX, float originY) {
		int scaledX = (int) Math.floor(originX / scale);
		int scaledY = (int) Math.floor(originY / scale);
		
		if (x != scaledX) {
			final int dx = scaledX - x;
			if (Math.abs(dx) > 1) {
				populate(scaledX, scaledY);
			} else {
				shiftX(dx);
			}
			x = scaledX;
		}

		if (y != scaledY) {
			final int dy = scaledY - y;
			if (Math.abs(dy) > 1) {
				populate(scaledX, scaledY);
			} else {
				shiftY(dy);
			}
			y = scaledY;
		}
		
		// Find closest point to origin
		float closestDist = Float.POSITIVE_INFINITY;

		for (int i = 0; i < arraySize; i++) {
			for (int j = 0; j < arraySize; j++) {
				float dx = originX / scale - points[i][j].x;
				float dy = originY / scale - points[i][j].y;

				float distanceSqr = (dx * dx) + (dy * dy);

				if (distanceSqr < closestDist) {
					closestDist = distanceSqr;
					closestPointToOrigin = points[i][j];
				}
			}
		}

		closestDist = (float) Math.sqrt(closestDist);
	}

	private void shiftX(int dx) {
		final byte shiftDir = (byte) Math.signum(dx);
		final int shiftStartPos = shiftDir == 1 ? 1 : arraySize - 2;
		final int shiftEndPos = shiftDir == 1 ? arraySize : - 1;

		for (int i = shiftStartPos; i != shiftEndPos; i += shiftDir) {
			for (int j = 0; j < arraySize; j++) {
				points[i - shiftDir][j].copy(points[i][j]);
			}
		}

		for (int j = 0; j < arraySize; j++) {
			final int i = shiftDir == 1 ? arraySize - 1 : 0;
			final int nx = points[i][j].indexX + shiftDir;
			final int ny = points[i][j].indexY;
			points[i][j] = calculatePoint(nx, ny);
		}
	}

	private void shiftY(int k) {
		final byte shiftDir = (byte) Math.signum(k);
		final int shiftStartPos = shiftDir == 1 ? 1 : arraySize - 2;
		final int shiftEndPos = shiftDir == 1 ? arraySize : -1;

		for (int i = 0; i < arraySize; i++) {
			for (int j = shiftStartPos; j != shiftEndPos; j += shiftDir) {
				points[i][j - shiftDir].copy(points[i][j]);
			}
		}

		for (int i = 0; i < arraySize; i++) {
			final int j = shiftDir == 1 ? arraySize - 1 : 0;
			final int nx = points[i][j].indexX;
			final int ny = points[i][j].indexY + shiftDir;
			points[i][j] = calculatePoint(nx, ny);
		}
	}
	
	private VoronoiDataPoint calculatePoint(int nx, int ny) {
		VoronoiDataPoint point = new VoronoiDataPoint();
		point.indexX = nx;
		point.indexY = ny;
		point.x = (nx + (SPREAD / 2f) + NoiseUtil.valueNoise2d(nx, ny, seed) * SPREAD);
		point.y = (ny + (SPREAD / 2f) + NoiseUtil.valueNoise2d(nx, ny, -seed) * SPREAD);
		return point;
	}
	
	public VoronoiDataPoint getPoint(int x, int y) {
		return points[x][y];
	}
	
	public VoronoiDataPoint getClosestPointToOrigin() {
		return closestPointToOrigin;
	}

	public VoronoiDataPoint[][] getPoints() {
		return points;
	}

	public int getArraySize() {
		return arraySize;
	}

	public float getScale() {
		return scale;
	}
}
