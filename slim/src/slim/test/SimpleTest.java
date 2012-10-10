package slim.test;

import slim.app.App;
import slim.app.DesktopApp;
import slim.core.SlimException;
import slim.game.Game2D;
import slim.game.Gfx;
import slim.game.SimpleGfx;
import slim.util.GLUtil;

//entry point
public class SimpleTest extends Game2D {
	
	public final static DesktopApp APP = new DesktopApp(new SimpleTest());
	
	public static void main(String[] args) {
		try {
			APP.setDisplayMode(800, 600);
			APP.setFullscreen(false);
			APP.setTargetFPS(App.NO_TARGET_FPS);
			APP.start();
		} catch (SlimException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	@Override
	public void create(App app) throws SlimException {
		
	}
	
	
	@Override
	public void destroy(App app) throws SlimException {
		
	}
	
    
	@Override
	public void render(App app) throws SlimException {
		
	}

	@Override
	public void update(App app, int delta) throws SlimException {
		APP.setTitle(""+app.getFPS());
	}
	

	
}
