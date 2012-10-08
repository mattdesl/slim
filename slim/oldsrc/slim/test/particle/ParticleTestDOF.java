package slim.test.particle;

import java.net.URL;
import java.util.ArrayList;

import javax.swing.JFrame;

import slim.g2d.Image;
import slim.test.bare.GUITestBase;
import slim.util2.Utils2;
import slimold.SlimException;
import slimold.SpriteBatch;

public class ParticleTestDOF extends GUITestBase {
	public static void main(String[] args) throws SlimException {
		System.out.println(new JFrame().getContentPane().getClass());
		new ParticleTestDOF().start();
	}
	
	class ZEntity {
		float x, y;
		float z;
		Image image;

		float size; //TODO: scale by size
		
		float dx, dy;
		
		public ZEntity(Image image, float x, float y, float z) {
			this.image = image;
			this.x = x;
			this.y = y;
			this.z = z;
		}
		
		void draw(SpriteBatch batch) {
			batch.drawImageScaled(image, x, y, 1f);
		}
		
	}
	
	

	Image image;
	ArrayList<ZEntity> entities = new ArrayList<ZEntity>();
	SpriteBatch batch;
	
	@Override
	public void init() throws SlimException {
		init2D();
		image = new Image("res/particle.png");
		
		
		
		
		batch = new SpriteBatch();
		
		for (int i=0; i<100; i++) {
			entities.add(new ZEntity(image, Utils2.rnd()*800, Utils2.rnd()*600, Utils2.rnd(1f, 0f)));
		}
	}

	@Override
	public void render() throws SlimException {
		for (ZEntity e : entities) {
			//batch.setSpriteAttrib(10, 
		}
	}
	
	@Override
	public void update(int delta) throws SlimException {
		
	}

	@Override
	public URL getThemeURL() throws SlimException {
		return Utils2.getResource("res/gui/chutzpah/chutzpah.xml");
	}
}
