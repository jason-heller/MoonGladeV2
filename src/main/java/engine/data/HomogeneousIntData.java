package engine.data;

public class HomogeneousIntData implements IIntData {
	
	private int i;
	
	public HomogeneousIntData(int i) {
		this.i = i;
	}
	
	public int get(int index) {
		return i;
	}
}
