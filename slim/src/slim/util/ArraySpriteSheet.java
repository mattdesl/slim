package slim.util;

import java.util.ArrayList;

import slim.Image2D;
import slim.SlimException;

public class ArraySpriteSheet implements SpriteSheet {
	private Image2D sheet;
	private ArrayList<Image2D> sprites;
	
	public ArraySpriteSheet(Image2D sheet, int tileWidth, int tileHeight) throws SlimException {
		this(sheet, tileWidth, tileHeight, 0);
	}
	
	public ArraySpriteSheet(Image2D sheet, int tileWidth, int tileHeight, int spacing) throws SlimException {
		this(sheet, tileWidth, tileHeight, spacing, -1);
	}
	
	public ArraySpriteSheet(Image2D sheet, int tileWidth, int tileHeight, int spacing, int tileCount) throws SlimException {
		this.sheet = sheet;
		
		if (tileCount<0) {
			int xtiles = (int)(sheet.getWidth()/(tileWidth+spacing));
			int ytiles = (int)(sheet.getHeight()/(tileHeight+spacing));
			if (xtiles<=0 || ytiles<=0)
				throw new IllegalArgumentException("invalid tile size relative to sheet size");
			tileCount = xtiles * ytiles;
		}
		sprites = new ArrayList<Image2D>(tileCount);
		
		
		int x = 0;
		int y = 0;
		for (int i=0; i<tileCount; i++) {
			//new line...
			if (x+tileWidth > sheet.getWidth()) {
				x = 0;
				y += tileHeight + spacing;
			}
			sprites.add( sheet.getSubImage(x, y, tileWidth, tileHeight) );
			x += tileWidth + spacing;
		}
		sprites.trimToSize();
	}
	
	public int size() {
		return sprites.size();
	}
	
	public Image2D remove(int index) {
		return sprites.remove(index);
	}
	
	public void add(int index, Image2D sprite) {
		sprites.add(index, sprite);
	}
	
	public void add(Image2D sprite) {
		sprites.add(sprite);
	}
	
	public Image2D[] toArray() {
		return sprites.toArray(new Image2D[size()]);
	}
	
	public Image2D getSprite(int index) {
		return sprites.get(index);
	}
	
	public Image2D getSheet() {
		return sheet;
	}
}
