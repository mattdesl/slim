package slim.texture;

import static org.lwjgl.opengl.EXTTextureCompressionS3TC.GL_COMPRESSED_RGBA_S3TC_DXT1_EXT;
import static org.lwjgl.opengl.EXTTextureCompressionS3TC.GL_COMPRESSED_RGBA_S3TC_DXT3_EXT;
import static org.lwjgl.opengl.EXTTextureCompressionS3TC.GL_COMPRESSED_RGBA_S3TC_DXT5_EXT;
import static org.lwjgl.opengl.EXTTextureCompressionS3TC.GL_COMPRESSED_RGB_S3TC_DXT1_EXT;

import java.nio.ByteBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.ContextCapabilities;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GLContext;

import slim.util.Utils;


public abstract class Texture {
	
	public static final int TEXTURE_1D = GL11.GL_TEXTURE_1D;
	public static final int TEXTURE_2D = GL11.GL_TEXTURE_2D;
	public static final int TEXTURE_1D_ARRAY = GL30.GL_TEXTURE_1D_ARRAY;
	public static final int TEXTURE_2D_ARRAY = GL30.GL_TEXTURE_2D_ARRAY;
	public static final int TEXTURE_3D = GL12.GL_TEXTURE_3D;
	public static final int TEXTURE_CUBE_MAP = GL13.GL_TEXTURE_CUBE_MAP;
	public static final int TEXTURE_RECTANGLE = GL31.GL_TEXTURE_RECTANGLE;
	public static final int FILTER_LINEAR = GL11.GL_LINEAR;
	public static final int FILTER_NEAREST = GL11.GL_NEAREST;
	public static final int FILTER_LINEAR_MIPMAP_LINEAR = GL11.GL_LINEAR_MIPMAP_LINEAR;
	public static final int FILTER_LINEAR_MIPMAP_NEAREST = GL11.GL_LINEAR_MIPMAP_NEAREST;
	public static final int FILTER_NEAREST_MIPMAP_LINEAR = GL11.GL_NEAREST_MIPMAP_LINEAR;
	public static final int FILTER_NEAREST_MIPMAP_NEAREST = GL11.GL_NEAREST_MIPMAP_NEAREST;
	public static final int WRAP_CLAMP = GL11.GL_CLAMP;
	public static final int WRAP_CLAMP_TO_EDGE = GL12.GL_CLAMP_TO_EDGE;
	public static final int WRAP_REPEAT = GL11.GL_REPEAT;
	public static final int WRAP_MIRRORED_REPEAT = GL14.GL_MIRRORED_REPEAT;
	
	public static final Format DEFAULT_DATA_FORMAT = Format.RGBA;
    public static final int DEFAULT_DATA_TYPE = GL11.GL_UNSIGNED_BYTE;
    
    public static final Format DEFAULT_INTERNAL_FORMAT = Format.RGBA;
    
    
    public static class Format  {
        
        public static final Format COMPRESSED_RGB_DXT1 = new Format("COMPRESSED_RGB_DXT1", GL_COMPRESSED_RGB_S3TC_DXT1_EXT, 3, true);
        public static final Format COMPRESSED_RGBA_DXT1 = new Format("COMPRESSED_RGBA_DXT1", GL_COMPRESSED_RGBA_S3TC_DXT1_EXT, 4, true);
        public static final Format COMPRESSED_RGBA_DXT3 = new Format("COMPRESSED_RGBA_DXT3", GL_COMPRESSED_RGBA_S3TC_DXT3_EXT, 4, true);
        public static final Format COMPRESSED_RGBA_DXT5 = new Format("COMPRESSED_RGBA_DXT5", GL_COMPRESSED_RGBA_S3TC_DXT5_EXT, 4, true);
        
        public static final Format RGBA = new Format("RGBA", GL11.GL_RGBA, 4);
        public static final Format RGB = new Format("RGB", GL11.GL_RGB, 3);
        
        public static final Format BGRA = new Format("BGRA", GL12.GL_BGRA, 4);
        public static final Format BGR = new Format("BGR", GL12.GL_BGR, 3);
        
        public static final Format RED = new Format("RED", GL11.GL_RED, 1);
        public static final Format GREEN = new Format("GREEN", GL11.GL_GREEN, 1);
        public static final Format BLUE = new Format("BLUE", GL11.GL_BLUE, 1);
        public static final Format ALPHA = new Format("ALPHA", GL11.GL_BLUE, 1);
        
        public static final Format LUMINANCE = new Format("LUMINANCE", GL11.GL_LUMINANCE, 1);
        public static final Format LUMINANCE_ALPHA = new Format("LUMINANCE_ALPHA", GL11.GL_LUMINANCE_ALPHA, 2);
        
        private int glFormat;
        private int bpp;
        private String debugName = "";
        private boolean compressed;
        
        public Format(int glFormat, int bpp) {
            this.glFormat = glFormat;
            this.bpp = bpp;
        }
        
        public Format(String debugName, int glFormat, int bpp) {
        	this(debugName, glFormat, bpp, false);
        }
        
        public Format(String debugName, int glFormat, int bpp, boolean compressed) {
        	this.glFormat = glFormat;
        	this.bpp = bpp;
        	this.debugName = debugName;
        	this.compressed = compressed;
        }
        
        public boolean isCompressed() {
        	return compressed;
        }
        
        public int getGLFormat() {
            return glFormat;
        }
        
        public int getBytesPerPixel() {
            return bpp;
        }
        
        public String toString() {
        	return debugName!=null&&debugName.length()!=0 ? debugName : super.toString();
        }
        
        public boolean equals(Object o) {
        	return o instanceof Format && ((Format)o).getGLFormat()==getGLFormat();
        }
    }
    
    private static boolean forcePOT = false;
    static int lastTarget = 0;
    static Texture lastBind;
    static int textureCount = 0;
    
    public static int generateTextureID() {
    	textureCount++;
    	return GL11.glGenTextures();
    }
    
    public static void deleteTextureID(int id) {
    	textureCount--;
    	GL11.glDeleteTextures(id);
    }
    
    public static int getMaxTextureSize() {
    	return GL11.glGetInteger(GL11.GL_MAX_TEXTURE_SIZE);
    }

	/**
	 * Slick uses glGenerateMipmap() or GL14.GL_GENERATE_MIPMAP to automatically
	 * build mipmaps (for advanced users). If neither of these versions are supported,
	 * the GL_EXT_framebuffer_object is used as a fallback, and if that extension is also
	 * missing, this method returns false.
	 *  
	 * @return whether the version is >= 1.4 or GL_EXT_framebuffer_object extension exists
	 */
	public static boolean isGenerateMipmapSupported() {
		return GLContext.getCapabilities().OpenGL14 || GLContext.getCapabilities().GL_EXT_framebuffer_object;
	}
    
    /**
     * A convenience method to replace glEnable(texture.getTarget()); it will store 
     * the last enabled target and ensure that only one target is enabled at a time. 
     * If a new target is selected, the last enabled target (if it exists) will be 
     * disabled before enabling the new target.
     * 
     * This is intended to be used with the following targets, all wrapped
     * by Texture constants: TEXTURE_RECTANGLE, TEXTURE_2D, TEXTURE_1D,
     * TEXTURE_CUBE_MAP. Other targets, such as GL_TEXTURE_GEN_S should
     * call glEnable/glDisable directly. 
     * 
     * @param target the target to enable, e.g. GL_TEXTURE_2D
     */
    public static void enable(int target) {
        if (lastTarget != target) {
            clearLastTarget();
            GL11.glEnable(target);
            lastTarget = target;
        }
    }
    
    /**
     * A convenience method to glDisable the last texture target enabled via
     * Texture.enable. If targetCaching is disabled, this method will have no effect.
     */
    public static void disable() {
    	if (lastTarget != 0) {
    		GL11.glDisable(lastTarget);
    		lastTarget = 0;
    	}
    }
    
    /** 
     * Clears the cached target made with Texture.enable and returns it's 
     * value (or 0 if no target had been cached). Will not call glDisable
     * (call disable() to clear AND disable the last target).
     */
    public static int clearLastTarget() {
    	int ret = lastTarget;
        lastTarget = 0;
        return ret;
    }
    
    /**
     * Clears the texture last cached by bind() and returns it; if the 
     * return value is null, it means no texture is cached.  
     * @return the last texture or null if no texture is cached
     */
    public static Texture clearLastBind() {
    	Texture ret = lastBind;
    	lastBind = null;
    	return ret;
    }
    
    public static boolean isForcePOT() {
        return forcePOT;
    }
    
    /**
     * Determines whether to attempt to "fix" non power-of-two textures
     * when creating a new OpenGL texture. If enabled, all non-power-of-two
     * images will be placed in power-of-two textures (with empty data for
     * outer regions). If disabled, then the 'fix' will only be applied if
     * the system does not support NPOT textures with 
     * GL_ARB_texture_non_power_of_two.
     * 
     * @param b whether to force POT textures during texture generation
     */
    public static void setForcePOT(boolean b) {
        forcePOT = b;
    }
    
    public static int toPowerOfTwo(int n) {
        return 1 << (32 - Integer.numberOfLeadingZeros(n-1));
    }
    
    public static boolean isPowerOfTwo(int n) {
        return (n & -n) == n;
    }
    
    public static boolean isNPOTSupported() {
        return GLContext.getCapabilities().GL_ARB_texture_non_power_of_two;
    }
    
    public static boolean isTextureArraySupported() {
		ContextCapabilities ctx = GLContext.getCapabilities();
		return ctx.OpenGL30 || ctx.GL_EXT_texture_array;
	}

//    public static boolean isTexture3DSupported() {
//        return GLContext.getCapabilities().GL_EXT_texture_3d;
//    }
//
//    public static boolean isCubeMapSupported() {
//        return GLContext.getCapabilities().GL_ARB_texture_cube_map;
//    }
//    
//    public static boolean isTextureRectangleSupported() {
//        ContextCapabilities caps = GLContext.getCapabilities();
//        return caps.GL_EXT_texture_rectangle || caps.GL_ARB_texture_rectangle;
//    }
    
    protected int target;
    protected Format internalFormat = DEFAULT_INTERNAL_FORMAT;
    protected int id;
    protected int width, height=1, depth=1;
    protected int texWidth, texHeight, texDepth;
    protected float normalizedWidth = 1f, normalizedHeight = 1f, normalizedDepth = 1f;
    
    /**
     * Creates a new texture with the given target type and initial internal format,
     * and also sets the wrapping to CLAMP_TO_EDGE if 1.2 is available, or CLAMP otherwise.
     */
    protected Texture() {
    	
    }
    
    protected void setup(int target) {
    	this.target = target;
        this.id = generateTextureID();
        bind();
		GL11.glPixelStorei(GL11.GL_UNPACK_ROW_LENGTH, 0);
        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
        int c = GLContext.getCapabilities().OpenGL12 ? WRAP_CLAMP_TO_EDGE : WRAP_CLAMP;
        setWrap(c);
    }
    
    protected void setup(int target, int width, int height, 
    				     int depth, Format internalFormat, 
    				     Format dataFormat, ByteBuffer data, 
    				     int minFilter, int magFilter, boolean genMipmaps) {
    	setup(target);
		
        ContextCapabilities cx = GLContext.getCapabilities();
        if (genMipmaps && !isGenerateMipmapSupported()) { //nothing for auto mipmap gen
        	minFilter = magFilter;
        	genMipmaps = false;
        }
        
		setFilter(minFilter, magFilter);
		
        //if we are < 3.0 and have no FBO support, fall back to GL_GENERATE_MIPMAP
        if (genMipmaps && !cx.OpenGL30 && !cx.GL_EXT_framebuffer_object) { 
        	GL11.glTexParameteri(target, GL14.GL_GENERATE_MIPMAP, GL11.GL_TRUE);
        	genMipmaps = false;
        }
		
		glTexImage(target, width, height, depth, internalFormat, dataFormat, data);
		
        if (genMipmaps) {
        	Texture.enable(getTarget());
        	if (cx.OpenGL30)
        		GL30.glGenerateMipmap(target);
        	else
        		EXTFramebufferObject.glGenerateMipmapEXT(target);
        }
    }
    
    public int getID() {
        return id;
    }
    
    public int getTarget() {
        return target;
    }
    
    public Format getFormat() {
        return internalFormat;
    }
    
    public void bind() {
    	if (isDestroyed())
    		throw new IllegalStateException("trying to bind a texture that was destroyed");
    	if (lastBind!=this) {
    		GL11.glBindTexture(getTarget(), getID());
    		lastBind = this;
    	}
    }
    
    /** If this texture is valid (i.e. it hasn't been destroyed and it's ID is not zero). */
    public boolean isDestroyed() {
    	return id == 0;
    }
    
    /** Destroys this texture and sets its ID to zero. */
    public void destroy() {
    	if (id==0)
    		return;
    	deleteTextureID(id);
    	id = 0;
    	width = height = depth = 0;
    	texWidth = texHeight = texDepth = 0;
    	normalizedWidth = normalizedHeight = normalizedDepth = 0;
    }
    
    /**
     * A convenience method which calls setFilter with the same min and mag filters.
     * @param filter the min/mag filter
     */
    public void setFilter(int filter) {
    	setFilter(filter, filter);
    }
    
    /**
     * Binds this texture and sets the min/mag texture parameters.
     * @param minFilter minification
     * @param magFilter magnification
     */
    public void setFilter(int minFilter, int magFilter) {
        bind();
        int target = getTarget();
        GL11.glTexParameteri(target, GL11.GL_TEXTURE_MIN_FILTER, minFilter);
        GL11.glTexParameteri(target, GL11.GL_TEXTURE_MAG_FILTER, magFilter);
    }
    
	public int getWidth() {
		return width;
	}
	
	public int getTextureWidth() {
		return texWidth;
	}
	
	public float getNormalizedWidth() {
		return normalizedWidth;
	}
	
	public int getHeight() {
		return height;
	}
	
	public int getTextureHeight() {
		return texHeight;
	}
	
	public float getNormalizedHeight() {
		return normalizedHeight;
	}
	
	public int getDepth() {
		return depth;
	}
	
	public int getTextureDepth() {
		return texDepth;
	}
	
	public float getNormalizedDepth() {
		return normalizedDepth;
	}
	
	public int size() {
    	return getFormat().getBytesPerPixel() 
    			* getTextureWidth()  * getTextureHeight()
    			* getTextureDepth();
    }
	
	/**
	 * Clears the contents of the texture by uploading an empty buffer
	 * to glTexImage2D.
	 */
	public void clear() {
		glTexImage(getTarget(), getTextureWidth(), getTextureHeight(), getTextureDepth(),
					getFormat(), Texture.Format.RGBA, null);
	}
	
	void glTexSubImage(int target, int x, int y, int z, 
					int width, int height, int depth,
					Format dataFormat, ByteBuffer data) {
		bind();
    	int size = dataFormat.getBytesPerPixel() * texWidth * texHeight * texDepth;
		data = data == null 
				? BufferUtils.createByteBuffer(size) 
				: data;
    	int df = dataFormat.getGLFormat();
		switch (target) {
			case TEXTURE_1D:
				if (dataFormat.isCompressed())
					GL13.glCompressedTexSubImage1D(target, 0, x, width, df, data);
				else 
					GL11.glTexSubImage1D(target, 0, x, width, df, DEFAULT_DATA_TYPE, data);
				break;
			case TEXTURE_2D:
			case TEXTURE_1D_ARRAY:
				if (dataFormat.isCompressed())
					GL13.glCompressedTexSubImage2D(target, 0, x, y, width, height, df, data);
				else
					GL11.glTexSubImage2D(target, 0, x, y, width, height, df, DEFAULT_DATA_TYPE, data);
				break;
			case TEXTURE_3D:
			case TEXTURE_2D_ARRAY:
				if (dataFormat.isCompressed())
					GL13.glCompressedTexSubImage3D(target, 0, x, y, z, width, height, depth, df, data);
				else
					GL12.glTexSubImage3D(target, 0, x, y, z, width, height, depth, df, DEFAULT_DATA_TYPE, data);
				break;
		}
    }
    
	void glTexImage(int target, int width, int height,
					int depth, Format internalFormat, 
					Format dataFormat, ByteBuffer data) {
    	bind();
    	this.width = texWidth = width;
    	this.height = texHeight = height;
    	this.depth = texDepth = depth;
    	this.internalFormat = internalFormat;
		boolean compressed = dataFormat.isCompressed();
    	//if we are forcing POT or if POT is not supported...
        boolean usePOT = Texture.isForcePOT() || !Texture.isNPOTSupported();
        if (usePOT) {
        	if (compressed) //TODO: add NPOT support for compressed textures
        		throw new IllegalArgumentException("use POT sizes for compressed textures");
            texWidth = Texture.toPowerOfTwo(width);
            texHeight = Texture.toPowerOfTwo(height);
            texDepth = Texture.toPowerOfTwo(depth);
            
        }
        normalizedWidth = texWidth!=0 ? width / (float)texWidth : 0;
        normalizedHeight = texHeight!=0 ? height / (float)texHeight : 0;
        normalizedDepth = texDepth!=0 ? depth / (float)texDepth : 0;
        
		int f = internalFormat.getGLFormat(); // internal format GL type
		int df = dataFormat.getGLFormat(); // data format GL type
		
		if (dataFormat.isCompressed() && !internalFormat.equals(dataFormat)) {
			Utils.warn("internalFormat must match compressed dataFormat;" +
					" format will be stored using dataFormat");
		}
		
		boolean hasNewSize = width!=texWidth || height!=texHeight || depth!=texDepth;
		
		//if we're forcing POT or if the given data is null
		int size = dataFormat.getBytesPerPixel() * texWidth * texHeight * texDepth;
		ByteBuffer d = hasNewSize || data == null 
				? BufferUtils.createByteBuffer(size) 
				: data;
		
		switch (target) {
			case TEXTURE_1D:
				if (compressed)
					GL13.glCompressedTexImage1D(target, 0, df, texWidth, 0, d);
				else
					GL11.glTexImage1D(target, 0, f, texWidth, 0, df, DEFAULT_DATA_TYPE, d);
				break;
			case TEXTURE_2D:
				if (compressed) 
					GL13.glCompressedTexImage2D(target, 0, df, texWidth, texHeight, 0, d);
				else
					GL11.glTexImage2D(target, 0, f, texWidth, texHeight, 0, df, DEFAULT_DATA_TYPE, d);
				break;
			case TEXTURE_3D:
				if (compressed)
					GL13.glCompressedTexImage3D(target, 0, df, texWidth, texHeight, texDepth, 0, d);
				else
					GL12.glTexImage3D(target, 0, f, texWidth, texHeight, texDepth, 0, df, DEFAULT_DATA_TYPE, d);
				break;
		}
		
		if (data!=null && hasNewSize) 
			glTexSubImage(target, 0, 0, 0, width, height, depth, dataFormat, data);
		
    }
	

    public abstract void setWrap(int wrap);
}
