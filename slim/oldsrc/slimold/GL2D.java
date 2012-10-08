package slimold;

import java.nio.ByteBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import slim.g2d.Image;
import slim.texture.Texture;
import slim.texture.Texture2D;

/**
 * Utility methods for GL operations in a 2D setting.
 * @author davedes
 */
public class GL2D {

	static Color sharedColor = new Color(Color.white);
	
	static Image pixel;
	
	public static void drawRect(SpriteBatch batch, float x, float y, float w, float h, float thickness) {
		if (pixel==null)
			createPixel();
		batch.drawImage(pixel, x, y, w, thickness);
		batch.drawImage(pixel, x+w-thickness, y, thickness, h);
		batch.drawImage(pixel, x, y, thickness, h);
		batch.drawImage(pixel, x, y+h-thickness, w, thickness);
	}
	
	public static void drawRect(SpriteBatch batch, float x, float y, float w, float h) {
		drawRect(batch, x, y, w, h, 1);
	}
	
	public static void fillRect(SpriteBatch batch, float x, float y, float w, float h) {
		if (pixel==null)
			createPixel();
		batch.drawImage(pixel, x, y, w, h);
	}
	
	static void createPixel() {
		ByteBuffer buf = BufferUtils.createByteBuffer(4);
		buf.put((byte)255).put((byte)255).put((byte)255).put((byte)255);
		buf.flip();
		Texture2D tex = new Texture2D(1, 1, Texture.Format.RGBA,
									  Texture.Format.RGBA, buf,
									  Texture.FILTER_NEAREST,
									  Texture.FILTER_NEAREST, false);
		pixel = new Image(tex);
	}
	
	/**
	 * Sets the GL clear color to the given color.
	 * @param color the new clear color
	 * @param useAlpha true if the alpha component should be 0.0, false
	 * 		if we should use Color's alpha component (default)
	 */
	public static void setBackground(Color color, boolean transparent) {
		GL11.glClearColor(color.r, color.g, color.b, transparent ? 0 : color.a);
	}
	
	public static void setBackground(Color color) {
		setBackground(color, false);
	}
	
	/**
	 * Clears the color buffer bit.
	 */
	public static void clear() {
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
	}
	
	
	/**
	 * Sets the blend function to GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA 
	 */
	public static void resetBlendFunc() {
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
	}
}
