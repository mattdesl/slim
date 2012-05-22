package slim.texture;

import java.io.IOException;
import java.net.URL;

import slim.texture.io.ImageDecoder;
import slim.texture.io.ImageDecoderBMP;
import slim.texture.io.ImageDecoderDDS;
import slim.texture.io.ImageDecoderJPEG;
import slim.texture.io.ImageDecoderPNG;
import slim.texture.io.ImageDecoderTGA;

/**
 *
 * @author davedes
 */
public class TextureLoader {

    private static TextureLoader loader = new TextureLoader();
    
    public static TextureLoader get() {
        return loader;
    }
    
    public static void set(TextureLoader t) {
        if (t==null)
            throw new IllegalArgumentException("loader cannot be null");
        loader = t;
    }
    
    /**
     * Override for additional image decoders; return null if extension
     * is not supported.
     * 
     * @param url the URL of the image
     * @return the implemented image decoder or null
     */
    protected ImageDecoder createDecoderImpl(URL url) throws IOException {
        return null;
    }
    
    /**
     * Creates an image decoder based on the URL's extension, with the
     * following formats supported: PNG, TGA, JPG/JPEG and BMP. For additional
     * formats, override createLoaderImpl.
     * 
     * @param url the URL of the image
     * @return an image decoder
     */
    public ImageDecoder createDecoder(URL url) throws IOException {
        String p = url.getPath();
        if (endsWithIgnoreCase(p, ".png")) {
            return new ImageDecoderPNG(url);
        } else if (endsWithIgnoreCase(p, ".tga")) {
            return new ImageDecoderTGA(url);
        } else if (endsWithIgnoreCase(p, ".jpg") || endsWithIgnoreCase(p, ".jpeg")) {
            return new ImageDecoderJPEG(url);
        } else if (endsWithIgnoreCase(p, ".dds")) {
        	return new ImageDecoderDDS(url);
        } else if (endsWithIgnoreCase(p, ".bmp")) {
            return new ImageDecoderBMP(url);
        }
        ImageDecoder d = createDecoderImpl(url);
        if (d==null)
            throw new IOException("no decoder found for the given image: '"
                    + p + "'");
        return d;
    }
    
    public final static boolean endsWithIgnoreCase(String str, String end) {
        return str.regionMatches(true, str.length()-
                end.length(), end, 0, end.length());
    }    
}
