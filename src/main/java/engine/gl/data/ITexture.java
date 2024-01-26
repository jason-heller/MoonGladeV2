package engine.gl.data;

public interface ITexture {
	
	public int getID();
	
	public void destroy();
	
	public void bind();
	public void unbind();
}
