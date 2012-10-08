package slim.tools.polyedit;

import java.net.URL;

import org.lwjgl.opengl.GL11;

import slim.math.Matrix3;
import slim.math.Vector2;
import slim.test.bare.GUITestBase;
import slim.util2.Utils2;
import slimold.Color;
import slimold.SlimException;

public class EmptyGUITest extends GUITestBase {
	public static void main(String[] args) throws SlimException {
		new EmptyGUITest().start();
	}
	
	Matrix3 mtx = new Matrix3();
	
	@Override
	public void init() throws SlimException {
		init2D();
		mtx.setToRotation(45);
	}
	
	
	@Override
	public void render() throws SlimException {
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		Color.white.bind();
		drawQuad(50, 50, 250, 250);
	}
	
	void vert(float x, float y) {
		Vector2 vec = new Vector2(x, y);
		vec.
		GL11.glVertex2f(vec.x, vec.y);
	}
	
	void drawQuad(float x, float y, float w, float h) {
		
		
		GL11.glBegin(GL11.GL_QUADS);
		vert(x, y);
		GL11.glVertex2f(x + w, y);
		GL11.glVertex2f(x + w, y + h);
		GL11.glVertex2f(x, y + h);
		GL11.glEnd();
	}
	
	@Override
	public void update(int delta) throws SlimException {
		
	}

	@Override
	public URL getThemeURL() throws SlimException {
		return Utils2.getResource("res/gui/chutzpah/chutzpah.xml");
	}
}
