package slim.texture;

import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL11;

/**
 * A utility class for 1D textures.
 * 
 * For consistency with Texture2D/Texture3D, this will account for
 * non-power-of-two textures (if not supported in hardware, or if "force POT" is
 * enabled) by generating the texture the next largest power-of-two size. It is
 * then up to the user to handle this with getNormalizedWidth -- which is
 * desiredWidth / powerOfTwoWidth.
 * 
 * @author davedes / Matt DesLauriers
 */
public class Texture1D extends Texture {

	/**
	 * Creates a Texture1D with a given width and image data. 
	 * Generally it is good practice to use power-of-two sizes when possible.
	 * 
	 * @param width the width of the texture
	 * @param internalFormat
	 * @param dataFormat
	 * @param data
	 */
	public Texture1D(int width, Format internalFormat, Format dataFormat, ByteBuffer data) {
		setup(TEXTURE_1D, width, 1, 1, internalFormat, dataFormat, 
				data, FILTER_NEAREST, FILTER_NEAREST, false);
	}
	
	public Texture1D(int width, Format internalFormat) {
		this(width, internalFormat, DEFAULT_DATA_FORMAT, null);
	}
	
	public Texture1D(int width) {
		this(width, DEFAULT_INTERNAL_FORMAT);
	}
	
	@Override
	public void setWrap(int wrap) {
		bind();
		GL11.glTexParameteri(getTarget(), GL11.GL_TEXTURE_WRAP_S, wrap);
	}
    
    /**
     * Binds and uploads the given image using glTexImage2D and a LOD of zero. 
     * 
     * This may change the width of this texture.
     * This will account for non-power-of-two sizes if it isn't supported
     * in hardware or if forcePOT is enabled.
     * 
     * If the data is null, it will be converted to transparent black pixels
     * (i.e. to create an empty texture).
     * 
     * @param width the new width of the texture; should match width of incoming data
     * @param dataFormat the format of our data, e.g. RGBA
     * @param data the data
     */
    public void uploadImage(int width, Format internalFormat, Format dataFormat, ByteBuffer data) {
    	glTexImage(getTarget(), width, 1, 1, internalFormat, dataFormat, data);
    }
    
    /**
     * Binds and uploads the given sub-image data using glTexSubImage2D and a LOD
     * of 0, with the specified target, data format and type.
     * 
     * If the data is null, it will be converted to transparent black pixels.
     * 
     * @param x the x offset to place the data in this texture
     * @param width the width of the data
     * @param dataFormat the format of the data, e.g. RGBA
     * @param buffer the new buffer data in the specified format
     */
    public void uploadSubImage(int x, int width, Format dataFormat, ByteBuffer data) {
    	glTexSubImage(getTarget(), x, 0, 0, width, 1, 1, dataFormat, data);
	}
}
