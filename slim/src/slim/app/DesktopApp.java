package slim.app;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;

import slim.core.SlimException;
import slim.game.AppContext;

public class DesktopApp extends App {
	
	public DesktopApp(AppContext context) {
		super(context);
	}

	public DesktopApp(AppContext context, int width, int height) throws SlimException {
		this(context);
		setDisplayMode(width, height);
	}
	
	public DesktopApp(AppContext context, int width, int height, boolean fullscreen) throws SlimException {
		this(context, width, height);
		setFullscreen(fullscreen);
	}

	public void setDisplayMode(DisplayMode mode) throws SlimException {
		try {
			Display.setDisplayMode(mode);
		} catch (LWJGLException e) {
			throw new SlimException("Could not set display mode: "+e.getMessage(), e);
		}
	}
	
	public void setDisplayMode(int width, int height) throws SlimException {
		setDisplayMode(new DisplayMode(width, height));
	}
	
	public void setTitle(String title) {
		Display.setTitle(title);
	}
	
	public String getTitle() {
		return Display.getTitle();
	}
	
	@Override
	public void create() throws SlimException {
		try {
			Display.create();
		} catch (LWJGLException e) {
			throw new SlimException("Could not create display: "+e.getMessage(), e);
		}
	}
	
	@Override
	public void destroy() throws SlimException {
		Display.destroy();
	}
	
	@Override
	public int getWidth() {
		return Display.getWidth();
	}
	
	@Override
	public int getHeight() {
		return Display.getHeight();
	}

	public void setFullscreen(boolean fullscreen) throws SlimException {
		try {
			Display.setFullscreen(fullscreen);
		} catch (LWJGLException e) {
			throw new SlimException(e.getMessage(), e);
		}
	}
	
	@Override
	public boolean isFullscreen() {
		return Display.isFullscreen();
	}
	
	@Override
	public boolean running() {
		return super.running() && !Display.isCloseRequested();
	}
	
	@Override
	protected void onFrameStart() throws SlimException {
		if (Display.wasResized())
			game.onResize(this);
	}

	@Override
	protected void onFrameEnd() throws SlimException {
		Display.update();
		int f = getTargetFPS();
		if (f > 0 && f != NO_TARGET_FPS)
			Display.sync(f);
	}
}
