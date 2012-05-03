package slickmini.texture;

import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;

import slim.texture.Texture2Dold;
import slim.texture.TextureLoader;
import slim.texture.io.ImageDecoder;

/**
 *
 * @author davedes
 */
public class CubeMap extends Texture2Dold {
    
    public static final int RIGHT = GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X;
    public static final int LEFT = GL13.GL_TEXTURE_CUBE_MAP_NEGATIVE_X;
    public static final int TOP = GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_Y; 
    public static final int BOTTOM = GL13.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y;
    public static final int FRONT = GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_Z;
    public static final int BACK = GL13.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z;
    
    public static final int FACE_0 = GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X;
    
    public static CubeMap loadCubeMap(URL[] files, Format internalFormat) throws IOException {
        if (files.length>6)
            throw new IllegalArgumentException("no more than 6 files to a cube map");
        
        int maxWidth = 0;
        int maxHeight = 0;
        
        ImageDecoder[] decoders = new ImageDecoder[files.length];
        ByteBuffer[] buf = new ByteBuffer[files.length];
        for (int i=0; i<files.length; i++) {
            decoders[i] = TextureLoader.get().createDecoder(files[i]);
            if (!decoders[i].open()) 
                throw new IOException("could not open a decoder for "+files[i].getPath());
            int width = decoders[i].getWidth();
            int height = decoders[i].getHeight();
            maxWidth = Math.max(width, maxWidth);
            maxHeight = Math.max(height, maxHeight);
            int perPixel = decoders[i].getFormat().getBytesPerPixel();
            try {
                buf[i] = BufferUtils.createByteBuffer(width * height * perPixel);
                decoders[i].decode(buf[i]);
                buf[i].position(0);
            } finally {
                decoders[i].close();
            }
        }
        
        CubeMap cubeMap = new CubeMap(maxWidth, maxHeight, internalFormat);
        //create each image using glTexSubImage2D
        for (int i=0; i<decoders.length; i++) {
            int width = decoders[i].getWidth();
            int height = decoders[i].getHeight();
            int fmt = decoders[i].getFormat().getGLFormat();
            cubeMap.createTexture(width, height, FACE_0 + i, fmt, 
                    DEFAULT_DATA_TYPE, buf[i]);
        }
        //fill in remaining textures as empty
        for (int i=decoders.length; i<6; i++) {
            cubeMap.createTexture(cubeMap.getTextureWidth(), 
                    cubeMap.getTextureHeight(), FACE_0 + i, 
                    DEFAULT_DATA_FORMAT, DEFAULT_DATA_TYPE, null);
        }
        return cubeMap;
    }
    
    public CubeMap(int width, int height, Format internalFormat) {
        super();
        if (!isCubeMapSupported())
            throw new IllegalArgumentException("GL_TEXTURE_CUBE_MAP is"
                    + " not supported on this system");
        if (width!=height)
            throw new IllegalArgumentException("cube maps must have "
                    + "square texture sizes");
        this.target = GL13.GL_TEXTURE_CUBE_MAP;
        this.internalFormat = internalFormat;
        this.imageWidth = texWidth = width;
        this.imageHeight = texHeight = height;
        
        //if we are forcing POT or if POT is not supported...
        boolean usePOT = Texture2Dold.isForcePowerOfTwo() || !Texture2Dold.isNPOTSupported();
        if (usePOT) {
            texWidth = Texture2Dold.toPowerOfTwo(width);
            texHeight = Texture2Dold.toPowerOfTwo(height);
        }
        
        id = GL11.glGenTextures();
        bind();
        GL11.glPixelStorei(GL11.GL_UNPACK_ROW_LENGTH, 0);
        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
        
        GL11.glTexParameteri(target, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(target, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        
        //since cube maps are supported we assume GL12 works as well...
        GL11.glTexParameteri(target, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(target, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(target, GL12.GL_TEXTURE_WRAP_R, GL12.GL_CLAMP_TO_EDGE);
    }
    
    public void setWrap(int wrap) {
        super.setWrap(wrap);
        GL11.glTexParameteri(target, GL12.GL_TEXTURE_WRAP_R, wrap);
    }
}