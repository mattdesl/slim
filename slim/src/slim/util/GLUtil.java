package slim.util;

import static org.lwjgl.opengl.GL11.*;

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
}
