package engine.geo;

import org.joml.Vector3f;

import engine.dev.Log;
import engine.space.IntersectionTestEntity;
import engine.utils.math.Vectors;

// Axis aligned bounding box
public class BoundingBox {

	public float x, y, z;
	public float w, h, l;
	
	public Vector3f min;
	public Vector3f max;
	
	public BoundingBox() {
		min = new Vector3f();
		max = new Vector3f();
	}
	
	public void setPosition(Vector3f position) {
		x = position.x;
		y = position.y;
		z = position.z;
		
		calcBounds();
	}
	
	public void setDimensions(float width, float height, float length) {
		this.w = width;
		this.h = height;
		this.l = length;
		
		calcBounds();
	}
	
	private void calcBounds() {
		min.x = x - w;
		max.x = x + w;
		min.y = y - h;
		max.y = y + h;
		min.z = z - l;
		max.z = z + l;
	}

	public float raycast(Vector3f origin, Vector3f direction) {
		Vector3f invDir = new Vector3f(1.0f / direction.x, 1.0f / direction.y, 1.0f / direction.z);
		
		float t1 = (min.x - origin.x) * invDir.x;
		float t2 = (max.x - origin.x) * invDir.x;
		float t3 = (min.y - origin.y) * invDir.y;
		float t4 = (max.y - origin.y) * invDir.y;
		float t5 = (min.z - origin.z) * invDir.z;
		float t6 = (max.z - origin.z) * invDir.z;

		float minT = Math.max(Math.max(Math.min(t1, t2), Math.min(t3, t4)), Math.min(t5, t6));
		float maxT = Math.min(Math.min(Math.max(t1, t2), Math.max(t3, t4)), Math.max(t5, t6));

		if (maxT < 0)
		    return Float.NaN;

		if (minT > maxT)
		    return Float.NaN;

		return minT;
	}
	
	public Vector3f collide(BoundingBox box) {
		final AxisMagnitude escapeVector = new AxisMagnitude();
		escapeVector.m = Float.POSITIVE_INFINITY;
		
		if (!axisTest(Vectors.X_AXIS, min.x, max.x, box.min.x, box.max.x, escapeVector))
			return null;

		if (!axisTest(Vectors.Y_AXIS, min.y, max.y, box.min.y, box.max.y, escapeVector))
			return null;

		if (!axisTest(Vectors.Z_AXIS, min.z, max.z, box.min.z, box.max.z, escapeVector))
			return null;

		Vector3f result = new Vector3f(escapeVector.x, escapeVector.y, escapeVector.z);
		result.normalize();
		result.mul((float)Math.sqrt(escapeVector.m) * 1.001f);
		
		return result;
	}
	
	private static boolean axisTest(Vector3f axis, float minA, float maxA, float minB, float maxB, AxisMagnitude escapeVector) {
        float axisLengthSquared = axis.dot(axis);

        if (axisLengthSquared < .0001f) {
            return true;
        }

        // Overlap ranges
        float d0 = (maxB - minA);   // Left side
        float d1 = (maxA - minB);   // Right side

        // No overlap, return
        if (d0 <= 0.0f || d1 <= 0.0f)
            return false;

        // Determine which side we overlap
        float overlap = (d0 < d1) ? d0 : -d1;

        // The mtd vector for that axis
        Vector3f sep = new Vector3f(axis).mul(overlap / axisLengthSquared);

        float sepLengthSquared = sep.dot(sep);

       if (sepLengthSquared < escapeVector.m) {
    	   escapeVector.x = sep.x;
    	   escapeVector.y = sep.y;
    	   escapeVector.z = sep.z;
           escapeVector.m = sepLengthSquared;
        }

        return true;
    }

	public boolean intersects(BoundingBox box) {
		if (Math.abs(x - box.x) > (w + box.w))
			return false;
		if (Math.abs(y - box.y) > (h + box.h))
			return false;
		if (Math.abs(z - box.z) > (l + box.l))
			return false;

		return true;
	}

	public boolean intersects(Vector3f planeNormal, float planeDistance) {
		Vector3f minV = new Vector3f((planeNormal.x > 0.0) ? -w : w, (planeNormal.y > 0.0) ? -h : h,
				(planeNormal.z > 0.0) ? -l : l);

		if (planeNormal.dot(minV) + planeDistance > 0.0)
			return false;

		Vector3f maxV = new Vector3f(minV).negate();
		if (planeNormal.dot(maxV) + planeDistance < 0.0)
			return false;

		return true;
	}
	
	public AxisMagnitude collide(Vector3f planeNormal, float planeDistance) {
		Vector3f minV = new Vector3f((planeNormal.x > 0.0) ? -w : w,
				(planeNormal.y > 0.0) ? -h : h,
				(planeNormal.z > 0.0) ? -l : l);

		float sepMinPt = planeNormal.dot(minV) + planeDistance;
		if (sepMinPt > 0.0)
			return null;
		
		Vector3f maxV = new Vector3f(minV).negate();
		float sepMaxPt = planeNormal.dot(maxV) + planeDistance;
		if (sepMaxPt < 0.0)
			return null;

		return new AxisMagnitude(planeNormal, -sepMinPt);
	}

	public AxisMagnitude collide(Vector3f p0, Vector3f p1, Vector3f p2) {
		// To local space
		Vector3f v0 = new Vector3f(p0.x - x, p0.y - y, p0.z - z);
		Vector3f v1 = new Vector3f(p1.x - x, p1.y - y, p1.z - z);
		Vector3f v2 = new Vector3f(p2.x - x, p2.y - y, p2.z - z);

		Vector3f ea = new Vector3f();
		Vector3f e_v0 = new Vector3f();
		Vector3f e_v1 = new Vector3f();
		Vector3f e_v2 = new Vector3f();
		
		// Edges
		Vector3f e0 = new Vector3f(v1).sub(v0);
		Vector3f e1 = new Vector3f(v2).sub(v1);
		Vector3f e2 = new Vector3f(v0).sub(v2);
		
		// Test edge 0
		ea.set(e0).absolute();
		e_v0.set(e0).cross(v0);
		e_v1.set(e0).cross(v1);
		e_v2.set(e0).cross(v2);

		if (axisTest(ea.z * h + ea.y * l, e_v0.x, e_v2.x))
			return null;
		if (axisTest(ea.z * w + ea.x * l, e_v0.y, e_v2.y))
			return null;
		if (axisTest(ea.y * w + ea.x * h, e_v1.z, e_v2.z))
			return null;

		// Test edge 1
		ea.set(e1).absolute();
		e_v0.set(e1).cross(v0);
		e_v1.set(e1).cross(v1);
		e_v2.set(e1).cross(v2);

		if (axisTest(ea.z * h + ea.y * l, e_v0.x, e_v2.x))
			return null;
		if (axisTest(ea.z * w + ea.x * l, e_v0.y, e_v2.y))
			return null;
		if (axisTest(ea.y * w + ea.x * h, e_v0.z, e_v1.z))
			return null;

		// Test edge 2
		ea.set(e2).absolute();
		e_v0.set(e2).cross(v0);
		e_v1.set(e2).cross(v1);
		e_v2.set(e2).cross(v2);
		
		if (axisTest(ea.z * h + ea.y * l, e_v0.x, e_v1.x))
			return null;
		if (axisTest(ea.z * w + ea.x * l, e_v0.y, e_v1.y))
			return null;
		if (axisTest(ea.y * w + ea.x * h, e_v1.z, e_v2.z))
			return null;
		
		// Bullet 1:
		// First test overlap in the {x,y,z}-directions.
		// Find min, max of the triangle each direction, and test for overlap in
		// that
		// direction -- this is equivalent to testing a minimal AABB around the
		// triangle against the AABB.
		if (directionTest(v0.x, v1.x, v2.x, w))
			return null;
		
		if (directionTest(v0.y, v1.y, v2.y, h))
			return null;
		
		if (directionTest(v0.z, v1.z, v2.z, l))
			return null;
		
		// Bullet 2:
		// Test if the box intersects the plane of the triangle. Compute plane
		// equation of triangle: normal*x+d=0.
		Vector3f normal = new Vector3f(e0).cross(e1).normalize();
		float d = -normal.dot(v0);
	
		return collide(normal, d);
	}
	
	private static boolean axisTest(float rad, float p0, float p1) {
		return (Math.min(p0, p1) > rad || Math.max(p0, p1) < -rad);
	}

	private static boolean directionTest(float a, float b, float c, float bounds) {
		return (Math.min(Math.min(a, b), c) > bounds || Math.max(
				Math.max(a, b), c) < -bounds);
	}

	public boolean intersects(Vector3f point) {
		return (point.x >= x - w && point.x <= x + w)
				&& (point.y >= y - h && point.y <= y + h)
				&& (point.z >= z - l && point.z <= z + l);
	}
}
