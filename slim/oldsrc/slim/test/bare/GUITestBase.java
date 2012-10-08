package slim.test.bare;

import java.net.URL;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import slimold.SlimException;
import de.matthiasmann.twl.DesktopArea;
import de.matthiasmann.twl.Event;
import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.renderer.lwjgl.LWJGLRenderer;
import de.matthiasmann.twl.theme.ThemeManager;
import de.matthiasmann.twleffects.lwjgl.LWJGLEffectsRenderer;

public abstract class GUITestBase extends TestBase {

	protected LWJGLRenderer renderer;
	protected GUI gui;
	protected ThemeManager theme;
	protected RootPane root;

	public GUITestBase() {
		this(800, 600, false);
	}

	public GUITestBase(int width, int height, boolean fullscreen) {
		super(width, height, fullscreen);
	}

	protected RootPane createRootPane() {
		return new RootPane(this);
	}

	public RootPane getRootPane() {
		return root;
	}

	protected void initImpl() throws SlimException {
		root = createRootPane();
		try {
			// renderer = new LWJGLRenderer();
			renderer = new LWJGLEffectsRenderer();
			renderer.setUseSWMouseCursors(true);
			gui = new GUI(root, renderer);
			URL url = getThemeURL();
			if (url == null)
				throw new NullPointerException("theme url");
			theme = ThemeManager.createThemeManager(url, renderer);
		} catch (Exception e) {
			throw new SlimException(e);
		}
		renderer.syncViewportSize();
		gui.setSize();
		gui.applyTheme(theme);
	}

	public void destroy() {
		if (gui!=null)
			gui.destroy();
		if (theme!=null)
			theme.destroy();
		super.destroy();
	}

	protected void postDisplayUpdateImpl() throws SlimException {
		GL11.glGetError(); // this call will burn the time between vsyncs
		Display.processMessages(); // process new native messages since
									// Display.update();
		Mouse.poll(); // now update Mouse events
		Keyboard.poll(); // and Keyboard too

		if (!Display.isActive()) {
			gui.clearKeyboardState();
			gui.clearMouseState();
		}
	}

	protected void postRenderImpl() throws SlimException {
		gui.draw();
		gui.setCursor();
	}

	protected void updateImpl(int delta) throws SlimException {
		gui.setSize();
		gui.updateTime();
		gui.handleInput();
		gui.handleKeyRepeat();
		gui.handleTooltips();
		gui.updateTimers();
		gui.invokeRunables();
		gui.validateLayout();
	}

	public abstract URL getThemeURL() throws SlimException;

	public abstract void update(int delta) throws SlimException;

	public abstract void init() throws SlimException;

	public abstract void render() throws SlimException;

	protected void handleEvent(Event evt) {
	}

	protected void layoutRootPane() {
	}

	protected void keyboardFocusLost() {
	}

	public static class RootPane extends DesktopArea {

		protected GUITestBase state;
		protected int oldMouseX;
		protected int oldMouseY;

		public RootPane(GUITestBase state) {
			if (state == null)
				throw new NullPointerException("state");
			this.state = state;
			setTheme("");
		}

		/**
		 * Returns the game state to which this root pane is associated with.
		 * 
		 * @return the game state or null when in preview mode (Theme Editor).
		 * @see #isPreviewMode()
		 */
		public final GUITestBase getTestBase() {
			return state;
		}

		@Override
		protected void keyboardFocusLost() {
			state.keyboardFocusLost();
		}

		@Override
		protected boolean requestKeyboardFocus(Widget child) {
			if (child != null && child != this) {
				state.keyboardFocusLost();
			}
			return super.requestKeyboardFocus(child);
		}

		@Override
		protected boolean handleEvent(Event evt) {
			if (super.handleEvent(evt))
				return true;
			state.handleEvent(evt);
			return true;
		}

		@Override
		protected void layout() {
			super.layout();
			state.layoutRootPane();
		}
	}

}