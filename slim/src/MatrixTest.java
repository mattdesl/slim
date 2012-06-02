import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;


public class MatrixTest {
	
	static Matrix4f tmp = new Matrix4f();
	static Vector4f tmpV = new Vector4f(0, 0, 0, 1f);
	
	public static void translate(Matrix4f m, float x, float y, float z) {
		if (m==null)
			m = new Matrix4f();
		tmp.setIdentity();
		tmp.m03 = x;
		tmp.m13 = y;
		tmp.m23 = z;
		tmp.transpose();
		Matrix4f.mul(tmp, m, m);
	}
	
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
	
	
	public static Vector2f transform(Matrix4f m, float x, float y) {
		tmpV.set(x, y);
		Matrix4f.transform(m, tmpV, tmpV);
		return new Vector2f(tmpV.x, tmpV.y);
	}

	static Vector2f point(Matrix4f m) {
		return transform(m, 0, 0);
	}
	
	public static void main(String[] args) {
		Matrix4f m = new Matrix4f(); //identity
		
//		translate(m, 50, 50, 0);
//		translate(m, 100, 0, 0);
//		System.out.println(point(m));
//		
//		scale(m, 2f, .5f, 1f);
//		System.out.println(transform(m, 0, 0));
	
		System.out.println(point(m));
		
		m.translate(new Vector3f(50, 50, 0));
		m.translate(new Vector3f(0, 50, 0));
		m.scale(new Vector3f(2f, 2f, 2f));
		System.out.println(transform(m, 2, 2));
	}
}
