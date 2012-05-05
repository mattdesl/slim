package slim.test.lighting1;

import java.net.URL;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import slim.FBO;
import slim.GL2D;
import slim.Image2D;
import slim.SlimException;
import slim.SpriteBatch;
import slim.easing.Easing;
import slim.easing.SimpleFX;
import slim.shader.ShaderProgram;
import slim.test.GUITestBase;
import slim.texture.Texture;
import slim.util.Utils;
import de.matthiasmann.twl.Event;

public class ShaderLightingTest extends GUITestBase {
	public static void main(String[] args) throws SlimException {
		new ShaderLightingTest(800, 600, false).start();
	}
	
	private Image2D shadowCasters; //default shadow casters
	private Image2D boxTool; //a 'brush' tool to add boxes to the scene
	
	private FBO screenFBO;
	private SpriteBatch batch;
	
	private int paintMode = 2; //1 - sprites, 2 - lights
	
	private int mouseX, mouseY;
	
	private ShaderLighting lightManager;
	private ShaderLighting.Light lastLight;
	
	private SimpleFX fx = new SimpleFX(-1f, 1f, 2000, Easing.SINE_IN_OUT);
	
	public ShaderLightingTest(int w, int h, boolean f) { 
		super(w, h, f); 
	}
	
	@Override
	public void init() throws SlimException {
		init2D();
		setTargetFPS(-1);
		setClearEachFrame(false);
		
		GL11.glClearColor(0.2f, 0.3f, 0.2f, 1f);
		
		batch = new SpriteBatch();
		
		shadowCasters = new Image2D("res/casters.png");
		boxTool = new Image2D("res/box1.png");
		
		screenFBO = new FBO(getWidth(), getHeight(), Texture.FILTER_NEAREST);
		
		resetShadowCasters();
		ShaderProgram.setStrictMode(false);
		
		lightManager = new ShaderLighting(batch, screenFBO.getImage(), 256);
		resetLights();
	}
	
	void resetLights() throws SlimException {
		lightManager.clear();
		lastLight = lightManager.createLight(mouseX, mouseY, Utils.rndColor());
	}
	
	void resetShadowCasters() {
		screenFBO.bind();
		GL2D.clear();
		batch.drawImage(shadowCasters);
		batch.flush();
		screenFBO.unbind();
	}
	
	@Override
	public void render() throws SlimException {
		GL2D.clear();
		//render our shadow casters
		batch.resetTranslation();
		
		batch.drawImage(screenFBO.getImage());
		
		if (paintMode == 1) {
			batch.drawImage(boxTool, mouseX-boxTool.getWidth()/2f, mouseY-boxTool.getHeight()/2f);
		}
		
		lightManager.draw();
		
//		batch.drawImageScaled(lightManager.targetA.getImage(), 0, 0, 1f);
//		batch.drawImageScaled(lightManager.targetB.getImage(), 256, 0, 1f);
		
		batch.flush();
	}
	
	public void paintBox() {
		screenFBO.bind();
		batch.drawImage(boxTool, mouseX-boxTool.getWidth()/2f, mouseY-boxTool.getHeight()/2f);
		batch.flush();
		screenFBO.unbind();
		//update lights
	}
	
	public void handleEvent(Event e) {
		if (e.isKeyEvent()) {
			if (e.getKeyCode()==Event.KEY_1) {
				paintMode = 1;
			} else if (e.getKeyCode()==Event.KEY_2) {
				paintMode = 2;
			}
		}
		
		if (e.isMouseEvent()) {
			mouseX = e.getMouseX();
			mouseY = e.getMouseY();
			if (e.getType()==Event.Type.MOUSE_MOVED) {
				if (paintMode==2) {
					if (lastLight!=null)
						lastLight.setPosition(mouseX, mouseY);
				}
			} else if (e.getType()==Event.Type.MOUSE_BTNDOWN) {
				if (paintMode==1)
					paintBox();
				else if (paintMode==2) {
					try {
						lastLight = lightManager.createLight(mouseX, mouseY, Utils.rndColor());
					} catch (SlimException ex) {
						ex.printStackTrace();
					}
				}
			}
		}
	}
	
	@Override
	public void update(int delta) throws SlimException {
		for (int i=0; i<lightManager.size(); i++) {
			lightManager.getLightAt(i).translate(delta * .05f * fx.getValue(), 0);
		}
		lightManager.update(delta);
		fx.update(delta);
		if (fx.finished()) {
			fx.flip();
			fx.restart();
		}
		Display.setTitle(String.valueOf(getFPS()));
	}

	@Override
	public URL getThemeURL() throws SlimException {
		return Utils.getResource("res/gui/chutzpah/chutzpah.xml");
	}
}
