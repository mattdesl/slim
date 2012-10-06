package slim.audio;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class CodecWav2 extends Codec2 {
	
	private int dataRemaining;
	
	public CodecWav2(InputStream in) throws IOException {
		super(in);
		this.bitDepth = 16; //only 16 bit audio is supported for now
		
		if (!found('R', 'I', 'F', 'F'))
			throw new IOException("RIFF header not found");
		
		skipFully(4); //skip chunkSize
		
		if (!found('W', 'A', 'V', 'E'))
			throw new IOException("WAVE header not found");
		
		//seek to "fmt " chunk
		int fmtChunkLength = seekToChunk('f', 'm', 't', ' ');
		
		//read format
		int type = read() & 0xff | (read() & 0xff) << 8;
		if (type != 1) //WAVE_FORMAT_PCM = 0x0001
			throw new IOException("WAV files must be in PCM format: "+ type);
		
		//read channels
		channels = read() & 0xff | (read() & 0xff) << 8;
		if (channels != 1 && channels != 2)
			throw new IOException(
					"WAV files must have 1 or 2 channels: " + channels);
		
		//read sample rate
		sampleRate = read() & 0xff | (read() & 0xff) << 8
				| (read() & 0xff) << 16 | (read() & 0xff) << 24;
		
		//skip average bytes per sec and block align
		skipFully(6);
		
		//read bits per sample
		int bitsPerSample = read() & 0xff | (read() & 0xff) << 8;
		if (bitsPerSample != 16)
			throw new IOException(
					"WAV files must have 16 bits per sample: "
							+ bitsPerSample);

		skipFully(fmtChunkLength - 16);
		
		//amount of data we need to read
		dataRemaining = seekToChunk('d', 'a', 't', 'a');
	}
	
	
	
	/**
	 * Reads a chunk of PCM data into the given byte array.
	 * @param chunk the destination
	 * @return the remaining data
	 * @throws IOException if there was a problem reading the data
	 */
	public int read(byte[] chunk) throws IOException {
		if (dataRemaining == 0)
			return -1;
		int length = Math.min(in.read(chunk), dataRemaining);
		if (length == -1)
			return -1;
		dataRemaining -= length;
		return length;
	}
	
	

	private boolean found(char c1, char c2, char c3, char c4) throws IOException {
		boolean found = read() == c1;
		found &= read() == c2;
		found &= read() == c3;
		found &= read() == c4;
		return found;
	}
	
	private int seekToChunk(char c1, char c2, char c3, char c4)
			throws IOException {
		while (true) {
			boolean found = found(c1, c2, c3, c4);
			int chunkLength = read() & 0xff | (read() & 0xff) << 8
					| (read() & 0xff) << 16 | (read() & 0xff) << 24;
			if (chunkLength == -1)
				throw new IOException("Chunk not found: " + c1 + c2 + c3
						+ c4);
			if (found)
				return chunkLength;
			skipFully(chunkLength);
		}
	}

}


