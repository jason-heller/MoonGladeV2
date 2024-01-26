package engine.pg.noise;

public class VoronoiDataPoint {
	public float x, y;
	public int indexX, indexY;
	
	public void copy(VoronoiDataPoint voronoiDataPoint) {
		this.x = voronoiDataPoint.x;
		this.y = voronoiDataPoint.y;
		
		this.indexX = voronoiDataPoint.indexX;
		this.indexY = voronoiDataPoint.indexY;
	}
}
