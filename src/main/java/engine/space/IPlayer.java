package engine.space;

import org.joml.Quaternionf;
import org.joml.Vector3f;

public interface IPlayer {

	boolean isTangible();
	float getX();
	float getY();
	float getZ();
	Vector3f getPosition();
	Quaternionf getRotation();
	
	void tick(ISpace space);
	
	void setPosition(Vector3f position);
	void setRotation(Quaternionf rotation);
	void setPosition(int x, int y, int z);
	void setTangible(boolean tangible);


	

}
