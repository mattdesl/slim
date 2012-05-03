package slim.util;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

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
}
