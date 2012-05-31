package slim.g2d;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector4f;

import slim.shader.ShaderProgram;

public abstract class AbstractBatch {
	
	protected MatrixStack viewStack;
	protected Matrix4f viewMatrix;
	protected Matrix4f projMatrix;
	
	private Matrix4f tmpM = new Matrix4f();
	private Vector4f tmpV = new Vector4f();
	
	private boolean transformEnabled = true;
	
	protected FloatBuffer vertexData;
	
	protected int idx;
	
	protected AbstractBatch() {
		this.viewStack = new MatrixStack(32);
		viewMatrix = viewStack.push(new Matrix4f());
	}
	
	public void flush() {
		if (idx>0) 
			render();
		idx = 0;
		vertexData.clear();
	}
	
	protected abstract void render();
	
	/**
	 * Place a single float value in our array.
	 * @param vert
	 */
	protected FloatBuffer put(float vert) {
		return vertexData.put(vert);
	}
	
	/**
	 * Once color, texture, position, etc. has been placed,
	 * end the vertex.
	 */
	protected void endVertex() {
		idx++;
	}
	
	public void pushTransform() {
		viewMatrix = viewStack.push(new Matrix4f(viewMatrix));
	}
	
	public void popTransform() {
		if (viewStack.size()==1)
			throw new IllegalStateException("trying to a stack of size 1");
		viewMatrix = viewStack.pop();
	}
	
	public void setTransform(Matrix4f m) {
		viewStack.set(viewStack.size()-1, m);
		viewMatrix = m;
	}
	
	public Matrix4f getTransform() {
		return viewMatrix;
	}
	
	/**
	 * Sets the current transform matrix to the identity matrix.
	 */
	public void resetTransform() {
		viewMatrix.setIdentity();
	}
	
	/** 
	 * If transform is enabled, each vertex will be transformed by the 
	 * currently set view matrix. Disabling transform will have no
	 * effect on the current matrix or the rest of the stack. Transform
	 * is enabled by default. This is different from the view/projection
	 * uniforms used in the shader; which is customizable by setting a new
	 * shader program or by binding the shader program and calling 
	 * setUniform on "projMatrix" and "viewMatrix".
	 * 
	 * @param transformEnabled whether to enable CPU vertex transforms
	 */
	public void setTransformEnabled(boolean transformEnabled) {
		this.transformEnabled = transformEnabled;
	}
	
	/**
	 * Whether transforms are enabled, true by default.
	 * @return true if vertex positions should be transformed
	 */
	public boolean isTransformEnabled() {
		return transformEnabled;
	}
	
	/** 
	 * Used by subclasses to transform the given 3D point based on the current view matrix.
	 * 
	 * Note: You should not rely on the values of the returned vector to stay constant; 
	 * it is frequently reused by this sprite batch! 
	 */
	protected Vector4f transform(float x, float y, float z) {
		tmpV.set(x, y, z, 1f);
		if (transformEnabled)
			return Matrix4f.transform(viewMatrix, tmpV, tmpV);
		else
			return tmpV;
	}
	
	protected class MatrixStack extends ArrayList<Matrix4f> {

		public MatrixStack(int size) {
			super(size);
		}
		
	    public Matrix4f push(Matrix4f o) {
	        add(o);
	        return o;
	    }

	    public Matrix4f pop() {
	        return remove(size() - 1);
	    }
	    
	    public Matrix4f peek() {
	        return get(size() - 1);
	    }
	}
}



//
//.. multiple texcoords ..
//drawMulti(Geom g, Image blend1, Image blend2);

