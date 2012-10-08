package slim.util2;

import java.util.ArrayList;

import slim.g2d.Image;
import slimold.SlimException;

public class ArraySpriteSheet implements SpriteSheet {
	private Image sheet;
	private ArrayList<Image> sprites;
	
	public ArraySpriteSheet(Image sheet, int tileWidth, int tileHeight) throws SlimException {
		this(sheet, tileWidth, tileHeight, 0);
	}
	
	public ArraySpriteSheet(Image sheet, int tileWidth, int tileHeight, int spacing) throws SlimException {
		this(sheet, tileWidth, tileHeight, spacing, -1);
	}
	
	public ArraySpriteSheet(Image sheet, int tileWidth, int tileHeight, int spacing, int tileCount) throws SlimException {
		this.sheet = sheet;
		
		if (tileCount<0) {
			int xtiles = (int)(sheet.getWidth()/(tileWidth+spacing));
			int ytiles = (int)(sheet.getHeight()/(tileHeight+spacing));
			if (xtiles<=0 || ytiles<=0)
				throw new IllegalArgumentException("invalid tile size relative to sheet size");
			tileCount = xtiles * ytiles;
		}
		sprites = new ArrayList<Image>(tileCount);
		
		
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
	
	public Image remove(int index) {
		return sprites.remove(index);
	}
	
	public void add(int index, Image sprite) {
		sprites.add(index, sprite);
	}
	
	public void add(Image sprite) {
		sprites.add(sprite);
	}
	
	public Image[] toArray() {
		return sprites.toArray(new Image[size()]);
	}
	
	public Image getSprite(int index) {
		return sprites.get(index);
	}
	
	public Image getSheet() {
		return sheet;
	}
}
