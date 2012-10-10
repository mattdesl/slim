package slim.game;

public interface Gfx {

	void flush();
	void draw(Image image, float x, float y, float width, float height, float rotation);
	
}
