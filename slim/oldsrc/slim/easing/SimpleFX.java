package slim.easing;

public class SimpleFX {
	
	private float duration, start, end, value;
	private Easing easing;
	private int time;
	private boolean finished = false;
	
	public SimpleFX(float start, float end, float duration, Easing easing) {
		this.start = this.value = start;
		this.end = end;
		this.duration = duration;
		this.easing = easing;
	}
	
	public void update(int delta) {
		if (!finished) {
			time += delta;
			animate(time);
		}
	}
	
	public boolean finished() {
		return finished;
	}
	
	public void restart() {
		finished = false;
		time = 0;
		value = start;
	}
	
	public void setDuration(float duration) {
		this.duration = duration;
	}
	
	public void setStart(float start) {
		this.start = start;
	}
	
	public void setEnd(float end) {
		this.end = end;
	}
	
	public void setEasing(Easing easing) {
		this.easing = easing;
	}
	
	public Easing getEasing() {
		return easing;
	}
	
	public float getEnd() {
		return end;
	}
	
	public float getStart() {
		return start;
	}
	
	public void flip() {
		float t = getStart();
		setStart(getEnd());
		setEnd(t);
	}
	
	public void setValue(float value) {
		this.value = value;
	}
	
	public float getValue() {
		return value;
	}
	
	protected void animate(int time) {
		float change = end-start;
		value = easing.ease(time, start, change, duration);
		if (time > duration) {
			finished = true;
		}
	}
}
