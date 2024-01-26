package engine.gl;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import engine.Window;
import engine.dev.DevFlags;
import engine.dev.console.DeveloperConsole;
import engine.io.Input;
import engine.io.Keybinds;
import engine.space.CameraInheritable;
import engine.utils.math.MatrixUtils;
import engine.utils.math.Vectors;

public class FirstPersonCamera implements ICamera {

	private static final float PITCH_SENSITIVITY = 0.005f;
	private static final float YAW_SENSITIVITY = 0.005f;

	private static final float FOV = 70;
	private static final float NEAR_PLANE = 0.1f;
	private static final float FAR_PLANE = 1000f;

	private static final float HALFPI = ((float) Math.PI) / 2f;
	
	private Matrix4f projectionMatrix = new Matrix4f();
	private Matrix4f viewMatrix = new Matrix4f();

	private Vector3f position = new Vector3f();
	private Vector3f look = new Vector3f();

	private float yaw = 0;
	private float pitch = 0;
	private float cameraSpeed = .1f;
	
	// Related to attaching to entities
	private CameraInheritable targetEntity;

	public FirstPersonCamera(float aspectRatio) {
		MatrixUtils.setProjectionMatrix(projectionMatrix, FOV, aspectRatio, NEAR_PLANE, FAR_PLANE);
	}

	@Override
	public void update() {
		if (Window.isFocused() && !DeveloperConsole.isVisible()) {
			yaw += Input.getMouseDeltaX() * YAW_SENSITIVITY;
			pitch += Input.getMouseDeltaY() * PITCH_SENSITIVITY;
		}
		
		float pitchSign = Math.signum(pitch);
		pitch = (pitch * pitchSign > HALFPI) ? HALFPI * pitchSign : pitch;
		
		look.set(MatrixUtils.getLookVector(viewMatrix));
		
		if (targetEntity == null || DevFlags.noclip)
			handleMovement();
		else {
			position.set(targetEntity.getPosition());
			position.add(targetEntity.getAttachedCameraOffset());		// Camera need not be directly at the origin, so we get an offset from the interface
		}
			
		MatrixUtils.setViewMatrix(viewMatrix, position, yaw, pitch);
	}
	
	private void handleMovement() {
		final Vector3f forward = new Vector3f(look);
		final Vector3f strafe = new Vector3f(-(float) Math.sin(yaw), 0, (float) Math.cos(yaw));
		strafe.cross(Vectors.Y_AXIS);

		final float speed = Input.isDown(Keybinds.CROUCH) ? cameraSpeed * 4f : cameraSpeed;
		
		if (Input.isDown(Keybinds.JUMP)) {
			position.y++;
		}

		if (Input.isDown(Keybinds.MOVE_FORWARD)) {
			
			forward.mul(speed);
		} else if (Input.isDown(Keybinds.MOVE_BACKWARD)) {
			
			forward.mul(-speed);
		} else {
			
			forward.zero();
		}

		if (Input.isDown(Keybinds.STRAFE_RIGHT)) {
			
			strafe.mul(-speed);
		} else if (Input.isDown(Keybinds.STRAFE_LEFT)) {
			
			strafe.mul(speed);
		} else {
			
			strafe.zero();
		}

		position.add(forward).add(strafe);
	}

	
	@Override
	public Vector3f getLookVector() {
		return look;
	}

	@Override
	public Matrix4f getProjectionMatrix() {
		return projectionMatrix;
	}

	@Override
	public Matrix4f getViewMatrix() {
		return viewMatrix;
	}

	@Override
	public Matrix4f getProjectionViewMatrix() {
		return new Matrix4f(projectionMatrix).mul(viewMatrix);
	}

	@Override
	public Vector3f getPosition() {
		return position;
	}

	@Override
	public void setPosition(float x, float y, float z) {
		this.position.set(x,y,z);
	}

	@Override
	public void setPosition(Vector3f position) {
		this.position.set(position);
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

	public void attachTo(CameraInheritable targetEntity) {
		this.targetEntity = targetEntity;
	}

	@Override
	public float getYaw() {
		return yaw;
	}

	@Override
	public float getPitch() {
		return pitch;
	}
}
