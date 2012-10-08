package slim.game;

import java.util.Arrays;
import java.util.List;

import org.lwjgl.util.vector.Matrix4f;

import slim.core.SlimException;
import slim.core.VertexArray;
import slim.core.VertexAttrib;
import slim.core.VertexData;
import slim.shader.ShaderProgram;
import slim.util.MathUtil;

/**
 * A simple implementation of a sprite batch using vertex 
 * arrays and specifying {x, y, r, g, b, a, u, v} coordinates.
 * 
 * The vertex attributes of the shader are expected in the following format:
 * <pre>
 *     (location=0) Position vec2;
 *     (location=1) Color vec4;
 *     (location=2) Texture vec2;
 * </pre>
 * 
 * The fragment shader is expected to have a "tex0" sampler2D and the vertex shader
 * requires a "projMatrix" mat4 uniform.
 * 
 * @author davedes
 */
public class SpriteRenderer {
	
	protected VertexData data;
	protected ShaderProgram program;
	
	public static final String TEXCOORD_0 = "tex0";
	public static final String PROJECTION_MATRIX = "projMatrix";
	
	protected static final String DEFAULT_VERT_SHADER = "uniform mat4 "+PROJECTION_MATRIX+";\n" +
			"attribute vec4 Color;\n" + 
			"attribute vec2 TexCoord;\n" + 
			"attribute vec2 Position;\n" +
			"varying vec4 vColor;\n" + 
			"varying vec2 vTexCoord; \n" + 
			"void main() {\n" + 
			"	vColor = Color;\n" + 
			"	vTexCoord = TexCoord;\n" + 
			"	gl_Position = "+PROJECTION_MATRIX+" * vec4(Position.xy, 0, 1);\n" + 
			"}";
	
	protected static final String DEFAULT_FRAG_SHADER = "uniform sampler2D "+TEXCOORD_0+";\n" + 
			"varying vec4 vColor;\n" + 
			"varying vec2 vTexCoord;\n" +
			"void main(void) {\n" + 
			"	vec4 texColor = texture2D("+TEXCOORD_0+", vTexCoord);\n" + 
			"	gl_FragColor = vColor * texColor;\n" + 
			"}";
	
	protected static final List<VertexAttrib> DEFAULT_ATTRIBUTES = Arrays.asList(
			new VertexAttrib(0, "Position", 2),
			new VertexAttrib(1, "Color", 4),
			new VertexAttrib(2, "TexCoord", 2));

	
	public SpriteRenderer(ShaderProgram program, VertexData data) {
		this.program = program;
		this.data = data;
	}
	
	public SpriteRenderer(int width, int height, int capacity) throws SlimException {
		program = new ShaderProgram(DEFAULT_VERT_SHADER, DEFAULT_FRAG_SHADER, DEFAULT_ATTRIBUTES);
		Matrix4f projMatrix = MathUtil.toOrtho2D(new Matrix4f(), 0, 0, width, height);
		program.bind();
		program.setUniform(TEXCOORD_0, 0);
		program.setUniformMatrix4(PROJECTION_MATRIX, true, projMatrix);
		
		data = new VertexArray(DEFAULT_ATTRIBUTES, capacity * 6);
	}
}
