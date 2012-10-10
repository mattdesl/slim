package slim.test.bare;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.BasicGame;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.opengl.EmptyImageData;
import org.newdawn.slick.opengl.ImageData;
import org.newdawn.slick.opengl.InternalTextureLoader;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.renderer.Renderer;
import org.newdawn.slick.opengl.renderer.SGL;
import org.newdawn.slick.util.FastTrig;

public class PerPixelTexture1 extends BasicGame {

	public static void main(String[] args) {
//		ByteBuffer buf = BufferUtils.createByteBuffer(10);
//		buf.order(ByteOrder.BIG_ENDIAN);
//		byte[] stuff = { (byte)1, (byte)2, (byte)3, (byte)4 };
//		buf.put(stuff);
//		buf.flip();
//		System.out.println(buf.get());
//		System.out.println(buf.get());
//		System.out.println(buf.get());
//		System.out.println(buf.get());
		
//		System.exit(0);
		try {
			new AppGameContainer(new PerPixelTexture1(), 800, 600, false).start();
		} catch (SlickException e) {
			e.printStackTrace();
		}
	}

	public PerPixelTexture1() {
		super("Per Pixel");
	}
	
	PixelData pixels;
	PixelData gradient;
	Image image;
	float time;
	
	boolean running = false;
	int mode = 1;
	
	public void init(GameContainer container) throws SlickException {
		container.getGraphics().setBackground(Color.darkGray);

		final int WIDTH = 64, HEIGHT = 64;
		image = new Image(WIDTH, HEIGHT);
		
		//for the sake of demonstration, let's start with basic shapes
		pixels = new PixelData(WIDTH, HEIGHT);
		//first let's fill the data with white pixels
		pixels.clear(0xFF); //flips position to (0, 0)
		
		//then let's move to (25, 25) ...
		pixels.position(25, 25);
		
		//... and start adding pixels directly (this is fast!)
		//chaining is allowed too
		pixels.putPixel(Color.red).putPixel(Color.black); 
		pixels.putPixel(Color.red).putPixel(Color.black); 
		pixels.putPixel(Color.red).putPixel(Color.black); 
		pixels.putPixel(Color.red).putPixel(Color.black);
		
		//or we could set pixels by location
		for (int i=0; i<20; i++) {
			Color c = new Color(i/20f, i/20f, i/20f, 1f);
			pixels.setPixel(10+i, 20, c);
			pixels.setPixel(10+i, 22, c);
		}
		
		//when we're ready to push the pixels to the texture:
		pixels.upload(image.getTexture());
	}

	void drawHeart(PixelData data, int x, int y, float width, float height, byte white, float tween) {
		final float tx = x/width /2f;
		final float ty = y/height /2f;
		
//		float yy = (x+width) *2* (float)Math.pow(3f, (float)Math.sin(time/2f)) * (float)Math.sin((y)/2000f) + time * 2f;
//		float r = (float)Math.sin(yy);
//		byte b = (byte)(r*255);
//		data.putPixel(b, -b, white, white);
		
		
//		float cLength = (float)Math.sqrt(tx * tx + ty * ty));
//		
//		vec2 uv =  tx + ( cPos / cLength ) * cos( cLength * 15. - time * 4.0 ) * .05;
//		vec2 col =  uv;
//
//		gl_FragColor = vec4( col, .3, 1.0 );
		
//		float ANG = (float)(2 * Math.PI/15f);
//		
//		
//		#define ANG (2.0*PI/15.0)
//		vec2 r = gl_FragCoord.xy / resolution.y - vec2(.5, .5);
//		float c = float(mod(atan(r.y, r.x), ANG) >= ANG/2.0 && length(r) > .3);
//		gl_FragColor = vec4(.742, 0, .148, 1) + c*vec4(.268, 1, .852, 1)
	}
	
	
	
	void drawGradient(PixelData data, int x, int y, float width, float height, byte white, float tween) {
		final float tx = x/width - 0.5f;
		final float ty = y/height - 0.5f;
		float r = Math.max(0, 1 - 1.5f*(float)Math.sqrt(tx * tx + ty * ty));
		r *= tween;
		//convert to bytes
		final byte b = (byte)(r*255);
		final byte red = (byte)0x44;
		//if we are placing successive pixels,
		//we can use relative "put" method for 
		//an optimization
		data.putPixel(red, b, b, white);
	}
	
	public void render(GameContainer container, Graphics g)
			throws SlickException {
		image.draw(50, 50, 1f);
		
		g.drawString("Press SPACE to start modifying pixels on the fly", 10, 25);
	}

	public void update(GameContainer container, int delta)
			throws SlickException {
		time += delta / 1000f;
		
		Input in = container.getInput();
		if (in.isKeyPressed(Input.KEY_SPACE)) {
			running = !running;
		} else {
			
		}
		
		if (running) {
			final float tween = 0.5f + .5f*(float)Math.sin(time);
			pixels.position(0);
			final float width = pixels.getWidth();
			final float height = pixels.getHeight();
			final byte white = (byte)0xFF;
			for (int x=0; x<width; x++) {
				for (int y=0; y<height; y++) {
					if (mode==0)
						drawGradient(pixels, x, y, width, height, white, tween);
					else if (mode==1) {
						drawHeart(pixels, x, y, width, height, white, tween);
					}
				}
			}
			pixels.upload(image.getTexture());
		}
		
		
	}

	/**
	 * Renders a horizontal scan line to the data.
	 */
	public void rect(PixelData data, Color color, int x, int y, int width, int height) {
		//move to initial position
		data.position(x, y);
		//for each row...
		for (int i=0; i<height; i++) {
			//draw the row horizontally
			for (int j=0; j<width; j++) {
				data.putPixel(color);
			}
			y++;
			data.position(x, y);
		}
	}
	
	
	/**
	 * 
	 * @author davedes
	 */
	static class PixelData {
		
		/**
		 * Convenience method to create a texture with a given internal format.
		 * @param width the width of the empty texture
		 * @param height the height of the empty texture
		 * @param filter the filter to use
		 * @param format the internal format
		 * @return a generated texture
		 */
		public static Texture createTexture(int width, int height, ImageData.Format format, int filter) throws SlickException {
			EmptyImageData data = new EmptyImageData(width, height);
			ByteBuffer dataBuffer = data.getImageBufferData();
			String ref = "pixelhandler:"+width+"x"+height+":"+format.toString();
			try {
				return InternalTextureLoader.get().createTexture(data, dataBuffer, ref,
							GL11.GL_TEXTURE_2D, filter, filter, false, format);
			} catch (IOException e) {
				throw new SlickException("Error generating texture", e);
			}
		}
		
		private ByteBuffer pixels;
		private ImageData.Format format;
		private int bpp = 4;
		private int width, height;
		
		/**
		 * Creates a PixelData buffer with the specified size, using RGBA format.
		 * 
		 * @param width the width in pixels of our data
		 * @param height the height in pixels of our data
		 */
		public PixelData(int width, int height) {
			this(width, height, ImageData.Format.RGBA);
		}
		
		/**
		 * Creates a PixelData buffer with the specified size and format.
		 * 
		 * Note that Slick currently loads textures with an internal RGBA format;
		 * this means that even if we upload, say, 2-component (e.g. GRAYALPHA)
		 * texture data, it will eventually be stored in OpenGL video memory 
		 * using RGBA. For better performance and memory management, 
		 * create textures with the same internal format as the format given to PixelData.
		 * 
		 * The static 'createTexture' utility method is intended for this purpose. 
		 * 
		 * @param width the width in pixels of our data
		 * @param height the height in pixels of our data
		 * @param format the desired format to use during uploading
		 */
		public PixelData(int width, int height, ImageData.Format format) {
			this.format = format;
			this.width = width;
			this.height = height;
			this.bpp = format.getColorComponents();
			this.pixels = BufferUtils.createByteBuffer(width * height * bpp);
		}
		
		/**
		 * Sets the pixel data to the given array, which should be less
		 * than the size of length().
		 * @param pixelData the new pixel data
		 */
		public void set(byte[] pixelData) {
			pixels.clear();
			pixels.put(pixelData);
			pixels.flip();
		}
		
		/**
		 * Clears the pixel array to 0x00 (i.e. transparent black for RGBA).
		 */
		public void clear() {
			clear(0x00);
		}
		
		/** 
		 * Clears the pixel array to the specified single-component
		 * color, i.e. to clear to white you would use:
		 * <pre>    clear(0xFF)</pre>
		 * 
		 * @param value the byte value to fill the array with
		 */
		public void clear(int value) {
			pixels.clear();
			byte b = (byte)value;
			for (int i=0; i<pixels.capacity(); i++) 
				pixels.put(b);
			pixels.flip();
		}

		/**
		 * Uploads the pixel data to the given texture at the top-left origin (0, 0).
		 * This only needs to be called once, after the pixel data has changed.
		 */
		public void upload(Texture texture) {
			upload(texture, 0, 0);
		}
		
		/**
		 * Uploads the pixel data to the given texture at the specified position.
		 * This only needs to be called once, after the pixel data has changed.
		 * 
		 * @param x the x position to place the pixel data on the texture
		 * @param y the y position to place the pixel data on the texture
		 */
		public void upload(Texture texture, int x, int y) {
			if (x+width > texture.getTextureWidth() || y+height > texture.getTextureHeight())
				throw new IndexOutOfBoundsException("pixel data won't fit in given texture");
			pixels.position(pixels.capacity());
			pixels.flip();
			int glFmt = format.getOGLType();
			final SGL GL = SimpleGfx.get();
			texture.bind();
			GL.glTexSubImage2D(GL.GL_TEXTURE_2D, 0, x, y, width, height, 
					glFmt, GL.GL_UNSIGNED_BYTE, pixels);
		}
		
		/**
		 * Returns a color representation of the given pixel.
		 * @param x the x position of the pixel
		 * @param y the y position of the pixel
		 * @return a Color representing this pixel
		 */
		public Color getPixel(int x, int y) {
			position(x, y);
			return getPixel();
		}
		
		int translate(byte b) {
			return b<0 ? 256+b : b;
		}
		
		/**
		 * Relative <i>get</i> method which handles color components based on image formats.
		 * Does not offset the buffer. 
		 * 
		 * Color is converted like so based on the Format's color components:
		 * - If getColorComponents returns 1 and hasAlpha is true: Color(0xFF, 0xFF, 0xFF, A)
		 * - If getColorComponents returns 1 and hasAlpha is false: Color(L, L, L, 0xFF)
		 * - If getColorComponents returns 2: Color(L, L, L, A)
		 * - If getColorComponents returns 3: Color(R, G, B, 0xFF)
		 * - If getColorComponents returns 4: Color(R, G, B, A)
		 * 
		 * See setPixel for details.
		 * @return a Color representation for this pixel
		 */
		public Color getPixel() {
			boolean hasAlpha = getFormat().hasAlpha();
			int c1 = translate(pixels.get());
			if (bpp==1)
				return hasAlpha ? new Color(0xFF, 0xFF, 0xFF, c1) : new Color(c1, c1, c1, 0xFF);
			int c2 = translate(pixels.get());
			if (bpp==2)
				return new Color(c1, c1, c1, c2);
			int c3 = translate(pixels.get());
			if (bpp==3)
				return new Color(c1, c2, c3, 0xFF);
			int c4 = translate(pixels.get());
			return new Color(c1, c2, c3, c4);
		}
		
		/**
		 * Relative <i>put</i> method which handles color components based on image formats.
		 * Does not offset the buffer.
		 * 
		 * See setPixel for details.
		 * @param c the color to put
		 */
		public PixelData putPixel(int r, int g, int b, int a) {
			boolean hasAlpha = getFormat().hasAlpha();
			if (bpp==1) {
				pixels.put((byte)(hasAlpha ? a : (r+g+b)/3));
			} else if (bpp==2) {
				pixels.put((byte)((r+g+b)/3)).put((byte)a);
			} else {
				pixels.put((byte)r).put((byte)g).put((byte)b);
				if (bpp>=4)
					pixels.put((byte)a);
			}
			return this;
		}
		
		
		/**
		 * Sets the RGBA pixel at the given (x, y) location.
		 * 
		 * The Format determines what the resulting color will be:
		 * - If getColorComponents returns 1 and hasAlpha is true, 'a' will be used
		 * - If getColorComponents returns 1 and hasAlpha is false, 'rgb' will be averaged for luminance
		 * - If getColorComponents returns 2, 'rgb' will be averaged for luminance and and 'a' will be used for alpha
		 * - If getColorComponents returns 3 'r', 'g', 'b' will be used
		 * - If getColorComponents returns 4 'r', 'g', 'b' and 'a' will be used
		 * 
		 * In this fashion the following formats will produce expected results
		 * when using the values from Slick's Color class: RGBA, RGB, GRAYALPHA,
		 * GRAY, ALPHA.
		 * 
		 * @param x the x position to place the pixel
		 * @param y the y position to place the pixel
		 * @param r the red, luminance or alpha component
		 * @param g the green component
		 * @param b the blue component
		 * @param a the alpha component
		 */
		public void setPixel(int x, int y, int r, int g, int b, int a) {
			position(x, y);
			putPixel(r, g, b, a);
		}
		
		/**
		 * Calls setPixel with an alpha value of 0xFF.
		 * 
		 * @param x the x position to place the pixel
		 * @param y the y position to place the pixel
		 * @param r the red component
		 * @param g the green component
		 * @param b the blue component
		 */
		public void setPixel(int x, int y, int r, int g, int b) {
			setPixel(x, y, r, g, b, 0xFF);
		}
		
		/**
		 * A convenience method to set a given pixel's color using Slick's color class.
		 * See the other setPixel method for details.
		 * 
		 * @param x the x position of the pixel
		 * @param y the y position of the pixel
		 * @param rgba the RGBA components of the pixel
		 * @see setPixel(int x, int y, int r, int g, int b, int a) 
		 */
		public void setPixel(int x, int y, Color rgba) {
			setPixel(x, y, rgba.getRed(), rgba.getGreen(), rgba.getBlue(), rgba.getAlpha());
		}

		/**
		 * A convenience method to <i>put</i> a given pixel's color using Slick's color class.
		 * See the other putPixel method for details.
		 * 
		 * @param rgba the RGBA components of the pixel
		 */
		public PixelData putPixel(Color rgba) {
			return putPixel(rgba.getRed(), rgba.getGreen(), rgba.getBlue(), rgba.getAlpha());
		}

		/**
		 * The total capacity of this pixel buffer in bytes.
		 * @return total bytes contained by the backing array
		 */
		public int length() {
			return pixels.capacity();
		}
		
		/**
		 * Returns the width in pixels.
		 * @return the width of the pixel data region
		 */
		public int getWidth() {
			return width;
		}
		
		/**
		 * Returns the height in pixels.
		 * @param the height of the pixel data region
		 */
		public int getHeight() {
			return height;
		}
		
		/**
		 * Returns the format defined by this pixel data.
		 * @return the image format
		 */
		public ImageData.Format getFormat() {
			return format;
		}

		/**
		 * Returns the backing byte buffer.
		 * @return the byte buffer
		 */
		public ByteBuffer buffer() {
			return pixels;
		}
		
		/**
		 * Sets the position of this buffer, used alongside relative getPixel and putPixel methods.
		 * @param pos the position; the index for the buffer
		 */
		public void position(int pos) {
			pixels.position(pos);
		}
		
		/**
		 * Sets the position of the buffer to the offset given by the (x, y) coordinates, in pixels.
		 * @param x the x pixel to offset to
		 * @param y the y pixel to offset to
		 */
		public void position(int x, int y) {
			if ((x < 0) || (x >= width) || (y < 0) || (y >= height)) 
				throw new IndexOutOfBoundsException("Specified location: "+x+","+y+" outside of region");
			int offset = ((x + (y * width)) * bpp);
			position(offset);
		}
	}
}