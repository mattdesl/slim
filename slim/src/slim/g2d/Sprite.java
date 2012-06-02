package slim.g2d;

import org.lwjgl.util.vector.Vector2f;

import slim.texture.Texture;

public interface Sprite {
	public float getCenterX();
	public float getCenterY();
	public void draw(SpriteBatch batch);
	public Texture getTexture();
}
