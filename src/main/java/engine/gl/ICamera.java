package engine.gl;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public interface ICamera {
	
	public Matrix4f getProjectionMatrix();
	public Matrix4f getViewMatrix();
	
	public Matrix4f getProjectionViewMatrix();
	
	public void update();
	
	public Vector3f getLookVector();
	public Vector3f getPosition();
	
	public void setPosition(float x, float y, float z);
	public void setPosition(Vector3f position);
	
	public float getX();
	public float getY();
	public float getZ();
	public float getYaw();
	public float getPitch();
}
