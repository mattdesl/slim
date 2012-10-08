package slim.test.lighting1;

import java.net.URL;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import slim.g2d.FBO;
import slim.g2d.Image;
import slim.shader2.ShaderProgram;
import slim.test.bare.GUITestBase;
import slim.texture.Texture;
import slim.texture.Texture2D;
import slim.util2.Utils2;
import slimold.SlimException;
import slimold.SpriteBatch;
import de.matthiasmann.twl.Event;
import de.matthiasmann.twl.renderer.Font;
import de.matthiasmann.twl.renderer.lwjgl.LWJGLFont;

public class LightingTest extends GUITestBase {
	public static void main(String[] args) throws SlimException {
		new LightingTest().start();
	}
	
	Image img;
	Texture2D imgTex;
	SpriteBatch batch;
	ShaderProgram unwrapShader, lightMapShader, wrapShader, blurHShader, blurVShader;
	
	FBO subFBO, unwrapFBO, occlusionMapFBO, lightMapFBO, wrappedMapFBO, blurFBO;
	
	Image subImage, unwrapImage, occlusionMapImage, lightMapImage, wrappedMapImage, blurImage;
	
	private boolean useShader = false;
	private float scale = 1f;
	
	private Image lightPoint;
	private float lightX, lightY;
	private final int RTSIZE = 256; 
	private Font defaultFont;
	
	public void drawString(String str, int x, int y) {
		if (defaultFont!=null) {
			batch.flush();
			Texture.clearLastBind();
			//renderer.startRendering();
			defaultFont.drawText(null, x, y, str);
			//renderer.endRendering();
		}
	}
	
	@Override
	public void init() throws SlimException {
		init2D();
		
		defaultFont = theme!=null ? theme.getDefaultFont() : null;
		
		GL11.glClearColor(0.2f, 0.2f, 0.2f, 1f);
		
		lightPoint = new Image("res/light.png");
		
		img = new Image("res/fx.png");
		imgTex = img.getTexture();
		imgTex.setFilter(Texture.FILTER_NEAREST);
		
		batch = new SpriteBatch();
		
		
		ShaderProgram.setStrictMode(false);
		reloadShaders();

		subFBO = new FBO(RTSIZE, RTSIZE);
		subFBO.getTexture().setFilter(Texture.FILTER_NEAREST);
		subImage = new Image(subFBO.getTexture());
		
		unwrapFBO = new FBO(RTSIZE, RTSIZE);
		unwrapFBO.getTexture().setFilter(Texture.FILTER_NEAREST);
		unwrapImage = new Image(unwrapFBO.getTexture());
		
		occlusionMapFBO = new FBO(RTSIZE, 1);
		occlusionMapFBO.getTexture().setFilter(Texture.FILTER_NEAREST);
		
		occlusionMapImage = new Image(occlusionMapFBO.getTexture());
		
		lightMapFBO = new FBO(RTSIZE, RTSIZE);
		lightMapFBO.getTexture().setFilter(Texture.FILTER_LINEAR);
		lightMapImage = new Image(lightMapFBO.getTexture());
		
		wrappedMapFBO = new FBO(RTSIZE, RTSIZE);
		wrappedMapFBO.getTexture().setFilter(Texture.FILTER_LINEAR);
		wrappedMapImage = new Image(wrappedMapFBO.getTexture());
		
		blurFBO = new FBO(RTSIZE, RTSIZE);
		blurFBO.getTexture().setFilter(Texture.FILTER_LINEAR);
		blurImage = new Image(blurFBO.getTexture());
		
		//updateLights();
	}
	
	public void updateLights() {
		updateSubImage();
		unwrap();
		drawOcclusionMap();
		drawLightMap();
		wrapLightMap();
		blurHorizontally();
		blurVertically();
	}
	
	public void blurVertically() {
		wrappedMapFBO.bind();
		GL11.glClearColor(1f,1f,1f,0f);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
		blurVShader.bind();
		batch.drawImage(blurImage);
		batch.flush();
		blurVShader.unbind();
		wrappedMapFBO.unbind();
	}
	
	public void blurHorizontally() {
		//blur the wrapped image
		blurFBO.bind();
		GL11.glClearColor(1f,1f,1f,0f);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
		blurHShader.bind();
		batch.drawImage(wrappedMapImage);
		batch.flush();
		blurHShader.unbind();
		blurFBO.unbind();
	}
	
	public void updateSubImage() {
		subFBO.bind();
		renderer.setViewport(0, 0, subFBO.getWidth(), subFBO.getHeight());
		GL11.glClearColor(1f,1f,1f,0f);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
		batch.drawImage(img, RTSIZE/2f-lightX, RTSIZE/2f-lightY);

		batch.flush();
		subFBO.unbind();
		renderer.setViewport(0, 0, getWidth(), getHeight());
	}

	public void unwrap() {
		unwrapFBO.bind();
		unwrapShader.bind();
		GL11.glClearColor(1f,1f,1f,0f);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
		batch.drawImage(subImage);
		batch.flush();
		unwrapShader.unbind();
		unwrapFBO.unbind();
	}
	
	private void drawOcclusionMap() {
		occlusionMapFBO.bind();
		
		//clear it to white; i.e. start with max 1.0
		GL11.glClearColor(1f,1f,1f,1f);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
		
		//set blending to min
		GL11.glEnable(GL11.GL_BLEND);
		GL14.glBlendEquation(GL14.GL_MIN);
		GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);
		
		//draw each line to pix 1
		for (int y=0; y<unwrapFBO.getHeight(); y++) {
			batch.drawSubImage(unwrapImage, 0, y, unwrapFBO.getWidth(), 1, 0, 0, unwrapFBO.getWidth(), 1, null);
		}
		batch.flush();
		
		//reset blending
		GL14.glBlendEquation(GL11.GL_ADD);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		occlusionMapFBO.unbind();
	}
	
	private void drawLightMap() {
		lightMapFBO.bind();
		GL11.glClearColor(1f,1f,1f,0f);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
		lightMapShader.bind();
		batch.drawImage(occlusionMapImage, 0, 0, imgTex.getWidth(), imgTex.getHeight());
		batch.flush();
		lightMapShader.unbind();
		lightMapFBO.unbind();
	}
	
	private void wrapLightMap() {
		wrappedMapFBO.bind();
		GL11.glClearColor(1f,1f,1f,0f);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
		wrapShader.bind();
		batch.drawImage(lightMapImage, 0, 0, imgTex.getWidth(), imgTex.getHeight());
		batch.flush();
		wrapShader.unbind();
		wrappedMapFBO.unbind();
	}
	
	ShaderProgram shdr(ShaderProgram old, String vert, String frag) {
		try {
			ShaderProgram prog;
			if (old!=null)
				old.release();
			prog = ShaderProgram.load(vert, frag);
			if (prog.getLog()!=null && prog.getLog().length()!=0)
				System.out.println(prog.getLog());
			prog.bind();
			prog.setUniform1f("renderTargetSize", 256);
			prog.setUniform1i("tex0", 0);
			prog.unbind();
			return prog;
		} catch (SlimException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private void reloadShaders() {
		unwrapShader = shdr(unwrapShader, "res/shader/pass.vert", "res/shader/polar.frag");
		lightMapShader = shdr(lightMapShader, "res/shader/pass.vert", "res/shader/lightmap.frag");
		wrapShader = shdr(wrapShader, "res/shader/pass.vert", "res/shader/rect2polar.frag");
		blurHShader = shdr(blurHShader, "res/shader/pass.vert", "res/shader/hblur.frag");
		blurVShader = shdr(blurVShader, "res/shader/pass.vert", "res/shader/vblur.frag");
	}

	
	@Override
	public void render() throws SlimException {
		//prog.bind();
		
		batch.drawImage(img, 0, 0);
		
		batch.drawImage(unwrapImage, 256, 0);
		
		
		batch.drawImage(lightMapImage, 0, 256);
		
		batch.drawImageScaled(wrappedMapImage, 256+lightX-RTSIZE/2f, 256+lightY-RTSIZE/2f, scale);
		batch.drawImageScaled(img, 256, 256, scale);
		
		batch.drawImage(lightPoint, lightX-lightPoint.getWidth()/2f, lightY-lightPoint.getHeight()/2f);
		batch.drawImage(subImage, 512, 256);
		batch.drawImage(occlusionMapImage, 512, 5);
		batch.flush();
	}
	
	public void handleEvent(Event e) {
		if (e.isKeyEvent() && e.getKeyCode()==Event.KEY_SPACE) {
			reloadShaders();
			updateLights();
		} else if (e.isKeyEvent() && e.getKeyCode()==Event.KEY_A) {
			unwrap();
		}
		
		if (e.isMouseEvent() && e.getType()==Event.Type.MOUSE_MOVED) {
			lightX = e.getMouseX();
			lightY = e.getMouseY();
			updateLights();
		}
	}
	
	@Override
	public void update(int delta) throws SlimException {
		
	}

	@Override
	public URL getThemeURL() throws SlimException {
		return Utils2.getResource("res/gui/chutzpah/chutzpah.xml");
	}
}
