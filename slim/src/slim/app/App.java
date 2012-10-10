package slim.app;

import org.lwjgl.Sys;
import org.lwjgl.opengl.Display;

import slim.core.SlimException;
import slim.game.AppContext;

public abstract class App {

	public static final int NO_TARGET_FPS = -1;
	
	protected AppContext game;

	/** time at last frame */
	protected long lastFrame;
	/** frames per second */
	protected int fps, recordedFPS;
	/** last fps time */
	protected long lastFPS;
	protected boolean running;
	
	protected int targetFPS = 60;
	
	protected App(AppContext game) {
		this.game = game;
	}
	
	public AppContext getGame() {
		return game;
	}
	
	public int getTargetFPS() {
		return targetFPS;
	}
	
	public void setTargetFPS(int targetFPS) {
		this.targetFPS = targetFPS;
	}
	
	public void start() throws SlimException {
		running = true;
		
		//create the display
		create();
		
		//create the game context
		game.createContext(this);
		
		getDelta(); // call once before loop to initialise lastFrame
		lastFPS = getTime(); // call before loop to initialise fps timer
		
		while (running()) {
			//e.g. check resize
			onFrameStart();
			
			//TODO: cap delta, semi-fixed time step?
			
			//simple game loop for now...
			int delta = getDelta();
			game.updateContext(this, delta);
			updateFPS(); // update FPS Counter
			game.renderContext(this);
			
			//swap buffers
			onFrameEnd();
		}
		
		//destroy the game context
		game.destroyContext(this);
		
		//destroy the display
		destroy();
	}
	
	
	public void quit() {
		running = false;
	}
	
	public boolean running() {
		return running;
	}
	
	public abstract int getWidth();
	public abstract int getHeight();
	public abstract boolean isFullscreen();
	
	protected abstract void create() throws SlimException;
	protected abstract void destroy() throws SlimException;
	
	protected abstract void onFrameStart() throws SlimException;
	protected abstract void onFrameEnd() throws SlimException;
	

	/** 
	 * Calculate how many milliseconds have passed 
	 * since last frame.
	 * 
	 * @return milliseconds passed since last frame 
	 */
	protected int getDelta() {
	    long time = getTime();
	    int delta = (int) (time - lastFrame);
	    lastFrame = time;
	 
	    return delta;
	}
	
	/**
	 * Get the accurate system time
	 * 
	 * @return The system time in milliseconds
	 */
	public long getTime() {
	    return (Sys.getTime() * 1000) / Sys.getTimerResolution();
	}
	
	/**
	 * Calculate the FPS and set it in the title bar
	 */
	protected void updateFPS() {
		if (getTime() - lastFPS > 1000) {
			recordedFPS = fps;
			//Display.setTitle("FPS: " + fps);
			fps = 0;
			lastFPS += 1000;
		}
		fps++;
	}

	public int getFPS() {
		return recordedFPS;
	}
}
