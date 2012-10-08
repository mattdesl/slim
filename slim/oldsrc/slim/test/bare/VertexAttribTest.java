package slim.test.bare;

import java.net.URL;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.HashMap;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.util.vector.Matrix4f;

import slim.shader2.ShaderProgram;
import slim.util2.Utils2;
import slimold.Color;
import slimold.GL2D;
import slimold.SlimException;

public class VertexAttribTest {
	
	public static void main(String[] args) throws SlimException {
		new VertexAttribTest().start();
	}

	public static final String TEXCOORD_0 = "TexCoord0";
	public static final String COLOR = "Color";
	public static final String POSITION = "Position";
	public static final String MYATTRIB = "Rotation";

	private FloatBuffer position, color, myattrib, texcoord;
	private ShaderProgram prog;
	private Matrix4f viewMatrix, projMatrix;
	
	private final float[] VERTS_ARRAY = new float[] {
			//format: x,y,z,r,g,b,a,u,v
		50,50,0, 0,1,1,1, 0,0,
		50,100,0, 0,0,1,1, 1,0,
		100,100,0, 1,1,1,1, 1,1,
		100,50,0, 1,1,1,1, 0,1
	};
	private FloatBuffer verts = BufferUtils.createFloatBuffer(VERTS_ARRAY.length);

	@Override
	public void init() {
		//init2D();
		
		verts.put(VERTS_ARRAY);
		
		GL2D.setBackground(Color.gray);
		GL11.glViewport(0, 0, 800, 600);
		
		HashMap<String, Integer> ids = new HashMap<String, Integer>();
		ids.put("Position", 0);
		ids.put("Color", 1);
		ids.put("TexCoord0", 2);
		
		prog = ShaderProgram.load("res/shader/batch/sprite.vert", "res/shader/batch/sprite.frag", ids);
		prog.bind();
		System.out.println(Arrays.toString(prog.getAttributes()));
		
		viewMatrix = ortho2D(0, 0, 800, 600);
		projMatrix = new Matrix4f();
		
		FloatBuffer buf = BufferUtils.createFloatBuffer(16);
		projMatrix.store(buf);
		buf.flip();
		prog.setUniformMatrix4("projMatrix", true, buf);
		
		buf.clear();
		viewMatrix.store(buf);
		buf.flip();
		prog.setUniformMatrix4("viewMatrix", true, buf);
	}

	

	@Override
	public void render() {
		int stride = 4 * 9;
		
		//vertex format - x,y,z r,g,b,a u,v 
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL20.glEnableVertexAttribArray(2);
		
		//position
		verts.position(0);
		GL20.glVertexAttribPointer(0, 3, false, stride, verts);
		
		//color
		verts.position(3);
		GL20.glVertexAttribPointer(1, 4, false, stride, verts);

		//texcoord
		verts.position(8);
		GL20.glVertexAttribPointer(2, 2, false, stride, verts);
		
	    GL11.glDrawArrays(GL11.GL_QUADS, 0, 4);
		
		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		GL20.glDisableVertexAttribArray(2);
	}
	
	static Matrix4f ortho2D(Matrix4f m, float x, float y, float width, float height) {
		return ortho(m, x, x + width, y + height, y, 1, -1);
	}
	
	static Matrix4f ortho(Matrix4f m, float left, float right, float bottom, float top,
			float near, float far) {
		//handy code from LibGDX's Matrix4
		float x_orth = 2 / (right - left);
		float y_orth = 2 / (top - bottom);
		float z_orth = -2 / (far - near);
		float tx = -(right + left) / (right - left);
		float ty = -(top + bottom) / (top - bottom);
		float tz = -(far + near) / (far - near);
		m.m00 = x_orth;
		m.m10 = 0;
		m.m20 = 0;
		m.m30 = 0;
		m.m01 = 0;
		m.m11 = y_orth;
		m.m21 = 0;
		m.m31 = 0;
		m.m02 = 0;
		m.m12 = 0;
		m.m22 = z_orth;
		m.m32 = 0;
		m.m03 = tx;
		m.m13 = ty;
		m.m23 = tz;
		m.m33 = 1;
		return m;
	}
}
