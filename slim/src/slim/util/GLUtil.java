package slim.util;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glTexCoord2f;
import static org.lwjgl.opengl.GL11.glVertex2f;
import slim.texture.Texture;

public class GLUtil {
	
	public static void clear() {
		glClear(GL_COLOR_BUFFER_BIT);
	}
	
	public static void clearColor(float r, float g, float b, float a) {
		glClearColor(r, g, b, a);
	}
	
	public static void rect(int x, int y, int w, int h) {
		glBegin(GL_QUADS);
		glVertex2f(x, y);
		glVertex2f(x+w, y);
		glVertex2f(x+w, y+h);
		glVertex2f(x, y+h);
		glEnd();
	}
	
	public static void rect(int x, int y, int w, int h, float uStart, float vStart, float uWidth, float vHeight) {
		glBegin(GL_QUADS);
		glTexCoord2f(uStart, vStart);
		glVertex2f(x, y);
		glTexCoord2f(uStart+uWidth, vStart);
		glVertex2f(x+w, y);
		glTexCoord2f(uStart+uWidth, vStart+vHeight);
		glVertex2f(x+w, y+h);
		glTexCoord2f(uStart, vStart+vHeight);
		glVertex2f(x, y+h);
		glEnd();
	}
}
