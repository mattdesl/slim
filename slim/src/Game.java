import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

public class Game extends javax.swing.JFrame {

    private static final long serialVersionUID = 1L;
    /* difference between time of update and world step time */
    float localTime = 0f;
    
    BufferedImage sprite;
    private float x = 50, y = 250;
    private final float SPEED = 255f;
    
    private boolean isUp, isLeft, isDown, isRight;
    
    /** Creates new form Game */
    public Game() {
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(Color.black);
        setSize(800, 600);
        setLocationRelativeTo(null);
    }

    /**
     * init Game (override/replace)
     */
    protected void init() {
    	try {
    		URL url = Thread.currentThread().getContextClassLoader().getResource("res/ship.png");
    		sprite = ImageIO.read(url);
    	} catch (IOException e) {
    		e.printStackTrace(); 
    	}
    	
        
        this.addKeyListener(new KeyListener() {

			public void keyPressed(KeyEvent e) {
				switch (e.getKeyCode()) {
				case KeyEvent.VK_W: isUp = true; break;
				case KeyEvent.VK_A: isLeft = true; break;
				case KeyEvent.VK_D: isRight = true; break;
				case KeyEvent.VK_S: isDown = true; break;
				}
			}

			public void keyReleased(KeyEvent e) {
				switch (e.getKeyCode()) {
				case KeyEvent.VK_W: isUp = false; break;
				case KeyEvent.VK_A: isLeft = false; break;
				case KeyEvent.VK_D: isRight = false; break;
				case KeyEvent.VK_S: isDown = false; break;
				}
			}

			public void keyTyped(KeyEvent e) {
			}
        	
        });
    }
    
    /**
     * update game. elapsedTime is fixed.  (override/replace)
     * @param elapsedTime 
     */
    protected void update(float elapsedTime) {
        if (isUp)
        	y -= SPEED * elapsedTime;
        if (isDown)
        	y += SPEED * elapsedTime;
        if (isLeft)
        	x -= SPEED * elapsedTime;
        if (isRight)
        	x += SPEED * elapsedTime;
    }
    
    /**
     * render the game  (override/replace)
     * @param g
     * @param interpolationTime time of the rendering within a fixed timestep (in seconds)
     */
    protected void render(Graphics2D g, float interpolationTime) {
        g.clearRect(0, 0, getWidth(), getHeight());
        g.drawImage(sprite, (int)x, (int)y, null);
    }
    

    /**
     * Starts the game loop in a new Thread.
     * @param fixedTimeStep
     * @param maxSubSteps maximum steps that should be processed to catch up with real time.
     */
    public final void start(final float fixedTimeStep, final int maxSubSteps) {
        this.createBufferStrategy(2);
        init();
        new Thread() {

            {
                setDaemon(true);
            }

            @Override
            public void run() {
                long start = System.nanoTime();
                while (true) {
                    long now = System.nanoTime();
                    float elapsed = (now - start) / 1000000000f;
                    start = now;
                    internalUpdateWithFixedTimeStep(elapsed, maxSubSteps, fixedTimeStep);
                    internalUpdateGraphicsInterpolated();
                    if (1000000000 * fixedTimeStep - (System.nanoTime() - start) > 1000000) {
                        try {
                            Thread.sleep(0, 999999);
                        } catch (InterruptedException ex) {
                        }
                    }
                }
            }
        }.start();
    }

    /**
     * Updates game state if possible and sets localTime for interpolation.
     * @param elapsedSeconds
     * @param maxSubSteps
     * @param fixedTimeStep 
     */
    private void internalUpdateWithFixedTimeStep(float elapsedSeconds, int maxSubSteps, float fixedTimeStep) {
        int numSubSteps = 0;
        if (maxSubSteps != 0) {
            // fixed timestep with interpolation
            localTime += elapsedSeconds;
            if (localTime >= fixedTimeStep) {
                numSubSteps = (int) (localTime / fixedTimeStep);
                localTime -= numSubSteps * fixedTimeStep;
            }
        }
        if (numSubSteps != 0) {
            // clamp the number of substeps, to prevent simulation grinding spiralling down to a halt
            int clampedSubSteps = (numSubSteps > maxSubSteps) ? maxSubSteps : numSubSteps;
            for (int i = 0; i < clampedSubSteps; i++) {
                update(fixedTimeStep);
            }
        }
    }

    /**
     * Calls render with Graphics2D context and takes care of double buffering.
     */
    private void internalUpdateGraphicsInterpolated() {
        BufferStrategy bf = this.getBufferStrategy();
        Graphics2D g = null;
        try {
            g = (Graphics2D) bf.getDrawGraphics();
            render(g, localTime);
        } finally {
            g.dispose();
        }
        // Shows the contents of the backbuffer on the screen.
        bf.show();
        //Tell the System to do the Drawing now, otherwise it can take a few extra ms until 
        //Drawing is done which looks very jerky
        Toolkit.getDefaultToolkit().sync();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Create and display the form */
        new Thread() {

            {
                setDaemon(true);
                start();
            }

            public void run() {
                while (true) {
                    try {
                        Thread.sleep(Integer.MAX_VALUE);
                    } catch (Throwable t) {
                    }
                }
            }
        };
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                Game game = new Game();
                game.setVisible(true);
                game.start(1 / 60f, 5);
            }
        });
    }
}