package slim.test;

import java.io.IOException;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import slim.app.App;
import slim.app.DesktopApp;
import slim.core.SlimException;
import slim.game.Game2D;
import slim.shader.ShaderProgram;
import slim.texture.Texture;
import slim.texture.Texture2D;
import slim.util.GLUtil;
import slim.util.Utils;

public class Normal2DTest extends Game2D {

	public static void main(String[] args) {
		try {
			new DesktopApp(new Normal2DTest(), 1024, 600, false).start();
		} catch (SlimException e) {
			e.printStackTrace();
		}
	}
	
	Texture2D color;
	Texture2D normals;
	ShaderProgram program;
	
	Vector4f lightDiffuse = new Vector4f(1f, 1f, 1f, 1f);
	Vector3f lightPos = new Vector3f(0f, 0f, 2f);
	Vector3f ncoords = new Vector3f(0.0f, 0.0f, 1.0f);
	boolean enabled = true;
	
	@Override
	public void create(App app) throws SlimException {
		app.setTargetFPS(60);
		Display.setVSyncEnabled(true);
		try {
			color = Texture2D.loadTexture(Utils.getResource("res/images.png"));
			normals = Texture2D.loadTexture(Utils.getResource("res/images_n.png"));
		} catch (IOException e) {
			e.printStackTrace();
			app.quit();
			return;
		}
		
		Texture.clearLastBind();
		ShaderProgram.setStrictMode(false);
		program = ShaderProgram.load("res/shader/normtest.vert", "res/shader/normtest.frag");
		program.bind();
		program.setUniform("tex0", 0);
		program.setUniform("tex1", 1);
	    program.setUniform("light", lightPos);
	}

	@Override
	public void destroy(App app) throws SlimException {
		color.destroy();
		normals.destroy();
	}

	@Override
	public void render(App app) throws SlimException {
		GLUtil.clear();
		
		Texture.enable(Texture.TEXTURE_2D);
		
		glActiveTexture(GL_TEXTURE0);
		color.bind();
		
		if (enabled) {
		    glActiveTexture(GL_TEXTURE1);
			normals.bind();
		    
		    program.setUniform("light", lightPos);
		}
	    
		float normWidth = color.getNormalizedWidth();
		float normHeight = color.getNormalizedHeight();
		GLUtil.rect(0, 0, color.getWidth(), color.getHeight(), 0, 0, normWidth, normHeight);
		
	}
	
	int timer = 0;
	int delay = 2000;
	
	@Override
	public void update(App app, int delta) throws SlimException {
		timer += delta;
		
		while (Keyboard.next()) {
			Keyboard.poll();
			int key = Keyboard.getEventKey();
			char c = Keyboard.getEventCharacter();
			boolean hit = Keyboard.getEventKeyState();
			if (key==Keyboard.KEY_SPACE && hit) {
				program.unbind();
				program.release();
				program = ShaderProgram.load("res/shader/normtest.vert", "res/shader/normtest.frag");
				program.bind();
				program.setUniform("tex0", 0);
				program.setUniform("tex1", 1);
			    program.setUniform("light", lightPos);
			}
		}
		
//		if (timer > delay) {
//			enabled = !enabled;
//			if (enabled)
//				program.bind();
//			else
//				program.unbind();
//			System.out.println("Lighting is now: "+enabled);
//			timer = 0;
//		}
		
		lightPos.x = Mouse.getX() / (float)app.getWidth();
		lightPos.y = Mouse.getY() / (float)app.getHeight();
	}

}
