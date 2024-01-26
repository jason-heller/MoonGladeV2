package engine.space;

import java.util.List;

import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;

import engine.Window;
import engine.geo.AxisMagnitude;
import engine.gl.LineRenderer;
import engine.utils.math.Maths;
import engine.utils.math.Vectors;
import engine.world.ChunkDataHandler;

public abstract class PhysicsEntity extends TangibleEntity {

	private static final float EPSILON = 0.0001f;

	protected final Vector3f velocity = new Vector3f();	

	protected float gravityForce = 0;
	
	protected boolean grounded, submerged, fullySubmerged, climbing;

	protected Quaternionf rotation;
	protected boolean sliding;
	
	public float maxSpeed = 10f, maxAirSpeed = 12f, maxWaterSpeed = 1f;
	public float friction = Physics.FRICTION;
	public float airFriction = Physics.AIR_FRICTION;
	private boolean previouslyGrounded;
	
	public abstract void tick(ISpace space);

	protected void handlePhysics(OverworldSpace overworld) {
		// Gravity
		gravityForce += Physics.GRAVITY_INCREMENT;
		
		if (!submerged && !climbing)
			velocity.y = Math.max(velocity.y - gravityForce * Window.deltaTime, -Physics.MAX_GRAVITY);

		if (!climbing) {
			position.x += velocity.x * Window.deltaTime;
			position.z += velocity.z * Window.deltaTime;
		}

		position.y += velocity.y * Window.deltaTime;
		
		boundingBox.setPosition(position);
		
		collideTerrain(overworld.getChunkData());
		
		// Friction
		if (!sliding && previouslyGrounded || submerged) {
			final float speed = velocity.length();
			if (speed != 0) {
				float drop = speed * friction * Window.deltaTime;
				if (submerged) {
					drop /= 2;
					grounded = false;
				}
				final float offset = Math.max(speed - drop, 0) / speed;
				velocity.mul(offset); // Scale the velocity based on friction.
			}
		} else if (climbing) {
			final float speed = Math.abs(velocity.y);
			if (speed != 0) {
				final float drop = speed * friction * Window.deltaTime;
				final float offset = Math.max(speed - drop, 0) / speed;
				velocity.y *= offset;
				velocity.x = Math.signum(velocity.x) * velocity.y;
				velocity.z = Math.signum(velocity.z) * velocity.y;
			}
		}

		else if (airFriction != 0f && !sliding && !submerged) {
			final float speed = new Vector2f(velocity.x, velocity.z).length();
			if (speed != 0f) {
				final float drop = speed * airFriction * Window.deltaTime;
				final float offset = Math.max(speed - drop, 0) / speed;
				velocity.set(velocity.x * offset, velocity.y, velocity.z * offset); // Scale the velocity based on
																					// friction.
			}
		}
		
		previouslyGrounded = grounded;
		// LineRenderer.box(boundingBox.min, boundingBox.max);
	}
	
	private void collideTerrain(ChunkDataHandler chunkData) {
		List<Vector3f> vertices = chunkData.getVerticesWithin(boundingBox.min, boundingBox.max);

		if (vertices.size() == 0)
			return;

		final int numTriangles = vertices.size() / 3;
		Vector3f p0 = new Vector3f(), p1 = new Vector3f(), p2 = new Vector3f();

		for (int i = 0; i < numTriangles; ++i) {
			p0.set(vertices.get(i * 3));
			p1.set(vertices.get(i * 3 + 1));
			p2.set(vertices.get(i * 3 + 2));

			float triMaxY = Math.max(Math.max(p0.y, p1.y), p2.y) - boundingBox.y + boundingBox.h;
			float triMinY = Math.min(Math.min(p0.y, p1.y), p2.y) - boundingBox.y + boundingBox.h;
			
			AxisMagnitude escape = boundingBox.collide(p0, p1, p2);
			
			if (escape == null)
				continue;
			
			// Resolve collision
			
			if (escape.y > Physics.SLOPE_WALKABLE_FACTOR) {
				Vector3f[] bottomPts = {
						new Vector3f(boundingBox.w, -boundingBox.h, boundingBox.l),
						new Vector3f(-boundingBox.w, -boundingBox.h, boundingBox.l),
						new Vector3f(boundingBox.w, -boundingBox.h, -boundingBox.l),
						boundingBox.min
				};
				
				float distance = 0f;
				Vector3f normal = new Vector3f(escape.x, escape.y, escape.z);
				float d = normal.dot(new Vector3f(p0).sub(boundingBox.x, boundingBox.y, boundingBox.z));
				for (Vector3f pt : bottomPts) {
					float ptDistance = (d - normal.dot(pt)) / normal.y;		// normal.Y = normal DOT Y-up
					
					ptDistance = Maths.clamp(ptDistance, triMinY, triMaxY);

					distance = Math.max(distance, ptDistance);
				}

				escape = new AxisMagnitude(Vectors.Y_AXIS, distance + EPSILON);
				
				if (velocity.y <= 0) {
					gravityForce = 0f;
					grounded = true;
				}
				
			} else if (escape.y < 0 && velocity.y > 0f) {
				velocity.y = 0f;
				escape.y = 0f;
			}
			
			position.add(escape.getVector());
			boundingBox.setPosition(position);
		}
	}
	
	public void accelerate(Vector3f dir, float amount) {
		if (climbing) {
			velocity.y += amount * Window.deltaTime;

			velocity.x += dir.x * amount * Window.deltaTime;
			velocity.z += dir.z * amount * Window.deltaTime;
		} else {
			final float projVel = velocity.dot(dir); // Vector projection of Current velocity onto accelDir.
			float accelVel = amount * Window.deltaTime; // Accelerated velocity in direction of movment

			// If necessary, truncate the accelerated velocity so the vector projection does
			// not exceed max_velocity
			final float speedCap;
			if (submerged) {
				speedCap = maxWaterSpeed;
			} else {
				speedCap = grounded ? maxSpeed : maxAirSpeed;
			}
			// if (projVel + accelVel < -speedCap)
			// accelVel = -speedCap - projVel;

			if (projVel + accelVel > speedCap) {
				accelVel = speedCap - projVel;
			}

			velocity.x += dir.x * accelVel;
			velocity.y += dir.y * accelVel;
			velocity.z += dir.z * accelVel;
		}
	}
	
	public void jump(float height) {
		if (climbing) {
			velocity.x = -velocity.x;
			velocity.z = -velocity.z;
			velocity.y = height;
			climbing = false;
			grounded = false;
			sliding = false;

		} else {
			velocity.y = height;
			grounded = false;
			sliding = false;
			position.y += .2f;
			boundingBox.setPosition(position);
		}
	}

	public Quaternionf getRotation() {
		return rotation;
	}

	public void setRotation(Quaternionf rotation) {
		this.rotation = rotation;
	}
}
