package slim.tools.slimsheet;

import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.Sys;

import slim.g2d.FBO;
import slim.g2d.Image;
import slim.test.bare.GUITestBase;
import slim.texture.Texture;
import slim.texture.Texture2D;
import slim.util2.ArraySpriteSheet;
import slim.util2.SpriteSheet;
import slim.util2.Utils2;
import slimold.Color;
import slimold.GL2D;
import slimold.SlimException;
import slimold.SpriteBatch;
import de.matthiasmann.twl.Event;

public class Main extends GUITestBase {
	public static void main(String[] args) throws SlimException {
		new Main().start();
	}
	
	static final int TYPE_SINGLE_TILE = 1;
	static final int TYPE_ARRAY_SHEET = 2;
	static final int TYPE_NAMED_SHEET = 3; //TODO: named sheets
	static final int TYPE_INNER_SHEET = 4; //TODO: a SimpleSheet within a NamedSheet 
	static final int ZOOM_PADDING = 100;
	static final float MIN_ZOOM = 0.25f;
	static final float MAX_ZOOM = 22f;
	static final float WHEEL_ZOOM_AMT = .05f;
	static final int BPP = 4; //we'll be using 4 bytes per pixel for rendering
	
	boolean snapAfterZoom = true; //snap after zoom/pan
	private boolean useCommandKey = true;
	private boolean showGrid = false;
	private boolean showPixelGrid = false;
	private boolean showTiling = true;
	private boolean showPreview = true;
	private int tileCount = 5;
	private Timeout animationDelay = new Timeout(100);
	private boolean animating = true;
	private int animTileIndex = 0;
	
	int spriteWidth = 32; //width of CURRENT sprite
	int spriteHeight = 32; //height of CURRENT sprite
	int texWidth = 512;
	int texHeight = 512;
	Image currentSprite;
	int currentSpriteIndex = 0;
	Image[] sprites;
	
	
	SpriteBatch batch;

	float zoom = 1f, zoomFit = 1f, zoomFitTiles = 1f;
	boolean panning = false, commandKeyDown = false;
	int panX, panY;
	int mouseX, mouseY;
	int spriteX, spriteY;
	int cursorX, cursorY; //the screen space coords to draw the cursor at
	int mousePixelX, mousePixelY; //mouse coordinates in pixels, relative to image location
	private Timeout zoomTimeout = new Timeout(300);
	boolean moving = false; //whether the sprite is currently "moving" i.e. zooming or panning
	
	private Color gridColor = new Color(0f, 0f, 0f, 0.2f);
	private Color gridEdgeColor = new Color(0f, 0f, 0f, 0.5f);
	
	FBO sheetFBO;
	SpriteSheet sheet;
	final int DEFAULT_FBO_SIZE = 256;
	
	Image checkeredBG;
	
	Color currentColor = Color.green.darker();
	
	Image drawImage;
	Texture2D drawTexture;
	ByteBuffer drawBuffer = BufferUtils.createByteBuffer(4);
	boolean drawing = false;
	
	class SingleTileSheet implements SpriteSheet {
		Image sheet;
		
		public SingleTileSheet(Image sheet) {
			this.sheet = sheet;
		}
		
		public Image getSheet() { return sheet; }
		public Image[] toArray() { return new Image[] { sheet }; }
		public int size() { return 1; }
	}
	
	@Override
	public void init() throws SlimException {
		init2D();
		GL2D.setBackground(Color.lightGray);
		
		batch = new SpriteBatch();
		
		checkeredBG = new Image("res/check.png", Texture.FILTER_NEAREST);
		checkeredBG.getTexture().setWrap(Texture.WRAP_REPEAT);
		
		//currentSprite = new Image2D("res/grass.png", Texture.FILTER_NEAREST);
		//tileWidth = (int)currentSprite.getWidth();
		//tileHeight = (int)currentSprite.getHeight();
		
		try {
			
			Image img = new Image(new Texture2D(256, 256, Texture.FILTER_NEAREST));
			//Image2D img = new Image2D("res/tilesheet.png", Texture.FILTER_NEAREST);
			
			
//			sheet = new SingleTileSheet(img);
			sheet = new ArraySpriteSheet(img, 32, 32, 2, 5);
			setSheet(sheet);
		} catch (Exception e) {
			e.printStackTrace();
			Sys.alert("Error", "Error loading image: "+e.getMessage()); 
		}
		
	}
	
	void previousSprite() {
		currentSpriteIndex--;
		if (currentSpriteIndex < 0) 
			currentSpriteIndex = sprites.length-1;
		setSprite(currentSpriteIndex);
	}
	
	void nextSprite() {
		currentSpriteIndex++;
		if (currentSpriteIndex > sprites.length-1) 
			currentSpriteIndex = 0;
		setSprite(currentSpriteIndex);
	}
	
	void setSheet(SpriteSheet sheet) throws SlimException {
		if (sheet.size()<=0) {
			throw new SlimException("Sheet is empty!");
		}
		
		if (sheetFBO!=null) { //delete old FBO
			sheetFBO.getTexture().destroy();
			sheetFBO.destroy();
		}
		sheetFBO = new FBO(sheet.getSheet().getTexture());
		texWidth = sheetFBO.getWidth();
		texHeight = sheetFBO.getHeight();
		sprites = sheet.toArray();
		setSprite(0);
		zoom = showTiling ? zoomFitTiles : zoomFit;
	}
	
	void setSprite(int spriteIndex) {
		currentSpriteIndex = spriteIndex;
		animTileIndex = spriteIndex;
		currentSprite = sprites[spriteIndex];
		spriteWidth = (int)currentSprite.getWidth();
		spriteHeight = (int)currentSprite.getHeight();
		
		int zoomW = getWidth()-ZOOM_PADDING;
		int zoomH = getHeight()-ZOOM_PADDING;
		zoomFit = Math.min(zoomW/(float)spriteWidth, zoomH/(float)spriteHeight);
		zoomFitTiles = Math.min(zoomW/((float)spriteWidth*tileCount), zoomH/((float)spriteHeight*tileCount));
		//zoom = showTiling ? zoomFitTiles : zoomFit;
		if (drawTexture!=null) //destroy old tex
			drawTexture.destroy();
		drawTexture = new Texture2D(spriteWidth, spriteHeight, Texture.FILTER_NEAREST);
		drawBuffer.clear();
		drawImage = new Image(drawTexture);
	}
	
	@Override
	public void render() throws SlimException {
		batch.resetTranslation();
		float sw = spriteWidth * zoom;
		float sh = spriteHeight * zoom;

		int tiles = showTiling ? tileCount : 1;
		int thalf = showTiling ? tileCount/2 : 0;
		
    	batch.setColor(Color.white);
    	
    	float repeats = 10f;
    	
		batch.drawImage(checkeredBG, spriteX-(sw*thalf), spriteY-(sh*thalf), sw*tiles, sh*tiles, 0, 0, repeats, repeats, null); 
    	
    	//draw the sprite
    	drawSprite(currentSprite, spriteX, spriteY, zoom);
    	//draw the "draw" layer if needed
    	if (drawing)
    		drawSprite(drawImage, spriteX, spriteY, zoom);
    	if (!moving) 
			GL2D.fillRect(batch, cursorX, cursorY, zoom, zoom);
		
		if (showGrid) 
			drawGrid();
		
		batch.setColor(Color.white);

		
		if (showPreview) {
			int padding = 10;
			int border = showTiling ? 5 : 1;
			
			if (border!=0){
				int rw = spriteWidth*tiles;
				int rh = spriteHeight*tiles;
				batch.setColor(Color.black);
				GL2D.drawRect(batch, getWidth()-rw-padding-border, padding-border, rw+border*2, rh+border*2, border);
			}
			batch.setColor(Color.white);
			
			Image currentPreview = animating ? sprites[animTileIndex] : currentSprite;
			float pW = currentPreview.getWidth();
			float pH = currentPreview.getHeight();
			drawSprite(currentPreview, getWidth()-(pW*(thalf+1))-padding, pH*thalf+padding, 1f);
			if (drawing && currentPreview==currentSprite)
				drawSprite(drawImage, getWidth()-(spriteWidth*(thalf+1))-padding, spriteHeight*thalf+padding, 1f);
		}
//		
//		int tx = (mousePixelX+tiles*spriteWidth)/spriteWidth - tiles;
//		int ty = (mousePixelY+tiles*spriteHeight)/spriteHeight - tiles;
//		GL2D.drawRect(batch, spriteX+sw*tx, spriteY+sh*ty, sw, sh);
//		System.out.println(mousePixelX - spriteWidth*((mousePixelX+tiles*spriteWidth)/spriteWidth - tiles));
		batch.flush();
	}
	
	//draws the sprite either tiled or single... depending on view
	void drawSprite(Image image, float xOff, float yOff, float scale) {
		float sw = spriteWidth * scale;
		float sh = spriteHeight * scale;
		if (showTiling) {
			int tileOff = tileCount/2;
			for (int x=0; x<tileCount; x++) {
				for (int y=0; y<tileCount; y++) {
					batch.drawImage(image, -sw*tileOff + xOff+x*sw, -sh*tileOff + yOff+y*sh, sw, sh);
				}
			}
		} else {
			batch.drawImage(image, xOff, yOff, sw, sh);
		}
	}
	
	void drawGrid() {
//		batch.flush();
//		
//		GL11.glEnable(GL11.GL_COLOR_LOGIC_OP);
//		GL11.glLogicOp(GL11.GL_INVERT);
//	    
		batch.setColor(gridColor);
		float pixel = 1f / zoom;
		if (zoom < 1f)
			return;
		
		int tileOff = showTiling ? tileCount/2 : 0;
		int tiles = showTiling ? tileCount : 1;
		
		float sw = spriteWidth*zoom;
		float sh = spriteHeight*zoom;
		batch.setColor(Color.white);
		if (!showPixelGrid) {
			batch.setColor(gridEdgeColor);
			for (int r=0; r<=tiles; r++) 
				GL2D.fillRect(batch, (spriteX+r*sw) -sw*tileOff, spriteY -sh*tileOff, 1, sh*tiles);
			for (int r=0; r<=tiles; r++) 
				GL2D.fillRect(batch, spriteX -sh*tileOff, (spriteY+r*sh) -sh*tileOff, sw*tiles, 1);
		}
		else {
			for (int r=0; r<=spriteWidth*tiles; r++) {
				batch.setColor(r % spriteWidth == 0 ? gridEdgeColor : gridColor);
				GL2D.fillRect(batch, (spriteX+r*zoom) -sw*tileOff, spriteY -sh*tileOff, 1, sh*tiles);
			}
			for (int r=0; r<=spriteHeight*tiles; r++) {
				batch.setColor(r % spriteHeight == 0 ? gridEdgeColor : gridColor);
				GL2D.fillRect(batch, spriteX -sw*tileOff, (spriteY+r*zoom) -sh*tileOff, sw*tiles, 1);
			}
		}
		

//	    GL11.glDisable(GL11.GL_COLOR_LOGIC_OP);
//	    batch.flush();
	}
	
	@Override
	public void update(int delta) throws SlimException {
		zoomTimeout.update(delta);
		if (!moving && zoom > 4f)
			zoom = (int)zoom;
		
		if (animating) {
			animationDelay.update(delta);
			if (animationDelay.finished()) {
				animTileIndex++;
				if (animTileIndex>sprites.length-1)
					animTileIndex = 0;
				animationDelay.restart();
			}
		}
		
		updateLocations();
	}
	
	void updateLocations() {
		float w = spriteWidth * zoom;
		float h = spriteHeight * zoom;
		boolean mouseZoom = zoom > zoomFit && !zoomTimeout.finished();
//		float anchorX = mouseZoom ? mouseX-w/2f : getWidth()/2f-w/2f;
//		float anchorY = mouseZoom ? mouseY-w/2f : getHeight()/2f-h/2f;
		
		float centerX = getWidth()/2f;
		float centerY = getHeight()/2f;
		
		float anchorX = getWidth()/2f-w/2f;
		float anchorY = getHeight()/2f-h/2f;
		float startX = panX + anchorX;//getWidth()/2f-w/2f;
		float startY = panY + anchorY; //getHeight()/2f-h/2f;
		moving = panning || !zoomTimeout.finished();
    	float sx = (snapAfterZoom && moving) ? startX : (int)(startX/zoom) * zoom;
    	float sy = (snapAfterZoom && moving) ? startY : (int)(startY/zoom) * zoom;
    	float cx = ((int)(mouseX / zoom) * zoom);
    	float cy = ((int)(mouseY / zoom) * zoom);
    	spriteX = (int)sx;
    	spriteY = (int)sy;
    	cursorX = (int)cx;
    	cursorY = (int)cy;
    	mousePixelX = (int)(cx/zoom)-(int)(sx/zoom);
    	mousePixelY = (int)(cy/zoom)-(int)(sy/zoom);
	}
	
	boolean drawPixel(int x, int y, Color color) {
		if (showTiling){
			int tiles = tileCount;
			int tx = (mousePixelX+tiles*spriteWidth)/spriteWidth - tiles;
			int ty = (mousePixelY+tiles*spriteHeight)/spriteHeight - tiles;
			x = mousePixelX - spriteWidth*tx;
			y = mousePixelY - spriteHeight*ty;
		}
		
		if (x < 0 || x > spriteWidth-1 || x < 0 || y > spriteHeight-1)
			return false;
		drawBuffer.clear();
		drawBuffer.put(color.getRed());
		drawBuffer.put(color.getGreen());
		drawBuffer.put(color.getBlue());
		drawBuffer.put(color.getAlpha());
		drawBuffer.flip();
		drawTexture.uploadSubImage(x, y, 1, 1, Texture.Format.RGBA, drawBuffer);
		return true;
	}
	
	void flushDrawing() {
		sheetFBO.bind();
//		System.out.println("drawing sub "+sheetSubX+" "+sheetSubY+" "+sheetSubWidth+" "+sheetSubHeight);
		batch.setColor(Color.white);
		//TODO: draw image
		
		batch.drawImage(drawImage, currentSprite.getOffsetX(), currentSprite.getOffsetY(), 
						currentSprite.getWidth(), currentSprite.getHeight());
		
		
		batch.flush();
		sheetFBO.unbind();
		drawTexture.clear();
	}
	
	
	
//	/**
//	 * Quickly copies the specified pixels from the source image to the destination image -- 
//	 * both images need to be TYPE_INT_ARGB. If the region of pixels falls outside of the range
//	 * of either array, then an ArrayIndexOutOfBoundsException will be thrown.
//	 * 
//	 * @param src the source image
//	 * @param dst the destination image
//	 * @param srcX the x position of the source image to start copying
//	 * @param srcY the y position of the source image to start copying
//	 * @param srcW the width of the section to copy from source
//	 * @param srcH the height of the section to copy from source
//	 * @param dstX the x position to place the copied pixels at on the destination image
//	 * @param dstY the y position to place the copied pixels at on the destination image
//	 */
//	public void copyPixels(BufferedImage src, BufferedImage dst, int srcX, int srcY, int srcW, int srcH, int dstX, int dstY) {
//		int[] srcbuf = ((DataBufferInt)src.getRaster().getDataBuffer()).getData();
//		int[] dstbuf = ((DataBufferInt)dst.getRaster().getDataBuffer()).getData();
//		int srcOff = srcX + srcY * src.getWidth();
//		int dstOff = dstX + dstY * dst.getWidth();
//		for (int y=0; y<srcH; y++) { //copy every row of src starting at offset
//			System.arraycopy(srcbuf, srcOff, dstbuf, dstOff, srcW);
//			srcOff += src.getWidth();
//			dstOff += dst.getWidth();
//		}
//	}

	//handles a SINGLE key release
	void handleKeyReleased(int code, char c, Event e) {
		
	}
	
	//handles a SINGLE key press
	void handleKeyPressed(int code, char c, Event e) {
		if (code==Event.KEY_R) {
			panX = panY = 0;
		} else if (code==Event.KEY_G) {
			if (isCommandModifier(e.getModifiers()))
				showPixelGrid = !showPixelGrid;
			else
				showGrid = !showGrid;
		} else if (code==Event.KEY_T)
			showTiling = !showTiling;
		else if (code==Event.KEY_P)
			showPreview = !showPreview;
		else if (code==Event.KEY_1 && commandKeyDown) {
			if (isShiftModifier(e.getModifiers()))
				zoom = showTiling ? zoomFitTiles : zoomFit;
			else
				zoom = 1f;
			panX = panY = 0;
		} else if (code==Event.KEY_2 && commandKeyDown) {
			zoom = 2f;
			panX = panY = 0;
		} else if (code==Event.KEY_3 && commandKeyDown) {
			zoom = 3f;
			panX = panY = 0;
		} else if (code==Event.KEY_4 && commandKeyDown) {
			zoom = 6f;
			panX = panY = 0;
		} else if (code==Event.KEY_5 && commandKeyDown) {
			zoom = 10f;
			panX = panY = 0;
		} else if (code==Event.KEY_6 && commandKeyDown) {
			zoom = MAX_ZOOM;
			panX = panY = 0;
		} else if (code==Event.KEY_0 && commandKeyDown) {
			zoom = showTiling ? zoomFitTiles : zoomFit;
			panX = panY = 0;
		} if (code==Event.KEY_A) {
			animTileIndex = currentSpriteIndex;
			animating = !animating;
		}
	}
	
	public void handleEvent(Event e) {
		if (e.isMouseEvent()) {
			int nx = e.getMouseX();
			int ny = e.getMouseY();
			int ox = mouseX;
			int oy = mouseY;
			mouseX = nx;
			mouseY = ny;
			
			if (e.getType()==Event.Type.MOUSE_WHEEL) {
				int delta = e.getMouseWheelDelta();
				if (commandKeyDown) {
					float old = zoom;
					zoom = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, zoom+delta*WHEEL_ZOOM_AMT));
					if (old!=zoom)
						zoomTimeout.restart();
				} 
			} else if (e.getType()==Event.Type.MOUSE_BTNUP) {
				if (drawing) {
					drawing = false;
					flushDrawing();
				}
				if (commandKeyDown) {
					panning = false;
				}
			} else if (e.getType()==Event.Type.MOUSE_BTNDOWN) {
				if (!commandKeyDown && !moving) { 
					updateLocations();
					if (drawPixel(mousePixelX, mousePixelY, currentColor))
						drawing = true;
				}
			} else if (e.getType()==Event.Type.MOUSE_DRAGGED) {
				if (commandKeyDown) {
					panning = true;
					panX += nx - ox;
					panY += ny - oy;
				} else if (!moving) {
					updateLocations();
					if (drawPixel(mousePixelX, mousePixelY, currentColor))
						drawing = true;
				}
			}
		} else if (e.isKeyEvent()) {
			//KEY DOWN EVENTS
			int code = e.getKeyCode();
			if (isCommandKey(code)) {
				if (e.getType()==Event.Type.KEY_PRESSED)
					commandKeyDown = true;
				else {
					commandKeyDown = panning = false;
				}
			}
			
			if (e.getType()==Event.Type.KEY_PRESSED) {
				float oldZoom = zoom;
				if (code==Event.KEY_ADD || code==Event.KEY_EQUALS) {
					zoom = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, zoom+1f));
					if (oldZoom!=zoom)
						zoomTimeout.restart();
				} else if (code==Event.KEY_MINUS || code==Event.KEY_UNDERLINE) {
					zoom = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, zoom-1f));
					if (oldZoom!=zoom)
						zoomTimeout.restart();
				} else  if (code==Event.KEY_RIGHT)
					nextSprite();
				else if (code==Event.KEY_LEFT)
					previousSprite();
				else if (!e.isKeyRepeated())
					handleKeyPressed(code,e.getKeyChar(), e);
			} else if (e.getType()==Event.Type.KEY_RELEASED && !e.isKeyRepeated())
				handleKeyReleased(code, e.getKeyChar(), e);
			
		}
	}
	
	//if win key is enabled, returns true if win key
	//if win key is disabled, returns true if alt
	boolean isCommandKey(int code) {
		return useCommandKey ? code==Event.KEY_LMETA || code==Event.KEY_RMETA
						: code==Event.KEY_LMENU || code==Event.KEY_RMENU;
	}
	
	boolean isCommandModifier(int modifiers) {
		int l = useCommandKey ? Event.MODIFIER_LMETA : Event.MODIFIER_RMETA;
		int r = useCommandKey ? Event.MODIFIER_LALT : Event.MODIFIER_RALT;
		return isModifier(modifiers, l, r);
	}
	
	boolean isShiftModifier(int modifiers) {
		return isModifier(modifiers, Event.MODIFIER_LSHIFT, Event.MODIFIER_RSHIFT);
	}
	
	boolean isModifier(int modifiers, int l, int r) {
		return (modifiers & l) == l || (modifiers & r) == r;
	}
	
	

	@Override
	public URL getThemeURL() throws SlimException {
		return Utils2.getResource("res/gui/chutzpah/chutzpah.xml");
	}
	
	
	class Timeout {
		int length;
		int time = 0;
		
		/** Timeout in milliseconds. */
		public Timeout(int length) {
			this.length = time = length;
		}
		
		public void setLength(int length) {
			this.length = length;
		}
		
		public void update(int delta) {
			time += delta;
		}
		
		public void restart() {
			time = 0;
		}
		
		public boolean finished() {
			return time >= length;
		}
	}
}
