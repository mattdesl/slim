package slim.g2d;

import java.util.HashMap;

import org.lwjgl.BufferUtils;
import org.lwjgl.util.vector.Matrix4f;

import slim.MathUtil;
import slim.SlimException;
import slim.shader.ShaderProgram;
import slim.shader.VertexAttrib;
import slim.shader.VertexAttribs;

/**
 * A simple sprite batch class that uses the following format for
 * sprites:
 * 		x, y, z,    r, g, b, a    u, v
 * 
 * 
 * @author Matt
 */
public class SpriteBatch extends AbstractBatch {
	
	public static final VertexAttrib DEFAULT_POSITION = new VertexAttrib("Position", 2);
	public static final VertexAttrib DEFAULT_COLOR = new VertexAttrib("Color", 4);
	public static final VertexAttrib DEFAULT_TEXCOORD0 = new VertexAttrib("TexCoord", 2);
	public static final VertexAttribs DEFAULT_ATTRIBS = new VertexAttribs(DEFAULT_POSITION, DEFAULT_COLOR, DEFAULT_TEXCOORD0);
	
	protected static ShaderProgram createDefaultShaderProgram() throws SlimException {
		ShaderProgram prog = ShaderProgram.load("res/shader/batch/sprite.vert", "res/shader/batch/sprite.frag", DEFAULT_ATTRIBS);
		prog.bind();
		Matrix4f projMatrix = MathUtil.toOrtho2D(new Matrix4f(), 0, 0, 800, 600);
		prog.setUniformMatrix4("projMatrix", true, projMatrix);
		prog.setUniform1i("tex0", 0);
		prog.unbind();
		return prog;
	}
	
	/**
	 * Creates a SpriteBatch with a custom shader program. This implementation of SpriteBatch assumes
	 * the following:
	 * <pre>
	 *     - Attribute "Position" (a vec3) has been bound to location 0
	 *     - Attribute "Color" (a vec4) has been bound to location 1
	 *     - Attribute "TexCoord" (a vec2) has been bound to location 2
	 * </pre>
	 * 
	 * These will be used in place of the built-in gl_Vertex, gl_Color, etc.
	 * 
	 * This sprite batch implementation uses triangles to render sprites, so a simple
	 * image will consist of 6 vertices, each with interleaved vertex data that looks like this:
	 *   x, y, z    r, g, b, a     u, v
	 *   
	 * 
	 * 
	 * @param verts the number of vertices this batch will hold 
	 * @param program the number 
	 */
	public SpriteBatch(int verts, ShaderProgram program) {
		int comps = 0;
		for (VertexAttrib a : attribs) {
			comps += a.numComponents;
		}
		this.vertexData = BufferUtils.createFloatBuffer(bcomps * verts);
	}
	
	public SpriteBatch(int verts) throws SlimException {
		this(verts, createDefaultShaderProgram());
	}
	
	public SpriteBatch() throws SlimException {
		this(1000);
	}
	
	public void render() {
		
	}
}
