package slim.game;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_PROJECTION;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glLoadIdentity;
import static org.lwjgl.opengl.GL11.glMatrixMode;
import static org.lwjgl.opengl.GL11.glOrtho;
import static org.lwjgl.opengl.GL11.glViewport;
import slim.app.App;
import slim.core.SlimException;

public abstract class Game2D implements AppContext {

	protected void setupViewport(App app) {
		int width = app.getWidth(), height = app.getHeight();
		glViewport(0, 0, width, height);
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		glOrtho(0, width, height, 0, 1, -1);
		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
	}
	
	public final void createContext(App app) throws SlimException {
		glDisable(GL_DEPTH_TEST);
		setupViewport(app);
		create(app);
	}
	
	public final void renderContext(App app) throws SlimException {
		render(app);
	}

	public final void updateContext(App app, int delta) throws SlimException {
		update(app, delta);
	}

	public final void destroyContext(App app) throws SlimException {
		destroy(app);
	}

	public void onResize(App app) throws SlimException {
		setupViewport(app);
	}

	public abstract void create(App app) throws SlimException;
	public abstract void destroy(App app) throws SlimException;
	public abstract void render(App app) throws SlimException;
	public abstract void update(App app, int delta) throws SlimException;
}
