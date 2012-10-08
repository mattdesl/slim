package slim.test.bare;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_TRANSFORM_BIT;
import static org.lwjgl.opengl.GL11.GL_VIEWPORT_BIT;
import static org.lwjgl.opengl.GL11.glViewport;

import java.net.URL;

import org.lwjgl.opengl.GL11;

import slim.g2d.FBO;
import slim.g2d.Image;
import slim.shader2.ShaderProgram;
import slim.util2.Utils2;
import slimold.Color;
import slimold.GL2D;
import slimold.SlimException;
import slimold.SpriteBatch;

public class SpriteBatchTest extends GUITestBase {
	public static void main(String[] args) throws SlimException {
		new SpriteBatchTest().start();
	}
	
	public URL getThemeURL() throws SlimException {
		return Utils2.getResource("res/gui/chutzpah/chutzpah.xml");
	}

	private ShaderProgram prog1, prog2;
	private Image image, image2;
	private SpriteBatch batch, batch2;
	FBO fbo;
	
	@Override
	public void init() throws SlimException {
		init2D();
		GL2D.setBackground(Color.gray);
		
		
		image = new Image("res/tilesheet.png");
		fbo = new FBO(256, 256);
		batch = new SpriteBatch();
		
		//GL11.glTranslatef(30, 0, 0);
		fbo.bind();
		batch.setColor(Color.white);
		GL2D.fillRect(batch, 0, 0, 252, 252);
		batch.drawImage(image);
		GL2D.drawRect(batch, 5, 5, 25, 25);
		batch.setColor(Color.red);
		GL2D.fillRect(batch, 25, 25, 10, 10);
		batch.flush();
		fbo.unbind();
		
	}

	@Override
	public void render() throws SlimException {
		
		
		batch.setColor(Color.white);
		batch.drawImage(fbo.getImage());
		
		batch.drawImage(image, 400, 400);
		batch.flush();
	}
	
	
	@Override
	public void update(int delta) throws SlimException {
		
	}
}
