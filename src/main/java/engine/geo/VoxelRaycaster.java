package engine.geo;

import org.joml.Vector3f;
import org.joml.Vector3i;

import engine.data.chunk.IChunkData;
import engine.dev.DevFlags;
import engine.gl.LineRenderer;
import engine.utils.math.Maths;
import engine.world.Chunk;
import engine.world.ChunkDataHandler;

// Implemented from this post:
// https://www.gamedev.net/blogs/entry/2265248-voxel-traversal-algorithm-ray-casting/

public class VoxelRaycaster {

	private Vector3f origin;
	private Vector3f direction;
	private float rayLength;
	private float halfVoxelSize;
	private int voxelDistance;
	
	private final Vector3f trace;
	
	private final ChunkDataHandler chunkData;
	
	private boolean foundVoxel = false;
	private Vector3i voxelPosition = new Vector3i();
	private Vector3i voxelPosLocal = new Vector3i();
	private Vector3i voxelPositionPrior = new Vector3i();
	private Vector3i voxelPosPriorLocal = new Vector3i();
	
	
	private Chunk targetChunk, priorChunk;

	public VoxelRaycaster(ChunkDataHandler chunkData, Vector3f origin, Vector3f direction, float rayLength, float halfVoxelSize) {
		this.chunkData = chunkData;
		this.rayLength = rayLength;
		this.halfVoxelSize = halfVoxelSize;
		
		this.origin = origin;
		this.direction = direction;
		
		trace = new Vector3f();
	}
	
	// Returns the hit block, or -1 is nothing
	public int raycast() {
		
		Vector3i voxelIndex = new Vector3i(Maths.floor(origin.x), (int)(origin.y), Maths.floor(origin.z));

		voxelPositionPrior.set(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
		voxelPosPriorLocal.set(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
		
		trace.set(origin);
		
		float voxelSize = halfVoxelSize * 2f;
		foundVoxel = false;
		
		// Get first voxel
		voxelDistance = getVoxelDistance(voxelIndex);
		
		int stepX = (int) Maths.getSignZeroPositive(direction.x);
		int stepY = (int) Maths.getSignZeroPositive(direction.y);
		int stepZ = (int) Maths.getSignZeroPositive(direction.z);
		
		// Distance along the ray to the next voxel border from the current position (tMaxX, tMaxY, tMaxZ).
		float nextVoxelBoundaryX = (voxelIndex.x + (Maths.getNegativeSign(stepX) + 1)) * voxelSize;
		float nextVoxelBoundaryY = (voxelIndex.y + (Maths.getNegativeSign(stepY) + 1)) * voxelSize;
		float nextVoxelBoundaryZ = (voxelIndex.z + (Maths.getNegativeSign(stepZ) + 1)) * voxelSize;
		
		// tMaxX, tMaxY, tMaxZ -- distance until next intersection with voxel-border
		// the value of t at which the ray crosses the first vertical voxel boundary
		float tMaxX = (direction.x != 0f) ? (nextVoxelBoundaryX - trace.x) / direction.x : Float.MAX_VALUE;
		float tMaxY = (direction.y != 0f) ? (nextVoxelBoundaryY - trace.y) / direction.y : Float.MAX_VALUE;
		float tMaxZ = (direction.z != 0f) ? (nextVoxelBoundaryZ - trace.z) / direction.z : Float.MAX_VALUE;
		
		// tDeltaX, tDeltaY, tDeltaZ --
		// how far along the ray we must move for the horizontal component to equal the width of a voxel
		// the direction in which we traverse the grid
		// can only be FLT_MAX if we never go in that direction
		float tDeltaX = (direction.x != 0f) ? stepX * voxelSize / direction.x : Float.MAX_VALUE;
		float tDeltaY = (direction.y != 0f) ? stepY * voxelSize / direction.y : Float.MAX_VALUE;
		float tDeltaZ = (direction.z != 0f) ? stepZ * voxelSize / direction.z : Float.MAX_VALUE;

		int traversedVoxelCount = 0;
		while (++traversedVoxelCount < voxelDistance) {
			if (DevFlags.los)
				LineRenderer.add(new Vector3f(voxelIndex).add(.5f,.5f,.5f), new Vector3f(1,1,0));
			
			if (tMaxX < tMaxY && tMaxX < tMaxZ) {
				voxelIndex.x += stepX;
				tMaxX += tDeltaX;
			}
			else if (tMaxY < tMaxZ) {
				voxelIndex.y += stepY;
				tMaxY += tDeltaY;
			}
			else {
				voxelIndex.z += stepZ;
				tMaxZ += tDeltaZ;
			}
			
			if (DevFlags.los)
				LineRenderer.add(new Vector3f(voxelIndex).add(.5f,.5f,.5f), new Vector3f(1,1,0));
			
			if (voxelIndex.y <= 0) {
				foundVoxel = true;
				voxelPosition.set(voxelIndex);     
				return 1;
			}
			
			if (voxelIndex.y >= Chunk.NUM_VERTICES_Y-1)
				continue;
			
			targetChunk = chunkData.getChunkContaining(voxelIndex.x, voxelIndex.z);
			
			
			if (targetChunk == null || targetChunk.getTileData() == null)
				return -1;

			voxelPosLocal.set(voxelIndex.x - targetChunk.getX(), voxelIndex.y, voxelIndex.z - targetChunk.getZ());
			
			// TODO: ew
			int voxelData = targetChunk.getTileData().getData(voxelPosLocal);
			voxelData |= targetChunk.getTileData().getData(voxelPosLocal.x+1, voxelPosLocal.y, voxelPosLocal.z);
			voxelData |= targetChunk.getTileData().getData(voxelPosLocal.x+1, voxelPosLocal.y, voxelPosLocal.z+1);
			voxelData |= targetChunk.getTileData().getData(voxelPosLocal.x+1, voxelPosLocal.y, voxelPosLocal.z+1);
			voxelData |= targetChunk.getTileData().getData(voxelPosLocal.x, voxelPosLocal.y+1, voxelPosLocal.z);
			voxelData |= targetChunk.getTileData().getData(voxelPosLocal.x+1, voxelPosLocal.y+1, voxelPosLocal.z);
			voxelData |= targetChunk.getTileData().getData(voxelPosLocal.x+1, voxelPosLocal.y+1, voxelPosLocal.z+1);
			voxelData |= targetChunk.getTileData().getData(voxelPosLocal.x+1, voxelPosLocal.y+1, voxelPosLocal.z+1);

			// Inspected, Inspector
			// I think we've found something over here
			if (voxelData > 0) {
				foundVoxel = true;
				voxelPosition.set(voxelIndex);
				return voxelData;
			}
			
			voxelPositionPrior.set(voxelPosition);
			voxelPosPriorLocal.set(voxelPosLocal);
			priorChunk = targetChunk;
			
		}

		return -1;
	}
	
	private int getVoxelDistance(Vector3i startIndex) {
		return 1
			+ Math.abs(Maths.floor(trace.x + direction.x * rayLength) - startIndex.x)
			+ Math.abs((int)(trace.y + direction.y * rayLength) - startIndex.y)
			+ Math.abs(Maths.floor(trace.z + direction.z * rayLength) - startIndex.z);
    }
	
	public boolean hasFoundVoxel() {
		return foundVoxel;
	}
	
	public Vector3i getVoxelPosition() {
		return voxelPosition;
	}
	
	public Vector3i getVoxelPositionLocal() {
		return voxelPosLocal;
	}
	
	public Chunk getTargetChunk() {
		return targetChunk;
	}


	/**
	 * Convenience function to change an ID of a tile that has been raycasted to
	 */
	public void setTarget(byte id) {
		chunkData.setTile(id, targetChunk, voxelPosLocal.x, voxelPosLocal.y, voxelPosLocal.z);
	}

	public boolean addTarget(byte id) {
		if (voxelPositionPrior.x == Integer.MAX_VALUE)
			return false;
		
		chunkData.setTile(id, priorChunk, voxelPosPriorLocal.x, voxelPosPriorLocal.y, voxelPosPriorLocal.z);
		return true;
	}
}
