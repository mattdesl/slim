package slim.g2d;

import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector4f;

import slim.shader2.ShaderProgram;
import slim.shader2.VertexAttrib;
import slim.texture.Texture;
import slimold.Color;
import slimold.MathUtil;
import slimold.SlimException;

/**
 * A simple sprite batch class that uses the following format for
 * sprites:
 * <pre>		x, y,    r, g, b, a    u, v</pre>
 * 
 * Attributes should be defined specifically as such in the shader.
 * 
 * Version 330 or later would look like this:
 * <pre>layout(location=0) in vec2 Position;
 * layout(location=1) in vec4 Color;
 * layout(location=2) in vec2 TexCoord;</pre>
 * 
 * For older GLSL versions, it might look like this:
 * <pre>attribute vec2 Position;
 * attribute vec4 Color;
 * attribute vec2 TexCoord;</pre>
 * 
 * For older GLSL versions, you need to pass a list of VertexAttribs to the
 * shader in order to explicitly bind locations.
 * 
 * 
 * 
 * @author davedes
 */
public class SpriteBatch extends AbstractBatch {
	
	protected static ShaderProgram createDefaultShaderProgram() throws SlimException {
		List<VertexAttrib> attribs = Arrays.asList(
				new VertexAttrib(0, "Position", 2),
				new VertexAttrib(1, "Color", 4),
				new VertexAttrib(2, "TexCoord", 2));
		ShaderProgram prog = ShaderProgram.load(
				"res/shader/batch/sprite.vert", 
				"res/shader/batch/sprite.frag", 
				attribs);
		prog.bind();
		Matrix4f projMatrix = MathUtil.toOrtho2D(new Matrix4f(), 0, 0, 800, 600);
		prog.setUniformMatrix4("projMatrix", true, projMatrix);
		prog.setUniform1i("tex0", 0);
		prog.unbind();
		return prog;
	}
	
	protected Texture texture;
	protected Color currentColor = new Color(Color.white);
	protected int vertCount;
	
	public SpriteBatch(int vertCount, ShaderProgram program) {
		if (vertCount < 3)
			throw new IllegalArgumentException("SpriteBatch uses triangles so vertCount must be > 3");
		useShaderProgram(program);
		this.program.setUniformMatrix4("viewMatrix", true, viewMatrix);
		
		this.vertCount = vertCount;
		vertexData = BufferUtils.createFloatBuffer(vertCount * getTotalNumComponents());
	}
	
	public SpriteBatch(int vertCount) throws SlimException {
		this(vertCount, createDefaultShaderProgram());
	}
	
	public SpriteBatch() throws SlimException {
		this(6 * 1000);
	}

	protected void render() {
		//bind the last texture
		if (texture!=null) {
			Texture.enable(texture.getTarget());
			texture.bind();
		}
		super.render();
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
	public FloatBuffer vertex(Vector4f transformed, Color color, float u, float v) {
		Color c = color!=null ? color : currentColor;
		put(transformed.x).put(transformed.y).put(c.r).put(c.g).put(c.b).put(c.a).put(u).put(v);
		idx++;
		return vertexData;
	}
	
	private void checkRender(Image s) {
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
}