package slim.game;

import slim.app.App;
import slim.core.SlimException;


public interface AppContext {
	
	public void createContext(App app) throws SlimException;
	public void renderContext(App app) throws SlimException;
	public void updateContext(App app, int delta) throws SlimException;
	public void destroyContext(App app) throws SlimException;
	
	public void onResize(App app) throws SlimException;
}
