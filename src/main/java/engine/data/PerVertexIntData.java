package engine.data;

public class PerVertexIntData implements IIntData {
	private int[] values;
	
	public PerVertexIntData(int[] values) {
		this.values = values;
	}
	
	public int get(int index) {
		return values[index];
	}
}
