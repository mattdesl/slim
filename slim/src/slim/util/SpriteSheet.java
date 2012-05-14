package slim.util;

import slim.Image;

public interface SpriteSheet {
	public Image[] toArray(); //lists all sprites contained in this sheet
	public Image getSheet();
	public int size();
}
