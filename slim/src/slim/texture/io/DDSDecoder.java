package slim.texture.io;

import static org.lwjgl.opengl.EXTTextureCompressionS3TC.GL_COMPRESSED_RGBA_S3TC_DXT1_EXT;
import static org.lwjgl.opengl.EXTTextureCompressionS3TC.GL_COMPRESSED_RGBA_S3TC_DXT3_EXT;
import static org.lwjgl.opengl.EXTTextureCompressionS3TC.GL_COMPRESSED_RGBA_S3TC_DXT5_EXT;
import static org.lwjgl.opengl.EXTTextureCompressionS3TC.GL_COMPRESSED_RGB_S3TC_DXT1_EXT;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL11;


public class DDSDecoder {

	private static final int DDSCAPS2_CUBEMAP = 0x200;
	private static final int DDSCAPS_MIPMAP = 0x400000;
	private static final int DDSCAPS_COMPLEX = 0x8;
	
	private static final int DDPF_ALPHAPIXELS = 0x1;
//	private static final int DDPF_ALPHA = 0x2;
	private static final int DDPF_FOURCC = 0x4;
	private static final int DDPF_RGB = 0x40;
	private static final int DDPF_LUMINANCE = 0x20000; //not yet supported
//	private static final int DDPF_YUV = 0x200; //not yet supported	

	private static final int DDPF_FOURCC_DXT1 = 0x31545844;
//	private static final int DDPF_FOURCC_DXT2 = 0x32545844;
	private static final int DDPF_FOURCC_DXT3 = 0x33545844;
//	private static final int DDPF_FOURCC_DXT4 = 0x34545844;
	private static final int DDPF_FOURCC_DXT5 = 0x35545844;
	private static final int DDPF_FOURCC_DX10 = 0x30315844;
	
	public static enum Format {
		RGB_DXT1(GL_COMPRESSED_RGB_S3TC_DXT1_EXT, 3),
		RGBA_DXT1(GL_COMPRESSED_RGBA_S3TC_DXT1_EXT, 4),
		RGBA_DXT3(GL_COMPRESSED_RGBA_S3TC_DXT3_EXT, 4),
		RGBA_DXT5(GL_COMPRESSED_RGBA_S3TC_DXT5_EXT, 4),
		
		LUMINANCE(GL11.GL_LUMINANCE, 1),
		LUMINANCE_ALPHA(GL11.GL_LUMINANCE_ALPHA, 2),
		ALPHA(GL11.GL_ALPHA, 1),
		RGB(GL11.GL_RGB, 3);
		
		int glFormat;
		int bpp;
		
		Format(int glFormat, int bpp) {
			this.glFormat = glFormat;
			this.bpp = bpp;
		}
		
		public int getGLFormat() {
			return glFormat;
		}
		
		public int getBytesPerPixel() {
			return bpp;
		}
	}
	
	InputStream input;
	byte[] buffer;
	int offset;
	
    private static final int MAGIC_NUMBER = 0x20534444;
    private static final int DXT1_BLOCKSIZE = 8;
    private static final int DEFAULT_BLOCKSIZE = 16;
    
	private Header header;
    private PixelFormat pixelFormat;
    boolean isCompressed;
    boolean isDX10;
    int blockSize = DEFAULT_BLOCKSIZE;
    int bpp = 4;
    boolean hasAlpha = false;
    Format ddsFmt;
    int imageSize;
    
    private class Header {
    	int width, height, depth;
	    int flags;
	    int linearSize;
	    int mipMapCount;
	    int caps;
	    int caps2; 
    }
    
    private class PixelFormat {
    	int flags;
    	int fourCC;
    	int rgbBitCount;
    	int rBitMask;
    	int gBitMask;
    	int bBitMask;
    	int aBitMask;
    }
    
    public DDSDecoder(InputStream input) throws IOException {
        this.input = input;
        this.buffer = new byte[128];
        readHeader();
    }
    
    public int getWidth() {
    	return header.width;
    }
    
    public int getHeight() {
    	return header.height;
    }
    
    public int getDepth() {
    	return header.depth;
    }
    
    public int getSize() {
    	int s = (int)(Math.ceil(getWidth()/4f) * Math.ceil(getHeight()/4f) * blockSize); //size of main image
    	System.out.println(s);
    	return s;
    }
    
    public int getMipMapCount() {
    	return header.mipMapCount;
    }
    
    public Format getFormat() {
    	return ddsFmt;
    }
    
    public void decode(ByteBuffer buffer) throws IOException {
    	final int offset = buffer.position();
    	System.out.println(getFormat().name());
    	this.buffer = new byte[getSize()];
    	readFully(this.buffer, 0, this.buffer.length);
    	buffer.put(this.buffer);
    }
    
    
    private void readHeader() throws IOException {
    	readFully(buffer, 0, 128);
    	offset = 0;
        long magic = readInt();
        if (magic != MAGIC_NUMBER) 
        	throw new IOException("invalid header -- magic number "+magic+" != "+MAGIC_NUMBER);
        int size = readInt();
        if (size != 124)
        	throw new IOException("invalid header -- size "+size+" != 124");
        header = new Header();
        header.flags = readInt();
        header.height = readInt();
        header.width = Math.max(1, readInt());
        header.linearSize = readInt(); //ignore linear size from file
        header.depth = Math.max(1, readInt());
        header.mipMapCount = Math.max(1, readInt());
        
        offset += 11 * 4; //skips reserved1
        
        readPixelFormat();
        
        header.caps = readInt();
        header.caps2 = readInt();
        offset += 12; //skips caps3, caps4 and reserved2
        
        //since caps is sometimes not written, we will only assume we have mipmaps
        //if mipMapCount > 1
        
        if (isCompressed && pixelFormat.fourCC == DDPF_FOURCC_DX10)
        	readDX10Header();
    }
    
    private void readDX10Header() throws IOException {
    	throw new IOException("DDSDecoder doesn't support DX10 formats yet");
    }
    
    private void readPixelFormat() throws IOException {
    	int size = readInt();
    	if (size != 32)
    		throw new IOException("invalid PixelFormat header size "+size+"; corrupt file");
    	
    	pixelFormat = new PixelFormat();
    	pixelFormat.flags = readInt();
    	pixelFormat.fourCC = readInt();
    	pixelFormat.rgbBitCount = readInt();
    	pixelFormat.rBitMask = readInt();
    	pixelFormat.gBitMask = readInt();
    	pixelFormat.bBitMask = readInt();
    	pixelFormat.aBitMask = readInt();
    	
    	if ((pixelFormat.flags & DDPF_ALPHAPIXELS) == DDPF_ALPHAPIXELS)
    		hasAlpha = true;
    	
    	if ((pixelFormat.flags & DDPF_LUMINANCE) == DDPF_LUMINANCE) {
    		ddsFmt = hasAlpha ? Format.LUMINANCE_ALPHA : Format.LUMINANCE;
    	}
    		
        if ((pixelFormat.flags & DDPF_RGB) == DDPF_RGB) {
            this.isCompressed = false;
        	ddsFmt = Format.RGB; 
        } else if ((pixelFormat.flags & DDPF_FOURCC) == DDPF_FOURCC) {
            this.isCompressed = true;
            if (pixelFormat.fourCC == DDPF_FOURCC_DXT1)
            	this.blockSize = DXT1_BLOCKSIZE; 
            this.isDX10 = pixelFormat.fourCC == DDPF_FOURCC_DX10;
        }
        
        if (isCompressed) {
	        switch (pixelFormat.fourCC) {
	        case DDPF_FOURCC_DXT1:
	        	ddsFmt = hasAlpha ? Format.RGBA_DXT1 : Format.RGB_DXT1; break;
	        case DDPF_FOURCC_DXT3:
	        	ddsFmt = Format.RGBA_DXT3; break;
	        case DDPF_FOURCC_DXT5:
	        	ddsFmt = Format.RGBA_DXT5; break;
	        default:
	        	throw new IOException("Invalid format. Supported formats:\nDXT1, DXT1A, DXT3, DXT5");
	        }
        } else {
        	throw new IOException("Invalid format. Supported formats:\nDXT1, DXT1A, DXT3, DXT5");
        }
    }
    
    private void readFully(byte[] buffer, int offset, int length) throws IOException {
        do {
            int read = input.read(buffer, offset, length);
            if(read < 0) {
                throw new EOFException();
            }
            offset += read;
            length -= read;
        } while(length > 0);
    }
    
    /** Reads little-endian. */
    private int readInt() {
    	int i = ((buffer[offset+3]) << 24) |
                ((buffer[offset+2] & 255) << 16) |
                ((buffer[offset+1] & 255) <<  8) |
                ((buffer[offset] & 255));
    	offset += 4;
    	return i;
    }
    
    private void skip(long amount) throws IOException {
        while(amount > 0) {
            long skipped = input.skip(amount);
            if(skipped < 0) {
                throw new EOFException();
            }
            amount -= skipped;
        }
    }
}
