package engine.space;

import java.util.LinkedList;
import java.util.List;

import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import engine.Window;
import engine.geo.AxisMagnitude;
import engine.geo.BoundingBox;
import engine.gl.LineRenderer;
import engine.io.Input;
import engine.io.Keybinds;
import engine.utils.math.Maths;
import engine.utils.math.Vectors;
import engine.world.ChunkDataHandler;

public class IntersectionTestEntity extends TangibleEntity {
	
	public static int minOrMax;
	private boolean colliding = false;
	
	public IntersectionTestEntity(IPlayer player) {
		this.tangible = true;
		this.boundingBox = new BoundingBox();
		this.boundingBox.setDimensions(.4f, 1f, .4f);
		this.position.set(player.getPosition());
	}

	@Override
	public void tick(ISpace space) {
		final boolean A = Input.isDown(GLFW.GLFW_KEY_LEFT),
				D = Input.isDown(GLFW.GLFW_KEY_RIGHT),
				W = Input.isDown(GLFW.GLFW_KEY_UP),
				S = Input.isDown(GLFW.GLFW_KEY_DOWN),
				JUMP = Input.isDown(GLFW.GLFW_KEY_PAGE_UP),
				CTRL = Input.isDown(GLFW.GLFW_KEY_PAGE_DOWN);
		
		float s = Input.isDown(GLFW.GLFW_KEY_LEFT_CONTROL) ? 10f : 1f;
		
		position.x += (A ? s : D ? -s : 0) * Window.deltaTime;
		position.y += (JUMP ? s : CTRL ? -s : 0) * Window.deltaTime;
		position.z += (W ? s : S ? -s : 0) * Window.deltaTime;
		
		this.boundingBox.setPosition(position);
		
		collideTerrain(((OverworldSpace)space).getChunkData());
		
		LineRenderer.box(boundingBox.min, boundingBox.max, colliding?new Vector3f(1,1,1):new Vector3f(.7f,.7f,.7f));
		
	}

	private void collideTerrain(ChunkDataHandler chunkData) {
		colliding = false;
			
		List<Vector3f> vertices = chunkData.getVerticesWithin(boundingBox.min, boundingBox.max);

		if (vertices.size() == 0)
			return;

		final int numTriangles = vertices.size() / 3;
		Vector3f p0 = new Vector3f(), p1 = new Vector3f(), p2 = new Vector3f();

		Vector3f finalPos = new Vector3f(position);

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

				escape = new AxisMagnitude(Vectors.Y_AXIS, distance);
				
			}
			
			position.add(escape.getVector());
			boundingBox.setPosition(position);
			colliding = true;
			
			//LineRenderer.box(p,pp,verticesAtIndex.length==0? new Vector3f(1,0,0): C);
			
			Vector3f a0 = new Vector3f(p0);
			Vector3f a1 = new Vector3f(p1);
			Vector3f a2 = new Vector3f(p2);
			Vector3f C = new Vector3f(1,1,1);
			LineRenderer.add(a0, C);
			LineRenderer.add(a1, C);
			LineRenderer.add(a1, C);
			LineRenderer.add(a2, C);
			LineRenderer.add(a2, C);
			LineRenderer.add(a0, C);
		}

		Vector3f eMin = new Vector3f(boundingBox.min);
		Vector3f eMax = new Vector3f(boundingBox.max);
		LineRenderer.box(eMin, eMax, new Vector3f(1,0,0));
		position.set(finalPos);
		boundingBox.setPosition(position);
	}

}
