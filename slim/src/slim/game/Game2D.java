package slim.game;

import static org.lwjgl.opengl.GL11.*;

import slim.app.App;

public abstract class Game2D implements Game {

	protected void setupViewport(App app) {
		int width = app.getWidth(), height = app.getHeight();
		glViewport(0, 0, width, height);
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		glOrtho(0, width, height, 0, 1, -1);
		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();
	}
	
	public final void createContext(App app) {
		glDisable(GL_DEPTH_TEST);
		setupViewport(app);
		create(app);
	}
	
	public final void renderContext(App app) {
		render(app);
	}

	public final void updateContext(App app, int delta) {
		update(app, delta);
	}

	public final void destroyContext(App app) {
		destroy(app);
	}

	public void onResize(App app) {
		setupViewport(app);
	}

	public abstract void create(App app);
	public abstract void destroy(App app);
	public abstract void render(App app);
	public abstract void update(App app, int delta);
}
