package engine.gl.meshing.transvoxel;

import static engine.world.Chunk.CHUNK_WIDTH;

public enum Seam {
	NEG_X(0, 1, 0, 0, -1,  0),
	POS_X(0, 1, 1, 0,  1,  0),
	NEG_Z(1, 0, 0, 0,  0, -1),
	POS_Z(1, 0, 0, 1,  0,  1),
	
	NO_SEAM(0, 0, 0, 0, 0, 0);
	
	public final static int SEAM_LENGTH = CHUNK_WIDTH;
	
	public final int incX, incZ, initX, initZ, signX, signZ;

	Seam(int incX, int incZ, int initX, int initZ, int signX, int signZ) {
		this.incX = incX;
		this.incZ = incZ;
		this.initX = initX;
		this.initZ = initZ;
		this.signX = signX;
		this.signZ = signZ;
	}
	
	// -dx, -dz + offsets -> the last 4 pts
}
