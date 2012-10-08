package slimold;

public class SlimException extends Exception {

	private static final long serialVersionUID = -6054065177556973123L;

	public SlimException(Throwable t) {
		super(t);
	}
	
	public SlimException(String msg) {
		super(msg);
	}
	
	public SlimException(String msg, Throwable t) {
		super(msg, t);
	}
}
