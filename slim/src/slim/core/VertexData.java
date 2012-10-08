package slim.core;

public interface VertexData {

	public VertexData put(float v);
	public void clear();
	public void flip();
	
	public void bind();
	public void unbind();
	public void draw(int geom, int first, int count);
}
