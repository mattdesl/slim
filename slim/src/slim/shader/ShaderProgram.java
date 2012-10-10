package slim.shader;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_INT;
import static org.lwjgl.opengl.GL20.GL_ACTIVE_ATTRIBUTES;
import static org.lwjgl.opengl.GL20.GL_ACTIVE_ATTRIBUTE_MAX_LENGTH;
import static org.lwjgl.opengl.GL20.GL_ACTIVE_UNIFORMS;
import static org.lwjgl.opengl.GL20.GL_ACTIVE_UNIFORM_MAX_LENGTH;
import static org.lwjgl.opengl.GL20.GL_BOOL;
import static org.lwjgl.opengl.GL20.GL_BOOL_VEC2;
import static org.lwjgl.opengl.GL20.GL_BOOL_VEC3;
import static org.lwjgl.opengl.GL20.GL_BOOL_VEC4;
import static org.lwjgl.opengl.GL20.GL_COMPILE_STATUS;
import static org.lwjgl.opengl.GL20.GL_FLOAT_MAT2;
import static org.lwjgl.opengl.GL20.GL_FLOAT_MAT3;
import static org.lwjgl.opengl.GL20.GL_FLOAT_MAT4;
import static org.lwjgl.opengl.GL20.GL_FLOAT_VEC2;
import static org.lwjgl.opengl.GL20.GL_FLOAT_VEC3;
import static org.lwjgl.opengl.GL20.GL_FLOAT_VEC4;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_INFO_LOG_LENGTH;
import static org.lwjgl.opengl.GL20.GL_INT_VEC2;
import static org.lwjgl.opengl.GL20.GL_INT_VEC3;
import static org.lwjgl.opengl.GL20.GL_INT_VEC4;
import static org.lwjgl.opengl.GL20.GL_LINK_STATUS;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL20.glAttachShader;
import static org.lwjgl.opengl.GL20.glBindAttribLocation;
import static org.lwjgl.opengl.GL20.glCompileShader;
import static org.lwjgl.opengl.GL20.glCreateProgram;
import static org.lwjgl.opengl.GL20.glCreateShader;
import static org.lwjgl.opengl.GL20.glDeleteProgram;
import static org.lwjgl.opengl.GL20.glDeleteShader;
import static org.lwjgl.opengl.GL20.glDetachShader;
import static org.lwjgl.opengl.GL20.glGetActiveAttrib;
import static org.lwjgl.opengl.GL20.glGetActiveAttribType;
import static org.lwjgl.opengl.GL20.glGetActiveUniform;
import static org.lwjgl.opengl.GL20.glGetAttribLocation;
import static org.lwjgl.opengl.GL20.glGetProgram;
import static org.lwjgl.opengl.GL20.glGetProgramInfoLog;
import static org.lwjgl.opengl.GL20.glGetShader;
import static org.lwjgl.opengl.GL20.glGetShaderInfoLog;
import static org.lwjgl.opengl.GL20.glGetUniform;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glLinkProgram;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glUniform1f;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL20.glUniform2f;
import static org.lwjgl.opengl.GL20.glUniform2i;
import static org.lwjgl.opengl.GL20.glUniform3f;
import static org.lwjgl.opengl.GL20.glUniform3i;
import static org.lwjgl.opengl.GL20.glUniform4f;
import static org.lwjgl.opengl.GL20.glUniform4i;
import static org.lwjgl.opengl.GL20.glUniformMatrix2;
import static org.lwjgl.opengl.GL20.glUniformMatrix3;
import static org.lwjgl.opengl.GL20.glUniformMatrix4;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL21.GL_FLOAT_MAT2x3;
import static org.lwjgl.opengl.GL21.GL_FLOAT_MAT2x4;
import static org.lwjgl.opengl.GL21.GL_FLOAT_MAT3x2;
import static org.lwjgl.opengl.GL21.GL_FLOAT_MAT3x4;
import static org.lwjgl.opengl.GL21.GL_FLOAT_MAT4x2;
import static org.lwjgl.opengl.GL21.GL_FLOAT_MAT4x3;
import static org.lwjgl.opengl.GL32.GL_GEOMETRY_SHADER;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.ContextCapabilities;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.util.vector.Matrix3f;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import slim.core.SlimException;
import slim.core.VertexAttrib;
import slim.util.Utils;

public class ShaderProgram {

	/** The vertex shader type (GL_VERTEX_SHADER). */
	public static final int VERTEX_SHADER = GL_VERTEX_SHADER;
	/** The fragment shader type (GL_FRAGMENT_SHADER). */
	public static final int FRAGMENT_SHADER = GL_FRAGMENT_SHADER;
	
	private static boolean strict = true;
	
	/**
	 * Returns true if OpenGL 2.0 is present.
	 * 
	 * @return true if shaders are supported
	 */
	public static boolean isSupported() {
		ContextCapabilities c = GLContext.getCapabilities();
		//return c.GL_ARB_shader_objects && c.GL_ARB_vertex_shader && c.GL_ARB_fragment_shader;
		return c.OpenGL20;
	}
	
	/**
	 * Whether shader programs are to use "strict" uniform/attribute name
	 * checking. That is, when strict mode is enabled, trying to modify or retrieve uniform/attribute
	 * data by name will fail and throw an IllegalArgumentException if there exists no
	 * 'active' uniforms/attributes by the given name. (In GLSL, declared uniforms might still be
	 * "inactive" if they are not used.) If strict mode is disabled, getting/setting uniform/attribute
	 * data will fail silently if the name is not found.
	 * @param enabled true to enable strict mode
	 */
	public static void setStrictMode(boolean enabled) {
		strict = enabled;
	}
	
	/**
	 * Returns <tt>true</tt> if shader programs are to use "strict" uniform/attribute name
	 * checking. That is, when strict mode is enabled, trying to modify or retrieve uniform/attribute
	 * data by name will fail and throw an IllegalArgumentException if there exists no
	 * 'active' uniforms/attributes by the given name. (In GLSL, declared uniforms might still be
	 * "inactive" if they are not used.) If strict mode is disabled, getting/setting uniform/attribute
	 * data will fail silently if the name is not found.
	 * @return true if strict mode is enabled
	 */
	public static boolean isStrictMode() {
		return strict;
	}
	
	/**
	 * Unbinds all shader programs.
	 */
	public static void unbindAll() {
		glUseProgram(0);
	}
	
	/** The OpenGL handle for this shader program object. */
	protected int program;
	/** The log for this program. */
	protected String log = "";
	/** A map of uniforms by <name, int>. */
	protected HashMap<String, Integer> uniforms = new HashMap<String, Integer>();
	/** A list of attributes for this program. */
	protected List<VertexAttrib> attributes;
	
	/** The vertex shader source. */
	protected String vertShaderSource;
	/** The fragment shader source. */
	protected String fragShaderSource;
	/** The OpenGL handle for this program's vertex shader object. */
	protected int vert;
	/** The OpenGL handle for this program's fragment shader object. */
	protected int frag;
	
	private FloatBuffer fbuf16;
	private IntBuffer ibuf4;
	private Matrix4f m4;
	private Matrix3f m3;

	public static ShaderProgram load(String vertFile, String fragFile) throws SlimException {
		return load(vertFile, fragFile, null);
	}
	
	/**
	 * A convenience method to load a ShaderProgram from two text files.
	 * @param vertFile the location of the vertex shader source
	 * @param fragFile the location of the frag shader source
	 * @return the compiled and linked ShaderProgram
	 * @throws SlimException if there was an issue reading the file, compiling the source,
	 * 				or linking the program, or if the resources couldn't be located
	 */
	public static ShaderProgram load(String vertFile, String fragFile,
			List<VertexAttrib> attribLocations) throws SlimException {
		String vSrc=null, fSrc=null;
		//first check for I/O errors
		try { 
			vSrc = Utils.readFile(Utils.getResourceAsStream(vertFile)); 
			fSrc = Utils.readFile(Utils.getResourceAsStream(fragFile));
		} catch (IOException e) {
			String res = vSrc==null ? vertFile : fragFile;
			throw new SlimException("error loading "+res, e);
		}
		//now try compiling...
		try {
			return new ShaderProgram(vSrc, fSrc, attribLocations);
		} catch (SlimException e) {
			// just for clearer debugging...
			String res = null;
			if (e.getMessage().startsWith("VERTEX")) res = vertFile;
			else if (e.getMessage().startsWith("FRAGMENT")) res = fragFile;
			throw e;
		}
	}
	
    
    /**
     * Creates a new shader program with the given vertex and fragment shader
     * source code. The given source code is compiled, then the shaders attached
     * and linked. 
     * 
     * If shaders are not supported on this system (isSupported returns false), 
     * a SlimException will be thrown.
     * 
     * If one of the shaders does not compile successfully, a SlimException will be thrown.
     * 
     * If there was a problem in linking the shaders to the program, a SlimException will
     * be thrown and the program will be deleted.
     * 
     * @param vertexShaderSource the shader code to compile, attach and link
     * @param fragShaderSource the frag code to compile, attach and link
     * @throws SlimException if there was an issue
     * @throws IllegalArgumentException if there was an issue
     */
    public ShaderProgram(String vertexShaderSource, String fragShaderSource, List<VertexAttrib> attribLocations) throws SlimException {
    	if (vertexShaderSource==null || fragShaderSource==null) 
			throw new IllegalArgumentException("shader source must be non-null");
    	if (!isSupported())
			throw new SlimException("no shader support found; shaders require OpenGL 2.0");
    	this.vertShaderSource = vertexShaderSource;
    	this.fragShaderSource = fragShaderSource;
    	vert = compileShader(VERTEX_SHADER, vertexShaderSource);
    	frag = compileShader(FRAGMENT_SHADER, fragShaderSource);
		program = createProgram();
		try {
			linkProgram(attribLocations);
		} catch (SlimException e) {
			release();
			throw e;
		}
		if (log!=null && log.length()!=0)
			Utils.warn(log);
    }
    
    public ShaderProgram(String vertexShaderSource, String fragShaderSource) throws SlimException {
    	this(vertexShaderSource, fragShaderSource, null);
    }
    
	/**
	 * Subclasses may wish to implement this to manually handle program/shader creation, compiling, and linking.
	 * This constructor does nothing; users will need to call compileShader, createProgram and linkProgram manually.
	 * @throws SlimException
	 */
	protected ShaderProgram() {
	}
	
	/**
	 * Creates a shader program and returns its OpenGL handle. If the result is zero, an exception will be thrown.
	 * @return the OpenGL handle for the newly created shader program
	 * @throws SlimException if the result is zero
	 */
	protected int createProgram() throws SlimException {
		int program = glCreateProgram();
		if (program == 0)
			throw new SlimException("could not create program; check ShaderProgram.isSupported()");
		return program;
	}
	
	/** Used only for clearer debug messages. */
	private String shaderTypeString(int type) {
		if (type==FRAGMENT_SHADER) return "FRAGMENT_SHADER";
		else if (type==VERTEX_SHADER) return "VERTEX_SHADER";
		else if (type==GL_GEOMETRY_SHADER) return "GEOMETRY_SHADER";
		else return "shader";
	}
	
	/**
	 * Compiles a shader from source and returns its handle. If the compilation failed, 
	 * a SlimException will be thrown. If the compilation had error, info or warnings messages,
	 * they will be appended to this program's log.
	 *  
	 * @param type the type to use in compilation
	 * @param source the source code to compile
	 * @return the resulting ID
	 * @throws SlimException if compilation was unsuccessful
	 */
	protected int compileShader(int type, String source) throws SlimException {
		int shader = glCreateShader(type);
		if (shader==0) 
			throw new SlimException("could not create shader object; check ShaderProgram.isSupported()");
		glShaderSource(shader, source);
		glCompileShader(shader);
		
		int comp = glGetShader(shader, GL_COMPILE_STATUS);
		int len = glGetShader(shader, GL_INFO_LOG_LENGTH);
		String t = shaderTypeString(type);
		String err = glGetShaderInfoLog(shader, len); 
		if (err!=null&&err.length()!=0) 
			log += t+" compile log:\n"+err+"\n";
		if (comp==GL11.GL_FALSE)
			throw new SlimException(log);
		return shader;
	}
	
	/**
	 * Called to attach vertex and fragment; users may override this for more specific purposes.
	 */
	protected void attachShaders() {
		glAttachShader(getID(), vert);
		glAttachShader(getID(), frag);
	}
	
	/**
	 * Tries to bind the given attributes by location, then calls attachShaders() and links the program.
	 * 
	 * @param attribs tries to bind the given attributes in their order of appearance
	 * @throws SlimException
	 *             if this program is invalid (released) or
	 *             if the link was unsuccessful
	 */
	protected void linkProgram(List<VertexAttrib> attribLocations) throws SlimException {
		if (!valid())
			throw new SlimException("trying to link an invalid (i.e. released) program");
		
		uniforms.clear();
		
		//bind user-defined attribute locations
		if (attribLocations!=null) {
			for (VertexAttrib a : attribLocations) {
				if (a!=null)
					glBindAttribLocation(program, a.location, a.name);
			}
		}
		
		attachShaders();
        glLinkProgram(program);
		int comp = glGetProgram(program, GL_LINK_STATUS);
		int len = glGetProgram(program, GL_INFO_LOG_LENGTH);
		String err = glGetProgramInfoLog(program, len); 
		if (err!=null&&err.length()!=0) log = err + "\n" + log;
		if (log!=null) log = log.trim();
        if (comp==GL11.GL_FALSE) 
			throw new SlimException(log);
        
        fetchUniforms();
		fetchAttributes();
	}
	
	IntBuffer buf = BufferUtils.createIntBuffer(2);
	
	private void fetchAttributes() {
		int len = glGetProgram(program, GL_ACTIVE_ATTRIBUTES);
		
		this.attributes = new ArrayList<VertexAttrib>(len);
		int strLen = glGetProgram(program, GL_ACTIVE_ATTRIBUTE_MAX_LENGTH); 
		for (int i=0; i<len; i++) {
			String name = glGetActiveAttrib(program, i, strLen);
			int location = glGetAttribLocation(program, name);
			int numComponents = attribNumComponents(name, i);
			
			IntBuffer buf = BufferUtils.createIntBuffer(3);
			
			glGetActiveAttrib(program, i, strLen, buf);
			System.out.println(name);
			System.out.println(buf.get());
			System.out.println(buf.get());
			System.out.println(buf.get());
			System.out.println(numComponents);
			System.out.println();
			this.attributes.add(new VertexAttrib(location, name, numComponents));
		}
	}
	
	//only used for clearer debugging messages
	private int attribNumComponents(String attribName, int attribIndex) {
		int type = glGetActiveAttribType(program, attribIndex);
		switch (type) {
		case GL_FLOAT:
		case GL_INT:
		case GL_BOOL:
			return 1;
		case GL_FLOAT_VEC2:
		case GL_BOOL_VEC2:
		case GL_INT_VEC2:	
			return 2;
		case GL_FLOAT_VEC3: 
		case GL_BOOL_VEC3:
		case GL_INT_VEC3:
			return 3;
		case GL_FLOAT_VEC4:
		case GL_BOOL_VEC4:
		case GL_INT_VEC4:
			return 4;
		case GL_FLOAT_MAT2: return 4;
		case GL_FLOAT_MAT3: return 9;
		case GL_FLOAT_MAT4: return 16;
		case GL_FLOAT_MAT2x3:
		case GL_FLOAT_MAT3x2:
			return 6;
		case GL_FLOAT_MAT2x4: 
		case GL_FLOAT_MAT4x2:
			return 8;
		case GL_FLOAT_MAT3x4:
		case GL_FLOAT_MAT4x3:
			return 12;
		default: //couldn't figure it out
			Utils.warn("unsupported attribute type "+type+" for '"+attribName+"'");
			return 1;
		}
	}
	
	/**
	 * Returns the full log of compiling/linking errors, info, warnings, etc.
	 * @return the full log of this ShaderProgram
	 */
	public String getLog() {
		return log;
	}
	
	/**
	 * Enables this shader for use -- only one shader can be bound at a time. Calling
	 * bind() when another program is bound will simply make this object the active program.
	 * @throw IllegalStateException if this program is invalid
	 */
	public void bind() {
		if (!valid())
			throw new IllegalStateException("trying to enable a program that is not valid");
		glUseProgram(program);
	}
	
	/**
	 * Disables shaders (unbind), then detaches and releases the shaders associated with this program. 
	 * This can be called after linking a program in order to free up memory (as the shaders are no longer needed),
	 * however, since it is not a commonly used feature and thus not well tested on all drivers,
	 * it should be used with caution.
	 * Shaders shouldn't be used after being released.
	 */
	public void releaseShaders() {
		unbind();
		if (vert!=0) {
			glDetachShader(getID(), vert);
			glDeleteShader(vert);
			vert = 0;
		}
		if (frag!=0) {
			glDetachShader(getID(), frag);
			glDeleteShader(frag);
			frag = 0;
		}
	}
	
	/**
	 * If this program has not yet been released, this will disable shaders (unbind), 
	 * then releases this program and its shaders. To only release
	 * the shaders (not the program itself), call releaseShaders().
	 * Programs will be considered "invalid" after being released, and should no longer be used.
	 */
	public void release() {
		if (program!=0) {
			unbind();
			releaseShaders();
			glDeleteProgram(program);
			program = 0;
		}
	}
	
	/**
	 * Unbinds all shaders; this is the equivalent of ShaderProgram.unbindAll(), and only included
	 * for consistency with bind() and the rest of the API (i.e. FBO). Users do not need to unbind
	 * one shader before binding a new one.
	 */
	public void unbind() {
		ShaderProgram.unbindAll();
	}
	
	/**
	 * Returns the OpenGL handle for this program's vertex shader.
	 * @return the vertex ID
	 */
	public int getVertexShaderID() {
		return vert;
	}

	/**
	 * Returns the OpenGL handle for this program's fragment shader.
	 * @return the fragment ID
	 */
	public int getFragmentShaderID() {
		return frag;
	}

	/**
	 * Returns the source code for the vertex shader.
	 * @return the source code
	 */
	public String getVertexShaderSource() {
		return vertShaderSource;
	}

	/**
	 * Returns the source code for the fragment shader.
	 * @return the source code
	 */
	public String getFragmentShaderSource() {
		return fragShaderSource;
	}
	
	/**
	 * Returns the OpenGL handle for this shader program
	 * @return the program ID
	 */
	public int getID() {
		return program;
	}
	
	/**
	 * A shader program is "valid" if it's ID is not zero. Upon
	 * releasing a program, the ID will be set to zero. 
	 * 
	 * @return whether this program is valid
	 */
	public boolean valid() {
		return program != 0;
	}
	
	private void fetchUniforms() {
		int len = glGetProgram(program, GL_ACTIVE_UNIFORMS);
		//max length of all uniforms stored in program
		int strLen = glGetProgram(program, GL_ACTIVE_UNIFORM_MAX_LENGTH);
		
		for (int i=0; i<len; i++) {
			String name = glGetActiveUniform(program, i, strLen);
			int id = glGetUniformLocation(program, name);
			uniforms.put(name, id);
		}
	}
	

	/**
	 * Returns the location of the uniform by name. If the uniform
	 * is not found and we are in strict mode, an IllegalArgumentException
	 * will be thrown, otherwise -1 will be returned if no active uniform
	 * by that name exists.
	 * @param name the uniform name
	 * @return the ID (location) in the shader program
	 */
	public int getUniformLocation(String name) {
		Integer locI = uniforms.get(name);
		int location = locI==null ? -1 : locI.intValue();
		if (location!=-1)
			return location;
		location = glGetUniformLocation(program, name);
		if (location == -1 && strict)
			throw new IllegalArgumentException("no active uniform by name '"+name+"' " +
					"(disable strict compiling to suppress warnings)");
		uniforms.put(name, location); 
		return location;
	}

	/**
	 * Returns the location of the attribute by name. If the attribute
	 * is not found and we are in strict mode, an IllegalArgumentException
	 * will be thrown, otherwise -1 will be returned if no active attribute
	 * by that name exists.
	 * @param name the attribute name
	 * @return the ID (location) in the shader program
	 */
	public int getAttributeLocation(String name) {
		VertexAttrib a = getAttribute(name);
		int loc = a != null ? a.location : -1;
		if (loc==-1 && strict)
			throw new IllegalArgumentException("no active attribute by the name '"+name+"' " +
					"(disable strict compiling to suppress warnings)");
		return loc;
	}
	
	public VertexAttrib getAttribute(String name) {
		for (VertexAttrib a : attributes) {
			if (name.equals(a.name))
				return a;
		}
		return null;
	}
	
	/**
	 * Returns the size of the list of active attributes.
	 * @return the number of active attributes in this program
	 */
	public int getAttributeCount() {
		return attributes.size();
	}
	
	/**
	 * Returns the active attribute at the given INDEX in the
	 * list of attributes (not the same as LOCATION).
	 * @param index
	 * @return
	 */
	public VertexAttrib getAttributeAt(int index) {
		return attributes.get(index);
	}
	
	/**
	 * Returns an array of active attributes in this program; the user is
	 * free to modify this array. For maximum performance, you should use
	 * getAttributeCount() and getAttributeAt(int) for fast access.
	 * @return an array representing the vertex attributes
	 */
	public VertexAttrib[] attributes() {
		return attributes.toArray(new VertexAttrib[getAttributeCount()]);
	}
	
	/**
	 * Creates and returns an array for all the active attribute names 
	 * found in this program.
	 * @return the list of active attribute names
	 */
	public String[] getAttributeNames() {
		String[] s = new String[attributes.size()];
		for (int i=0; i<s.length; i++)
			s[i] = attributes.get(i).name;
		return s;
	}
	
	/**
	 * Creates and returns an array for all active uniforms that were found
	 * when linking the program.
	 * @return an array list of active uniform names
	 */
	public String[] getUniformNames() {
		return uniforms.keySet().toArray(new String[uniforms.size()]);
	}

	private FloatBuffer uniformf(String name) {
		if (fbuf16==null)
			fbuf16 = BufferUtils.createFloatBuffer(16);
		fbuf16.clear();
		getUniform(name, fbuf16);
		return fbuf16;
	}
	
	private IntBuffer uniformi(String name) {
		//TODO: add setters/getters for ivec2, ivec3, ivec4
		if (ibuf4==null)
			ibuf4 = BufferUtils.createIntBuffer(4);
		ibuf4.clear();
		getUniform(name, ibuf4);
		return ibuf4;
	}
	

	/**
	 * Retrieves data from a uniform and places it in the given buffer. If 
	 * strict mode is enabled, this will throw an IllegalArgumentException
	 * if the given uniform is not 'active' -- i.e. if GLSL determined that
	 * the shader isn't using it. If strict mode is disabled, this method will
	 * return <tt>true</tt> if the uniform was found, and <tt>false</tt> otherwise.
	 * 
	 * @param name the name of the uniform
	 * @param buf the buffer to place the data
	 * @return true if the uniform was found, false if there is no active uniform by that name
	 */
	public boolean getUniform(String name, FloatBuffer buf) {
		int id = getUniformLocation(name);
		if (id==-1) return false;
		glGetUniform(program, id, buf);
		return true;
	}
	
	/**
	 * Retrieves data from a uniform and places it in the given buffer. If 
	 * strict mode is enabled, this will throw an IllegalArgumentException
	 * if the given uniform is not 'active' -- i.e. if GLSL determined that
	 * the shader isn't using it. If strict mode is disabled, this method will
	 * return <tt>true</tt> if the uniform was found, and <tt>false</tt> otherwise.
	 * 
	 * @param name the name of the uniform
	 * @param buf the buffer to place the data
	 * @return true if the uniform was found, false if there is no active uniform by that name
	 */
	public boolean getUniform(String name, IntBuffer buf) {
		int id = getUniformLocation(name);
		if (id==-1) return false;
		glGetUniform(program, id, buf);
		return true;
	}
	
	
	/**
	 * Sets the value of a vec2 uniform to the given Vector2f.
	 * @param name the vec2 uniform
	 * @param vec the vector to use
	 */
	public void setUniform(String name, Vector2f vec) {
		setUniform(name, vec.x, vec.y);
	}
	
	/**
	 * Sets the value of a vec3 uniform to the given Vector3f.
	 * @param name the vec3 uniform
	 * @param vec the vector to use
	 */
	public void setUniform(String name, Vector3f vec) {
		setUniform(name, vec.x, vec.y, vec.z);
	}
	
	/**
	 * Sets the value of a vec4 uniform to the given Vector4f.
	 * @param name the vec4 uniform
	 * @param vec the vector to use
	 */
	public void setUniform(String name, Vector4f vec) {
		setUniform(name, vec.x, vec.y, vec.z, vec.w);
	}
	

	/**
	 * A convenience method to retrieve an integer/sampler2D uniform.
	 * @param name the uniform name
	 * @return the value
	 */
	public int getUniform1i(String name) {
		return uniformi(name).get(0);
	}

	/**
	 * A convenience method to retrieve an ivec2 uniform;
	 * for maximum performance and memory efficiency you 
	 * should use getUniform(String, IntBuffer) with a shared
	 * buffer.
	 * @param name the name of the uniform
	 * @return a newly created int[] array with 2 elements; e.g. (x, y)
	 */
	public int[] getUniform2i(String name) {
		IntBuffer buf = uniformi(name);
		return new int[] { buf.get(0), buf.get(1) };
	}

	/**
	 * A convenience method to retrieve an ivec3 uniform;
	 * for maximum performance and memory efficiency you 
	 * should use getUniform(String, IntBuffer) with a shared
	 * buffer.
	 * @param name the name of the uniform
	 * @return a newly created int[] array with 3 elements; e.g. (x, y, z)
	 */
	public int[] getUniform3i(String name) {
		IntBuffer buf = uniformi(name);
		return new int[] { buf.get(0), buf.get(1), buf.get(2) };
	}

	/**
	 * A convenience method to retrieve an ivec4 uniform;
	 * for maximum performance and memory efficiency you 
	 * should use getUniform(String, IntBuffer) with a shared
	 * buffer.
	 * @param name the name of the uniform
	 * @return a newly created int[] array with 2 elements; e.g. (r, g, b, a)
	 */
	public int[] getUniform4i(String name) {
		IntBuffer buf = uniformi(name);
		return new int[] { buf.get(0), buf.get(1), buf.get(2), buf.get(3) };
	}
	
	/**
	 * A convenience method to retrieve a float uniform.
	 * @param name the uniform name
	 * @return the value
	 */
	public float getUniform1f(String name) {
		return uniformf(name).get(0);
	}
	
	/**
	 * A convenience method to retrieve a vec2 uniform;
	 * for maximum performance and memory efficiency you 
	 * should use getUniform(String, FloatBuffer) with a shared
	 * buffer.
	 * @param name the name of the uniform
	 * @return a newly created float[] array with 2 elements; e.g. (x, y)
	 */
	public float[] getUniform2f(String name) {
		FloatBuffer buf = uniformf(name);
		return new float[] { buf.get(0), buf.get(1) };
	}

	/**
	 * A convenience method to retrieve a vec3 uniform;
	 * for maximum performance and memory efficiency you 
	 * should use getUniform(String, FloatBuffer) with a shared
	 * buffer.
	 * @param name the name of the uniform
	 * @return a newly created float[] array with 3 elements; e.g. (x, y, z)
	 */
	public float[] getUniform3f(String name) {
		FloatBuffer buf = uniformf(name);
		return new float[] { buf.get(0), buf.get(1), buf.get(2) };
	}

	/**
	 * A convenience method to retrieve a vec4 uniform;
	 * for maximum performance and memory efficiency you 
	 * should use getUniform(String, FloatBuffer) with a shared
	 * buffer.
	 * @param name the name of the uniform
	 * @return a newly created float[] array with 4 elements; e.g. (r, g, b, a)
	 */
	public float[] getUniform4f(String name) {
		FloatBuffer buf = uniformf(name);
		return new float[] { buf.get(0), buf.get(1), buf.get(2), buf.get(3) };
	}
	
	/**
	 * Whether the shader program was linked with the active uniform by the given name. A
	 * uniform might be "inactive" even if it was declared at the top of a shader;
	 * if GLSL finds that a uniform isn't needed (i.e. not used in shader), then
	 * it will not be active.
	 * @param name the name of the uniform
	 * @return true if this shader program could find the active uniform
	 */
	public boolean hasUniform(String name) {
		return uniforms.containsKey(name);
	}

	/**
	 * Whether the shader program was linked with the active attribute by the given name. A
	 * attribute might be "inactive" even if it was declared at the top of a shader;
	 * if GLSL finds that a attribute isn't needed (i.e. not used in shader), then
	 * it will not be active.
	 * @param name the name of the attribute
	 * @return true if this shader program could find the active attribute
	 */
	public boolean hasAttribute(String name) {
		return attributes.indexOf(name) != -1;
	}

	/**
	 * Sets the value of a float uniform.
	 * @param name the uniform by name
	 * @param f the float value
	 */
	public void setUniform(String name, float f) {
		int id = getUniformLocation(name);
		if (id==-1) return;
		glUniform1f(id, f);
	}
	
	/**
	 * Sets the value of a sampler2D uniform.
	 * @param name the uniform by name
	 * @param i the integer / active texture (e.g. 0 for TEXTURE0)
	 */
	public void setUniform(String name, int i) {
		int id = getUniformLocation(name);
		if (id==-1) return;
		glUniform1i(id, i);
	}
	
	/**
	 * Sets the value of a vec2 uniform.
	 * @param name the uniform by name
	 * @param a vec.x / tex.s
	 * @param b vec.y / tex.t
	 */
	public void setUniform(String name, float a, float b) {
		int id = getUniformLocation(name);
		if (id==-1) return;
		glUniform2f(id, a, b);
	}
	
	/**
	 * Sets the value of a vec3 uniform.
	 * @param name the uniform by name
	 * @param a vec.x / color.r / tex.s
	 * @param b vec.y / color.g / tex.t
	 * @param c vec.z / color.b / tex.p
	 */
	public void setUniform(String name, float a, float b, float c) {
		int id = getUniformLocation(name);
		if (id==-1) return;
		glUniform3f(id, a, b, c);
	}

	/**
	 * Sets the value of a vec4 uniform.
	 * @param name the uniform by name
	 * @param a vec.x / color.r
	 * @param b vec.y / color.g
	 * @param c vec.z / color.b 
	 * @param d vec.w / color.a 
	 */
	public void setUniform(String name, float a, float b, float c, float d) {
		int id = getUniformLocation(name);
		if (id==-1) return;
		glUniform4f(id, a, b, c, d);
	}
	
	/**
	 * Sets the value of a ivec2 uniform.
	 * @param name the uniform by name
	 * @param a vec.x / tex.s
	 * @param b vec.y / tex.t
	 */
	public void setUniform(String name, int a, int b) {
		int id = getUniformLocation(name);
		if (id==-1) return;
		glUniform2i(id, a, b);
	}

	/**
	 * Sets the value of a ivec3 uniform.
	 * @param name the uniform by name
	 * @param a vec.x / color.r
	 * @param b vec.y / color.g
	 * @param c vec.z / color.b 
	 */
	public void setUniform(String name, int a, int b, int c) {
		int id = getUniformLocation(name);
		if (id==-1) return;
		glUniform3i(id, a, b, c);
	}
	
	/**
	 * Sets the value of a ivec4 uniform.
	 * @param name the uniform by name
	 * @param a vec.x / color.r
	 * @param b vec.y / color.g
	 * @param c vec.z / color.b 
	 * @param d vec.w / color.a 
	 */
	public void setUniform(String name, int a, int b, int c, int d) {
		int id = getUniformLocation(name);
		if (id==-1) return;
		glUniform4i(id, a, b, c, d);
	}
	
	/**
	 * Sets a uniform matrix2 with the given name and transpose.
	 * @param name the name to use
	 * @param transpose whether to transpose the matrix
	 * @param buf the buffer representing the matrix2
	 */
	public void setUniformMatrix2(String name, boolean transpose, FloatBuffer buf) {
		int id = getUniformLocation(name);
		if (id==-1) return;
		glUniformMatrix2(id, transpose, buf);
	}

	/**
	 * Sets a uniform matrix3 with the given name and transpose.
	 * @param name the name to use
	 * @param transpose whether to transpose the matrix
	 * @param buf the buffer representing the matrix3
	 */
	public void setUniformMatrix3(String name, boolean transpose, FloatBuffer buf) {
		int id = getUniformLocation(name);
		if (id==-1) return;
		glUniformMatrix3(id, transpose, buf);
	}

	/**
	 * Sets a uniform matrix4 with the given name and transpose.
	 * @param name the name to use
	 * @param transpose whether to transpose the matrix
	 * @param buf the buffer representing the matrix4
	 */
	public void setUniformMatrix4(String name, boolean transpose, FloatBuffer buf) {
		int id = getUniformLocation(name);
		if (id==-1) return;
		glUniformMatrix4(id, transpose, buf);
	}

	public void setUniformMatrix3(String name, boolean transpose, Matrix3f m) {
		int id = getUniformLocation(name);
		if (id==-1) return;
		if (fbuf16==null)
			fbuf16 = BufferUtils.createFloatBuffer(16);
		fbuf16.clear();
		m.store(fbuf16);
		fbuf16.flip();
		glUniformMatrix3(id, transpose, fbuf16);
	}
	
	public void setUniformMatrix4(String name, boolean transpose, Matrix4f m) {
		int id = getUniformLocation(name);
		if (id==-1) return;
		if (fbuf16==null)
			fbuf16 = BufferUtils.createFloatBuffer(16);
		fbuf16.clear();
		m.store(fbuf16);
		fbuf16.flip();
		glUniformMatrix4(id, transpose, fbuf16);
	}
}