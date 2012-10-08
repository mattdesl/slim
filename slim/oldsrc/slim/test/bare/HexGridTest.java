package slim.test.bare;

import java.net.URL;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import slim.g2d.Image;
import slim.g2d.SpriteBatch;
import slim.shader2.ShaderProgram;
import slim.texture.Texture;
import slim.util2.Utils2;
import slimold.Color;
import slimold.GL2D;
import slimold.SlimException;

public class HexGridTest extends GUITestBase {
	public static void main(String[] args) throws SlimException {
		new HexGridTest().start();
	}


	Image img1, img2;
	SpriteBatch batch;
	
	@Override
	public void init() throws SlimException {
		this.setTargetFPS(60);
		Display.setVSyncEnabled(true);
		GL11.glViewport(0, 0, 800, 600);
		GL2D.setBackground(Color.gray);
		img1 = new Image("res/clouds.jpg");
		img2 = new Image("res/hexgrid.png");
		img2.getTexture().setWrap(Texture.WRAP_REPEAT);
		batch = new SpriteBatch(12);
		ShaderProgram.setStrictMode(false);
		batch.getShaderProgram().bind();
		batch.getShaderProgram().setUniform1i("tex1", 1);
		
	}

	private float rot = 0;

	@Override
	public void render() throws SlimException {
		batch.resetTransform();

		img1.getTexture().bind();
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		
		img2.getTexture().bind();
		GL13.glActiveTexture(GL13.GL_TEXTURE1);
		
		batch.drawImage(img2, 0, 0);
		
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
