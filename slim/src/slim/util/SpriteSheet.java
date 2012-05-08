package slim.util;

import slim.Image2D;

public interface SpriteSheet {
	public Image2D[] toArray(); //lists all sprites contained in this sheet
	public Image2D getSheet();
	public int size();
}
