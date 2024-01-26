package engine.space;

import org.joml.Vector3f;

import engine.dev.DevFlags;
import engine.gl.ICamera;
import engine.io.Input;
import engine.io.Keybinds;
import engine.utils.math.Maths;
import engine.utils.math.Vectors;

public class GenericPlayer extends PhysicsEntity implements IPlayer, CameraInheritable {
	
	private final Vector3f cameraOffset = new Vector3f(0, .5f, 0);
	
	public static float jumpVelocity = 6f;
	public static float accelSpeed = 60f, airAccel = 5f, waterAccel = 32f;
	
	private static final float HALF_PI = (float) (Math.PI / 2.0);
	
	public GenericPlayer(float width, float height, float length) {
		super();
		boundingBox.setDimensions(width, height, length);
	}
	
	public void tick(ISpace space) {
		if (!tangible)
			return;
		
		OverworldSpace overworld = (OverworldSpace)space;
		handlePhysics(overworld);
		if (!DevFlags.noclip)
			handleControls(overworld, overworld.getCamera());
	}

	private void handleControls(OverworldSpace space, ICamera camera) {
		float speed = 0;
		final float yaw = camera.getYaw();
		float direction = yaw;

		final boolean A = Input.isDown(Keybinds.STRAFE_LEFT),
				D = Input.isDown(Keybinds.STRAFE_RIGHT),
				W = Input.isDown(Keybinds.MOVE_FORWARD),
				S = Input.isDown(Keybinds.MOVE_BACKWARD),
				JUMP = Input.isDown(Keybinds.JUMP),
				CTRL = Input.isDown(Keybinds.CROUCH);

		if (submerged) {
			waterControls(space, W, A, S, D, JUMP, CTRL);
		} else if (climbing) {
			climbingControls(space, (W || A || D), S, JUMP);
		} else {
			// Handle game logic per tick, such as movement etc
			if (A && D) {
			} else if (A) {
				direction = yaw + HALF_PI;
				speed = accelSpeed;
			} else if (D) {
				direction = yaw - HALF_PI;
				speed = accelSpeed;
			}
	
			if (W && S) {
			} else if (S && !sliding) {
				if (direction != yaw) {
					direction += HALF_PI/2 * (direction > yaw ? -1f : 1f);
				}
	
				speed = accelSpeed;
			} else if (W && !sliding) {
	
				if (direction != yaw) {
					direction -= HALF_PI/2 * (direction > yaw ? -1f : 1f);
				} else {
					direction = yaw + (float)Math.PI;
				}
	
				speed = accelSpeed;
			}
	
			if ((grounded || submerged && velocity.y < 0) && JUMP) {
				jump(jumpVelocity);
				if (CTRL) {
					position.y += cameraOffset.y - 1f;
				}
			}
	
			if (speed != 0) {
				if (CTRL) {
					speed /= 2;
				}
	
				if (submerged) {
					speed = waterAccel;
				}
				else if (!grounded) {
					speed = airAccel;
				}
	
				//direction *= Math.PI / 180f;
				accelerate(new Vector3f(-(float) Math.sin(direction), 0, (float) Math.cos(direction)), speed);
			}
		}
	
		// TODO: Fps != 120 makes for bad times
		/*if (CTRL) {
			cameraHeight = MathUtil.sCurveLerp(cameraHeight, CAMERA_CROUCHING_HEIGHT, .16f);
		} else {
			cameraHeight = MathUtil.sCurveLerp(cameraHeight, CAMERA_STANDING_HEIGHT, .16f);
		}*/
	}
	
	private void climbingControls(ISpace space, boolean upKeysPressed, boolean downKeysPressed, boolean jumpPressed) {
		float pitch = space.getCamera().getPitch();
		
		if (upKeysPressed && !downKeysPressed) {
			accelerate(Vectors.Y_AXIS, accelSpeed * (pitch <= 0 ? 1 : -1));
		} else if (downKeysPressed) {
			accelerate(Vectors.Y_AXIS, accelSpeed * (pitch <= 0 ? -1 : 1));
		}
		
		if (jumpPressed) {
			Vector3f dir = new Vector3f(space.getCamera().getLookVector());
			velocity.x = dir.x * velocity.y;
			velocity.z = dir.z * velocity.y;
			jump(jumpVelocity);
		}
	}
	
	private void waterControls(ISpace space, boolean W, boolean A, boolean S, boolean D, boolean JUMP, boolean CROUCH) {
		float forwardSpeed = 0, strafeSpeed = 0;
		
		if (A && D) {
			if (!velocity.equals(0f, 0f, 0f)) {
				velocity.mul(.92f);
			}
		} else if (A && !D) {
			strafeSpeed = 60;
		} else if (!A && D) {
			strafeSpeed = -60;
		}
		
		if (W && S) {
			if (!velocity.equals(0f, 0f, 0f)) {
				velocity.mul(.92f);
			}
		} else if (W && !S) {
			forwardSpeed = -60;
			
		} else if (!W && S) {
			forwardSpeed = 60;
		}
		
		/*if (Input.isPressed(Keyboard.KEY_R) && isFullySubmerged()) {
			forwardSpeed = -4000;
		}*/
		
		if (JUMP && !CROUCH) {
			accelerate(Vectors.Y_AXIS, 60);
		} else if (!JUMP && CROUCH) {
			accelerate(Vectors.Y_AXIS, -60);
		}
		
		final Vector3f forward = Maths.getDirection(space.getCamera().getViewMatrix());
		final float yawRad = space.getCamera().getYaw();
		final Vector3f strafe = new Vector3f(-(float) Math.sin(yawRad), 0, (float) Math.cos(yawRad)).cross(Vectors.Y_AXIS);
		
		if (!fullySubmerged) {
			forward.y = Math.max(forward.y, 0f);
			strafe.y = Math.max(strafe.y, 0f);
		}
		
		accelerate(forward, forwardSpeed);
		accelerate(strafe, strafeSpeed);
	}

	@Override
	public Vector3f getAttachedCameraOffset() {
		return cameraOffset;
	}
}
