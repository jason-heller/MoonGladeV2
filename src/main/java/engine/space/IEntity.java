package engine.space;

import org.joml.Vector3f;

public interface IEntity {
	
	boolean isTangible();
	float getX();
	float getY();
	float getZ();
	Vector3f getPosition();
	
	void tick(ISpace space);
	
	void setPosition(Vector3f position);
	void setPosition(int x, int y, int z);
	void setTangible(boolean tangible);
	
}
