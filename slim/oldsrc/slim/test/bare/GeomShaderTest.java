package slim.test.bare;

import java.io.IOException;
import java.net.URL;
import java.nio.FloatBuffer;
import java.util.HashMap;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GLContext;

import slim.g2d.Image;
import slim.shader2.GeometryShaderProgram;
import slim.shader2.ShaderProgram;
import slim.texture.Texture;
import slim.texture.Texture2D;
import slim.util2.Utils2;
import slimold.Color;
import slimold.GL2D;
import slimold.SlimException;
import slimold.SpriteBatch;
import de.matthiasmann.twl.Event;

public class GeomShaderTest extends GUITestBase {
	public static void main(String[] args) throws SlimException {
		new GeomShaderTest().start();
	}
	
	Image img, img2;
	ShaderProgram prog;
	
	Texture texture;
	FloatBuffer transform, colors, texcoords, vertices;
	int idx = 0;
	

	int maxVerts = 1000;
	int numberOfBalls = 2000;
	Ball[] balls = new Ball[100000];
	static final int SCREEN_WIDTH = 800, SCREEN_HEIGHT = 600;
	float ballWidth, ballHeight;
	boolean useGeom = true, compressed = true;
	
	SpriteBatch batch;
	
	@Override
	public void init() throws SlimException {
		init2D();
		setTargetFPS(-1);
		GL2D.setBackground(Color.gray);
		URL url = Utils2.getResource("res/small.png");
		
		img = new Image(url);
		
		try {
			img2 = new Image(Texture2D.loadTexture(url, Texture.Format.COMPRESSED_RGB_DXT1));
			img2.getTexture().bind();
			int i = GL11.glGetTexLevelParameteri(img2.getTexture().getTarget(), 0, GL13.GL_TEXTURE_COMPRESSED);
			if (i==GL11.GL_TRUE)
				System.out.println("TRUE");
			else
				System.out.println("FALSE");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ballWidth = img.getWidth();
		ballHeight = img.getHeight();
		//17500, 18300
		vertices = BufferUtils.createFloatBuffer(maxVerts * 2);
		transform = BufferUtils.createFloatBuffer(maxVerts * 2);
		texcoords = BufferUtils.createFloatBuffer(maxVerts * 4);
		colors = BufferUtils.createFloatBuffer(maxVerts * 4);
		
		batch = new SpriteBatch(maxVerts);
		
		ShaderProgram.setStrictMode(false);
		
		//attribute locations
		HashMap<String, Integer> attributes = new HashMap<String, Integer>();
		attributes.put("SpriteTransform", 1);
		attributes.put("SpriteTexCoords", 2);
		attributes.put("SpriteColor", 3);
		
		//set up our geom shader program
		prog = GeometryShaderProgram.loadProgram(
					"res/shader/sprite.vert", 
					"res/shader/sprite.frag",
					"res/shader/sprite.geom",
					GL11.GL_POINTS,
					GL11.GL_TRIANGLE_STRIP,
					4, attributes);
		
		//set up sampler2D value
		prog.bind();
		prog.setUniform1i("tex0", 0);
		
		GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
		
		// initilise balls
		for (int i = 0; i < balls.length; i++) {
			balls[i] = new Ball((int)(Utils2.rndFloat() * SCREEN_WIDTH), (int)(Utils2.rndFloat() * SCREEN_HEIGHT));
		}
	}
	
	public void handleEvent(Event e) {
		if (e.isKeyPressedEvent()) {
			switch(e.getKeyCode()) {
				case Event.KEY_SPACE: 
					useGeom = !useGeom; 
					if (useGeom)
						prog.bind();
					else
						prog.unbind();
					break;
				case Event.KEY_1: numberOfBalls = 1; break;
				case Event.KEY_2: numberOfBalls = 2; break;
				case Event.KEY_3: numberOfBalls = 4; break;
				case Event.KEY_4: numberOfBalls = 250; break;
				case Event.KEY_5: numberOfBalls = 500; break;
				case Event.KEY_6: numberOfBalls = 1000; break;
				case Event.KEY_7: numberOfBalls = 5000; break;
				case Event.KEY_8: numberOfBalls = 10000; break;
				case Event.KEY_9: numberOfBalls = 15000; break;
				case Event.KEY_ADD: numberOfBalls += 100; break;
				case Event.KEY_0: numberOfBalls -= 100; break;
				case 147: numberOfBalls -= 100; break;
				case 13: numberOfBalls += 100; break;
				case Event.KEY_C: compressed = !compressed; break;
			}
			// cap at max balls
			if (numberOfBalls > balls.length) numberOfBalls = balls.length;
			if (numberOfBalls < 0) numberOfBalls = 0;
		}
	}
	
	@Override
	public void render() throws SlimException {
		Image img = compressed ? this.img2 : this.img;
		for (int i = 0; i < numberOfBalls; i++) {
			if (useGeom)
				drawSprite(img, balls[i].x, balls[i].y, ballWidth, ballHeight, Color.white);
			else
				batch.drawImage(img, balls[i].x, balls[i].y, ballWidth, ballHeight);
		}
		if (useGeom)
			flush();
		else
			batch.flush();
	}
	
	protected void flush() {
		if (idx==0) 
			return;
		//bind the last texture
		if (texture!=null) {
			Texture2D.enable(texture.getTarget());
			texture.bind();
		}
		vertices.flip();
		transform.flip();
		texcoords.flip();
		colors.flip();
		
		GL20.glEnableVertexAttribArray(1);
		GL20.glVertexAttribPointer(1, 2, false, 0, transform);
		
		GL20.glEnableVertexAttribArray(2);
		GL20.glVertexAttribPointer(2, 4, false, 0, texcoords);
//		GL13.gl
		
		GL20.glEnableVertexAttribArray(3);
		GL20.glVertexAttribPointer(3, 4, false, 0, colors);
		
		GL11.glVertexPointer(2, 0, vertices);
		
	    GL11.glDrawArrays(GL11.GL_POINTS, 0, idx);
		
		GL20.glDisableVertexAttribArray(1);
		GL20.glDisableVertexAttribArray(2);
		GL20.glDisableVertexAttribArray(3);
		
		vertices.clear();
		transform.clear();
		texcoords.clear();
		colors.clear();
	    idx = 0;
	}
	
	private void checkRender(Image image) {
		if (image==null || image.getTexture()==null)
			throw new NullPointerException("null texture");
		
		//we need to bind a different texture. this is
		//for convenience; ideally the user should order
		//their rendering wisely to minimize texture binds	
		if (image.getTexture()!=texture) {
			//apply the last texture
			flush();
			texture = image.getTexture();
		} else if (idx >= maxVerts) 
			flush();
	}
	
	public void drawSprite(Image image, float x, float y, float w, float h, Color tint) {
		checkRender(image);
		float u = image.getNormalizedXOffset();
		float v = image.getNormalizedYOffset();
		float uw = image.getNormalizedWidth();
		float uh = image.getNormalizedHeight();
		vertices.put(x).put(y); //gl_Position
		transform.put(w).put(h); //SpriteTransform
		texcoords.put(u).put(v).put(uw).put(uh); //SpriteTexCoords
		colors.put(tint.r).put(tint.g).put(tint.b).put(tint.a); //SpriteColor
		idx++;
	}
	
	@Override
	public void update(int delta) throws SlimException {
		Display.setTitle(numberOfBalls+" "+getFPS()+(useGeom?" (geom)":"")+(compressed?" (compressed)":""));
		// update ball movement
		for (int i = 0; i < numberOfBalls; i++) {
			balls[i].update(delta);
		}
	}

	@Override
	public URL getThemeURL() throws SlimException {
		return Utils2.getResource("res/gui/chutzpah/chutzpah.xml");
	}

	public static class Ball {
		public float x, y;
		public float dy, dx;
		
		public Ball(int x, int y) {
			this.x = x;
			this.y = y;
			
			this.dx = 0.1f + (float)(Utils2.rnd()*0.1f) * (Utils2.rnd() < 0.5 ? 1 : -1);
			this.dy = 0.1f + (float)(Utils2.rnd()*0.1f) * (Utils2.rnd() < 0.5 ? 1 : -1);
		}
		
		public void update(int delta) {
			
			// ball movement
			x += dx * delta;
			y += dy * delta;
			
			// wall collision
			if (x < 0) {
				x = 0;
				dx = -dx;
			}
			
			if (x > SCREEN_WIDTH - 42) {
				x = SCREEN_WIDTH - 42;
				dx = -dx;
			}
			
			if (y < 0) {
				y = 0;
				dy = -dy;
			}
			
			if (y > SCREEN_HEIGHT - 42) {
				y = SCREEN_HEIGHT - 42;
				dy = -dy;
			}
		}
	}
}
