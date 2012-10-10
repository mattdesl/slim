package slim.game;

import java.io.IOException;
import java.net.URL;

import slim.core.SlimException;
import slim.texture.Texture;
import slim.texture.Texture2D;
import slim.util.Utils;

/**
 * A minimal wrapper around Texture for use with 2D sprites (in ortho view).
 * 
 * The image is rendered by drawing a quad 
 * 
 * @author davedes
 */
public class Image {
    
	private float textureOffsetX, textureOffsetY, normalizedWidth = 1f, normalizedHeight = 1f;
	private float width, height;
	private float centerX, centerY;
	private float xOff, yOff;
	
	private Texture2D texture;
	
	protected Image() { }
	
	/**
	 * A convenience wrapper around Texture.loadTexture, useful for prototyping and rapid development. 
	 */
	public Image(String ref, int filter) throws SlimException {
		this(Utils.getResource(ref), filter);
	}
	
	public Image(String ref) throws SlimException {
		this(ref, Texture.FILTER_LINEAR);
	}

	/**
	 * A convenient way to create a standard RGBA texture from an image URL.
	 */
	public Image(URL url) throws SlimException {
		this(url, Texture.FILTER_LINEAR);
	}
	
	public Image(URL url, int filter) throws SlimException {
		try {
			this.texture = Texture2D.loadTexture(url, filter);
		} catch (IOException e) {
			throw new SlimException(e);
		}
		init();
	}
	
	public Image(Texture2D texture) {
		this.texture = texture;
		init();
	}
	
	public void bind() {
		texture.bind();
	}
	
	private void init() {
		if (texture!=null) {
			this.normalizedWidth = texture.getNormalizedWidth();
			this.normalizedHeight = texture.getNormalizedHeight();
			this.width = texture.getWidth();
			this.height = texture.getHeight();
			this.xOff = 0;
			this.yOff = 0;
			this.textureOffsetX = 0;
			this.textureOffsetY = 0;
			this.resetCenter();
		}
	}
	
	public void setTexture(Texture2D texture) {
		if (texture==null)
			throw new NullPointerException("null Texture2D");
		this.texture = texture;
		init();
	}
	
	public Texture2D getTexture() {
		return texture;
	}
	
	public float getNormalizedXOffset() {
		return textureOffsetX;
	}
	
	public float getNormalizedYOffset() {
		return textureOffsetY;
	}
	
	public float getNormalizedWidth() {
		return normalizedWidth;
	}
	
	public float getNormalizedHeight() {
		return normalizedHeight;
	}
	
	public int getTextureWidth() {
		return texture!=null ? texture.getTextureWidth() : 0;
	}
	
	public int getTextureHeight() {
		return texture!=null ? texture.getTextureHeight() : 0;
	}
	
	public float getWidth() {
		return width;
	}
	
	public float getHeight() {
		return height;
	}
	
	public float getCenterX() {
		return centerX;
	}
	
	public float getCenterY() {
		return centerY;
	}
	
	/**
	 * Get the x offset (in pixels) from this image's parent texture, i.e. if 
	 * created with getSubImage. 
	 * @return the x offset in the sprite sheet
	 */
	public float getOffsetX() {
		return xOff;
	}
	
	/**
	 * Get the y offset (in pixels) from this image's parent texture, i.e. if 
	 * created with getSubImage. 
	 * @return the y offset in the sprite sheet
	 */
	public float getOffsetY() {
		return yOff;
	}
	
	public void setCenter(float x, float y) {
		this.centerX = x;
		this.centerY = y;
	}
	
	public void resetCenter() {
		this.centerX = width / 2f;
		this.centerY = height / 2f;
	}
	
	public void setCenterX(float x) {
		this.centerX = x;
	}
	
	public void setCenterY(float y) {
		this.centerY = y;
	}
	
	/**
	 * Creates a shallow copy of this image (does not copy Texture / image data).
	 * @return a copy of this image wrapper
	 */
	public Image copy() {
		Image img = new Image();
		img.texture = texture;
		img.width = getWidth();
		img.height = getHeight();
		img.xOff = getOffsetX();
		img.yOff = getOffsetY();
		img.textureOffsetX = getNormalizedXOffset();
		img.textureOffsetY = getNormalizedYOffset();
		img.normalizedWidth = getNormalizedWidth();
		img.normalizedHeight = getNormalizedHeight();
		img.resetCenter();
		return img;
	}
	
	public Image getScaledCopy(float width, float height) {
		Image img = copy();
		img.width = width;
		img.height = height;
		img.resetCenter();
		return img;
	}
	
	public Image getScaledCopy(float scale) {
		return getScaledCopy(scale*getWidth(), scale*getHeight());
	}
	
	public Image getFlippedCopy(boolean horiz, boolean vert) {
		Image img = copy();
		if (horiz) {
			img.textureOffsetX = getNormalizedXOffset() + getNormalizedWidth();
			img.normalizedWidth = -getNormalizedWidth();
		}
		if (vert) {
			img.textureOffsetY = getNormalizedYOffset() + getNormalizedHeight();
			img.normalizedHeight = -getNormalizedHeight();
		}
		return img;
	}
	
	public Image getSubImage(float x, float y, float width, float height) {
		float tx = ( x / this.width * normalizedWidth ) + textureOffsetX;
		float ty = ( y / this.height * normalizedHeight ) + textureOffsetY;
		float tw = width / this.width * normalizedWidth;
		float th = height / this.height * normalizedHeight;
		
		Image img = copy();
		img.textureOffsetX = tx;
		img.textureOffsetY = ty;
		img.width = width;
		img.height = height;
		img.normalizedWidth = tw;
		img.normalizedHeight = th;
		img.xOff = x;
		img.yOff = y;
		img.resetCenter();
		return img;
	}
}
