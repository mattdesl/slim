package slim.test;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.glu.GLUtessellator;
import org.lwjgl.util.glu.GLUtessellatorCallbackAdapter;

public class TessellationTest {
  private GLUtessellator tesselator;
  
  class VertexData {
	  public double[] data;
	  
	  VertexData(double[] data) {
	    this.data = data;
	  }
	}
  
  public class TessCallback extends GLUtessellatorCallbackAdapter {

	  public void begin(int type) {
	    GL11.glBegin(type);
	  }

	  public void combine(double[] coords, Object[] data, float[] weight, Object[] outData) {
	    for (int i=0;i<outData.length;i++) {
	      double[] combined = new double[6];
	      combined[0] = coords[0];
	      combined[1] = coords[1];
	      combined[2] = coords[2];
	      combined[3] = 1;
	      combined[4] = 1;
	      combined[5] = 1;
	      
	      outData[i] = new VertexData(combined);
	    }
//	    vertex[0] = coords[0];
//	    vertex[1] = coords[1];
//	    vertex[2] = coords[2];
	//
//	    for (int i = 3; i < 6; i++)
//	    {
//	    vertex[i] = weight[0] * vertex_data[0][i] +
//	    indent indweight[1] * vertex_data[1][i] +
//	    indent indweight[2] * vertex_data[2][i] +
//	    indent indweight[3] * vertex_data[3][i];
//	    }
	//
//	    *dataOut = vertex;
	  }
	  
	  public void end() {
	    GL11.glEnd();
	  }

	  public void vertex(Object vertexData) {
	    VertexData vertex = (VertexData) vertexData;

	    GL11.glVertex3d(vertex.data[0], vertex.data[1], vertex.data[2]);
	    GL11.glColor3d(vertex.data[3], vertex.data[4], vertex.data[5]);
	  }
	}

  
  
  void init()
  {
    // Create a new tessellation object 
    tesselator = GLU.gluNewTess();
  
    // Set callback functions
    TessCallback callback = new TessCallback();
    tesselator.gluTessCallback(GLU.GLU_TESS_VERTEX, callback);
    tesselator.gluTessCallback(GLU.GLU_TESS_BEGIN, callback);
    tesselator.gluTessCallback(GLU.GLU_TESS_END, callback);
    tesselator.gluTessCallback(GLU.GLU_TESS_COMBINE, callback);
  }

  void setWindingRule(int windingRule)
  {
    // Set the winding rule
    tesselator.gluTessProperty(GLU.GLU_TESS_WINDING_RULE, windingRule); 
  }
   
  void renderContour(double obj_data[][], int num_vertices)
  {
    for (int x = 0; x < num_vertices; x++) //loop through the vertices
    {
      tesselator.gluTessVertex(obj_data[x], 0, new VertexData(obj_data[x])); //store the vertex
    }
  }
  
  void beginPolygon()
  {
    tesselator.gluTessBeginPolygon(null);
  }

  void endPolygon()
  {
    tesselator.gluTessEndPolygon();
  }
  
  void beginContour()
  {
    tesselator.gluTessBeginContour();
  }

  void endContour()
  {
    tesselator.gluTessEndContour();
  }
  
  void end()
  {
    tesselator.gluDeleteTess();
  }

  private void createDisplay() throws LWJGLException {
    int width = 300;
    int height = 300;
    
    Display.setDisplayMode(new DisplayMode(width,height));
    Display.create();
    Display.setVSyncEnabled(true);

    GL11.glEnable(GL11.GL_TEXTURE_2D);
    GL11.glShadeModel(GL11.GL_SMOOTH);        
    GL11.glDisable(GL11.GL_DEPTH_TEST);
    GL11.glDisable(GL11.GL_LIGHTING);                    
        
    GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);                
        GL11.glClearDepth(1);                                       
        
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        
        GL11.glViewport(0,0,width,height);
    GL11.glMatrixMode(GL11.GL_MODELVIEW);

    GL11.glMatrixMode(GL11.GL_PROJECTION);
    GL11.glLoadIdentity();
    GL11.glOrtho(0, width, height, 0, 1, -1);
    GL11.glMatrixMode(GL11.GL_MODELVIEW);
  }
  
  private void loop() {
    while (true) {
      render();
      Display.update();
      Display.sync(100);
      
      if (Display.isCloseRequested()) {
        System.exit(0);
      }
    }
  }
  
  private void render() {
    GL11.glTranslatef(150,125,0);
    
    GL11.glScalef(50,50,1);
    // first polygon: a star-5 vertices and color information
    double star[][] = { {0.6f,  -0.1f, 0f, 1.0f, 1.0f, 1.0f},
                {1.35f, 1.4f, 0f, 1.0f, 1.0f, 1.0f},
                {2.1f,  -0.1f, 0f, 1.0f, 1.0f, 1.0f},
                {0.6f, 0.9f, 0f, 1.0f, 1.0f, 1.0f},
                {2.1f, 0.9f, 0f, 1.0f, 1.0f, 1.0f} };

    //second polygon: a quad-4 vertices; first contour
    double quad[][] = { {0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f},
                    {1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f},
                    {1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f},
                    {0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f} };
    
    //second polygon: a triangle-3 vertices; second contour
    double tri[][] = {{0.3f, 0.3f, 0.0f, 0.0f, 0.0f, 0.0f},
                   {0.7f, 0.3f, 0.0f, 0.0f, 0.0f, 0.0f},
                   {0.5f, 0.7f, 0.0f, 0.0f, 0.0f, 0.0f} };

    // render the first polygon: the textured star
    
    // set winding rule to positive
    setWindingRule(GLU.GLU_TESS_WINDING_POSITIVE);
    beginPolygon();
    beginContour();
    renderContour(star, 5);
    endContour();
    endPolygon();

    // render the second polygon: triangle cut out of a quad

    GL11.glTranslatef(-2,0,0);
    // set winding rule to odd
    setWindingRule(GLU.GLU_TESS_WINDING_ODD);
    // begin the new polygon
    beginPolygon();
    beginContour();
    renderContour(quad, 4);
    endContour();
    beginContour();
    renderContour(tri, 3);
    endContour();
    endPolygon();
    // delete the tess object
    end();
  }
  
  private void start() throws LWJGLException {
    createDisplay();
    init();
    loop();
  }
  
  public static void main(String[] argv) throws LWJGLException {
    TessellationTest test = new TessellationTest();
    test.start();
  }
}