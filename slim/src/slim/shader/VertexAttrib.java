package slim.shader;

public class VertexAttrib {
	
	public static final VertexAttrib DEFAULT_POSITION = new VertexAttrib("Position", 2);
	public static final VertexAttrib DEFAULT_COLOR = new VertexAttrib("Color", 4);
	public static final VertexAttrib DEFAULT_TEXCOORD0 = new VertexAttrib("TexCoord", 2);
	
	public final String name;
	public final int numComponents;
	
	public VertexAttrib(String name, int numComponents) {
		this.name = name;
		this.numComponents = numComponents;
	}
}