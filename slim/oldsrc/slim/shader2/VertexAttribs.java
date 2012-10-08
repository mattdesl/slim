package slim.shader2;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class VertexAttribs implements Iterable<VertexAttrib> {
	
	public static void main(String[] args) {
		VertexAttribs a = new VertexAttribs(
				new VertexAttrib(1, "TexCoord", 3),
				new VertexAttrib(0, "Position", 3));
		
		VertexAttribs b = new VertexAttribs(
				new VertexAttrib(1, "TexCoord", 3),

				new VertexAttrib(0, "Position", 3));
		
		System.out.println(a.equals(b));
	}
	
	/*
	 * A) User needs to specify attribute locations when creating VertexAttribs
	 * B) VertexAttribs passed to shader program creation is ONLY A HINT!! if
	 * layout syntax is used, or if there was a mismatch in numComponents, name,
	 * or location, new vertex attribute instances will be created. so one
	 * should not rely on the INPUT VertexAttribs, especially since they only
	 * exist as a workaround for GLSL versions older than 330
	 * 
	 * VertexAttrib has final 'location' value
	 * VertexAttribs is not in any special order; it's just a list of attribs!!
	 */
	
	private List<VertexAttrib> attribs;
	private int totalNumComponents;
	
	public VertexAttribs(VertexAttrib ... attribs) {
		this.attribs = Arrays.asList(attribs);
		for (int i=0; i<size(); i++) {
			VertexAttrib a = get(i);
			if (a!=null)
				totalNumComponents += a.numComponents;
		}
	}
	
	public int size() {
		return attribs.size();
	}
	
	public VertexAttrib get(int index) {
		return attribs.get(index);
	}

	public VertexAttrib get(String name) {
		int i = indexOf(name);
		return i != -1 ? get(i) : null;
	}
	
	public int indexOf(String name) {
		for (int i=0; i<size(); i++) {
			VertexAttrib a = get(i);
			if (a!=null && a.name.equals(name))
				return i;
		}
		return -1;
	}
	
	public int getTotalNumComponents() {
		return totalNumComponents;
	}
	
	public Iterator<VertexAttrib> iterator() {
		return attribs.iterator();
	}
	
//	/**
//	 * If the other list contains matching vertex attributes, regardless
//	 * of ordering, then the two lists are considered equal.
//	 * @param o the other list
//	 * @return true if both attribute lists contain equal attributes in
//	 * 		the same order
//	 */
//	public boolean equals(VertexAttribs o) {
//		if (o==this) 
//			return true;
//		if (o==null || !(o instanceof VertexAttribs)) 
//			return false;
//		VertexAttribs v = (VertexAttribs)o;
//		if (v.size()!=size()) //size mismatch
//			return false;
//		for (VertexAttrib a : this) {
//			VertexAttrib b = v.get(a.name);
//			if (!a.equals(b)) //an element doesn't match..
//				return false;
//		}
//		return true;
//	}
	
	/**
	 * Two lists of vertex attributes are only equal if all of the vertex
	 * attributes are equal <i>and</i> in the same order.
	 * @param o the other list
	 * @return true if both attribute lists contain equal attributes in
	 * 		the same order
	 */
	public boolean equals(VertexAttribs o) {
		if (o==this) 
			return true;
		if (o==null || !(o instanceof VertexAttribs)) 
			return false;
		VertexAttribs v = (VertexAttribs)o;
		if (v.size()!=size()) //size mismatch
			return false;
		for (int i=0; i<size(); i++) {
			if (!get(i).equals(v.get(i))) //an element doesn't match..
				return false;
		}
		return true;
	}
}

