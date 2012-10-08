package slim.texture;

import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import slim.texture.io.ImageDecoder;

public class Texture2D extends Texture {

    public static Texture2D loadTexture(URL url) throws IOException {
        return loadTexture(url, DEFAULT_INTERNAL_FORMAT);
    }
    
    public static Texture2D loadTexture(URL url, int filter) throws IOException {
    	return loadTexture(url, DEFAULT_INTERNAL_FORMAT, filter, filter, false);
    }
    
    public static Texture2D loadTexture(URL url, Format internalFormat) throws IOException {
        return loadTexture(url, internalFormat, GL11.GL_LINEAR, GL11.GL_LINEAR, false);
    }

    /**
     * Loads the texture at the specified URL and places it in
     * an OpenGL texture 
     * 
     * @param url
     * @param format
     * @param target
     * @return 
     */
    public static Texture2D loadTexture(URL url, Format internalFormat,
    							int minFilter, int magFilter, boolean genMipmaps) throws IOException {
        ImageDecoder d = TextureLoader.get().createDecoder(url);
        if (!d.open()) {
            throw new IOException("could not open a decoder for "+url.getPath());
        }
        int width = d.getWidth();
        int height = d.getHeight();
        Texture2D.Format fmt = d.getFormat();
        ByteBuffer buf = null;
        try {
            buf = BufferUtils.createByteBuffer(d.getSize());
            d.decode(buf);
        } finally {
            d.close();
        }
        buf.flip();
        if (fmt.isCompressed() && !internalFormat.equals(fmt)) {
        	//if the format is compressed, internalFormat will be ignored
        	internalFormat = fmt;
        }
        Texture2D tex = new Texture2D(width, height, internalFormat, fmt, buf, 
        					 minFilter, magFilter, genMipmaps);
        return tex;
    }
	
	/**
	 * Creates a Texture2D with a given width and image data. 
	 * Generally it is good practice to use power-of-two sizes when possible.
	 * 
	 * @param width the width of the texture
	 * @param internalFormat
	 * @param dataFormat
	 * @param data
	 */
	public Texture2D(int width, int height, Format internalFormat, Format dataFormat, ByteBuffer data,
						int minFilter, int magFilter, boolean genMipmaps) {
		setup(TEXTURE_2D, width, height, 1, internalFormat, dataFormat, 
				data, minFilter, magFilter, false);
	}
	
	public Texture2D(int width, int height, Format internalFormat, Format dataFormat, ByteBuffer data) {
		this(width, height, internalFormat, dataFormat, data, FILTER_LINEAR, FILTER_LINEAR, false);
	}
	
	public Texture2D(int width, int height, Format internalFormat) {
		this(width, height, internalFormat, DEFAULT_DATA_FORMAT, null);
	}
	
	public Texture2D(int width, int height, int filter) {
		this(width, height, DEFAULT_INTERNAL_FORMAT, DEFAULT_DATA_FORMAT, null, filter, filter, false);
	}
	
	public Texture2D(int width, int height) {
		this(width, height, DEFAULT_INTERNAL_FORMAT);
	}
	
	@Override
	public void setWrap(int wrap) {
		bind();
		GL11.glTexParameteri(getTarget(), GL11.GL_TEXTURE_WRAP_S, wrap);
		GL11.glTexParameteri(getTarget(), GL11.GL_TEXTURE_WRAP_T, wrap);
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
    public void uploadImage(int width, int height, Format internalFormat, Format dataFormat, ByteBuffer data) {
    	glTexImage(getTarget(), width, height, 1, internalFormat, dataFormat, data);
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
    public void uploadSubImage(int x, int y, int width, int height, Format dataFormat, ByteBuffer data) {
    	glTexSubImage(getTarget(), x, y, 0, width, height, 1, dataFormat, data);
	}
}
