package slim.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import slim.Image2D;
import slim.SlimException;

/**
 * A small SpriteSheet utility based on Properties files. Format:
 * <pre>
 *     #my sprite sheet... 
 *     #4 comma-separated ints per key; white space ignored
 *     char1 = 0, 0, 32, 32
 *     char2 = 32, 32, 32, 32
 *     hud = 50, 55, 100, 150
 * </pre>
 * 
 * @author davedes
 */
public class SimpleSpriteSheet {
	
	private HashMap<String, Image2D> sprites = new HashMap<String, Image2D>();
	private Image2D sheet;
	
	public SimpleSpriteSheet(URL sheetDef, Image2D sheetImage) throws SlimException {
		this.sheet = sheetImage;
		Properties p;
		try {
			InputStream in = sheetDef.openStream();
			p = new Properties();
			p.load(in);
			in.close();
		} catch (IOException e) {
			throw new SlimException("error loading sprite sheet "+e.getMessage(), e);
		}
		sprites = new HashMap<String, Image2D>();
		for (Map.Entry<Object, Object> e : p.entrySet()) {
			String k = String.valueOf(e.getKey());
			Object v = e.getValue();
			String[] s = v!=null ? v.toString().trim().split(",") : null; 
			sprites.put(k, toimg(k, s));
		}
	}
	
	private Image2D toimg(String key, String[] s) throws SlimException {
		if (s==null || s.length<4)
			throw new SlimException("invalid value for key "+key);
		try { 
			int x = Integer.parseInt(s[0].trim());
			int y = Integer.parseInt(s[1].trim());
			int w = Integer.parseInt(s[2].trim());
			int h = Integer.parseInt(s[3].trim());
			return sheet.getSubImage(x, y, w+1, h+1);
		} catch (NumberFormatException e) {
			throw new SlimException("invalid int values for key "+key+" "+Arrays.toString(s));
		}
	}
	
	public int size() {
		return sprites.size();
	}

	public Image2D getSprite(String key) {
		return sprites.get(key);
	}
	
	public HashMap<String, Image2D> sprites() {
		return sprites;
	}
}
