package whs.botdriver.desktop.controllers;

public interface Joystick {
	public float getX();
	public float getY();
	public String getName();
	public void addEventListener(EventListener evt);
	
	public interface EventListener {
		public boolean joystickMoved(Joystick j, float x, float y); // returns false if this listener should be removed
	}
}
