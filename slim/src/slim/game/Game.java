package slim.game;

import slim.app.App;


public interface Game {
	
	public void createContext(App app);
	public void renderContext(App app);
	public void updateContext(App app, int delta);
	public void destroyContext(App app);
	
	public void onResize(App app);
}
