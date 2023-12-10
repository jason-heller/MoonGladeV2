package engine.utils.math;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class MatrixUtils {
	
	/** Generates a square projection matrix based off the given parameters
	 * 
	 * The matrix will look as such:
	 * 
	 * | cot(fov/2)/a  0            0                0 |
	 * | 0             cot(fov/2)   0                0 |
	 * | 0             0           -(f+n)/(f-n)   -2fn/(f-n) |
	 * | 0             0           -1                0 |
	 * 
	 * @param fov - field of view of camera
	 * @param aspectRatio - aspect ratio of the window (width / height)
	 * @param near - near plane distance
	 * @param far - far plane distance
	 * @return
	 */
	public static void setProjectionMatrix(Matrix4f matrix, float fov, float aspectRatio, float near, float far) {
		
		final float yScale = (float) (1f / Math.tan(Math.toRadians(fov / 2f)));
		final float xScale = yScale / aspectRatio;
		final float range = far - near;

		matrix.identity();
		
		matrix.m00( xScale );
		matrix.m11( yScale );
		matrix.m22( -((far + near) / range) );
		matrix.m23( -1 );
		matrix.m32( -((2 * far * near) / range) );
		matrix.m33( 0 );
	}

	/** Returns the look (direction) vector of a matrix
	 * @param matrix The matrix to derive the look matrix from
	 * @return The look vector
	 */
	public static Vector3f getLookVector(Matrix4f matrix) {
		final Matrix4f inverse = new Matrix4f();
		matrix.invert(inverse);

		return new Vector3f(inverse.m20(), inverse.m21(), inverse.m22());
	}

	public static void setViewMatrix(Matrix4f matrix, Vector3f position, float yaw, float pitch) {
		matrix.identity();
		
		matrix.rotateX(pitch);
		matrix.rotateY(yaw);
		
		matrix.translate(-position.x, -position.y, -position.z); 
	}
}
