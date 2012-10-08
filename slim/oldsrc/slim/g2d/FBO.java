package slim.g2d;

import static org.lwjgl.opengl.EXTFramebufferObject.GL_FRAMEBUFFER_EXT;
import static org.lwjgl.opengl.EXTFramebufferObject.glBindFramebufferEXT;
import static org.lwjgl.opengl.EXTFramebufferObject.glCheckFramebufferStatusEXT;
import static org.lwjgl.opengl.EXTFramebufferObject.glDeleteFramebuffersEXT;
import static org.lwjgl.opengl.EXTFramebufferObject.glFramebufferTexture2DEXT;
import static org.lwjgl.opengl.EXTFramebufferObject.glGenFramebuffersEXT;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_PROJECTION;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_TRANSFORM_BIT;
import static org.lwjgl.opengl.GL11.GL_VIEWPORT_BIT;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glLoadIdentity;
import static org.lwjgl.opengl.GL11.glMatrixMode;
import static org.lwjgl.opengl.GL11.glOrtho;
import static org.lwjgl.opengl.GL11.glPopAttrib;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushAttrib;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL30.GL_COLOR_ATTACHMENT0;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER_COMPLETE;
import static org.lwjgl.opengl.GL30.glDeleteFramebuffers;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;

import slim.texture.Texture;
import slim.texture.Texture2D;
import slimold.SlimException;

/**
 * A very thin wrapper around OpenGL Frame Buffer Objects, mostly intended for
 * 2D purposes.  
 * @author davedes
 */
public class FBO {
	
	public static boolean isSupported() {
		return GLContext.getCapabilities().GL_EXT_framebuffer_object;
	}
	
	/** Our render texture. */
	private Texture2D texture;
	/** The ID of the FBO in use */
	private int id;
	private int width, height;
	
	private int pushAttrib = GL_VIEWPORT_BIT | GL_TRANSFORM_BIT | GL_COLOR_BUFFER_BIT;
	
	public static final int NO_BITS = 0;
	
	private Image image;
	
	public FBO(Texture2D texture) throws SlimException {
		if (!isSupported()) {
			throw new SlimException("FBO extension not supported in hardware");
		}
		this.texture = texture;
		this.width = texture.getWidth();
		this.height = texture.getHeight();
		texture.bind();
		id = glGenFramebuffersEXT();
		glBindFramebufferEXT(GL_FRAMEBUFFER, id);
		glFramebufferTexture2DEXT(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0,
							 	  texture.getTarget(), texture.getID(), 0);
		int result = glCheckFramebufferStatusEXT(GL_FRAMEBUFFER);
		if (result!=GL_FRAMEBUFFER_COMPLETE) {
			glBindFramebufferEXT(GL_FRAMEBUFFER, 0);
			glDeleteFramebuffers(id);
			throw new SlimException("exception "+result+" when checking FBO status");
		}
		
		glBindFramebufferEXT(GL_FRAMEBUFFER, 0);
	}
	
	/**
	 * Creates a new TEXTURE_2D texture for this FBO.
	 * @param width
	 * @param height
	 * @throws SlimException
	 */
	public FBO(int width, int height, int filter) throws SlimException {
		this(new Texture2D(width, height, filter));
	}
	
	public FBO(int width, int height) throws SlimException {
		this(width, height, Texture.FILTER_LINEAR);
	}
	
	/**
	 * Lazily creates and returns the image instance for this FBO.
	 * @return the Image2D used for this FBO's color buffer texture
	 */
	public Image getImage() {
		if (image==null)
			image = new Image(getTexture());
		return image;
	}
	
	public void setPushAttrib(int attrib) {
		this.pushAttrib = attrib;
	}
	
	public int getPushAttrib() {
		return pushAttrib;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public int getID() {
		return id;
	}
	
	public Texture2D getTexture() {
		return texture;
	}
	
	/**
	 * Bind to the FBO created
	 */
	public void bind() {
		if (id == 0)
			throw new IllegalStateException("can't use FBO as it has been destroyed..");
		if (pushAttrib != NO_BITS)
			glPushAttrib(pushAttrib);
		
		glViewport(0, 0, width, height);
	    glMatrixMode(GL_PROJECTION);
		glPushMatrix();
	    glLoadIdentity();
	    glOrtho(0, width, 0, height, 1, -1);
	    glMatrixMode(GL_MODELVIEW);
		glPushMatrix();
	    glLoadIdentity();
	    glEnable(GL_BLEND);
	    glClearColor(0f, 0f, 0f, 0f);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        
		glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, id);
		//glReadBuffer(GL_COLOR_ATTACHMENT0);
	}
	
	/**
	 * Unbind from the FBO created
	 */
	public void unbind() {
		if (id==0)
			return;
		glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, 0);

		glMatrixMode(GL_PROJECTION);
		glPopMatrix();
		glMatrixMode(GL_MODELVIEW);
		glPopMatrix();
		if (pushAttrib != NO_BITS) 
			glPopAttrib();
	}
	
	/**
	 * Destroys this FBO, but does not release the associated texture(s).
	 */
	public void destroy() {
		glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, 0);
		//glReadBuffer(GL_BACK);
		glDeleteFramebuffersEXT(id);
		id = 0;
	}
}