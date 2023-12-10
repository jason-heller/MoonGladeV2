package render;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.joml.Matrix4f;
import org.junit.jupiter.api.Test;

import engine.utils.math.MatrixUtils;

public class RenderTests {

	@Test
	public void testProjectionCreation() {
		final float fov = 70;
		final float aspect = 1280f / 720f;
		final float near = 0.1f, far = 1000f;

		final Matrix4f proj = new Matrix4f();
		MatrixUtils.setProjectionMatrix(proj, fov, aspect, near, far);

		// Assert is symmetric
		final Matrix4f projTranspose = new Matrix4f(proj).transpose();
		assertTrue(projTranspose.equals(proj), "createProjectionMatrix's output should be symmetric");
		
		// Assert is idempotent
		final Matrix4f projSquare = new Matrix4f(proj).mul(proj);
		assertTrue(projSquare.equals(proj), "createProjectionMatrix's output should be idempotent");
	}
}
