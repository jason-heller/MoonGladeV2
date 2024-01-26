package engine.gl.data;

import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.GL_MAP_WRITE_BIT;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.lwjgl.opengl.GL30.glMapBufferRange;
import static org.lwjgl.opengl.GL32.GL_ALREADY_SIGNALED;
import static org.lwjgl.opengl.GL32.GL_CONDITION_SATISFIED;
import static org.lwjgl.opengl.GL32.GL_SYNC_FLUSH_COMMANDS_BIT;
import static org.lwjgl.opengl.GL32.GL_SYNC_GPU_COMMANDS_COMPLETE;
import static org.lwjgl.opengl.GL32.glClientWaitSync;
import static org.lwjgl.opengl.GL32.glDeleteSync;
import static org.lwjgl.opengl.GL32.glFenceSync;
import static org.lwjgl.opengl.GL44.GL_MAP_COHERENT_BIT;
import static org.lwjgl.opengl.GL44.GL_MAP_PERSISTENT_BIT;
import static org.lwjgl.opengl.GL44.glBufferStorage;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

public abstract class PersistentMesh implements IMesh {
	protected int vao;
	
	private int[] bufferObjects;
	private int[] bufObjStrides;
	private ByteBuffer[] bufferData;
	
	private SyncRange[] syncRanges;		// Buffer partition offsets (the sync ranges)
	
	private int numBuffers;		// Number of buffers, should be at least 2
	
	private long syncObj;		// If buffers = 1 this is what the sync lock / wait should be set to
	
	private int rangeIndex;
	
	private List<BufferData> bufferChanges = new LinkedList<>(); 

	private final int MAX_UPDATES_PER_DRAW = 2;		// How many commits to add per draw call
	
	public abstract void build();
	
	protected void build(int numVerticesPerMesh, int numBufferObjects) {
		
		try {
			this.build(numVerticesPerMesh, 3, numBufferObjects);	// Triple buffer by default (one buffer each for the GPU, CPU, and driver)
		} catch (Exception e) {}	// Exception occurs if numBuffers <= 1, here we hard code it to 3, so this never occurs
	}
	
	protected void build(int numVertices, int numBuffers, int numBufferObjects) throws NotEnoughBuffersException {
		if (numBuffers <= 1)
			throw new NotEnoughBuffersException(numBuffers);
		
		//this.numVertices = numVertices;
		this.numBuffers = numBuffers;
		
		bufferObjects = new int[numBufferObjects];
		bufObjStrides = new int[numBufferObjects];
		bufferData = new ByteBuffer[numBufferObjects];
		
		syncRanges = new SyncRange[numBuffers];
		for(int i = 0; i < numBuffers; ++i) {
			syncRanges[i] = new SyncRange();
			syncRanges[i].begin = numVertices * i;
		}
		
		vao = glGenVertexArrays();
		bind();

		addBufferObjects(numVertices);
		unbind();
	}
	
	protected abstract void addBufferObjects(int numVerticesPerMesh);

	protected void addImmutableBufferObject(int index, int numVertices, int perVertexSize, int dataType) {
		glEnableVertexAttribArray(index);
		
		// Create a vertex buffer object
		bufferObjects[index] = glGenBuffers();
		bufObjStrides[index] = perVertexSize;
		glBindBuffer(GL_ARRAY_BUFFER, bufferObjects[index]);
		glVertexAttribPointer(index, perVertexSize, dataType, false, 0, 0);
		
		// Create an immutable data store for the buffer
		int dataSize = perVertexSize * numVertices;
		long bufferSize = dataSize * numBuffers;
		
		final int flags = GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT;
		glBufferStorage(GL_ARRAY_BUFFER, bufferSize, flags);

		// Map the buffer forever
		bufferData[index] = glMapBufferRange(GL_ARRAY_BUFFER, 0, bufferSize, flags);
	}
	
	protected void addMutableBufferObject(int index, int numVertices, int perVertexSize, int dataType) {
		glEnableVertexAttribArray(index);
		
		// Create a vertex buffer object
		bufferObjects[index] = glGenBuffers();
		bufObjStrides[index] = perVertexSize;
		glBindBuffer(GL_ARRAY_BUFFER, bufferObjects[index]);
		glVertexAttribPointer(index, perVertexSize, dataType, false, 0, 0);
		
		glBufferData(GL_ARRAY_BUFFER, (ByteBuffer)null, GL_STATIC_DRAW);
	}
	
	public void destroy() {
		for(int i = 0; i < bufferObjects.length; ++i)
			glDeleteBuffers(bufferObjects[i]);
		
		glDeleteVertexArrays(vao);
	}
	
	public void update() {

		// Wait if not in sync
		waitBuffer(syncRanges[rangeIndex].sync);
		
		int startOffset = syncRanges[rangeIndex].begin;
		
		final int MAX_ITERS = 1;
		
		for (int iter = 0; iter < MAX_ITERS; ++iter) {
			// Commit changes
			int maxCommits = Math.min(MAX_UPDATES_PER_DRAW, bufferChanges.size());

			for(int i = 0; i < maxCommits; ++i) {
				BufferData bd = bufferChanges.remove(0);
				final int bdDataLen = bd.data.length;
				
				int startOffsetData = (startOffset * bufObjStrides[bd.bufferObject]);
				startOffsetData += bd.dataOffset;
				
				for(int j = 0; j < bdDataLen; ++j)
					bufferData[bd.bufferObject].put(startOffsetData + j, bd.data[j]);
		
			}
		}
		
		lockBuffer(syncRanges[rangeIndex].sync);
		
		rangeIndex = (rangeIndex + 1) % numBuffers;
	}
	
	public void setBufferSubData(int bufferObj, int dataOffset, byte[] data) {
		BufferData bd = new BufferData();
		bd.bufferObject = bufferObj;
		bd.dataOffset = dataOffset;
		bd.data = data;
		bufferChanges.add(bd);
	}
	
	private void lockBuffer(long sync) {
		if (sync != 0L)
			glDeleteSync(sync);

		syncObj = glFenceSync(GL_SYNC_GPU_COMMANDS_COMPLETE, 0);
	}

	private void waitBuffer(long sync) {
		if (sync != 0L) {
			while (true) {
				int waitReturn = glClientWaitSync(sync, GL_SYNC_FLUSH_COMMANDS_BIT, 1);
				
				if (waitReturn == GL_ALREADY_SIGNALED || waitReturn == GL_CONDITION_SATISFIED)
					return;

				// waitCount++;
			}
		}
	}
	
	@Override
	public int getVAO() {
		return vao;
	}
}

// Helper classes //

class SyncRange {
	public int begin;
	public int sync;
}

class BufferData {
	public int bufferObject;
	public int dataOffset;
	public byte[] data;
}

class NotEnoughBuffersException extends Exception { 
	private static final long serialVersionUID = 1L;

	public NotEnoughBuffersException(int numBuffers) {
        super("Built with too few (" + numBuffers + ") buffers, must be at least 2");
    }
}