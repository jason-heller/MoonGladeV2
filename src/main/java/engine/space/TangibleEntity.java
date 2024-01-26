package engine.space;

import org.joml.Vector3f;

import engine.geo.BoundingBox;

// A base class for any entity that has a tangible 'body' to it
public abstract class TangibleEntity implements IEntity {
	
	protected boolean tangible;
	
	protected Vector3f position = new Vector3f();
	protected BoundingBox boundingBox = new BoundingBox();
	
	@Override
	public boolean isTangible() {
		return tangible;
	}

	@Override
	public float getX() {
		return position.x;
	}

	@Override
	public float getY() {
		return position.y;
	}

	@Override
	public float getZ() {
		return position.z;
	}

	@Override
	public Vector3f getPosition() {
		return position;
	}

	@Override
	public void setPosition(Vector3f position) {
		position.set(position);
	}


	@Override
	public void setPosition(int x, int y, int z) {
		position.set(x, y, z);
	}

	@Override
	public void setTangible(boolean tangible) {
		this.tangible = tangible;
	}
}
