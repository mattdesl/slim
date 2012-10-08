package slim.test.bare;

import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Arrays;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import slim.shader2.ShaderProgram;
import slim.texture.Texture;
import slim.texture.Texture2DArray;
import slim.util2.Utils2;
import slimold.Color;
import slimold.SlimException;
import de.matthiasmann.twl.Widget;

public class GameTest extends GUITestBase {
	public static void main(String[] args) throws SlimException {
		System.out.println(Texture.toPowerOfTwo(1));
		new GameTest().start();
	}

	Texture2DArray texture;

	ShaderProgram program;
	// Texture2DArray

	Widget widgets;
	
	public void putSubSection(ByteBuffer src, Texture.Format format,
			int texWidth, int texHeight,
			int x, int y, int width, int height, ByteBuffer dest) {
		int bpp = format.getBytesPerPixel();
		int offset = x * bpp;
		offset += y * bpp * texWidth;
		src.position(offset);
		for (int j=0; j<height; j++) {
			src.position(offset + j * texWidth * bpp);
			for (int i=0; i<width * bpp; i++) {
				dest.put(src.get());
			}
		}
	}
	
	private int translate(byte b) {
		if (b < 0) {
			return 256 + b;
		}

		return b;
	}

	@Override
	public void init() throws SlimException {
		init2D();
//		Texture.setForcePOT(true);
		
		// creates an RGB texture
		int bpp = 3;
		texture = new Texture2DArray(2, 2, 2, Texture.Format.RGB);
		
		texture.setFilter(Texture.FILTER_NEAREST);
		
		int w = 2, h = 2;
		ByteBuffer buf = BufferUtils.createByteBuffer(w * h * bpp);
		buf.put((byte)255).put((byte)0).put((byte)0);
		buf.put((byte)255).put((byte)0).put((byte)0);
		
		buf.put((byte)0).put((byte)0).put((byte)255);
		buf.put((byte)0).put((byte)0).put((byte)255);
		buf.flip();
		texture.uploadSubImage(0, 0, 0, w, h, 1, Texture.Format.RGB, buf);
		
		
//		int texW = 5, texH = 3;
//	    buf = BufferUtils.createByteBuffer(bpp * texW * texH);
//		buf.put((byte)255).put((byte)0).put((byte)0);
//		buf.put((byte)5).put((byte)2).put((byte)7);
//		buf.put((byte)255).put((byte)100).put((byte)100);
//		buf.put((byte)255).put((byte)255).put((byte)255);
//		buf.put((byte)255).put((byte)255).put((byte)255);
//		
//		buf.put((byte)155).put((byte)100).put((byte)100);
//		buf.put((byte)155).put((byte)155).put((byte)155);
//		buf.put((byte)155).put((byte)100).put((byte)100);
//		buf.put((byte)255).put((byte)0).put((byte)255);
//		buf.put((byte)0).put((byte)5).put((byte)0);
//
//		buf.put((byte)155).put((byte)100).put((byte)100);
//		buf.put((byte)255).put((byte)0).put((byte)255);
//		buf.put((byte)155).put((byte)155).put((byte)155);
//		buf.put((byte)0).put((byte)5).put((byte)0);
//		buf.put((byte)155).put((byte)100).put((byte)100);
//		buf.flip();
////		texture.uploadSubImage(0, 0, texW, texH, Texture.Format.RGB, buf);
//		
//		int subW = 4, subH = 3;
//		ByteBuffer sub = BufferUtils.createByteBuffer(subW * subH * bpp);
//		putSubSection(buf, Texture.Format.RGB, texW, texH, 0, 0, subW, subH, sub);
//		sub.flip();
//		texture.uploadSubImage(1, 0, subW, subH, Texture.Format.RGB, sub);
		
		
		widgets = new PreviewWidgets();
		widgets.setTheme("previewwidgets");
		widgets.setPosition(10, 10);

		//getRootPane().add(widgets);
	}

	protected void layoutRootPane() {
		widgets.adjustSize();
	}

	@Override
	public void render() throws SlimException {
		// image size, e.g. 550x200
		float width = texture.getWidth();
		float height = texture.getHeight();
		// the physical width of the texture which will be used in glTexCoord
		// (generally a float between 0 and 1)
		float textureWidth = texture.getNormalizedWidth();
		float textureHeight = texture.getNormalizedHeight();
		// texture offsets, for texture atlas purposes. leave at 0 for full
		// image
		float textureOffsetX = 0, textureOffsetY = 0;
		// where on screen to draw the image
		float x = 10;
		float y = 10;
		GL11.glScalef(12f, 12f, 1f);
		
		float layer = 1;
		
		Color.white.bind();
		Texture.enable(texture.getTarget());
		texture.bind();
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glTexCoord3f(textureOffsetX, textureOffsetY, layer);
        GL11.glVertex2f(x, y);
        GL11.glTexCoord3f(textureOffsetX, textureOffsetY + textureHeight, layer);
        GL11.glVertex2f(x, y + height);
        GL11.glTexCoord3f(textureOffsetX + textureWidth, textureOffsetY
              + textureHeight, layer);
        GL11.glVertex2f(x + width, y + height);
        GL11.glTexCoord3f(textureOffsetX + textureWidth, textureOffsetY, layer);
        GL11.glVertex2f(x + width, y);
		GL11.glEnd();
		
		
//		GL11.glBegin(GL11.GL_QUADS);
//		GL11.glTexCoord2f(textureOffsetX, textureOffsetY);
//        GL11.glVertex2f(x, y);
//        GL11.glTexCoord2f(textureOffsetX, textureOffsetY + textureHeight);
//        GL11.glVertex2f(x, y + height);
//        GL11.glTexCoord2f(textureOffsetX + textureWidth, textureOffsetY
//              + textureHeight);
//        GL11.glVertex2f(x + width, y + height);
//        GL11.glTexCoord2f(textureOffsetX + textureWidth, textureOffsetY);
//        GL11.glVertex2f(x + width, y);
//		GL11.glEnd();
	}

	@Override
	public void update(int delta) throws SlimException {
	}

	@Override
	public URL getThemeURL() throws SlimException {
		return Utils2.getResource("res/gui/chutzpah/chutzpah.xml");
	}
}
