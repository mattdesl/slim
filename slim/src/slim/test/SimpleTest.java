package slim.test;

import java.util.Arrays;
import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;

import slim.app.App;
import slim.app.DesktopApp;
import slim.core.SlimException;
import slim.core.VertexArray;
import slim.core.VertexAttrib;
import slim.game.Game2D;
import slim.shader.ShaderProgram;
import slim.util.GLUtil;
import slim.util.MathUtil;
import slim.util.Utils;

//entry point
public class SimpleTest extends Game2D {
	
	public final static DesktopApp APP = new DesktopApp(new SimpleTest());
	
	public static void main(String[] args) {
		try {
			APP.setDisplayMode(800, 600);
			APP.setFullscreen(false);
			APP.setTargetFPS(App.NO_TARGET_FPS);
			APP.start();
		} catch (SlimException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	VertexArray array;
	ShaderProgram program;

	@Override
	public void create(App app) {
		
		
		
		try {
			final String VERTEX = Utils.readFile(Utils.getResourceAsStream("res/shader/sprite/sprite.vert"));
			final String FRAGMENT = Utils.readFile(Utils.getResourceAsStream("res/shader/sprite/sprite.frag"));
			
			//We need to specify the locations for < OpenGL 3.2 
			final List<VertexAttrib> ATTRIBUTES = Arrays.asList(
						new VertexAttrib(0, "Position", 2),
						new VertexAttrib(1, "Color", 4),
						new VertexAttrib(2, "TexCoord", 2));
			
			ShaderProgram.setStrictMode(false);
			program = new ShaderProgram(VERTEX, FRAGMENT, ATTRIBUTES);
			
			Matrix4f projMatrix = MathUtil.toOrtho2D(new Matrix4f(), 0, 0, app.getWidth(), app.getHeight());
			program.bind();
			program.setUniformMatrix4("projMatrix", true, projMatrix);
			program.setUniform("tex0", 0);
			
			array = new VertexArray(ATTRIBUTES, 3);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	@Override
	public void destroy(App app) {
		
	}
	
    
	@Override
	public void render(App app) {
		GLUtil.clear();
//		GLUtil.rect(50, 50, 100, 100);
		
		array.clear();
		
		array.put(25).put(50);
		array.put(1f).put(1f).put(1f).put(1f);
		array.put(0f).put(0f);
		
		array.put(50).put(50);
		array.put(1f).put(1f).put(1f).put(1f);
		array.put(0f).put(0f);
		
		array.put(50).put(150);
		array.put(1f).put(1f).put(1f).put(1f);
		array.put(0f).put(0f);
		
		array.flip();
		
		array.bind();
		array.draw(GL11.GL_TRIANGLES, 0, 3);
		array.unbind();
		
	}

	@Override
	public void update(App app, int delta) {
		APP.setTitle(""+app.getFPS());
	}
	

	
}
