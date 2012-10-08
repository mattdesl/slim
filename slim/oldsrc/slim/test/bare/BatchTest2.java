package slim.test.bare;

import java.net.URL;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Quaternion;

import slim.shader2.ShaderProgram;
import slim.shader2.VertexAttrib;
import slim.util2.FastTrig;
import slim.util2.Utils2;
import slimold.Color;
import slimold.GL2D;
import slimold.SlimException;

public class BatchTest2 extends GUITestBase {
	public static void main(String[] args) throws SlimException {
		new BatchTest2().start();
	}

	public static final String TEXCOORD_0 = "TexCoord0";
	public static final String COLOR = "Color";
	public static final String POSITION = "Position";
	public static final String MYATTRIB = "Rotation";

	private FloatBuffer position, color, myattrib, texcoord;
	private ShaderProgram prog;
	private  Matrix4f viewMatrix, projMatrix;
	
	private final float[] VERTS_ARRAY = new float[] {
			//format: x,y,z,r,g,b,a,u,v
		50,50,0, 0,1,1,1, 0,0,
		50,100,0, 0,0,1,1, 1,0,
		100,100,0, 1,1,1,1, 1,1,
		100,50,0, 1,1,1,1, 0,1
	};
	private FloatBuffer verts = BufferUtils.createFloatBuffer(VERTS_ARRAY.length);
	private static Matrix4f tmp = new Matrix4f();
	static Quaternion quat = new Quaternion();
	
	@Override
	public void init() throws SlimException {
		//init2D();
		
		verts.put(VERTS_ARRAY);
		
		GL2D.setBackground(Color.gray);
		GL11.glViewport(0, 0, 800, 600);
		VertexAttrib a2;
		
		//we can bind a few by default like so:
		List<VertexAttrib> a = Arrays.asList(
				new VertexAttrib(0, "Position", 2),
				new VertexAttrib(1, "Color", 4),
				new VertexAttrib(2, "TexCoord", 2));
		
		prog = ShaderProgram.load("res/shader/batch/sprite.vert", "res/shader/batch/sprite.frag", a);
		
		
		prog.bind();
		
		
		viewMatrix = new Matrix4f();
		projMatrix = ortho2D(0, 0, 800, 600);

		//scale(viewMatrix, 4f, 4f, 1f);
		translate(viewMatrix, 345, 100, 0);
		
		//rotateZ(viewMatrix, 45f, 75, 75);
//		rotate(viewMatrix, 1, 0, 0, 45f);
		
//		viewMatrix.mul(viewMatrix, )
//		FloatBuffer buf = BufferUtils.createFloatBuffer(16);
//		projMatrix.store(buf);
//		buf.flip();?
		prog.setUniformMatrix4("projMatrix", true, projMatrix);
//		
//		buf.clear();
//		viewMatrix.store(buf);
//		buf.flip();
	}
	
	static void printMatrix(Matrix4f m) {
		FloatBuffer buf = BufferUtils.createFloatBuffer(16);
		m.store(buf);
		buf.flip();
		for (int i=0;i<4; i++)
			System.out.println(buf.get()+" "+buf.get()+" "+buf.get()+" "+buf.get());
	}
	
	static void scale(Matrix4f m, float scaleX, float scaleY, float scaleZ) {
		tmp.m00 = scaleX;
		tmp.m01 = 0;
		tmp.m02 = 0;
		tmp.m03 = 0;
		tmp.m10 = 0;
		tmp.m11 = scaleY;
		tmp.m12 = 0;
		tmp.m13 = 0;
		tmp.m20 = 0;
		tmp.m21 = 0;
		tmp.m22 = scaleZ;
		tmp.m23 = 0;
		tmp.m30 = 0;
		tmp.m31 = 0;
		tmp.m32 = 0;
		tmp.m33 = 1;
		Matrix4f.mul(tmp, m, m);
	}
	
	static void translate(Matrix4f m, float x, float y, float z) {
		tmp.m00 = 1;
		tmp.m01 = 0;
		tmp.m02 = 0;
		tmp.m03 = x;
		tmp.m10 = 0;
		tmp.m11 = 1;
		tmp.m12 = 0;
		tmp.m13 = y;
		tmp.m20 = 0;
		tmp.m21 = 0;
		tmp.m22 = 1;
		tmp.m23 = z;
		tmp.m30 = 0;
		tmp.m31 = 0;
		tmp.m32 = 0;
		tmp.m33 = 1;
		Matrix4f.mul(tmp, m, m);
	}
	
	static void rotateZ(Matrix4f m, float angle, float x, float y) {
		double r = Math.toRadians(angle);
		
		float cos = (float)FastTrig.cos(r);
		float sin = (float)FastTrig.sin(r);
		float oneminus = 1f - cos;
		
		tmp.m00 = cos;
		tmp.m01 = -sin;
		tmp.m02 = 0;
		tmp.m03 = x * oneminus + y * sin;
		tmp.m10 = sin;
		tmp.m11 = cos;
		tmp.m12 = 0;
		tmp.m13 = y * oneminus - x * sin;
		tmp.m20 = 0;
		tmp.m21 = 0;
		tmp.m22 = 1;
		tmp.m23 = 0;
		tmp.m30 = 0;
		tmp.m31 = 0;
		tmp.m32 = 0;
		tmp.m33 = 1;
		Matrix4f.mul(tmp, m, m);
	}
	
	
	
	static Matrix4f ortho2D(float x, float y, float width, float height) {
		return ortho(x, x + width, y + height, y, 1, -1);
	}
	
	static Matrix4f ortho2D(float x, float y, float width, float height, float near, float far) {
		return ortho(x, x + width, y, y + height, near, far);
	}

	static Matrix4f ortho(float left, float right, float bottom, float top,
			float near, float far) {
		Matrix4f m = new Matrix4f();

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

	@Override
	public void render() throws SlimException {
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
		verts.position(7);
		GL20.glVertexAttribPointer(2, 2, false, stride, verts);
		
	    GL11.glDrawArrays(GL11.GL_QUADS, 0, 4);
		
		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		GL20.glDisableVertexAttribArray(2);
		
//		Matrix4f m = new Matrix4f(viewMatrix);
//		translate(m, 150, 50, 0);
		
		
//		GL20.glVertexAttrib4f(1, 0.5f, 1, 0.5f, 1);
//		GL11.glPointSize(10f);
//		GL11.glBegin(GL11.GL_POINTS);
//		GL20.glVertexAttrib3f(0, 10, 10, 0);
//		GL11.glEnd();
	}
	
	void draw() {
		
	}

	@Override
	public void update(int delta) throws SlimException {

	}

	@Override
	public URL getThemeURL() throws SlimException {
		return Utils2.getResource("res/gui/chutzpah/chutzpah.xml");
	}
}
