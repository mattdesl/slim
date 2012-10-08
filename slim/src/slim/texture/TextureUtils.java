package slim.texture;

import java.nio.ByteBuffer;

import org.lwjgl.BufferUtils;

public class TextureUtils {

	//TextureUtils.getSubSection(buffer, Texture.Format.RGB, 0, 0, 32, 32, dest);
	
}

//
//if (pixelData == null) {
//	pixelData = texture.getTextureData();
//}
//
//int xo = (int) (textureOffsetX * texture.getTextureWidth());
//int yo = (int) (textureOffsetY * texture.getTextureHeight());
//
//if (textureWidth < 0) {
//	x = xo - x;
//} else {
//	x = xo + x;
//} 
//
//if (textureHeight < 0) {
//	y = yo - y;
//} else {
//	y = yo + y;
//}
//
//int offset = x + (y * texture.getTextureWidth());
//offset *= texture.hasAlpha() ? 4 : 3;
//
//if (texture.hasAlpha()) {
//	return new Color(translate(pixelData[offset]),translate(pixelData[offset+1]),
//			translate(pixelData[offset+2]),translate(pixelData[offset+3]));
//} else {
//	return new Color(translate(pixelData[offset]),translate(pixelData[offset+1]),
//			translate(pixelData[offset+2]));
//}
//}