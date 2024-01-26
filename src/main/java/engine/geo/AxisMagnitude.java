package engine.geo;

import org.joml.Vector3f;

public class AxisMagnitude extends Vector3f {
	public float m;
	
	public AxisMagnitude(float x, float y, float z, float magnitude) {
		super(x,y,z);
		this.m = magnitude;
	}
	
	public AxisMagnitude(Vector3f v, float magnitude) {
		super(v);
		this.m = magnitude;
	}

	public AxisMagnitude() {
		super();
	}

	@Override
	public float length() {
		return m;
	}

	public Vector3f getVector() {
		return new Vector3f(x, y, z).mul(m);
	}
}
