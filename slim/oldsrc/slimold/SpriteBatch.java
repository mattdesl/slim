package slimold;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import slim.g2d.Image;
import slim.texture.Texture2D;
import slim.util2.FastTrig;


/**
 * 
 * @author Matt DesLauriers (davedes)
 */
public class SpriteBatch {

	public static final int ALIGN_LEFT = 0;
	public static final int ALIGN_CENTER = 1;
	public static final int ALIGN_RIGHT = 2;
	 
	/**
	 * Whether to send the image data as GL_TRIANGLES
	 * or GL_QUADS. By default, GL_TRIANGLES is used.
	 * 
	 * @param b true to use triangle rendering
	 */
	public static void setUseTriangles(boolean b) {
		mode = b ? GL11.GL_TRIANGLES : GL11.GL_QUADS;
	}
	
	/**
	 * Returns whether to send the image data as GL_TRIANGLES
	 * or GL_QUADS. By default, GL_TRIANGLES is used.
	 * 
	 * @return true if we are using triangle rendering
	 */
	public static boolean isUseTriangles() {
		return mode==GL11.GL_TRIANGLES;
	}
	
	private static int mode = GL11.GL_TRIANGLES; 
	private final int TOLERANCE = 48; //we assume triangles is in use...
	
	private int idx = 0;
	private Texture2D texture;
	public int renderCalls = 0;
	
	private FloatBuffer vertices, colors, texcoords;
	private int maxVerts;
	private Color currentColor = new Color(Color.white);
	private Color sharedColor = new Color(Color.white);
	
	private float translateX, translateY;
	
	public SpriteBatch() {
		this(1000);
	}
	
	public SpriteBatch(int size) {
		if (size<=0)
			throw new IllegalArgumentException("batch size must be larger than 0");
		this.maxVerts = size;
		vertices = BufferUtils.createFloatBuffer(size * 2);
		colors = BufferUtils.createFloatBuffer(size * 4);
		texcoords = BufferUtils.createFloatBuffer(size * 2);
		
		GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
        GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);
	}
	
	/**
	 * Returns the size of this ImageBatch as given in construction (default 1000).
	 * The internal array will have a capacity of size * 8.
	 * 
	 * A large internal array will require less calls to render(), but will take up
	 * more memory. 
	 * For example, an ImageBatch with a size of 6 would be ideal if we are only
	 * rendering a single image (made up of tris) within begin/end
	 * (six vertices, 8 bytes per vertex -- 2 for XY, 2 for texture UV, 4 for RGBA).
	 * 
	 * However, it's usually better to create a single large-size ImageBatch instance
	 * and re-use it throughout your game.
	 * 
	 * @return how many vertices to expect 
	 */
	public int getSize() {
		return maxVerts;
	}
	
	public void setColor(Color color) {
		this.currentColor = color;
	}
	
	public void setColor(float r, float g, float b, float a) {
		sharedColor.r = r;
		sharedColor.g = g;
		sharedColor.b = b;
		sharedColor.a = a;
		setColor(sharedColor);
	}
	
	public Color getColor() {
		return currentColor;
	}
	
	public void resetTranslation() {
		translateX = translateY = 0;
	}
	
	/** */
	public void translate(float x, float y) {
		translateX += x;
		translateY += y;
	}
	
	public void flush() {
		if (idx>0) 
			render();
		idx = 0;
		texture = null;
		
		vertices.clear();
		texcoords.clear();
		colors.clear();
	}
	
	/**
	 * Sends vertex, color and UV data to the GPU.
	 */
	protected void render() {
		if (idx==0) 
			return;
		renderCalls++;
		//bind the last texture
		if (texture!=null) {
			Texture2D.enable(texture.getTarget());
			texture.bind();
		}
		vertices.flip();
		colors.flip();
		texcoords.flip();
		GL11.glVertexPointer(2, 0, vertices);
		GL11.glColorPointer(4, 0, colors);     
	    GL11.glTexCoordPointer(2, 0, texcoords);
	    GL11.glDrawArrays(mode, 0, idx);
	    vertices.clear();
	    colors.clear();
	    texcoords.clear();
	    idx = 0;
	}
	
	public void drawImageScaled(Image image, float x, float y, float scale) {
		drawImage(image, x, y, image.getWidth()*scale, image.getHeight()*scale);
	}
	
	public void drawImage(Image image) {
		drawImage(image, 0, 0);
	}
	
	public void drawImage(Image image, float x, float y) {
		drawImage(image, x, y, image.getWidth(), image.getHeight());
	}

	public void drawImage(Image image, float x, float y, float w, float h) {
		drawImage(image, x, y, w, h, null);
	}

	public void drawImage(Image image, float x, float y, float rotation) {
		drawImage(image, x, y, rotation, image.getWidth(), image.getHeight(), null);
	}
	
	public void drawImage(Image image, float x, float y, float rotation, float w, float h, Color[] corners) {
		if (rotation==0) {
			drawImage(image, x, y, w, h, corners);
			return;
		}
		
		checkRender(image);
		
		float scaleX = w/image.getWidth();
		float scaleY = h/image.getHeight();
		
		float cx = image.getCenterX()*scaleX;
		float cy = image.getCenterY()*scaleY;

		float p1x = -cx;
		float p1y = -cy;
		float p2x = w - cx;
		float p2y = -cy;
		float p3x = w - cx;
		float p3y = h - cy;
		float p4x = -cx;
		float p4y = h - cy;

		double rad = Math.toRadians(rotation);
		final float cos = (float) FastTrig.cos(rad);
		final float sin = (float) FastTrig.sin(rad);

		float tx = image.getNormalizedXOffset();
		float ty = image.getNormalizedYOffset();
		float tw = image.getNormalizedWidth();
		float th = image.getNormalizedHeight();

		float x1 = (cos * p1x - sin * p1y) + cx; // TOP LEFT
		float y1 = (sin * p1x + cos * p1y) + cy;
		float x2 = (cos * p2x - sin * p2y) + cx; // TOP RIGHT
		float y2 = (sin * p2x + cos * p2y) + cy;
		float x3 = (cos * p3x - sin * p3y) + cx; // BOTTOM RIGHT
		float y3 = (sin * p3x + cos * p3y) + cy;
		float x4 = (cos * p4x - sin * p4y) + cx; // BOTTOM LEFT
		float y4 = (sin * p4x + cos * p4y) + cy;
		drawQuadElement(x+x1, y+y1, tx, ty, corners!=null ? corners[0] : null,
				 		x+x2, y+y2, tx+tw, ty, corners!=null ? corners[1] : null,
				 		x+x3, y+y3, tx+tw, ty+th, corners!=null ? corners[2] : null,
				 		x+x4, y+y4, tx, ty+th, corners!=null ? corners[3] : null);
	}
	
	public void drawImage(Image image, float x, float y, float w, float h, Color[] corners) {
		checkRender(image);
		float tx = image.getNormalizedXOffset();
		float ty = image.getNormalizedYOffset();
		float tw = image.getNormalizedWidth();
		float th = image.getNormalizedHeight();
		drawImage(image, x, y, w, h, tx, ty, tw, th, corners);
	}

	public void drawSubImage(Image image, float srcx, float srcy,
			float srcwidth, float srcheight, float x, float y) {
		drawSubImage(image, srcx, srcy, srcwidth, srcheight, x, y, srcwidth, srcheight);
	}
	
	public void drawSubImage(Image image, float srcx, float srcy,
			float srcwidth, float srcheight, float x, float y, float w, float h) {
		drawSubImage(image, srcx, srcy, srcwidth, srcheight, x, y, w, h, null);
	}

	public void drawSubImage(Image image, float srcx, float srcy,
			float srcwidth, float srcheight, float x, float y, float w,
			float h, Color[] corners) {
		checkRender(image);
		float iw = image.getWidth();
		float ih = image.getHeight();
		float tx = (srcx / iw * image.getNormalizedWidth()) + image.getNormalizedXOffset();
		float ty = (srcy / ih * image.getNormalizedHeight()) + image.getNormalizedYOffset();
		float tw = w / iw * image.getNormalizedWidth();
		float th = h / ih * image.getNormalizedHeight();
		drawQuadElement(x, y, tx, ty, corners != null ? corners[0] : null, x
				+ w, y, tx + tw, ty, corners != null ? corners[1] : null,
				x + w, y + h, tx + tw, ty + th, corners != null ? corners[2]
						: null, x, y + h, tx, ty + th,
				corners != null ? corners[3] : null);
	}
	
	public void drawImage(Image image, float x, float y, float width, float height, 
					float u, float v, float uWidth, float vHeight, Color[] corners) {
		checkRender(image);
		drawQuadElement(x, y, u, v, corners!=null ? corners[0] : null,
				 		x+width, y, u+uWidth, v, corners!=null ? corners[1] : null,
				 		x+width, y+height, u+uWidth, v+vHeight, corners!=null ? corners[2] : null,
				 		x, y+height, u, v+vHeight, corners!=null ? corners[3] : null);
	}
	
	/**
	 * 
	 * @param image
	 * @param x
	 * @param y
	 * @param points
	 * @param texcoords a texcoord for each vertex (8 elements 
	 * @param offset
	 * @param corners
	 */
	public void drawImage(Image image, float x, float y, float[] points, 
			float[] texcoords, int offset, int texcoordsOffset, Color[] corners) {
		checkRender(image);
		float x1 = points[offset++];
		float y1 = points[offset++];
		float x2 = points[offset++];
		float y2 = points[offset++];
		float x3 = points[offset++];
		float y3 = points[offset++];
		float x4 = points[offset++];
		float y4 = points[offset++];
		
		float u1 = texcoords[texcoordsOffset++];
		float v1 = texcoords[texcoordsOffset++];
		float u2 = texcoords[texcoordsOffset++];
		float v2 = texcoords[texcoordsOffset++];
		float u3 = texcoords[texcoordsOffset++];
		float v3 = texcoords[texcoordsOffset++];
		float u4 = texcoords[texcoordsOffset++];
		float v4 = texcoords[texcoordsOffset++];
		drawQuadElement(x+x1, y+y1, u1, v1, corners!=null ? corners[0] : null,
				 		x+x2, y+y2, u2, v2, corners!=null ? corners[1] : null,
				 		x+x3, y+y3, u3, v3, corners!=null ? corners[2] : null,
				 		x+x4, y+y4, u4, v4, corners!=null ? corners[3] : null);
	}
	
	private void checkRender(Image image) {
		if (image==null || image.getTexture()==null)
			throw new NullPointerException("null texture");
		
		//we need to bind a different texture. this is
		//for convenience; ideally the user should order
		//their rendering wisely to minimize texture binds	
		if (image.getTexture()!=texture) {
			//apply the last texture
			render();
			texture = image.getTexture();
		} else if (idx >= maxVerts - 4) 
			render();
	}
	
	/**
	 * Specifies vertex data.
	 * 
	 * @param x the x position
	 * @param y the y position
	 * @param u the U texcoord
	 * @param v the V texcoord
	 * @param color the color for this vertex
	 */
	protected void vertex(float x, float y, float u, float v, Color color) {
		vertices.put(x);
		vertices.put(y);
		texcoords.put(u);
		texcoords.put(v);
		Color c = color!=null ? color : currentColor;
		colors.put(c.r);
		colors.put(c.g);
		colors.put(c.b);
		colors.put(c.a);
		idx++;
	}
	
	/**
	 * Draws a quad-like element using either GL_QUADS or GL_TRIANGLES, depending
	 * on this batch's configuration.
	 */
	protected void drawQuadElement(
						float x1, float y1, float u1, float v1, Color c1,   //TOP LEFT 
						float x2, float y2, float u2, float v2, Color c2,   //TOP RIGHT
						float x3, float y3, float u3, float v3, Color c3,   //BOTTOM RIGHT
						float x4, float y4, float u4, float v4, Color c4) { //BOTTOM LEFT
		x1 += translateX;
		y1 += translateY;
		x2 += translateX;
		y2 += translateY;
		x3 += translateX;
		y3 += translateY;
		x4 += translateX;
		y4 += translateY;
		if (mode == GL11.GL_TRIANGLES) {
			//top left, top right, bottom left
			vertex(x1, y1, u1, v1, c1);
			vertex(x2, y2, u2, v2, c2);
			vertex(x4, y4, u4, v4, c4);
			//top right, bottom right, bottom left
			vertex(x2, y2, u2, v2, c2);
			vertex(x3, y3, u3, v3, c3);
			vertex(x4, y4, u4, v4, c4);
		} else {
			//quads: top left, top right, bottom right, bottom left
			vertex(x1, y1, u1, v1, c1);
			vertex(x2, y2, u2, v2, c2);
			vertex(x3, y3, u3, v3, c3);
			vertex(x4, y4, u4, v4, c4);
		}
	}
}