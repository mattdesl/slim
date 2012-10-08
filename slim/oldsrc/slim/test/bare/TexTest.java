package slim.test.bare;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import org.lwjgl.opengl.GL11;

import slim.g2d.FBO;
import slim.g2d.Image;
import slim.shader2.ShaderProgram;
import slim.texture.Texture;
import slim.texture.Texture2D;
import slim.util2.Utils2;
import slimold.Color;
import slimold.GL2D;
import slimold.SlimException;
import slimold.SpriteBatch;
import de.matthiasmann.twl.Event;

public class TexTest extends GUITestBase {
	public static void main(String[] args) throws SlimException {
		new TexTest().start();
	}
	public TexTest() { super(1024, 768, false); }
	
	private Image img;
	private SpriteBatch batch;
	private FBO targetA, targetB, occlusionMap;
	private ShaderProgram polar2rect, lightmapShader, rect2polar, hblur, vblur;
	
	final int SIZE = 256;
	
	final int LIGHT_ATLAS_SIZE = 1024;
	
	float mx, my;
	
	float texWidth, texHeight;
	
	ArrayList<Light> lights;
	
	class Light {
		float x, y;
		Color tint;
		
		Light(float x, float y, Color tint) {
			this.x = x;
			this.y = y;
			this.tint = tint;
		}
	}
	
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
		img = new Image("res/casters.png");
		
		targetA = new FBO(SIZE, SIZE);
		targetB = new FBO(SIZE, SIZE);
		occlusionMap = new FBO(SIZE, 1);
		
		final String PIXEL_BIAS_UNIFORM = "pixelBias";
		final String LIGHT_SIZE_UNIFORM = "renderTargetSize";
		
		ShaderProgram.setStrictMode(false);
		final String VERT = "res/shader/lights2/pass.vert";
		polar2rect = createShader(VERT, "res/shader/lights2/polar2rect.frag");
		rect2polar = createShader(VERT, "res/shader/lights2/rect2polar.frag");

		lightmapShader = createShader(VERT, "res/shader/lights2/lightmap.frag");
		lightmapShader.setUniform1f(PIXEL_BIAS_UNIFORM, 4/(float)SIZE);
		
		hblur = createShader(VERT, "res/shader/lights2/hblur.frag");
		hblur.setUniform1f(LIGHT_SIZE_UNIFORM, SIZE);
		
		vblur = createShader(VERT, "res/shader/lights2/vblur.frag");
		vblur.setUniform1f(LIGHT_SIZE_UNIFORM, SIZE);
		
		ShaderProgram.unbindAll();
		
		lights = new ArrayList<Light>();
		lights.add(new Light(50, 50, Color.white));
		lights.add(new Light(100, 100, Color.white));
		lights.add(new Light(150, 50, Color.white));
		lights.add(new Light(225, 450, Color.white));
		lights.add(new Light(0f, 0f, Color.white));
	}
	
	ShaderProgram createShader(String vert, String frag) throws SlimException {
		ShaderProgram prog = ShaderProgram.load("res/shader/lights2/pass.vert", "res/shader/lights2/polar2rect.frag");
		prog.bind();
		prog.setUniform1i("tex0", 0);
		return prog;
	}
	
	@Override
	public void render() throws SlimException {
		batch.setColor(Color.white);
		
		//render each light directly to the screen
		for (int i=0; i<lights.size(); i++) {
			renderLight(lights.get(i), null);
		}
		
		//render the shadow casters
		batch.drawImage(img);
			
		batch.flush();
	}
	
	/**
	 * Renders the given light to the FBO or to the screen.
	 * 
	 * <i>Note:</i> If the target is null (i.e. screen), then the 
	 * final batch draw will not be flushed.
	 * 
	 * @param light the light to render
	 * @param target the target FBO, or null to render to screen
	 */
	public void renderLight(Light l, FBO target) {
		Color c = l.tint;
		float x = l.x;
		float y = l.y;
		
		//Render shadow casters image to target A FBO
		float subX = SIZE/2f-x;
		float subY = SIZE/2f-y;
//		
//		//Draw a sub-region of our scene
//		drawPass(targetA, null, img, subX, subY, SIZE, SIZE, Color.white, true);
//		
//		//Polarize our region
//		drawPass(targetB, polar2rect, targetA.getImage(), 0, 0, Color.white, false);
//		
//		//Draw occlusion map
//		drawOcclusionMap(occlusionMap, targetB.getImage());
//
//		//Draw light map from occlusion map
//		lightmapShader.bind();
//		lightmapShader.setUniform4f(TINT_UNIFORM, c);
//		drawPass(targetA, null, occlusionMap.getImage(), 
//				0, 0, regionSize, regionSize, c, true);
//		lightmapShader.unbind();
//		
////		//Un-polarize
//		drawPass(targetB, rect2polar, targetA.getImage(), 0, 0, c, true);
////		
////		//Blur the image horizontally
//		drawPass(targetA, hblur, targetB.getImage(), 0, 0, c, true);
////		
//		batch.setColor(c);
////		//bake the light onto the Light's region
//		drawPass(l.fbo, vblur, targetA.getImage(), 0, 0, c, true);
//		batch.setColor(Color.white);
	}
	
	private void drawPass(FBO target, ShaderProgram shader, Image image,
			float x, float y, float w, float h, Color clearColor) {
		target.bind();
		if (clearColor!=null) {
			GL2D.setBackground(clearColor);
			GL2D.clear();
		}
		if (shader != null)
			shader.bind();
		batch.drawImage(image, x, y, w, h);
		batch.flush();
		if (shader != null)
			shader.unbind();
		target.unbind();
	}
	
	@Override
	public void update(int delta) throws SlimException {
	}
	
	public void handleEvent(Event e) {
		if (e.isMouseEvent()) {
			mx = e.getMouseX();
			my = e.getMouseY();
			if (!lights.isEmpty()) {
				Light l = lights.get(lights.size()-1);
				l.x = mx;
				l.y = my;
			}
		}
	}
	
	@Override
	public URL getThemeURL() throws SlimException {
		return Utils2.getResource("res/gui/chutzpah/chutzpah.xml");
	}
}
