package slim.test.bare;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.lwjgl.opengl.GLContext;

public class Java2DTest extends JFrame implements ActionListener {
	private GamePanel gamePanel = new GamePanel();
	private JButton startButton = new JButton("Start");
	private JButton quitButton = new JButton("Quit");
	private boolean running = false;
	private int fps = 60;
	private int frameCount = 0;

	int numberOfBalls = 500;
	Ball[] balls = new Ball[100000];
	int collisionDelay = 30, collisionTIme;
	static final int SCREEN_WIDTH = 800, SCREEN_HEIGHT = 600;
	private BufferedImage img;
	
	public Java2DTest() {
		super("Fixed Timestep Game Loop Test");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		Container cp = getContentPane();
		cp.setLayout(new BorderLayout());
		JPanel p = new JPanel();
		p.setLayout(new GridLayout(1, 2));
		p.add(startButton);
		p.add(quitButton);
		cp.add(gamePanel, BorderLayout.CENTER);
		cp.add(p, BorderLayout.SOUTH);
		setSize(500, 500);

		startButton.addActionListener(this);
		quitButton.addActionListener(this);
		

		// initilise balls
		for (int i = 0; i < balls.length; i++) {
			balls[i] = new Ball((int)(rnd() * SCREEN_WIDTH), (int)(rnd() * SCREEN_HEIGHT));
		}
	}

	public static void main(String[] args) {
		
		
		//Java2DTest glt = new Java2DTest();
		//glt.setVisible(true);
	}

	public void actionPerformed(ActionEvent e) {
		Object s = e.getSource();
		if (s == startButton) {
			running = !running;
			if (running) {
				startButton.setText("Stop");
				runGameLoop();
			} else {
				startButton.setText("Start");
			}
		} else if (s == quitButton) {
			System.exit(0);
		}
	}

	// Starts a new thread and runs the game loop in it.
	public void runGameLoop() {
		Thread loop = new Thread() {
			public void run() {
				gameLoop();
			}
		};
		loop.start();
	}

	// Only run this in another Thread!
	private void gameLoop() {
		// This value would probably be stored elsewhere.
		final double GAME_HERTZ = 30.0;
		// Calculate how many ns each frame should take for our target game
		// hertz.
		final double TIME_BETWEEN_UPDATES = 1000000000 / GAME_HERTZ;
		// At the very most we will update the game this many times before a new
		// render.
		final int MAX_UPDATES_BEFORE_RENDER = 5;
		// We will need the last update time.
		double lastUpdateTime = System.nanoTime();
		// Store the last time we rendered.
		double lastRenderTime = System.nanoTime();

		// If we are able to get as high as this FPS, don't render again.
		final double TARGET_FPS = 60;
		final double TARGET_TIME_BETWEEN_RENDERS = 1000000000 / TARGET_FPS;

		// Simple way of finding FPS.
		int lastSecondTime = (int) (lastUpdateTime / 1000000000);

		while (running) {
			double now = System.nanoTime();
			int updateCount = 0;

			// Do as many game updates as we need to, potentially playing
			// catchup.
			while (now - lastUpdateTime > TIME_BETWEEN_UPDATES
					&& updateCount < MAX_UPDATES_BEFORE_RENDER) {
				updateGame();
				lastUpdateTime += TIME_BETWEEN_UPDATES;
				updateCount++;
			}

			// If for some reason an update takes forever, we don't want to do
			// an insane number of catchups.
			// If you were doing some sort of game that needed to keep EXACT
			// time, you would get rid of this.
			if (lastUpdateTime - now > TIME_BETWEEN_UPDATES) {
				lastUpdateTime = now - TIME_BETWEEN_UPDATES;
			}

			// Render. To do so, we need to calculate interpolation for a smooth
			// render.
			float interpolation = Math.min(1.0f,
					(float) ((now - lastUpdateTime) / TIME_BETWEEN_UPDATES));
			drawGame(interpolation);
			lastRenderTime = now;

			// Update the frames we got.
			int thisSecond = (int) (lastUpdateTime / 1000000000);
			if (thisSecond > lastSecondTime) {
//				System.out.println("NEW SECOND " + thisSecond + " "
//						+ frameCount);
				fps = frameCount;
				frameCount = 0;
				lastSecondTime = thisSecond;
			}

			// Yield until it has been at least the target time between renders.
			// This saves the CPU from hogging.
			while (now - lastRenderTime < TARGET_TIME_BETWEEN_RENDERS
					&& now - lastUpdateTime < TIME_BETWEEN_UPDATES) {
				Thread.yield();

				// This stops the app from consuming all your CPU. It makes this
				// slightly less accurate, but is worth it.
				// You can remove this line and it will still work (better),
				// your CPU just climbs on certain OSes.
				// FYI on some OS's this can cause pretty bad stuttering. Scroll
				// down and have a look at different peoples' solutions to this.
				try {
					Thread.sleep(1);
				} catch (Exception e) {
				}

				now = System.nanoTime();
			}
		}
	}

	private void updateGame() {
		gamePanel.update();
	}

	private void drawGame(float interpolation) {
		gamePanel.setInterpolation(interpolation);
		gamePanel.repaint();
	}

	private class GamePanel extends JPanel {
		float interpolation;
		float ballX, ballY, lastBallX, lastBallY;
		int ballWidth, ballHeight;
		float ballXVel, ballYVel;
		float ballSpeed;

		int lastDrawX, lastDrawY;

		public GamePanel() {
			ballX = lastBallX = 100;
			ballY = lastBallY = 100;
			ballWidth = 25;
			ballHeight = 25;
			ballSpeed = 25;
			ballXVel = (float) Math.random() * ballSpeed * 2 - ballSpeed;
			ballYVel = (float) Math.random() * ballSpeed * 2 - ballSpeed;
		}

		public void setInterpolation(float interp) {
			interpolation = interp;
		}

		public void update() {
			// update ball movement
			for (int i = 0; i < numberOfBalls; i++) {
				balls[i].update();
			}
		}

		public void paintComponent(Graphics g) {
			g.clearRect(0, 0, 800, 600);
			
			g.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, observer)
			
			g.setColor(Color.black);
			g.drawString("FPS: "+fps+" COUNT: "+numberOfBalls, 5, 10);
			frameCount++;
			// BS way of clearing out the old rectangle to save CPU.
//			g.setColor(getBackground());
//			g.fillRect(lastDrawX - 1, lastDrawY - 1, ballWidth + 2,
//					ballHeight + 2);
//			g.fillRect(5, 0, 75, 30);
//
//			g.setColor(Color.RED);
//			int drawX = (int) ((ballX - lastBallX) * interpolation + lastBallX - ballWidth / 2);
//			int drawY = (int) ((ballY - lastBallY) * interpolation + lastBallY - ballHeight / 2);
//			g.fillOval(drawX, drawY, ballWidth, ballHeight);
//
//			lastDrawX = drawX;
//			lastDrawY = drawY;
//
//			g.setColor(Color.BLACK);
//			g.drawString("FPS: " + fps, 5, 10);
//
//			frameCount++;
		}
	}
	
	
	static Random r = new Random();
	static float rnd() {
		return r.nextFloat();
	}
	
	public static class Ball {
		public float x, y;
		public float dy, dx;
		
		public Ball(int x, int y) {
			this.x = x;
			this.y = y;
			float s = 5f;
			this.dx = s + (float)(rnd()*s) * (rnd() < 0.5 ? 1 : -1);
			this.dy = s + (float)(rnd()*s) * (rnd() < 0.5 ? 1 : -1);
		}
		
		public void update() {
			// ball movement
			x += dx;
			y += dy;
			
			// wall collision
			if (x < 0) {
				x = 0;
				dx = -dx;
			}
			
			if (x > SCREEN_WIDTH - 32) {
				x = SCREEN_WIDTH - 32;
				dx = -dx;
			}
			
			if (y < 0) {
				y = 0;
				dy = -dy;
			}
			
			if (y > SCREEN_HEIGHT - 32) {
				y = SCREEN_HEIGHT - 32;
				dy = -dy;
			}
		}
	}
}