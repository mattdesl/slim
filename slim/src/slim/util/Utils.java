package slim.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.lwjgl.BufferUtils;
import org.lwjgl.Sys;

import slim.Color;
import slim.SlimException;
import slim.texture.Texture;
import slim.texture.Texture2D;
import slim.texture.TextureLoader;
import slim.texture.io.ImageDecoder;

public class Utils {
	public static final long DEFAULT_SEED = 1331106565117L;
	private static final Random RND = new Random();
    private static ResourceLocator resourceLocator = new DefaultResourceLocator();
    
    public static float rnd() {
    	return rndFloat();
    }
    
    public static int rndInt() {
    	return RND.nextInt();
    }
    
    public static Color rndColor() {
    	return new Color(rnd(), rnd(), rnd());
    }
    
    public static int rnd(int high) {
    	return RND.nextInt(high);
    }
    
    public static float rndFloat() {
    	return RND.nextFloat();
    }
    
    public static int rnd(int low, int high) {
        if (low==high)
            return low;
        return RND.nextInt(high - low) + low;
    }
    
    public static float rnd(float low, float high) {
        if (low==high)
            return low;
        return low + (RND.nextFloat() * (high - low));
    }
    
    public static void log(String msg) {
    	getLogger().log(Level.INFO, msg);
    }
    
    public static void warn(String msg) {
    	getLogger().log(Level.WARNING, msg);
    }
    
    public static void error(String msg) {
    	error(msg, null);
    }
    
    public static Logger getLogger() {
    	return Logger.getLogger(Utils.class.getName());
    }
    
    public static void error(String msg, Throwable t) {
    	if (t!=null) getLogger().log(Level.SEVERE, msg, t);
    	else getLogger().log(Level.SEVERE, msg);
    }
    
    public static ImageDecoder uploadImageData(Texture2D dst, URL imageData, int x, int y) throws IOException {
    	ImageDecoder d = TextureLoader.get().createDecoder(imageData);
        if (!d.open()) 
            throw new IOException("could not open a decoder for "+imageData.getPath());
        int width = d.getWidth();
        int height = d.getHeight();
        Texture2D.Format fmt = d.getFormat();
        int perPixel = fmt.getBytesPerPixel();
        ByteBuffer buf = null;
        try {
            buf = BufferUtils.createByteBuffer(width * height * perPixel);
            d.decode(buf);
        } finally {
            d.close();
        }
        buf.flip();
        dst.uploadSubImage(x, y, width, height, fmt, buf);
        return d;
    }
    
	public static Texture2D createTexture(BufferedImage image) {
		int w = image.getWidth();
		int h = image.getHeight();
		int[] pixels = new int[w * h];
		image.getRGB(0, 0, w, h, pixels, 0, w);
		
		ByteBuffer buffer = BufferUtils.createByteBuffer(w * h * 4);
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				int pixel = pixels[y * w + x];
				buffer.put((byte) ((pixel >> 16) & 0xFF)); // Red component
				buffer.put((byte) ((pixel >> 8) & 0xFF)); // Green component
				buffer.put((byte) (pixel & 0xFF)); // Blue component
				buffer.put((byte) ((pixel >> 24) & 0xFF)); // Alpha component.
															// Only for RGBA
			}
		}
		buffer.flip(); 
		return new Texture2D(w, h, Texture.Format.RGBA, Texture.Format.RGBA, buffer);
	}
    
    public static URL getResource(String str) throws SlimException {
    	URL u = getResourceLocator().getResource(str);
    	if (u==null)
    		throw new SlimException("could not find resource "+str);
    	return u;
    }
    
    public static InputStream getResourceAsStream(String str) throws SlimException {
    	InputStream in = getResourceLocator().getResourceAsStream(str);
    	if (in==null)
    		throw new SlimException("could not find resource "+str);
    	return in;
    }
    
    public static void setResourceLocator(ResourceLocator r) {
    	resourceLocator = r;
    }
    
    public static ResourceLocator getResourceLocator() {
    	return resourceLocator;
    }
	
    public static final class DefaultResourceLocator implements ResourceLocator {

    	public static final File ROOT = new File(".");
    	
        private static File createFile(String ref) {
            File file = new File(ROOT, ref);
            if (!file.exists()) {
                file = new File(ref);
            }
            
            return file;
        }
        
	    public InputStream getResourceAsStream(String ref) {
	        String cpRef = ref.replace('\\', '/');
	        InputStream in = Utils.class.getClassLoader().getResourceAsStream(cpRef);
	        if (in==null) { // try file system
	            try { return new FileInputStream(createFile(ref)); }
	            catch (IOException e) {}
	        }
	        return in;
	    }
	    
	    public URL getResource(String ref) {
	        String cpRef = ref.replace('\\', '/');
	        URL url = Utils.class.getClassLoader().getResource(cpRef);
	        if (url==null) {
	            try { 
	                File f = createFile(ref);
	                if (f.exists())
	                    return f.toURI().toURL();
	            } catch (IOException e) {}
	        }
	        return url;
	    }
    }
    
    public static interface ResourceLocator {
    	public URL getResource(String str);
    	public InputStream getResourceAsStream(String str);
    }
}
