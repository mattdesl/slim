package slim.texture.io;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import static org.lwjgl.opengl.EXTTextureCompressionS3TC.*;

import slim.util.Utils;

public class DDSDecoder {

	public static void main(String[] args) throws Exception {
		DDSDecoder d = new DDSDecoder(Utils.getResourceAsStream("res/texture4.dds"));
	}
	 
	private static final int DDSCAPS2_CUBEMAP = 0x200;
	
	private static final int DDPF_ALPHAPIXELS = 0x1;
//	private static final int DDPF_ALPHA = 0x2;
	private static final int DDPF_FOURCC = 0x4;
	private static final int DDPF_RGB = 0x40;
//	private static final int DDPF_LUMINANCE = 0x20000; //not yet supported
//	private static final int DDPF_YUV = 0x200; //not yet supported	

	private static final int DDPF_FOURCC_DXT1 = 0x31545844;
//	private static final int DDPF_FOURCC_DXT2 = 0x32545844;
	private static final int DDPF_FOURCC_DXT3 = 0x33545844;
//	private static final int DDPF_FOURCC_DXT4 = 0x34545844;
	private static final int DDPF_FOURCC_DXT5 = 0x35545844;
	private static final int DDPF_FOURCC_DX10 = 0x30315844;
	

	
	InputStream input;
	byte[] buffer;
	int offset;
	
    private static final int MAGIC_NUMBER = 0x20534444;
    
	private Header header;
    private PixelFormat pixelFormat;
    boolean isCompressed;
    boolean isDX10;
    
    public static String toHex(String arg) {
        return String.format("%x", new BigInteger(arg.getBytes()));
    }
    
    private class Header {
    	int width, height, depth;
	    int flags;
	    int linearSize;
	    int mipMapCount;
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
        this.buffer = new byte[1024];
        readHeader();
    }
    
    private void readHeader() throws IOException {
    	readFully(buffer, 0, 1024);
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
        header.width = readInt();
        int linearSize = readInt(); //ignore linear size from file
        header.depth = readInt();
        header.mipMapCount = readInt();
        
        System.out.println(header.width+" "+header.height+" "+header.depth+" "+header.mipMapCount);
        
        offset += 11 * 4; //skips reserved1
        
        readPixelFormat();
        
        readInt(); //ignore caps from file
        header.caps2 = readInt();
        offset += 12; //skips caps3, caps4 and reserved2
        
        readDX10Header();
        
        if (isCompressed) {
	        switch (pixelFormat.fourCC) {
	        case DDPF_FOURCC_DXT1:
	        	System.out.println("dxt1"); break;
	        case DDPF_FOURCC_DXT3:
	        	System.out.println("dxt3"); break;
	        case DDPF_FOURCC_DXT5:
	        	System.out.println("dxt5"); break;
	        default:
	        	throw new IOException("DDSDecoder only supports DXT1, 3, 5 and DX10");
	        }
        } else {
        	throw new IOException("DDSDecoder currently only supports compressed data");
        }
    }
    
    private void readDX10Header() {
    	
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
    	
        if ((pixelFormat.flags & DDPF_RGB) == DDPF_RGB)
            this.isCompressed = false;
        else if ((pixelFormat.flags & DDPF_FOURCC) == DDPF_FOURCC) {
            this.isCompressed = true;
            this.isDX10 = pixelFormat.fourCC == DDPF_FOURCC_DX10;
        }
    }
//    
//    public void decode(ByteBuffer buffer, int stride, Format fmt) throws IOException {
//    	final int offset = buffer.position();
//    }
    
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
