package slim.game;

import java.util.Arrays;
import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector4f;

import slim.core.Color;
import slim.core.SlimException;
import slim.core.VertexArray;
import slim.core.VertexAttrib;
import slim.core.VertexData;
import slim.shader.ShaderProgram;
import slim.texture.Texture;
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
public class SimpleGfx implements Gfx {
	
	protected VertexData data;
	protected ShaderProgram program;
	
	public static final String TEXCOORD_0 = "tex0";
	public static final String PROJECTION_MATRIX = "projMatrix";
	
	public static final String ATTR_COLOR = "Color";
	public static final String ATTR_POSITION = "Position";
	public static final String ATTR_TEXCOORD = "TexCoord";
	
	protected static final String DEFAULT_VERT_SHADER = "uniform mat4 "+PROJECTION_MATRIX+";\n" +
			"attribute vec4 "+ATTR_COLOR+";\n" + 
			"attribute vec2 "+ATTR_TEXCOORD+";\n" + 
			"attribute vec2 "+ATTR_POSITION+";\n" +
			"varying vec4 vColor;\n" + 
			"varying vec2 vTexCoord; \n" + 
			"void main() {\n" + 
			"	vColor = "+ATTR_COLOR+";\n" + 
			"	vTexCoord = "+ATTR_TEXCOORD+";\n" + 
			"	gl_Position = "+PROJECTION_MATRIX+" * vec4("+ATTR_POSITION+".xy, 0, 1);\n" + 
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
	
	protected Texture texture;
	protected int idx;
	protected int geometryType = GL11.GL_TRIANGLES;
	protected Color currentColor;
	
	public SimpleGfx(ShaderProgram program, VertexData data) {
		this.program = program;
		this.data = data;
	}
	
	public SimpleGfx(int width, int height, int capacity) throws SlimException {
		program = new ShaderProgram(DEFAULT_VERT_SHADER, DEFAULT_FRAG_SHADER, DEFAULT_ATTRIBUTES);
		Matrix4f projMatrix = MathUtil.toOrtho2D(new Matrix4f(), 0, 0, width, height);
		program.bind();
		program.setUniform(TEXCOORD_0, 0);
		program.setUniformMatrix4(PROJECTION_MATRIX, true, projMatrix);
		
		data = new VertexArray(DEFAULT_ATTRIBUTES, capacity * 6);
	}
	
	public SimpleGfx(int width, int height) throws SlimException {
		this(width, height, 1000);
	}
	
	
	
	

	protected void render() {
		//bind the last texture
		if (texture!=null) {
			Texture.enable(texture.getTarget());
			texture.bind();
		}
		data.draw(geometryType, 0, idx);
	}
	
	/**
	 * Places the vertex data into the array, increments the index pointer by one,
	 * then returns the vertexData buffer for chaining. 
	 * 
	 * @param transformed a temp Vector4f containing the transformed position
	 * @param u the U texcoord
	 * @param v the V texcoord
	 * @param color the color for this vertex
	 */
	protected VertexData vertex(Vector4f transformed, Color color, float u, float v) {
		Color c = color!=null ? color : currentColor;
		data.put(transformed.x).put(transformed.y).put(c.r).put(c.g).put(c.b).put(c.a).put(u).put(v);
		idx++;
		return data;
	}
	
	protected void checkRender(Image s) {
		if (s==null || s.getTexture()==null)
			throw new NullPointerException("null texture");
		
		//we need to bind a different texture/type. this is
		//for convenience; ideally the user should order
		//their rendering wisely to minimize texture binds	
		if (s.getTexture()!=texture || idx >= vertCount - 3) {
			//apply the last texture
			flush();
			this.texture = s.getTexture();
		}
	}
	
	public void print() {
		vertexData.flip();
		float[] f = new float[totalNumComponents];
		for (int i=0; i<idx; i++) {
			for (int j=0; j<f.length; j++) 
				f[j] = vertexData.get();
	    	System.out.println(Arrays.toString(f));
		}
//		vertexData.flip();
//		
//		System.out.println(vertexData.get(0)+" "+vertexData.get(1)+" "+vertexData.get(2)+" "+vertexData.get(3)+" "+vertexData.get(4)+" "+vertexData.get(5));
	}
	
	public void drawImageScaled(Image image, float x, float y, float scale) {
		drawImage(image, x, y, image.getWidth()*scale, image.getHeight()*scale);
	}

	public void drawImage(Image image, float x, float y) {
		drawImage(image, x, y, 0);
	}
	
	public void drawImage(Image image, float x, float y, float rotation) {
		drawImage(image, x, y, image.getWidth(), image.getHeight(), rotation);
	}
	

	public void drawImage(Image image, float x, float y, float w, float h) {
		drawImage(image, x, y, w, h, 0);
	}
	
	public void drawImage(Image image, float x, float y, float w, float h, float rotation) {
		drawImage(image, x, y, w, h, rotation, null);
	}
	
	public void drawImage(Image image, float x, float y, float w, float h, float rotation, Color[] corners) {
		float tx = image.getNormalizedXOffset();
		float ty = image.getNormalizedYOffset();
		float tw = image.getNormalizedWidth();
		float th = image.getNormalizedHeight();
		drawImage(image, x, y, w, h, tx, ty, tw, th, rotation, corners);
	}
	
	public void drawImage(Image image, float x, float y, float width, float height, 
						  float u, float v, float uWidth, float vHeight, float rotation,
						  Color[] corners) {
		checkRender(image);
		
		float scaleX = width/image.getWidth();
		float scaleY = height/image.getHeight();
		float cx = image.getCenterX()*scaleX;
		float cy = image.getCenterY()*scaleY;
		
		float tx=0, ty=0;
		
		if (rotation!=0) {
			tx = x;
			ty = y;
			translate(tx, ty);
			x = 0; y = 0;
			rotate(rotation, cx, cy);
		}
		
		this.program.setUniformMatrix4("viewMatrix", true, viewMatrix);
		drawQuad(x, y, u, v, corners!=null ? corners[0] : null,
		 		 x+width, y, u+uWidth, v, corners!=null ? corners[1] : null,
		 		 x+width, y+height, u+uWidth, v+vHeight, corners!=null ? corners[2] : null,
		 		 x, y+height, u, v+vHeight, corners!=null ? corners[3] : null);
		
		if (rotation!=0) {
			rotate(-rotation, cx, cy);
			translate(-tx, -ty);
		}
	}
	
	private void drawQuad(
			float x1, float y1, float u1, float v1, Color c1,   //TOP LEFT 
			float x2, float y2, float u2, float v2, Color c2,   //TOP RIGHT
			float x3, float y3, float u3, float v3, Color c3,   //BOTTOM RIGHT
			float x4, float y4, float u4, float v4, Color c4) { //BOTTOM LEFT
		//top left, top right, bottom left
		vertex(transform(x1, y1, 0), c1, u1, v1);
		vertex(transform(x2, y2, 0), c2, u2, v2);
		vertex(transform(x4, y4, 0), c4, u4, v4);
		//top right, bottom right, bottom left
		vertex(transform(x2, y2, 0), c2, u2, v2);
		vertex(transform(x3, y3, 0), c3, u3, v3);
		vertex(transform(x4, y4, 0), c4, u4, v4);
	}

	public void flush() {
		// TODO Auto-generated method stub
		
	}

	public void draw(Image image, float x, float y, float width,
			float height, float rotation) {
		// TODO Auto-generated method stub
		
	}
}
