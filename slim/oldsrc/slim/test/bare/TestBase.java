package slim.test.bare;

import static org.lwjgl.opengl.GL11.glViewport;

import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.openal.AL;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.util.glu.GLU;

import slimold.SlimException;

public abstract class TestBase {

	private int width, height;
	private boolean fullscreen;
	
	public TestBase() {
		this(800, 600, false);
	}
	
	public TestBase(int width, int height, boolean fullscreen) {
		this.width = width;
		this.height = height;
		this.fullscreen = fullscreen;
	}
	
	/** time at last frame */
	long lastFrame;
	
	/** frames per second */
	int fps, recordedFPS;
	/** last fps time */
	long lastFPS;
	boolean running, clearEach=true;
	int targetFPS = 60;
	
	public void setTargetFPS(int targetFPS) {
		this.targetFPS = targetFPS;
	}
	
	public int getTargetFPS() {
		return targetFPS;
	}
	
	public void setClearEachFrame(boolean clearEach) {
		this.clearEach = clearEach;
	}
	
	public boolean isClearEachFrame() {
		return clearEach;
	}
	
	/**
	 * Set the display mode to be used 
	 * 
	 * @param width The width of the display required
	 * @param height The height of the display required
	 * @param fullscreen True if we want fullscreen mode
	 */
	protected void setDisplayMode(int width, int height, boolean fullscreen) {
	    // return if requested DisplayMode is already set
	    if ((Display.getDisplayMode().getWidth() == width) && 
	        (Display.getDisplayMode().getHeight() == height) && 
		(Display.isFullscreen() == fullscreen)) {
		    return;
	    }

	    try {
	        DisplayMode targetDisplayMode = null;
			
		if (fullscreen) {
		    DisplayMode[] modes = Display.getAvailableDisplayModes();
		    int freq = 0;
					
		    for (int i=0;i<modes.length;i++) {
		        DisplayMode current = modes[i];
						
			if ((current.getWidth() == width) && (current.getHeight() == height)) {
			    if ((targetDisplayMode == null) || (current.getFrequency() >= freq)) {
			        if ((targetDisplayMode == null) || (current.getBitsPerPixel() > targetDisplayMode.getBitsPerPixel())) {
				    targetDisplayMode = current;
				    freq = targetDisplayMode.getFrequency();
	                        }
	                    }

			    // if we've found a match for bpp and frequence against the 
			    // original display mode then it's probably best to go for this one
			    // since it's most likely compatible with the monitor
			    if ((current.getBitsPerPixel() == Display.getDesktopDisplayMode().getBitsPerPixel()) &&
	                        (current.getFrequency() == Display.getDesktopDisplayMode().getFrequency())) {
	                            targetDisplayMode = current;
	                            break;
	                    }
	                }
	            }
	        } else {
	            targetDisplayMode = new DisplayMode(width,height);
	        }

	        if (targetDisplayMode == null) {
	            System.out.println("Failed to find value mode: "+width+"x"+height+" fs="+fullscreen);
	            return;
	        }

	        Display.setDisplayMode(targetDisplayMode);
	        Display.setFullscreen(fullscreen);
				
	    } catch (LWJGLException e) {
	        System.out.println("Unable to setup mode "+width+"x"+height+" fullscreen="+fullscreen + e);
	    }
	}
	
	protected void onResize() {
		System.out.println("Resized "+getWidth()+" "+getHeight());
	}
	
	void rungame() throws SlimException {
		running = true;
		Display.setResizable(true);
		Display.setTitle("Game");
		try {
			setDisplayMode(width, height, fullscreen);
			Display.create();
		} catch (LWJGLException e) {
			e.printStackTrace();
			System.exit(0);
		}
		width = Display.getWidth();
		height = Display.getHeight();
		fullscreen = Display.isFullscreen();
		
		initImpl();
		init();
		getDelta(); // call once before loop to initialise lastFrame
		lastFPS = getTime(); // call before loop to initialise fps timer
		
		while (running && !Display.isCloseRequested()) {
			if (Display.wasResized()) {
				width = Display.getWidth();
				height = Display.getHeight();
				onResize();
			}
			int delta = getDelta();
			
			updateImpl(delta);
			update(delta);
			updateFPS(); // update FPS Counter
			renderImpl();
			render();
			postRenderImpl();
			
			Display.update();
			postDisplayUpdateImpl();
			if (targetFPS>0) {
				Display.sync(targetFPS);
			}
		}
		destroy();
	}
	
	public void start() throws SlimException {
		try {
			rungame();
		} finally {
			destroy();
		}
	}
	
	public void destroy() {
		if (Display.isCreated()) Display.destroy();
		if (AL.isCreated()) AL.destroy();
	}
	
	protected void initImpl() throws SlimException {
	}
	
	protected void updateImpl(int delta) throws SlimException {
	}
	
	protected void postRenderImpl() throws SlimException {
	}
	
	protected void postDisplayUpdateImpl() throws SlimException {
	}
	
	public void exit() {
		running = false;
	}
	
	public int getFPS() {
		return recordedFPS;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public abstract void update(int delta) throws SlimException;
	public abstract void init() throws SlimException;
	public abstract void render() throws SlimException;
	
	/** 
	 * Calculate how many milliseconds have passed 
	 * since last frame.
	 * 
	 * @return milliseconds passed since last frame 
	 */
	public int getDelta() {
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
	public void updateFPS() {
		if (getTime() - lastFPS > 1000) {
			recordedFPS = fps;
			//Display.setTitle("FPS: " + fps);
			fps = 0;
			lastFPS += 1000;
		}
		fps++;
	}
	
	public void init3D() {
    	GL11.glViewport(0,0,width,height);						// Reset The Current Viewport
    	GL11.glMatrixMode(GL11.GL_PROJECTION);					// Select The Projection Matrix
    	GL11.glLoadIdentity();									// Reset The Projection Matrix
    	
        // Calculate The Aspect Ratio Of The Window
        GLU.gluPerspective(45.0f, (float)width/(float)height, 0.1f, 100.0f);
    	GL11.glMatrixMode(GL11.GL_MODELVIEW);					// Select The Modelview Matrix
    	GL11.glLoadIdentity();									// Reset The Modelview Matrix
    	
    	GL11.glShadeModel(GL11.GL_SMOOTH);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glDepthFunc(GL11.GL_LEQUAL);
		GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_NICEST);
	}
	
	public void init2D() {
		glViewport(0, 0, width, height);
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(0, width, height, 0, 1, -1);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
		GL11.glDisable(GL11.GL_DEPTH_TEST);
	}
	
	public void initGL() {
		GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		GL11.glClearDepth(1.0f);
	}

	public void renderImpl() {
		// Clear The Screen And The Depth Buffer
		if (clearEach)
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glLoadIdentity();
	}
}