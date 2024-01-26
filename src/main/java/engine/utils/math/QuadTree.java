package engine.utils.math;

public class QuadTree<E> {
	
	private int level = 0;
	private int xMin, yMin, xMax, yMax;
	
	private E value;
	
	private QuadTree<E> northWest = null;
	private QuadTree<E> northEast = null;
	private QuadTree<E> southWest = null;
	private QuadTree<E> southEast = null;

	public QuadTree(int width, int height) {
		this(0, 0, 0, width, height);
	}
	
	private QuadTree(int level, int xMin, int yMin, int xMax, int yMax) {
		this.level = level;
		this.xMin = xMin;
		this.yMin = yMin;
		this.xMax = xMax;
		this.yMax = yMax;
	}
	
	public void put(E value, int x, int y) {
		if (!contains(x, y))
			return;

		if (value == null) {
			this.value = value;
			return;
		}

		if (northWest == null)
			split();

		// Partition
		if (northWest.contains(x, y))
			northWest.put(value, x, y);
		else if (northEast.contains(x, y))
			northEast.put(value, x, y);
		else if (southWest.contains(x, y))
			southWest.put(value, x, y);
		else //if (southEast.inRange(x, y))
			southEast.put(value, x, y);
	}
	
	public E get(int x, int y) {
		return depthFirst(this, x, y);
	}
	
	private E depthFirst(QuadTree<E> tree, int x, int y) {
		if (tree == null || !tree.contains(x, y))
			return null;

		if (northWest == null)
			return tree.value;
		
		E result;

		if ((result = depthFirst(tree.northWest, x, y)) != null)
			return result;
		
		if ((result = depthFirst(tree.northEast, x, y)) != null)
			return result;
		
		if ((result = depthFirst(tree.southWest, x, y)) != null)
			return result;

		return depthFirst(tree.southEast, x, y);
	}

	private void split() {
		int xOffset = xMin + (xMax - xMin) / 2;
		int yOffset = yMin + (yMax - yMin) / 2;
		split(xOffset, yOffset);
	}
	
	private void split(int xOffset, int yOffset) {
		northWest = new QuadTree<E>(level + 1, xMin, yMin, xOffset, yOffset);
		northEast = new QuadTree<E>(level + 1, xOffset, yMin, xMax, yOffset);
		southWest = new QuadTree<E>(level + 1, xMin, yOffset, xOffset, yMax);
		southEast = new QuadTree<E>(level + 1, xOffset, yOffset, xMax, yMax);
	}
	
	private boolean contains(int x, int y) {
		return (x >= xMin && x <= xMax && y >= yMin && y <= yMax);
	}
}
