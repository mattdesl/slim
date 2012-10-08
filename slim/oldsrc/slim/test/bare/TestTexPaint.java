/**
 * Copyright (c) 2012, Matt DesLauriers All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 *   * Redistributions of source code must retain the above copyright notice, this
 *     list of conditions and the following disclaimer. 
 * 
 *   * Redistributions in binary
 *     form must reproduce the above copyright notice, this list of conditions and
 *     the following disclaimer in the documentation and/or other materials provided
 *     with the distribution. 
 * 
 *   * Neither the name of the Matt DesLauriers nor the names
 *     of his contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package slim.test.bare;

import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glRotatef;
import static org.lwjgl.opengl.GL11.glTexCoord2f;
import static org.lwjgl.opengl.GL11.glTranslatef;
import static org.lwjgl.opengl.GL11.glVertex3f;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;

import slim.texture.Texture;
import slim.texture.Texture2D;
import slim.texture.TextureLoader;
import slim.texture.io.ImageDecoder;
import slim.util2.Utils2;
import slimold.SlimException;
import de.matthiasmann.twl.Event;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.ResizableFrame;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.renderer.Font;



public class TestTexPaint extends GUITestBase {
	public static void main(String[] args) throws SlimException {
		
		System.out.println((int)'*');
		TestTexPaint p = new TestTexPaint();
		try { p.start(); }
		catch (Exception e) { 
			e.printStackTrace();
			p.destroy();
			System.exit(0);
		}
	}
	
	public TestTexPaint() {
		super(800,600,false);
	}

	float rotqube;

	private Texture2D tex;
	private int watcher;
	
	private int pollDelay = 500;
	private int pollTime = pollDelay;
	
	private long lastModified = 0;
	
	
	private final File TEXTURE_PATH = new File("src/res/clouds.png");
	private URL textureURL;
	private boolean needsUpdate = false;
	private Label helloTwlLabel;
	private Widget widgets;
	private Font font;
	
	@Override
	public URL getThemeURL() throws SlimException {
		return Utils2.getResource("res/gui/chutzpah/chutzpah.xml");
	}
	
	protected void handleEvent(Event evt) {
		
	}
	
	@Override
	public void init() throws SlimException {
		System.out.println(GLContext.getCapabilities().GL_ARB_texture_float);
		System.out.println(GLContext.getCapabilities().GL_ARB_draw_instanced);
		GL11.glClearColor(0.8f, 0.8f, 0.8f, 1f);
		init3D();
		
		Texture2D.enable(GL11.GL_TEXTURE_2D);
		try {
			textureURL = TEXTURE_PATH.toURI().toURL();
		} catch (Exception e) {
			throw new SlimException(e);
		}
		
		updateTexture();
		
		font = theme.getDefaultFont();
		System.out.println("font is "+font);
		
//		Object obj = theme.getCursor("cursor_pointer");
//		if (obj!=null && obj instanceof MouseCursor) {
//			getRootPane().setMouseCursor((MouseCursor)obj);
//		} else {
//			Utils.warn("No default cursor found...");
//		}
//		ComboBox<String> cb = new ComboBox<String>();
//		SimpleChangableListModel<String> model = new SimpleChangableListModel<String>();
//        for(Field f : Color.class.getFields()) {
//            if(Modifier.isStatic(f.getModifiers()) && Modifier.isPublic(f.getModifiers())) {
//                model.addElement(f.getName().toLowerCase());
//            }
//        }
//        cb.setModel(model);
//        cb.setComputeWidthFromModel(true);
//        cb.setSelected(0);
//        cb.setPosition(5, 5);
//        cb.setTooltipContent("hello");
//        widgets = cb;
		
		System.out.println(0x01);
		
        widgets = new TestFrame();
        getRootPane().add(widgets);
	}
	
	protected void layoutRootPane() {
		widgets.adjustSize();
	}
	
	private void updateTexture() throws SlimException {
		if (!TEXTURE_PATH.exists()) {
			return;
		}
		try {
			ImageDecoder d = TextureLoader.get().createDecoder(textureURL);
	        if (!d.open()) 
	            throw new IOException("could not open a decoder for "+textureURL.getPath());
	        int width = d.getWidth();
	        int height = d.getHeight();
	        Texture2D.Format fmt = d.getFormat();
	        
	        int perPixel = fmt.getBytesPerPixel();
	        ByteBuffer buf = null;
	        try {
	            buf = BufferUtils.createByteBuffer(width * height * perPixel);
	            d.decode(buf);
	        } finally {
	            d.close();
	        }
	        buf.flip();
	        if (tex==null) { //lazily create the texture
	        	tex = new Texture2D(width, height, Texture.Format.RGBA, fmt, buf);
	        	tex.setFilter(GL11.GL_NEAREST);
	        } else { //upload the texture data...
	        	tex.uploadImage(width, height, tex.getFormat(), fmt, buf);
	        }
		} catch (IOException e) {
			throw new SlimException(e);
		}
		needsUpdate = false;
	}

	private void pollChange() throws SlimException {
		if (lastModified==0) {
			lastModified = TEXTURE_PATH.lastModified();
			return;
		}
		long old = lastModified;
		lastModified = TEXTURE_PATH.lastModified();
		if (old!=lastModified || needsUpdate) {
			updateTexture();
			needsUpdate = true;
		}
	}

	public void onResize() {
		
	}
	
	@Override
	public void render() throws SlimException {
		GL11.glColor4f(1f,1f,1f,1f);
		glTranslatef(0.0f, 0.0f, -7.0f); // Translate Into The Screen 7.0 Units
		glRotatef(rotqube, 0.0f, 1.0f, 0.0f); // Rotate The cube around the Y axis
		
		Texture2D.enable(tex.getTarget());
		tex.bind();
		
		glRotatef(rotqube, 1.0f, 1.0f, 1.0f);
		glBegin(GL_QUADS); // Draw The Cube Using quads
		    // Front Face
		    glTexCoord2f(0.0f, 0.0f); glVertex3f(-1.0f, -1.0f,  1.0f);  // Bottom Left Of The Texture and Quad
		    glTexCoord2f(1.0f, 0.0f); glVertex3f( 1.0f, -1.0f,  1.0f);  // Bottom Right Of The Texture and Quad
		    glTexCoord2f(1.0f, 1.0f); glVertex3f( 1.0f,  1.0f,  1.0f);  // Top Right Of The Texture and Quad
		    glTexCoord2f(0.0f, 1.0f); glVertex3f(-1.0f,  1.0f,  1.0f);  // Top Left Of The Texture and Quad
		    // Back Face
		    glTexCoord2f(1.0f, 0.0f); glVertex3f(-1.0f, -1.0f, -1.0f);  // Bottom Right Of The Texture and Quad
		    glTexCoord2f(1.0f, 1.0f); glVertex3f(-1.0f,  1.0f, -1.0f);  // Top Right Of The Texture and Quad
		    glTexCoord2f(0.0f, 1.0f); glVertex3f( 1.0f,  1.0f, -1.0f);  // Top Left Of The Texture and Quad
		    glTexCoord2f(0.0f, 0.0f); glVertex3f( 1.0f, -1.0f, -1.0f);  // Bottom Left Of The Texture and Quad
		    // Top Face
		    glTexCoord2f(0.0f, 1.0f); glVertex3f(-1.0f,  1.0f, -1.0f);  // Top Left Of The Texture and Quad
		    glTexCoord2f(0.0f, 0.0f); glVertex3f(-1.0f,  1.0f,  1.0f);  // Bottom Left Of The Texture and Quad
		    glTexCoord2f(1.0f, 0.0f); glVertex3f( 1.0f,  1.0f,  1.0f);  // Bottom Right Of The Texture and Quad
		    glTexCoord2f(1.0f, 1.0f); glVertex3f( 1.0f,  1.0f, -1.0f);  // Top Right Of The Texture and Quad
		    // Bottom Face
		    glTexCoord2f(1.0f, 1.0f); glVertex3f(-1.0f, -1.0f, -1.0f);  // Top Right Of The Texture and Quad
		    glTexCoord2f(0.0f, 1.0f); glVertex3f( 1.0f, -1.0f, -1.0f);  // Top Left Of The Texture and Quad
		    glTexCoord2f(0.0f, 0.0f); glVertex3f( 1.0f, -1.0f,  1.0f);  // Bottom Left Of The Texture and Quad
		    glTexCoord2f(1.0f, 0.0f); glVertex3f(-1.0f, -1.0f,  1.0f);  // Bottom Right Of The Texture and Quad
		    // Right face
		    glTexCoord2f(1.0f, 0.0f); glVertex3f( 1.0f, -1.0f, -1.0f);  // Bottom Right Of The Texture and Quad
		    glTexCoord2f(1.0f, 1.0f); glVertex3f( 1.0f,  1.0f, -1.0f);  // Top Right Of The Texture and Quad
		    glTexCoord2f(0.0f, 1.0f); glVertex3f( 1.0f,  1.0f,  1.0f);  // Top Left Of The Texture and Quad
		    glTexCoord2f(0.0f, 0.0f); glVertex3f( 1.0f, -1.0f,  1.0f);  // Bottom Left Of The Texture and Quad
		    // Left Face
		    glTexCoord2f(0.0f, 0.0f); glVertex3f(-1.0f, -1.0f, -1.0f);  // Bottom Left Of The Texture and Quad
		    glTexCoord2f(1.0f, 0.0f); glVertex3f(-1.0f, -1.0f,  1.0f);  // Bottom Right Of The Texture and Quad
		    glTexCoord2f(1.0f, 1.0f); glVertex3f(-1.0f,  1.0f,  1.0f);  // Top Right Of The Texture and Quad
		    glTexCoord2f(0.0f, 1.0f); glVertex3f(-1.0f,  1.0f, -1.0f);  // Top Left Of The Texture and Quad
		glEnd(); // End Drawing The Cube
	}
	
	@Override
	public void update(int delta) throws SlimException {
		pollTime += delta;
		if (pollTime >= pollDelay) {
			pollTime = 0;
			pollChange();
		}
		rotqube += 0.02f * delta;
		Display.setTitle(getFPS()+" FPS");
	}
	
}
