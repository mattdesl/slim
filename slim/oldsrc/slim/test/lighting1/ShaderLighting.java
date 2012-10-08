package slim.test.lighting1;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import slim.g2d.FBO;
import slim.g2d.Image;
import slim.shader2.ShaderProgram;
import slim.texture.Texture;
import slimold.Color;
import slimold.GL2D;
import slimold.SlimException;
import slimold.SpriteBatch;

public class ShaderLighting {

	Image shadowCasters;
	List<Light> lights = new ArrayList<Light>();
	
	FBO targetA, occlusionMap, targetB;
	private int regionSize;
	
	public static final int DEFAULT_PIXEL_BIAS = 6;

	public static final String REGION_SIZE_UNIFORM = "renderTargetSize";
	public static final String PIXEL_BIAS_UNIFORM = "pixelBias";
	public static final String TEXTURE0_UNIFORM = "tex0";
	public static final String TINT_UNIFORM = "tint";
	
	private ShaderProgram polar2rect, rect2polar, lightmapShader, hblur, vblur;
	private SpriteBatch batch;
	
	private boolean blurring = false;
	
	public ShaderLighting(SpriteBatch batch, Image shadowCasters, int regionSize, int pixelBias) throws SlimException {
		this.batch = batch;
		this.shadowCasters = shadowCasters;
		this.regionSize = regionSize;
		this.targetA = new FBO(regionSize, regionSize, Texture.FILTER_LINEAR);
		this.targetB = new FBO(regionSize, regionSize, Texture.FILTER_LINEAR);
		this.occlusionMap = new FBO(regionSize, 1, Texture.FILTER_NEAREST);
		
		final String VERT_PASS = "res/shader/lights/pass.vert";
		
		polar2rect = initShader(VERT_PASS, "res/shader/lights/polar2rect.frag");
		rect2polar = initShader(VERT_PASS, "res/shader/lights/rect2polar.frag");
		lightmapShader = initShader(VERT_PASS, "res/shader/lights/lightmap.frag");
		lightmapShader.bind();
		lightmapShader.setUniform1f(PIXEL_BIAS_UNIFORM, pixelBias/(float)regionSize);
		lightmapShader.setUniform4f(TINT_UNIFORM, Color.white);
		lightmapShader.unbind();
		hblur = initShader(VERT_PASS, "res/shader/lights/hblur.frag");
		vblur = initShader(VERT_PASS, "res/shader/lights/vblur.frag");
		hblur.bind();
		hblur.setUniform1f(REGION_SIZE_UNIFORM, regionSize);
		vblur.bind();
		vblur.setUniform1f(REGION_SIZE_UNIFORM, regionSize);
	}
	
	public ShaderLighting(SpriteBatch batch, Image shadowCasters, int regionSize) throws SlimException {
		this(batch, shadowCasters, regionSize, DEFAULT_PIXEL_BIAS);
	}
	
	public int size() {
		return lights.size();
	}
	
	public Light getLightAt(int i) {
		return lights.get(i);
	}
	
	protected ShaderProgram initShader(String vert, String frag) {
		try {
			ShaderProgram prog = ShaderProgram.load(vert, frag);
			if (prog.getLog().length()!=0)
				System.out.println(prog.getLog());
			prog.bind();
			prog.setUniform1i(TEXTURE0_UNIFORM, 0);
			prog.unbind();
			return prog;
		} catch (SlimException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public Light createLight(int x, int y, Color color) throws SlimException {
		return createLight(x, y, color, null);
	}
	
	public Light createLight(int x, int y, Color color, FBO sharedFBO) throws SlimException {
		Light l = new Light(color, sharedFBO);
		l.x = x;
		l.y = y;
		lights.add(l);
		return l;
	}
	
	public boolean removeLight(Light l) {
		return lights.remove(l);
	}
	
	public void clear() {
		lights.clear();
	}
	
	public void update(int delta) {
		for (int i=0; i<lights.size(); i++) {
			Light l = lights.get(i);
			if (l.isDirty()) {
				updateLight(l);
			}
		}
	}
	
	public void updateAllLights() {
		for (int i=0; i<lights.size(); i++) {
			Light l = lights.get(i);
			updateLight(l);
		}
	}
	
	void updateLight(Light l) {
		Color c = l.color;
		float x = l.x;
		float y = l.y;
		
		//Render shadow casters image to target A FBO
		float subX = regionSize/2f-x;
		float subY = regionSize/2f-y;
		
		//Draw a sub-region of our scene
		drawPass(targetA, null, shadowCasters, subX, subY, Color.white, true);
		
		//Polarize our region
		drawPass(targetB, polar2rect, targetA.getImage(), 0, 0, Color.white, false);
		
		//Draw occlusion map
		drawOcclusionMap(occlusionMap, targetB.getImage());

		//Draw light map from occlusion map
		lightmapShader.bind();
		lightmapShader.setUniform4f(TINT_UNIFORM, c);
		drawPass(targetA, null, occlusionMap.getImage(), 
				0, 0, regionSize, regionSize, c, false);
		lightmapShader.unbind();
		
//		//Un-polarize
		drawPass(targetB, rect2polar, targetA.getImage(), 0, 0, c, false);
//		
//		//Blur the image horizontally
		drawPass(targetA, hblur, targetB.getImage(), 0, 0, c, false);
//		
		batch.setColor(c);
//		//bake the light onto the Light's region
		drawPass(l.fbo, vblur, targetA.getImage(), 0, 0, c, false);
		batch.setColor(Color.white);
		
		l.updated();
	}
	
	void drawOcclusionMap(FBO target, Image image) {
		target.bind();
		
		//clear the background to white, our maximum value
		GL2D.setBackground(Color.white);
		GL2D.clear();
		
		//set blending to min
		GL11.glEnable(GL11.GL_BLEND);
		GL14.glBlendEquation(GL14.GL_MIN);
		GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);
		
		//draw each line to pix 1
		for (int y=0; y<regionSize; y++) {
			batch.drawSubImage(image, 0, y, regionSize, 1, 0, 0, regionSize, 1, null);
		}
		batch.flush();
		
		//reset blending to normal
		GL14.glBlendEquation(GL11.GL_ADD);
		GL2D.resetBlendFunc();
		
		target.unbind();
	}
	
	
	
	private void drawPass(FBO target, ShaderProgram shader,
							Image image, float x, float y,
							Color clearColor, boolean transparent) {
		drawPass(target, shader, image, x, y, image.getWidth(), 
				image.getHeight(), clearColor, transparent);
	}
	
	private void drawPass(FBO target, ShaderProgram shader,
							Image image, float x, float y,
							float w, float h,
							Color clearColor, boolean transparent) {
		target.bind();
		GL2D.setBackground(clearColor, transparent);
		GL2D.clear();
		if (shader!=null)
			shader.bind();
		batch.drawImage(image, x, y, w, h);
		batch.flush();
		if (shader!=null)
			shader.unbind();
		target.unbind();
	}
	
	public void draw() {
		for (int i=0; i<lights.size(); i++) {
			Light l = lights.get(i);
			float subX = l.x-regionSize/2f;
			float subY = l.y-regionSize/2f;
			batch.drawImage(l.fbo.getImage(), subX, subY);
		}
	}
	
//	updateSubImage();
//	unwrap();
//	drawOcclusionMap();
//	drawLightMap();
//	wrapLightMap();
//	blurHorizontally();
//	blurVertically();
	
	public class Light {
		private float x, lastX, y, lastY;
		private Color color;
		private boolean dirty = true;
		private FBO fbo;
		
		public Light(Color color, FBO fbo) throws SlimException {
			this.color = color!=null ? color : new Color(Color.white);
			if (fbo==null)
				fbo = new FBO(regionSize, regionSize, Texture.FILTER_LINEAR);
			this.fbo = fbo;
		}
		
		void updated() {
			lastX = x;
			lastY = y;
			dirty = false;
		}
		
		public boolean isDirty() {
			return dirty;
		}
		
		public void translate(float x, float y) {
			setPosition(this.x+x, this.y+y);
		}
		
		public void setPosition(float x, float y) {
			this.x = x; 
			this.y = y;
			dirty = lastX!=x || lastY!=y;
		}
	}
}
