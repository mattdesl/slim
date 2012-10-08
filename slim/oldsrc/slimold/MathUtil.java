package slimold;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.util.vector.Matrix4f;

import slim.util2.FastTrig;

public class MathUtil {
	
	static Matrix4f tmp = new Matrix4f();

	public static void scale(Matrix4f m, float scaleX, float scaleY, float scaleZ) {
		if (m==null)
			m = new Matrix4f();
		tmp.setIdentity();
		tmp.m00 = scaleX;
		tmp.m11 = scaleY;
		tmp.m22 = scaleZ;
		tmp.transpose();
		Matrix4f.mul(tmp, m, m);
	}
	
	public static void printMatrix(Matrix4f m) {
		FloatBuffer buf = BufferUtils.createFloatBuffer(16);
		m.store(buf);
		buf.flip();
		for (int i=0;i<4; i++)
			System.out.println(buf.get()+" "+buf.get()+" "+buf.get()+" "+buf.get());
	}
	
	public static void translate(Matrix4f m, float x, float y, float z) {
		if (m==null)
			m = new Matrix4f();
//		tmp.setIdentity();
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
		tmp.transpose();
		Matrix4f.mul(tmp, m, m);
	}
	
	public static void rotateZ(Matrix4f m, float angle, float x, float y) {
		if (m==null)
			m = new Matrix4f();
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
	
	public static Matrix4f toOrtho2D(Matrix4f m, float x, float y, float width, float height) {
		return toOrtho(m, x, x + width, y + height, y, 1, -1);
	}
	
	public static Matrix4f toOrtho2D(Matrix4f m, float x, float y, float width, float height, float near, float far) {
		return toOrtho(m, x, x + width, y, y + height, near, far);
	}
	
	public static Matrix4f toOrtho(Matrix4f m, float left, float right, float bottom, float top,
			float near, float far) {
		if (m==null)
			m = new Matrix4f();
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
