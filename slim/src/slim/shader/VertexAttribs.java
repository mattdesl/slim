package slim.shader;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class VertexAttribs implements Iterable<VertexAttrib> {
	
	private List<VertexAttrib> attribs;
	
	public VertexAttribs(VertexAttrib ... attribs) {
		this.attribs = Arrays.asList(attribs);
	}
	
	public int size() {
		return attribs.size();
	}
	
	public VertexAttrib get(int index) {
		return attribs.get(index);
	}

	public Iterator<VertexAttrib> iterator() {
		return attribs.iterator();
	}
	
	void add(VertexAttrib attrib) {
		attribs.add(attrib);
	}
}
