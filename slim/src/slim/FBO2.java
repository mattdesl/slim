package slim;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.opengl.GL11.GL_PROJECTION;
import static org.lwjgl.opengl.GL11.GL_TRANSFORM_BIT;
import static org.lwjgl.opengl.GL11.GL_VIEWPORT_BIT;
import static org.lwjgl.opengl.GL11.glMatrixMode;
import static org.lwjgl.opengl.GL11.glPopAttrib;
import static org.lwjgl.opengl.GL11.glPopMatrix;

import java.io.IOException;

import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GLContext;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.opengl.InternalTextureLoader;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.renderer.Renderer;
import org.newdawn.slick.opengl.renderer.SGL;

public class FBO2 {
	
	public static boolean isSupported() {
		return GLContext.getCapabilities().OpenGL30 || GLContext.getCapabilities().GL_EXT_framebuffer_object;
	}
	
	/** Our render texture. */
	private Texture2Dold texture;
	/** The ID of the FBO in use */
	private int id;
	private int width, height;
	private int pushAttrib = GL_VIEWPORT_BIT | GL_TRANSFORM_BIT | GL_COLOR_BUFFER_BIT;
	
	public static final int NO_BITS = 0;
	
	public FBO2(Texture2Dold texture) throws SlickException {
		if (!isSupported()) {
			throw new SlickException("Your OpenGL card does not support FBO and hence can't handle " +
					"the dynamic images required for this application.");
		}
		this.texture = texture;
		this.width = texture.getImageWidth();
		this.height = texture.getImageHeight();
		try {
			texture.bind();
			id = GL30.glGenFramebuffers();
			GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, id);
			GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0,
								 	  SGL.GL_TEXTURE_2D, texture.getTextureID(), 0);
			
			int result = GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER);
			
			GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
			
			if (result!=GL30.GL_FRAMEBUFFER_COMPLETE) {
				GL30.glDeleteFramebuffers(id);
				throw new SlickException("exception "+result+" with FBO");
			}
		} catch (SlickException ex) {
			throw ex;
		} catch (Exception e) {
			throw new SlickException("Failed to create new texture for FBO");
		}
	}
	
	public FBO2(int width, int height) throws SlickException, IOException {
		this(InternalTextureLoader.get().createTexture(width, height));
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
	
	public Texture2Dold getTexture() {
		return texture;
	}
	
	/**
	 * Bind to the FBO created
	 */
	public void bind() {
		if (id == 0)
			throw new IllegalStateException("can't use FBO as it has been destroyed..");
		SGL GL = Renderer.get();
		GL.enterOrtho(width, height);
		GL30.glBindFramebufferEXT(GL30.GL_FRAMEBUFFER, id);
	}
	
	/**
	 * Unbind from the FBO created
	 */
	public void unbind() {
		if (id==0)
			return;
		Renderer.get().flush();
		glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, 0);
		glMatrixMode(GL_PROJECTION);
		glPopMatrix();
		glMatrixMode(GL_MODELVIEW);
		glPopMatrix();
		if (pushAttrib != NO_BITS) 
			glPopAttrib();
	}
	
		
	/**
	 * @see org.newdawn.slick.Graphics#destroy()
	 */
	public void release() {
		glDeleteFramebuffersEXT(id);
		id = 0;
	}
}