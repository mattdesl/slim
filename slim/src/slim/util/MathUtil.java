package slim.util;

import org.lwjgl.util.vector.Matrix4f;

public class MathUtil {
	
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
