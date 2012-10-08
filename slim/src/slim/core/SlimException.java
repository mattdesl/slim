package slim.core;

public class SlimException extends Exception {

	public SlimException(String msg) {
		super(msg);
	}
	
	public SlimException(String msg, Throwable t) {
		super(msg, t);
	}
	
	public SlimException(Throwable t) {
		super(t);
	}
}
