package slim.texture;

public class TextureLayer extends Texture {
	
	protected Texture container;
	
	public TextureLayer(Texture container, int target, 
						int width, int height, 
						int texWidth, int texHeight,
						float normalizedWidth, float normalizedHeight) {
		this.container = container;
		this.width = width;
		this.height = height;
		this.texWidth = texWidth;
		this.texHeight = texHeight;
		this.normalizedWidth = normalizedWidth;
		this.normalizedHeight = normalizedHeight;
	}
	
	public void bind() {
		container.bind();
	}
	
	public Format getFormat() {
		return container.getFormat();
	}
	
	public int getTarget() {
		return container.getTarget();
	}
	
	public int getID() {
		return container.getID();
	}

	@Override
	public void setWrap(int wrap) {
		throw new UnsupportedOperationException("TextureLayer doesn't support wrap modes");
	}
	
	@Override
	public void setFilter(int minFilter, int magFilter) {
		throw new UnsupportedOperationException("TextureLayer doesn't support filter modes");
	}
}
