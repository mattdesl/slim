package slim.g2d;

import org.lwjgl.util.vector.Matrix4f;

import slim.MathUtil;
import slim.shader.ShaderProgram;

/**
 * A simple sprite batch class that uses the following format for
 * sprites:
 * 		x, y, z,    r, g, b, a    u, v
 * 
 * 
 * @author Matt
 */
public class SpriteBatch extends AbstractBatch {
	
	static ShaderProgram createDefaultShaderProgram() throws SlickException {
		ShaderProgram prog = ShaderProgram.load("res/shader/batch/sprite.vert", "res/shader/batch/sprite.frag");
		prog.bind();
		Matrix4f projMatrix = MathUtil.toOrtho2D(new Matrix4f(), 0, 0, 800, 600);
		prog.setUniformMatrix4("projMatrix", true, projMatrix);
		prog.setUniform1i("tex0", 0);
		prog.unbind();
		
		return prog;
	}
	
	/**
	 * 
	 * @param verts
	 * @param program
	 */
	public SpriteBatch(int verts, ShaderProgram program) {
		
	}
	
	public SpriteBatch(int verts) throws SlickException {
		this(verts, createDefaultShaderProgram());
	}
	
	public SpriteBatch() throws SlickException {
		this(verts * (3 + 4 + 2));
	}
}
