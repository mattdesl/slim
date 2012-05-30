package slim.test.bare;

import java.io.IOException;
import java.net.URL;

import org.lwjgl.opengl.Display;

import slim.Color;
import slim.GL2D;
import slim.SlimException;
import slim.SpriteBatch;
import slim.g2d.Image;
import slim.shader.ShaderProgram;
import slim.texture.Texture;
import slim.texture.Texture2D;
import slim.util.Utils;

public class TextureCompressionTest extends GUITestBase {
	public static void main(String[] args) throws SlimException {
		new TextureCompressionTest().start();
	}
	
	Image oneImg;
	SpriteBatch batch;
	
	@Override
	public void init() throws SlimException {
		init2D();
		ShaderProgram shdr = ShaderProgram.load("res/shader/pass.vert", "res/shader/invert.frag");
//		shdr.bind();
//		shdr.setUniform1i("tex0", 0);
		
		GL2D.setBackground(Color.gray);
		batch = new SpriteBatch();
		URL url = Utils.getResource("res/texture2.dds");
		Texture2D tex;
		try {
//			Texture.setForcePOT(true);
			tex = Texture2D.loadTexture(url);
			oneImg = new Image(tex);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Display.setTitle("DDS loader");
	}

	@Override
	public void render() throws SlimException {
		batch.drawImage(oneImg);
		
		batch.drawImage(oneImg, 0, 300, oneImg.getWidth(), oneImg.getHeight(), 0, 0, 2f, 2f, null);
		batch.flush();
	}
	
	@Override
	public void update(int delta) throws SlimException {
		
	}

	@Override
	public URL getThemeURL() throws SlimException {
		return Utils.getResource("res/gui/chutzpah/chutzpah.xml");
	}
}
