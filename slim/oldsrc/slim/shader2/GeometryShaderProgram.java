package slim.shader2;

import java.io.IOException;
import java.util.HashMap;

import org.lwjgl.opengl.EXTGeometryShader4;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL32;
import org.lwjgl.opengl.GLContext;

import slim.shader.ShaderProgram;
import slim.util2.Utils2;
import slimold.SlimException;

/**
 * Geometry shader, through the use of GL_EXT_geometry_shader4.
 * @author davedes
 */
public class GeometryShaderProgram extends ShaderProgram {
	
	public static boolean isSupported() {
		return ShaderProgram.isSupported() && GLContext.getCapabilities().GL_EXT_geometry_shader4;
	}
	
	public static GeometryShaderProgram loadProgram(String vertFile, String fragFile, String geomFile,
												int geomIn, int geomOut, int verts, VertexAttribs attribs) throws SlimException {
		String vSrc=null, fSrc=null, gSrc=null;
		try {
			vSrc = readFile(Utils2.getResourceAsStream(vertFile)); 
			fSrc = readFile(Utils2.getResourceAsStream(fragFile));
			gSrc = readFile(Utils2.getResourceAsStream(geomFile));
		} catch (IOException e) {
			String res = geomFile;
			if (vSrc==null) res = vertFile;
			else if (fSrc==null) res = fragFile;
			throw new SlimException("error loading "+res, e);
		}
		try {
			return new GeometryShaderProgram(vSrc, fSrc, gSrc, geomIn, geomOut, verts, attribs);
		} catch (SlimException e) {
			// just for clearer debugging...
			String res = null;
			if (e.getMessage().startsWith("VERTEX")) res = vertFile;
			else if (e.getMessage().startsWith("FRAGMENT")) res = fragFile;
			else if (e.getMessage().startsWith("GEOMETRY")) res = geomFile;
			if (res!=null)
				Utils2.error("Error compiling "+res);
			throw e;
		}
	}

	/** The geometry shader type (GL32.GL_GEOMETRY_SHADER). */
	public static final int GEOMETRY_SHADER = GL32.GL_GEOMETRY_SHADER;
	
	protected String geomShaderSource;
	protected int geom;
	protected int geomIn, geomOut, verts;
	
	public GeometryShaderProgram(String vert, String frag, String geom, 
					int geomIn, int geomOut, int verts, VertexAttribs attribs) throws SlimException {
		this.geomIn = geomIn;
		this.geomOut = geomOut;
		this.verts = verts;
		this.vertShaderSource = vert;
    	this.fragShaderSource = frag;
    	this.geomShaderSource = geom;

    	this.vert = compileShader(VERTEX_SHADER, vertShaderSource);
    	this.frag = compileShader(FRAGMENT_SHADER, fragShaderSource);
    	this.geom = compileShader(GEOMETRY_SHADER, geomShaderSource);
		program = createProgram();
		
		bindAttributes(attribs);
		
		try { 
			linkProgram(); 
		} catch (SlimException e) { //if it fails, delete the program
			release(); 
			throw e;
		}
	}
	
	public void attachShaders() {
		super.attachShaders(); //attaches vert + frag
		GL20.glAttachShader(getID(), geom);
		EXTGeometryShader4.glProgramParameteriEXT(program, EXTGeometryShader4.GL_GEOMETRY_INPUT_TYPE_EXT, geomIn);
		EXTGeometryShader4.glProgramParameteriEXT(program, EXTGeometryShader4.GL_GEOMETRY_OUTPUT_TYPE_EXT, geomOut);
		EXTGeometryShader4.glProgramParameteriEXT(program, EXTGeometryShader4.GL_GEOMETRY_VERTICES_OUT_EXT, verts);
	}
	
	public void releaseShaders() {
		super.releaseShaders(); //releases vert + frag
		if (geom!=0) {
			GL20.glDetachShader(getID(), geom);
			GL20.glDeleteShader(geom);
			geom = 0;
		}
	}
}
