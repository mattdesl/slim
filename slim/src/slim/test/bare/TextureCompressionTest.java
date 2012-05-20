package slim.test.bare;

import java.net.URL;

import slim.Image;
import slim.SlimException;
import slim.SpriteBatchImage;
import slim.texture.Texture;
import slim.texture.Texture2D;
import slim.util.Utils;

public class TextureCompressionTest extends GUITestBase {
	public static void main(String[] args) throws SlimException {
		new TextureCompressionTest().start();
	}
	
	Image oneImg;
	SpriteBatchImage batch;
	
	@Override
	public void init() throws SlimException {
		init2D();
		batch = new SpriteBatchImage();
		
		Texture tex = Texture2D.loadTexture(url, format);
		oneImg = new Image(tex);
	}

	@Override
	public void render() throws SlimException {
		batch.drawImage(oneImg);
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
