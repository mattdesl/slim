package slim.test.bare;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.poly2tri.Poly2Tri;
import org.poly2tri.geometry.polygon.Polygon;
import org.poly2tri.geometry.polygon.PolygonPoint;
import org.poly2tri.triangulation.TriangulationPoint;
import org.poly2tri.triangulation.delaunay.DelaunayTriangle;
import org.poly2tri.triangulation.util.PolygonGenerator;

import slim.g2d.Image;
import slim.texture.Texture;
import slim.util2.Utils2;
import slimold.Color;
import slimold.GL2D;
import slimold.SlimException;
import de.matthiasmann.twl.Event;

public class PolyTest extends GUITestBase {
	public static void main(String[] args) throws SlimException {
		new PolyTest().start();
	}

	Image img;
	ArrayList<Point> points = new ArrayList<Point>();
	private Polygon triangulated;
	
	float minX, minY, maxX, maxY;
	
	private float scale = 1f;

	float zoomScale=4f, panX=0f, panY=0f;
	float panX2, panY2;
	
	public class Point {
		float x, y;
		Point(float x, float y) {
			this.x = x;
			this.y = y;
		}
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
		img = new Image("res/grass.png", Texture.FILTER_NEAREST);
		img.getTexture().setWrap(Texture.WRAP_REPEAT);
		GL11.glPointSize(3);
		
		
//		points.add(new Point(70, 50));
		
		//bezier
		float steps = 50;
		Point start = new Point(10, 10);
		Point end = new Point(getWidth()/2f, getHeight()/2f);
//		Point A = new Point(start.x, getHeight()-50);
//		Point B = new Point(getWidth()/2f, 10);
		
		Point A = new Point(start.x+100, start.y-200);
		Point B = new Point(end.x, end.y-250);
		
//		for (int i=0; i<steps; i++) {
//			float x = bezier(start.x, A.x, B.x, end.x, i/steps);
//			float y = bezier(start.y, A.y, B.y, end.y, i/steps);
//			points.add(new Point(x, y));
//		}
		
//		points.add(new Point(200, 70));
//		points.add(new Point(180, 160));
		
//		points.add(new Point(20, 250));
		
		triangulated = PolygonGenerator.RandomCircleSweep2(50, 2000);
		Poly2Tri.triangulate(triangulated);
	}
	

	float bezier(float A, // Start value
			float B, // First control value
			float C, // Second control value
			float D, // Ending value
			float t) // Parameter 0 <= t <= 1
	{
		float s = 1 - t;
		float AB = A * s + B * t;
		float BC = B * s + C * t;
		float CD = C * s + D * t;
		float ABC = AB * s + CD * t;
		float BCD = BC * s + CD * t;
		return ABC * s + BCD * t;
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
	
	public void drawString(float x, float y, String str) {
		if (theme!=null && theme.getDefaultFont()!=null) {
			renderer.startRendering();
			theme.getDefaultFont().drawText(null, (int)x, (int)y, str);
			renderer.endRendering();
		}
	}
	
	@Override
	public void render() throws SlimException {
		GL11.glScalef(zoomScale, zoomScale, 1f);
		GL11.glTranslatef(-panX, -panY, 0f);
		
		img.getTexture().bind();
		
		if (triangulated!=null && triangulated.getTriangles()!=null) {
			List<DelaunayTriangle> l = triangulated.getTriangles();
			Color.white.bind();
			Texture.enable(img.getTexture().getTarget());
			GL11.glBegin(GL11.GL_TRIANGLES);
			GL11.glTexCoord2f(0, 0);
			for (int i=0; i<l.size(); i++) {
				TriangulationPoint[] p = l.get(i).points;
				for (int x=0; x<p.length; x++)  {
					float xp = p[x].getXf(), yp = p[x].getYf();
					float tx = (xp-minX) / (maxX-minX), ty = (yp-minY) / (maxY-minY);
					
					GL11.glTexCoord2d(tx * scale, ty * scale);
					GL11.glVertex2f(xp, yp);
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
//		if (triangulated!=null) {
//			Color.red.bind();
//			GL11.glVertex2f(minX, minY);
//			GL11.glVertex2f(maxX, maxY);
//		}
		GL11.glEnd();
	}
	
	boolean active = false;
	
	@Override
	public void update(int delta) throws SlimException {
		boolean disActive = Display.isActive();
		if (active && !disActive) { //we are losing focus
			System.out.println("Losing focus");
			active = false;
		} else if (!active && disActive) {
			System.out.println("Gaining focus");
			active = true;
		}
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
//			minmax();
			minX = minY = 0;
			maxX = maxY = 32;
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
			} else if (e.getKeyCode()==Event.KEY_1) {
				scale -= 0.25f;
			} else if (e.getKeyCode()==Event.KEY_2) {
				scale += 0.25f;
			}
		} else if (e.isMouseEvent()) {
			if (e.getType()==Event.Type.MOUSE_BTNDOWN) {
				if (e.getMouseButton()==Event.MOUSE_RBUTTON) {
					points.clear();
					triangulated = null;
				} else {
					System.out.println(e.getMouseY()+" "+(Display.getHeight()-Mouse.getY()-1));
					Point point = new Point(e.getMouseX()/zoomScale+panX, e.getMouseY()/zoomScale+panY);
					if (!points.contains(point)) {
						points.add(point);
						triangulated = null;
					} else
						System.out.println("Skipping point");
				}
			} else if (e.getType()==Event.Type.MOUSE_WHEEL) {
				if (Display.isActive()) {
					float mx = e.getMouseX();
					float my = e.getMouseY();
	//				float zoom = e.getMouseWheelDelta()*.1f;
	//				zoomScale += e.getMouseWheelDelta()*.1f;
					
					float scale = 1 + e.getMouseWheelDelta()*.01f;
					panX = (mx / zoomScale + panX - mx/(zoomScale*scale));
					panY = (my / zoomScale + panY - my/(zoomScale*scale));
					zoomScale *= scale;
				}
			}
		}
	}

	@Override
	public URL getThemeURL() throws SlimException {
		return Utils2.getResource("res/gui/chutzpah/chutzpah.xml");
	}
}
