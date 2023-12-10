package engine.space;

import engine.gl.HeightmapBuilder;
import engine.rendering.FirstPersonCamera;
import engine.rendering.ICamera;
import engine.rendering.Renderer;
import engine.rendering.data.GenericMesh;
import engine.rendering.data.HeightmapMesh;

public class WorldSpace implements ISpace {

	private ICamera camera;
	private Renderer renderer;

    private HeightmapMesh test;
	
	public WorldSpace(float aspectRatio) {
		camera = new FirstPersonCamera(aspectRatio);
		renderer = new Renderer();
		
		test = HeightmapBuilder.buildTriStripHeightmap(HeightmapMesh.VERTEX_RUN);
		test.create();
	}

	public void destroy() {

		test.destroy();
	}

	@Override
	public void updateTick() {
		camera.update();
		renderer.renderMesh(camera, test, camera.getProjectionMatrix(), camera.getViewMatrix());
	}

	@Override
	public void gameTick() {
		// TODO Auto-generated method stub
		
	}
}
