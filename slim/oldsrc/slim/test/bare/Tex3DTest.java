package slim.test.bare;

import java.net.URL;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;

import slim.g2d.Image;
import slim.g2d.SpriteBatch;
import slim.util2.Utils2;
import slimold.Color;
import slimold.GL2D;
import slimold.SlimException;

public class Tex3DTest extends GUITestBase {
	public static void main(String[] args) throws SlimException {
		new Tex3DTest().start();
	}

	Image img1;
	SpriteBatch batch;
	
	@Override
	public void init() throws SlimException {
		this.setTargetFPS(60);
		Display.setVSyncEnabled(true);
		
		//init2D();
		System.out.println(GLContext.getCapabilities().GL_ARB_draw_instanced);
		System.out.println(GLContext.getCapabilities().GL_ARB_instanced_arrays);
		System.out.println(GLContext.getCapabilities().OpenGL30);
		System.out.println(GLContext.getCapabilities().OpenGL21);
		
		GL11.glViewport(0, 0, 800, 600);
		GL2D.setBackground(Color.gray);
		img1 = new Image("res/box1.png");
		batch = new SpriteBatch(12);
	}

	private float rot = 0;

	@Override
	public void render() throws SlimException {
		batch.resetTransform();
		
		batch.drawImage(img1, 20, 20, rot+=0.03f);
		batch.drawImage(img1, 55, 20, 0);
		
		batch.flush();
		Display.setTitle(String.valueOf(getFPS()));
	}
	
	@Override
	public void update(int delta) throws SlimException {
		
	}

	@Override
	public URL getThemeURL() throws SlimException {
		return Utils2.getResource("res/gui/chutzpah/chutzpah.xml");
	}
}
