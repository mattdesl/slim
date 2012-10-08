package slim.test.bare;

import java.net.URL;

import slim.util2.Utils2;
import slimold.SlimException;

public class EmptyGUITest extends GUITestBase {
	public static void main(String[] args) throws SlimException {
		new EmptyGUITest().start();
	}

	@Override
	public void init() throws SlimException {
		
	}

	@Override
	public void render() throws SlimException {
		
	}
	
	@Override
	public void update(int delta) throws SlimException {
		
		System.out.println(Mouse.getDX());
	}

	@Override
	public URL getThemeURL() throws SlimException {
		return Utils2.getResource("res/gui/chutzpah/chutzpah.xml");
	}
}
