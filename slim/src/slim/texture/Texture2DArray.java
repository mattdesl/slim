package slim.texture;

import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.ContextCapabilities;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;

import slim.texture.io.ImageDecoder;

public class Texture2DArray extends Texture {
	
//	public static Texture2DArray loadImages(URL[] urls, Format internalFormat,
//			int minFilter, int magFilter, boolean genMipmaps) throws IOException {
//		int depth = urls.length;
//		int lastWidth, lastHeight;
//		ArrayList<ByteBuffer> buf = new ArrayList<ByteBuffer>();
//		for (int i=0; i<urls.length; i++) {
//			if (urls[i]==null)
//				throw new NullPointerException("url at index "+i+" is null");
//			ImageDecoder d = TextureLoader.get().createDecoder(urls[i]);
//			if (!d.open()) 
//				throw new IOException("could not open a decoder for " + urls[i].getPath());
//			int width = d.getWidth();
//			int height = d.getHeight();
//			if (lastWidth)
//			Texture2D.Format fmt = d.getFormat();
//			int perPixel = fmt.getBytesPerPixel();
//			ByteBuffer buf = null;
//			try {
//				buf = BufferUtils.createByteBuffer(width * height * perPixel);
//				d.decode(buf);
//			} finally {
//				d.close();
//			}
//			buf.flip();
//		}
//		return new Texture2D(width, height, internalFormat, fmt, buf,
//				minFilter, magFilter, genMipmaps);
//	}
	
	
//	public static Texture2DArray fromTiles(URL url, int tileWidth, int tileHeight, int spacing, int tileCount,
//						Format internalFormat, int minFilter, int magFilter, boolean genMipmaps) throws IOException {
//		ImageDecoder d = TextureLoader.get().createDecoder(url);
//		if (!d.open()) 
//			throw new IOException("could not open a decoder for " + url.getPath());
//		int width = d.getWidth();
//		int height = d.getHeight();
//		Texture2D.Format fmt = d.getFormat();
//		int perPixel = fmt.getBytesPerPixel();
//		ByteBuffer buf = null;
//		try {
//			buf = BufferUtils.createByteBuffer(width * height * perPixel);
//			d.decode(buf);
//		} finally {
//			d.close();
//		}
//		buf.flip();
//
//		int tileBytes = tileWidth * tileHeight * perPixel;
//		ByteBuffer tile = BufferUtils.createByteBuffer(tileBytes);
//		Texture2DArray array = new Texture2DArray(tileWidth, tileHeight,
//				tileCount, internalFormat, minFilter, magFilter, genMipmaps);
//		int x = 0, y = 0;
//		for (int i=0; i<tileCount; i++) {
//			
//		}
//		
//		return array;
//	}
	
	public Texture2DArray(int width, int height, int layers,
			Format internalFormat, int minFilter, int magFilter, boolean genMipmaps) {
		this(width, height, layers, internalFormat, Texture.DEFAULT_DATA_FORMAT, null, minFilter, magFilter, genMipmaps);
	}
	
	public Texture2DArray(int width, int height, int layers,
						Format internalFormat, Format dataFormat, ByteBuffer data,
						int minFilter, int magFilter, boolean genMipmaps) {
		if (!isTextureArraySupported())
			throw new UnsupportedOperationException("texture arrays are not supported by your driver");
		setup(TEXTURE_2D_ARRAY, width, height, layers, internalFormat, dataFormat, 
				data, FILTER_NEAREST, FILTER_NEAREST, false);
	}

	public Texture2DArray(int width, int height, int layers,
						Format internalFormat, Format dataFormat, ByteBuffer data) {
		this(width, height, layers, internalFormat, dataFormat, data,
				FILTER_NEAREST, FILTER_NEAREST, false);
	}
	
	public Texture2DArray(int width, int height, int layers,
						Format internalFormat) {
		this(width, height, layers, internalFormat, DEFAULT_DATA_FORMAT, null);
	}

	public Texture2DArray(int width, int height, int layers) {
		this(width, height, layers, DEFAULT_INTERNAL_FORMAT);
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
    public void uploadImage(int width, int height, int layers, Format internalFormat, Format dataFormat, ByteBuffer data) {
    	glTexImage(getTarget(), width, height, layers, internalFormat, dataFormat, data);
    }
    
    /**
     * Binds and uploads the given sub-image data using glTexSubImage2D and a LOD
     * of 0, with the specified target, data format and type.
     * 
     * If the data is null, it will be converted to transparent black pixels.
     */
    public void uploadSubImage(int x, int y, int layer, int width, int height, int layerCount, Format dataFormat, ByteBuffer data) {
    	glTexSubImage(getTarget(), x, y, layer, width, height, layerCount, dataFormat, data);
	}
}
