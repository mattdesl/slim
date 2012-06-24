package slim.audio;

public class AudioData {
	private byte[] buffer;
	private int channels;
	private int sampleRate;
	
	public AudioData(byte[] buffer, int channels, int sampleRate) {
		this.buffer = buffer;
		this.channels = channels;
		this.sampleRate = sampleRate;
	}
}
