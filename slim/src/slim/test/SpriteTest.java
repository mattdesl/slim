package slim.test;

import java.util.Random;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GLContext;
import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.BasicGame;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;

import space.engine.SpriteBatch;

public class SpriteTest extends BasicGame {

	public static final int SCREEN_WIDTH = 800;
	public static final int SCREEN_HEIGHT = 600;
	private static Random rnd = new Random();
	
	public static final float rnd() {
		return rnd.nextFloat();
	}
	
    public static void main(String[] args) throws SlickException {
    	new AppGameContainer(new SpriteTest(), SCREEN_WIDTH, SCREEN_HEIGHT, false).start();
    }

    public SpriteTest() {
        super("slick test");
    }
	
    SpriteBatchImage batch;
    Image image;
	boolean batching = true;
	boolean renderInUse = true;
	int batchSize =  5000;
	int numberOfBalls = 2000;
	Ball[] balls = new Ball[100000];
	int collisionDelay = 30, collisionTIme;
	boolean vsync = false;
	GameContainer container;
	private boolean vbo = false;
	boolean clearing = true;
	
	public void init(GameContainer c) throws SlickException {
		this.container = c;
		
		System.out.println(GLContext.getCapabilities().GL_EXT_gpu_shader4);
		System.out.println(GLContext.getCapabilities().GL_EXT_draw_instanced );
		System.out.println(GLContext.getCapabilities().GL_ARB_draw_instanced );
		System.out.println(GLContext.getCapabilities().GL_ARB_instanced_arrays );
		
		c.setShowFPS(false);
		c.getGraphics().setBackground(Color.white);
		c.setClearEachFrame(false);
		//19,200
		batch = new SpriteBatchImage(batchSize, vbo ? SpriteBatchImage.STRATEGY_VBO : SpriteBatchImage.STRATEGY_DEFAULT);
		image = new Image("res/small.png");
		
		// initilise balls
		for (int i = 0; i < balls.length; i++) {
			balls[i] = new Ball((int)(rnd() * SCREEN_WIDTH), (int)(rnd() * SCREEN_HEIGHT));
		}
	}
	
	public void render(GameContainer c, Graphics g) throws SlickException {
		if (clearing)
			g.clear();
		batch.renderCalls = 0;
		if (!batching && renderInUse)
			image.startUse();
		for (int i = 0; i < numberOfBalls; i++) {
			if (batching) 
				batch.drawImage(image, balls[i].x, balls[i].y);
			else if (renderInUse)
				image.drawEmbedded(balls[i].x, balls[i].y, image.getWidth(), image.getHeight());
			else
				image.draw(balls[i].x, balls[i].y);
		}
		if (batching)
			batch.flush();
		else if (renderInUse)
			image.endUse();
						
		Display.setTitle("FPS: "+c.getFPS()+" Count: "+numberOfBalls
				+" Mode: "+ 
					(batching 
							? "batch ("+batchSize+")" 
							: (renderInUse?"Image.drawEmbedded":"Image.draw"))
				+	(batching
							? " Render calls: "+batch.renderCalls
							: "")
				+ 	(vsync
							? " (vsync)"
							:""
					)
				+	(vbo 
							? " (vbo)"
							:""
					)
				+ (!clearing ? " (no-clear)":"")
		);
		
	}
	
	public void update(GameContainer c, int delta) throws SlickException {
		// update ball movement
		for (int i = 0; i < numberOfBalls; i++) {
			balls[i].update(delta);
		}
	}
	
	public void keyPressed(int key, char c) {
		switch (key) {
			case Input.KEY_SPACE: batching = !batching; break;
			case Input.KEY_C: clearing = !clearing; break;
        	case Input.KEY_1: numberOfBalls = 1; break;
        	case Input.KEY_2: numberOfBalls = 2; break;
        	case Input.KEY_3: numberOfBalls = 4; break;
        	case Input.KEY_4: numberOfBalls = 250; break;
        	case Input.KEY_5: numberOfBalls = 500; break;
        	case Input.KEY_6: numberOfBalls = 1000; break;
        	case Input.KEY_7: numberOfBalls = 5000; break;
        	case Input.KEY_8: numberOfBalls = 10000; break;
        	case Input.KEY_9: numberOfBalls = 15000; break;
        	case Input.KEY_ADD: numberOfBalls += 100; break;
        	case Input.KEY_0: numberOfBalls -= 100; break;
        	case Input.KEY_R: renderInUse = !renderInUse; break; //render in use 
        	case 147: numberOfBalls -= 100; break;
        	case 13: numberOfBalls += 100; break;
        	case Input.KEY_W: batchSize += 500; break;
        	case Input.KEY_Q: batchSize = Math.max(50, batchSize-500); break;
        	case Input.KEY_O: 
        		vbo = !vbo;
        		batch.flush(); 
        		batch = new SpriteBatchImage(batchSize, 
    				vbo ? SpriteBatchImage.STRATEGY_VBO : SpriteBatchImage.STRATEGY_VBO);
        		break;
        	case Input.KEY_B: batch.flush(); batch = new SpriteBatchImage(batchSize, 
        				vbo ? SpriteBatchImage.STRATEGY_VBO : SpriteBatchImage.STRATEGY_DEFAULT); 
        		break; //press B to update with new batch size 
        	case Input.KEY_V: vsync = !vsync;
        					  container.setVSync(vsync);break;
        	
		}
		
		// cap at max balls
		if (numberOfBalls > balls.length) numberOfBalls = balls.length;
		if (numberOfBalls < 0) numberOfBalls = 0;
	}
	
	
	public static class Ball {
		public float x, y;
		public float dy, dx;
		
		public Ball(int x, int y) {
			this.x = x;
			this.y = y;
			
			this.dx = 0.1f + (float)(rnd()*0.1f) * (rnd() < 0.5 ? 1 : -1);
			this.dy = 0.1f + (float)(rnd()*0.1f) * (rnd() < 0.5 ? 1 : -1);
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
			
			if (x > SpriteTest.SCREEN_WIDTH - 42) {
				x = SpriteTest.SCREEN_WIDTH - 42;
				dx = -dx;
			}
			
			if (y < 0) {
				y = 0;
				dy = -dy;
			}
			
			if (y > SpriteTest.SCREEN_HEIGHT - 42) {
				y = SpriteTest.SCREEN_HEIGHT - 42;
				dy = -dy;
			}
		}
	}

}
