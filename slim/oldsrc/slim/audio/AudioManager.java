package slim.audio;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;

import slim.util2.Utils2;

public class AudioManager {
	
	public static void main(String[] args) throws Exception {
		AL.create();
		
		//set up listener
		FloatBuffer ori = BufferUtils.createFloatBuffer(6);
		ori.put(0).put(0).put(-1).put(0).put(1).put(0).flip();
		
		AL10.alListener3f(AL10.AL_POSITION, 0, 0, 0);
		AL10.alListener3f(AL10.AL_VELOCITY, 0, 0, 0);
		AL10.alListener(AL10.AL_ORIENTATION, ori);    
		
		alCheckError();
		
		//set up channels
		final int MAX_SOURCES = 16;
		ArrayList<Integer> sourceIDs = new ArrayList<Integer>(MAX_SOURCES);
		for (int i=0; i<MAX_SOURCES; i++) {
			int id = AL10.alGenSources();
			//if we've reached the max number of sources...
			if (AL10.alGetError()!=AL10.AL_NO_ERROR)
				break;
			
			//set up source...
			AL10.alSourcef(id, AL10.AL_PITCH, 1f);
			AL10.alSourcef(id, AL10.AL_GAIN, 1f);
			AL10.alSource3f(id, AL10.AL_POSITION, 0f, 0f, 0f);
			AL10.alSource3f(id, AL10.AL_VELOCITY, 0f, 0f, 0f);
			sourceIDs.add(id);
		}
		
		//set up buffer
		int bufferID = AL10.alGenBuffers();
		
		//Codec c = new CodecWav2(Utils.getResourceAsStream("res/engine.wav"));
		//byte[] b = c.readAll();
		//c.close();
		
		AudioInputStream in = getStream(Utils2.getResource("res/kirby.ogg"));
		AudioFormat fmt = in.getFormat();
		byte[] b = readAll(in);
		//System.out.println("Actual size: "+b.length);
		try { in.close(); } catch (IOException ignore) {}
		
		
		ByteBuffer buf = BufferUtils.createByteBuffer(b.length);
		buf.put(b).flip();
		AL10.alBufferData(bufferID, toALFormat(fmt), buf, (int)fmt.getSampleRate());
		
		AL10.alSourcei(sourceIDs.get(0), AL10.AL_BUFFER, bufferID);
		alCheckError();
		
		AL10.alSourcePlay(sourceIDs.get(0));
		Thread.sleep(5000);
		AL.destroy();
	}
	
	static int toALFormat(AudioFormat fmt) throws IOException {
		int ch = fmt.getChannels();
		int bits = fmt.getSampleSizeInBits();
		
		if (ch==2) {
			if (bits==16)
				return AL10.AL_FORMAT_STEREO16;
			else if (bits==8)
				return AL10.AL_FORMAT_STEREO8;
		} else if (ch==1) {
			if (bits==16)
				return AL10.AL_FORMAT_MONO16;
			else if (bits==8)
				return AL10.AL_FORMAT_MONO8;
		}
		throw new IOException("could not convert AudioFormat "+fmt+" to 8/16 bit mono/stereo");
	}
	
	private static byte[] readAll(AudioInputStream in) throws IOException {
		AudioFormat fmt = in.getFormat();
		int bufSize = Math.max(2048, (int)(in.getFrameLength() * fmt.getFrameSize()));
		ByteArrayOutputStream output = new ByteArrayOutputStream(bufSize);
        byte[] buffer = new byte[2048];
		while (true) {
			int length = in.read(buffer);
			if (length == -1)
				break;
			output.write(buffer, 0, length);
		}
		return output.toByteArray();
	}
	

    public final static boolean endsWithIgnoreCase(String str, String end) {
        return str.regionMatches(true, str.length()-
                end.length(), end, 0, end.length());
    }    
	
	private static AudioInputStream getStream(URL url) throws IOException {
		AudioInputStream audioStream = null;
		try {
			audioStream = AudioSystem.getAudioInputStream(url);
			AudioFormat streamFormat = audioStream.getFormat();
			
			int sampleRate = (int)streamFormat.getSampleRate();
			int bits = streamFormat.getSampleSizeInBits();
			if (bits!=8 && bits!=16)
				bits = 16; //assume 16 bit depth
			int ch = streamFormat.getChannels();
			if (ch!=1 && ch!=2)
				ch = 2; //try conversion to stereo
			int frameSize = streamFormat.getFrameSize() * 2;
			if (frameSize < 0)
				frameSize = ch * 2;
			int frameRate = (int)streamFormat.getFrameRate();
			if (frameRate < 0)
				frameRate = sampleRate;
			
			System.out.println("FORMAT DETAILS");
			System.out.println("ENCODING: "+streamFormat.getEncoding());
			System.out.println("SAMPLE RATE: "+streamFormat.getSampleRate());
			System.out.println("CHANNELS: "+streamFormat.getChannels());
			System.out.println("BIT DEPTH: "+streamFormat.getSampleSizeInBits());
			System.out.println("FRAME SIZE: "+streamFormat.getFrameSize());
			System.out.println("FRAME RATE: "+streamFormat.getFrameRate());
			System.out.println("BIG ENDIAN? "+streamFormat.isBigEndian());
			System.out.println();
			System.out.println("to...");
			
			System.out.println("FORMAT DETAILS");
			System.out.println("ENCODING: PCM");
			System.out.println("SAMPLE RATE: "+sampleRate);
			System.out.println("CHANNELS: "+ch);
			System.out.println("BIT DEPTH: "+bits);
			System.out.println("FRAME SIZE: "+frameSize);
			System.out.println("FRAME RATE: "+frameRate);

			
			AudioFormat decodedFormat = new AudioFormat(
	                AudioFormat.Encoding.PCM_SIGNED,
	                sampleRate,
	                bits,
	                ch,
	                frameSize,
	                frameRate,
	                false);
			
//			System.out.println(AudioSystem.isConversionSupported(AudioFormat.Encoding.PCM_SIGNED, streamFormat));
//			
	        //try decoding to the above format...
	        audioStream = AudioSystem.getAudioInputStream(decodedFormat, audioStream);
	        streamFormat = audioStream.getFormat();
	        return audioStream;
		}
		catch (Throwable t) {
			throw new IOException("unsupported audio file "+url.getPath(), t);
		}
	}
	
	
	static void alCheckError() {
		int e = AL10.alGetError();
		if (e!=AL10.AL_NO_ERROR)
			throw new RuntimeException("al error "+e);
	}
	
	
	/**
	 * Plays the audio sources together in sync, if supported
	 * in hardware.
	 * @param audios
	 */
	public static void play(Audio ... audios) {
		
	}
}
