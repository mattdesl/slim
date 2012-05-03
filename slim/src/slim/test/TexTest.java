package slim.test;

import java.net.URL;

import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import de.matthiasmann.twl.Event;

import slim.Color;
import slim.FBO;
import slim.GL2D;
import slim.Image2D;
import slim.ShaderProgram;
import slim.SlimException;
import slim.SpriteBatch;
import slim.texture.Texture;
import slim.texture.Texture2D;
import slim.util.Utils;

public class TexTest extends GUITestBase {
	public static void main(String[] args) throws SlimException {
		new TexTest().start();
	}
	public TexTest() { super(1024, 768, false); }
	
	private Image2D img;
	private SpriteBatch batch;
	private FBO lightAtlas1, lightAtlas2;
	private ShaderProgram prog;
	
	final int SIZE = 256;
	
	final int LIGHT_ATLAS_SIZE = 512;
	
	float mx, my;
	
	float texWidth, texHeight;
	
	public void onResize() {
//		init3D();
		
		init2D();
		GL11.glViewport(0,0,getWidth(),getHeight());
	}
	
	@Override
	public void init() throws SlimException {
		init2D();
		GL2D.setBackground(Color.gray);
		batch = new SpriteBatch();
		img = new Image2D("res/casters.png");
		img.getTexture().setWrap(Texture.WRAP_REPEAT);
		texWidth = (float)img.getTextureWidth();
		texHeight = (float)img.getTextureHeight();
		
		lightAtlas1 = new FBO(new Texture2D(LIGHT_ATLAS_SIZE, LIGHT_ATLAS_SIZE));
		
		
		ShaderProgram.setStrictMode(false);
		try {
			prog = ShaderProgram.loadProgram("res/shader/lights2/pass.vert", "res/shader/lights2/polar2rect.frag");
			prog.bind();
			prog.setUniform1i("tex0", 0);
			prog.setUniform2f("texSize", SIZE/(float)img.getTextureWidth(), SIZE/(float)img.getTextureHeight());
			prog.setUniform2f("texOff", 0, 0);
			prog.unbind();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void render() throws SlimException {
		batch.setColor(Color.white);
		batch.drawImage(img);
		batch.flush();
		
		prog.bind();
		//since we are already passing color values to each vertex that are just being ignored
		//we can instead pass information about the "sprite" we are sending in
		//(x, y, xOff, yOff)
		//x/y -> the UV location on our atlas
		//xOff/yOff -> the offset used in sampling the polar coordinates
		
		// TODO : problem with edge is because we are sampling from what SHOULD be an empty space
		// but instead is just a texcoord outside of [0-1]
		// the old system didn't have this problem because it would bake onto a clear map, i.e. 
		// the space would have been empty. maybe we can get away with clearing it instead of having
		// to branch in the shader, but the latter might actually be faster
		
		batch.setColor(0, 0, (mx-SIZE/2f)/texWidth, (my-SIZE/2f)/texHeight);
		batch.drawSubImage(img, mx-SIZE/2f, my-SIZE/2f, SIZE, SIZE, 0, 0, SIZE, SIZE);
//		
		float x = SIZE/texWidth;
		batch.setColor(x, 0, (mx+SIZE/2f)/texWidth, (my-SIZE/2f)/texHeight);
		batch.drawSubImage(img, mx+SIZE/2f, my-SIZE/2f, SIZE, SIZE, SIZE, 0, SIZE, SIZE);

//		batch.setColor(color)
		batch.flush();
		prog.unbind();
		

		batch.setColor(Color.red);
		GL2D.drawRect(batch, mx-SIZE/2f, my-SIZE/2f, SIZE, SIZE, 4);
		batch.setColor(Color.white);
		GL2D.drawRect(batch, mx+SIZE/2f, my-SIZE/2f, SIZE, SIZE, 4);
		batch.flush();
	}
	
	@Override
	public void update(int delta) throws SlimException {
	}
	
	public void handleEvent(Event e) {
		if (e.isMouseEvent()) {
			mx = e.getMouseX();
			my = e.getMouseY();
			if (prog.valid()) {
				prog.bind();
				prog.setUniform2f("texOff", (mx-SIZE/2f)/(float)img.getTextureWidth(), (my-SIZE/2f)/(float)img.getTextureHeight());
				prog.unbind();
			}
		}
	}
	
	@Override
	public URL getThemeURL() throws SlimException {
		return Utils.getResource("res/gui/chutzpah/chutzpah.xml");
	}
}
