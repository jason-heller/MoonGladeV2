package engine.space;

import engine.gl.ICamera;

public interface ISpace {
	public void destroy();
	public void updateTick();
	public void gameTick();
	public ICamera getCamera();
	public IPlayer getPlayer();
}
