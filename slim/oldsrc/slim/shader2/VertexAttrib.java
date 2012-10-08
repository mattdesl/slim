package slim.shader2;

public class VertexAttrib {
	
	public final String name;
	public final int numComponents;
	public final int location;
	
	public VertexAttrib(int location, String name, int numComponents) {
		this.location = location;
		this.name = name;
		this.numComponents = numComponents;
	}
	
	public boolean equals(Object o) {
		if (this==o)
			return true;
		if (o!=null && o instanceof VertexAttrib) {
			VertexAttrib a = (VertexAttrib)o;
			return a.name.equals(name) && a.numComponents==numComponents && a.location==location;
		}
		return false;
	}
	
	public String toString() {
		return location+"("+name+":"+type()+")";
	}
	
	private String type() {
		String t;
		switch(numComponents) {
		case 1: return "float";
		case 2: return "vec2";
		case 3: return "vec3";
		case 4: return "vec4";
		default: return "numComponents="+numComponents;
		}
	}
	
}