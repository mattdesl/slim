package slim.test;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;
import org.poly2tri.Poly2Tri;
import org.poly2tri.geometry.polygon.Polygon;
import org.poly2tri.geometry.polygon.PolygonPoint;
import org.poly2tri.triangulation.TriangulationPoint;
import org.poly2tri.triangulation.delaunay.DelaunayTriangle;

import slim.Color;
import slim.GL2D;
import slim.Image;
import slim.SlimException;
import slim.texture.Texture;
import slim.util.Utils;
import de.matthiasmann.twl.Event;

public class PolyTest extends GUITestBase {
	public static void main(String[] args) throws SlimException {
		new PolyTest().start();
	}

	Image img;
	ArrayList<Point> points = new ArrayList<Point>();
	private Polygon triangulated;
	
	float minX, minY, maxX, maxY;
	
	public class Point {
		float x, y;
		public boolean equals(Object o) {
			return o instanceof Point
					&& ((Point)o).x == this.x
					&& ((Point)o).y == this.y;
		}
	}
	
	@Override
	public void init() throws SlimException {
		init2D();
		GL2D.setBackground(Color.gray);
		img = new Image("res/grass.png");
		img.getTexture().setWrap(Texture.WRAP_REPEAT);
		GL11.glPointSize(10);
	}

	public void minmax() {
		List<DelaunayTriangle> l = triangulated.getTriangles();
		minX = Float.MAX_VALUE; minY = Float.MAX_VALUE;
		maxX = Float.MIN_VALUE; maxY = Float.MIN_VALUE;
		for (int i=0; i<l.size(); i++) {
			TriangulationPoint[] p = l.get(i).points;
			for (int x=0; x<p.length; x++)  {
				minX = Math.min(minX, p[x].getXf());
				maxX = Math.max(maxX, p[x].getXf());
				minY = Math.min(minY, p[x].getYf());
				maxY = Math.max(maxY, p[x].getYf());
			}
		}
	}
	
	@Override
	public void render() throws SlimException {
		img.getTexture().bind();
		
		if (triangulated!=null) {
			List<DelaunayTriangle> l = triangulated.getTriangles();
			Color.white.bind();
			Texture.enable(img.getTexture().getTarget());
			GL11.glBegin(GL11.GL_TRIANGLES);
			GL11.glTexCoord2f(0, 0);
			for (int i=0; i<l.size(); i++) {
				TriangulationPoint[] p = l.get(i).points;
				for (int x=0; x<p.length; x++)  {
					float xp = p[x].getXf(), yp = p[x].getYf();
					GL11.glVertex2f(xp, yp);

					//GL11.glTexCoord2d((xp-minX) / (maxX-minX), (yp-minY) / (maxY-minY));
//					GL11.glTexCoord2d((xp-minX) / );
				}
			}
			
			GL11.glEnd();
			
			Texture.disable();
			Color.red.bind();
			GL11.glBegin(GL11.GL_LINES);
			for (int i=0; i<l.size(); i++) {
				TriangulationPoint[] p = l.get(i).points;
				for (int x=0; x<p.length-1; x++) { 
					GL11.glVertex2f(p[x].getXf(), p[x].getYf());
					GL11.glVertex2f(p[x+1].getXf(), p[x+1].getYf());
				}
			}
			GL11.glEnd();
			
		} else {
			Color.white.bind();
			GL11.glBegin(GL11.GL_POLYGON);
			for (Point point : points) {
				GL11.glVertex2f(point.x, point.y);
			}
			GL11.glEnd();
			
			Color.red.bind();
			
			GL11.glBegin(GL11.GL_LINE_LOOP);
			for (Point point : points) {
				GL11.glVertex2f(point.x, point.y);
			}
			GL11.glEnd();
		}
		
		
		Color.black.bind();
		GL11.glBegin(GL11.GL_POINTS);
		for (Point point : points) {
			GL11.glVertex2f(point.x, point.y);
		}
		if (triangulated!=null) {
			Color.red.bind();
			GL11.glVertex2f(minX, minY);
			GL11.glVertex2f(maxX, maxY);
		}
		GL11.glEnd();
	}
	
	
	
	@Override
	public void update(int delta) throws SlimException {
		
	}
	
	private void poly2tri() {
		List<PolygonPoint> pp = new ArrayList<PolygonPoint>(points.size());
		for (Point p : points) {
			pp.add(new PolygonPoint(p.x, p.y));
			triangulated = null;
		}
		try {
			triangulated = new Polygon(pp);
			Poly2Tri.triangulate(triangulated);
			minmax();
		} catch (Exception e) {
			e.printStackTrace();
			triangulated = null;
		}
	}
	
	public void handleEvent(Event e) {
		if (e.isKeyPressedEvent()) {
			if (e.getKeyCode()==Event.KEY_T) {
				if (triangulated!=null)
					triangulated = null;
				else if (points.size()>2)
					poly2tri();
			}
		} else if (e.isMouseEvent()) {
			if (e.getType()==Event.Type.MOUSE_BTNDOWN) {
				if (e.getMouseButton()==Event.MOUSE_RBUTTON) {
					points.clear();
					triangulated = null;
				} else {
					Point point = new Point();
					point.x = e.getMouseX();
					point.y = e.getMouseY();
					if (!points.contains(point)) {
						points.add(point);
						triangulated = null;
					} else
						System.out.println("Skipping point");
				}
			}
		}
	}

	@Override
	public URL getThemeURL() throws SlimException {
		return Utils.getResource("res/gui/chutzpah/chutzpah.xml");
	}
}
