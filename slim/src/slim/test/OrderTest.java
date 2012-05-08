package slim.test;

import java.net.URL;

import org.lwjgl.opengl.GL11;

import slim.Color;
import slim.GL2D;
import slim.Image2D;
import slim.SlimException;
import slim.SpriteBatch;
import slim.shader.ShaderProgram;
import slim.texture.Texture2D;
import slim.util.ArraySpriteSheet;
import slim.util.Utils;
import de.matthiasmann.twl.Event;

public class OrderTest extends GUITestBase {
	public static void main(String[] args) throws SlimException {
		new OrderTest().start();
	}
	
	public URL getThemeURL() throws SlimException {
		return Utils.getResource("res/gui/chutzpah/chutzpah.xml");
	}

	private ShaderProgram prog1, prog2;
	private Image2D image, image2;
	private SpriteBatch batch, batch2;
	private float zoom = 1f;
	private float x=0, y=0;
	private ArraySpriteSheet sheet;
	
	@Override
	public void init() throws SlimException {
		init2D();
		GL2D.setBackground(Color.gray);
		
		ShaderProgram.setStrictMode(false);
		final String VERT = "res/shader/pass.vert";
		prog1 = ShaderProgram.load(VERT, "res/shader/invert.frag");
		prog1.bind();
		prog1.setUniform1i("tex0", 0);
		prog2 = ShaderProgram.load(VERT, "res/shader/rect2polar.frag");
		prog2.bind();
		prog2.setUniform1i("tex0", 0);
		ShaderProgram.unbindAll();
		
		image = new Image2D("res/tilesheet.png");
		sheet = new ArraySpriteSheet(image, 40, 40, 2, 5);
		batch = new SpriteBatch();
	}

	@Override
	public void render() throws SlimException {
		batch.drawImage(image);
		//batch.drawImage(, 512, 256);
		
		
		batch.flush();
	}
	
	float mx, my;
	public void handleEvent(Event e) {
		if (e.isMouseEvent()) {
			mx = e.getMouseX();
			my = e.getMouseY();
		}
		
		if (e.isKeyPressedEvent() && e.getKeyCode()==Event.KEY_A) {
			zoom += .2f;
			float w = image.getWidth()*zoom;
			float h = image.getHeight()*zoom;
			if (1==1||w < getWidth() || h < getHeight()) {
				x = getWidth()/2f-w/2f;
				y = getHeight()/2f-w/2f;
			} else {
				x -= mx*.2f;
				y -= my*.2f;
			}
		} else if (e.isKeyPressedEvent() && e.getKeyCode()==Event.KEY_Z) {
			zoom -= .2f;
			float w = image.getWidth()*zoom;
			float h = image.getHeight()*zoom;
//			x += (getWidth()/2f)*.2f;
//			y += (getHeight()/2)*.2f;
			if (w < getWidth() || h < getHeight()) {
				x = getWidth()/2f-w/2f;
				y = getHeight()/2f-w/2f;
			} else {
				x += (w/4f)*.2f;
				y += (h/4f)*.2f;
			}
		}
	}
	
	void drawQuad(Image2D image, int x, int y) {
		Texture2D texture = image.getTexture();
		// image size, e.g. 550x200
		float width = texture.getWidth();
		float height = texture.getHeight();
		// the physical width of the texture which will be used in glTexCoord
		// (generally a float between 0 and 1)
		float textureWidth = texture.getNormalizedWidth();
		float textureHeight = texture.getNormalizedHeight();
		// texture offsets, for texture atlas purposes. leave at 0 for full
		// image
		float textureOffsetX = 0;
		float textureOffsetY = 0;

		GL11.glTexCoord2f(textureOffsetX, textureOffsetY);
		GL11.glVertex2f(x, y);
		GL11.glTexCoord2f(textureOffsetX, textureOffsetY + textureHeight);
		GL11.glVertex2f(x, y + height);
		GL11.glTexCoord2f(textureOffsetX + textureWidth, textureOffsetY
				+ textureHeight);
		GL11.glVertex2f(x + width, y + height);
		GL11.glTexCoord2f(textureOffsetX + textureWidth, textureOffsetY);
		GL11.glVertex2f(x + width, y);
	}
	
	@Override
	public void update(int delta) throws SlimException {
		
	}
}
