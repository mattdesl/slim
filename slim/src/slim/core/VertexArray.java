package slim.core;

import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;

import java.nio.FloatBuffer;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

public class VertexArray implements VertexData {
	
	protected FloatBuffer buffer;
	protected List<VertexAttrib> attribs;
	private int totalNumComponents;
	
	public VertexArray(List<VertexAttrib> attribs, int vertCount) {
		this.attribs = attribs;
		for (VertexAttrib a : attribs)
			totalNumComponents += a.numComponents;
		
		buffer = BufferUtils.createFloatBuffer(totalNumComponents * vertCount);
	}
	
	public void clear() {
		buffer.clear();
	}
	
	public VertexArray put(float v) {
		buffer.put(v);
		return this;
	}
	
	public void position(int offset) {
		buffer.position(offset);
	}
	
	public void flip() {
		buffer.flip();
	}
	
	//.... drawing commands ....
	
	
	public void bind() {
		int stride = totalNumComponents * 4;
		int offset = 0;
		for (int i=0; i<attribs.size(); i++) {
			VertexAttrib a = attribs.get(i);
			buffer.position(offset);
			//TODO: does not work with matrices and arrays
			glEnableVertexAttribArray(a.location);
			glVertexAttribPointer(a.location, a.numComponents, false, stride, buffer);
			
			offset += a.numComponents;
		}
	}

	public void draw(int geom, int first, int count) {
		GL11.glDrawArrays(geom, first, count);
	}
	
	public void unbind() {
		for (int i=0; i<attribs.size(); i++) {
			VertexAttrib a = attribs.get(i);
			if (a==null)
				continue;
			GL20.glDisableVertexAttribArray(a.location);
		}
	}
}
