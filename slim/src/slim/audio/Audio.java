package slim.audio;

public interface Audio {
	
	public void play();
	public void pause();
	public void stop();
	
	public void setLooping(boolean b);
	public boolean isLooping();
	
	public void setLoopCount();
	public int getLoopCount();
	
	public void setLoopIn(int ms);
	public int getLoopIn();
	public void setLoopOut(int ms);
	public int getLoopOut();
	
	public boolean isStopped();
	public boolean isPlaying();
	
	public void setLocation(float x, float y, float z);
	public float getX();
	public float getY();
	public float getZ();
	
	public void setVolume(float volume);
	public float getVolume();
	
	public void setPitch(float pitch);
	public float getPitch();
	
	public void setRelative();
	public boolean isRelative();
}
