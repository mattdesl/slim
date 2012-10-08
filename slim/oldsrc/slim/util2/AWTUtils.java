package slim.util2;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

public class AWTUtils {

	public BufferedImage createScaledCopy(BufferedImage original, int width, int height) {
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g = img.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		int w = original.getWidth();
		int h = original.getHeight();
		g.drawImage(original, 0, 0, width, height, 0, 0, w, h, null);
		g.dispose();
		return img;
	}
	
	/**
	 * Quickly copies the specified pixels from the source image to the destination image -- 
	 * both images need to be TYPE_INT_ARGB. If the region of pixels falls outside of the range
	 * of either array, then an ArrayIndexOutOfBoundsException will be thrown.
	 * 
	 * @param src the source image
	 * @param dst the destination image
	 * @param srcX the x position of the source image to start copying
	 * @param srcY the y position of the source image to start copying
	 * @param srcW the width of the section to copy from source
	 * @param srcH the height of the section to copy from source
	 * @param dstX the x position to place the copied pixels at on the destination image
	 * @param dstY the y position to place the copied pixels at on the destination image
	 */
	public void copyPixels(BufferedImage src, BufferedImage dst, int srcX, int srcY, int srcW, int srcH, int dstX, int dstY) {
		int[] srcbuf = ((DataBufferInt)src.getRaster().getDataBuffer()).getData();
		int[] dstbuf = ((DataBufferInt)dst.getRaster().getDataBuffer()).getData();
		int srcOff = srcX + srcY * src.getWidth();
		int dstOff = dstX + dstY * dst.getWidth();
		for (int y=0; y<srcH; y++) { //copy every row of src starting at offset
			System.arraycopy(srcbuf, srcOff, dstbuf, dstOff, srcW);
			srcOff += src.getWidth();
			dstOff += dst.getWidth();
		}
	}
}
