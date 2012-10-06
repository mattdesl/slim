package slim.audio;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public abstract class Codec {
	
	protected int channels = -1;
	protected int sampleRate = -1;
	protected int bitDepth = -1;
	
	protected Codec(InputStream in) throws IOException {
		this.in = in;
	}

	public int getChannels() {
		return channels;
	}
	
	public int getSampleRate() {
		return sampleRate;
	}
	
	public int getBitDepth() {
		return bitDepth;
	}
	
	public abstract int read(byte[] chunk) throws IOException;
	
	public void close() {
		try { in.close(); }
		catch (IOException e) {}
	}
	
	/**
	 * Reads all PCM data and returns a new byte array.
	 * @return
	 */
	public byte[] readAll() throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream(4096);
        byte[] buffer = new byte[BUFFER_SIZE];
		while (true) {
			int length = read(buffer);
			if (length == -1)
				break;
			output.write(buffer, 0, length);
		}
		return output.toByteArray();
	}
	
	protected int read() throws IOException {
		return in.read();
	}
	
	protected void skipFully(int count) throws IOException {
		while (count > 0) {
			long skipped = in.skip(count);
			if (skipped <= 0)
				throw new EOFException("Unable to skip.");
			count -= skipped;
		}
	}
	
	
	
	
	
	
	
}
