package slim.audio;

import java.io.IOException;
import java.net.URL;

import javax.sound.sampled.AudioFormat;

import paulscode.sound.ICodec;
import paulscode.sound.SoundBuffer;

public class PaulLambCodec extends Codec2 {
	
	ICodec codec;
	
	public PaulLambCodec(URL url, Class<? extends ICodec> codecClass) throws IOException {
		super(url);
		try {
			codec = codecClass.newInstance();
		} catch (Exception e) {
			close();
			throw new IOException("could not find codec "+codecClass.getName());
		}
		if (!codec.initialize(url)) {
			codec.cleanup();
			close();
			throw new IOException("codec initialization failed for "+url.getPath());
		}
		AudioFormat fmt = codec.getAudioFormat();
		SoundBuffer buf = codec.readAll();
		if (buf==null) {
			codec.cleanup();
			close();
			throw new IOException()
		}
	}
	

	@Override
	public int read(byte[] chunk) throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}
}
