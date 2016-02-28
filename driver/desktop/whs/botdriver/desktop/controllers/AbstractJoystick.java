package whs.botdriver.desktop.controllers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class AbstractJoystick implements Joystick {
	private List<EventListener> listeners = new ArrayList<EventListener>();
	private float lastX = 0;
	private float lastY = 0;
	
	@Override
	public void addEventListener(EventListener evt) {
		listeners.add(evt);
	}

	protected void triggerEvent() {
		float x = getX();
		float y = getY();
		if(x != lastX || y != lastY) {
			for(Iterator<EventListener> i = listeners.iterator(); i.hasNext();) {
				if(!i.next().joystickMoved(this, x, y)) {
					i.remove();
				}
			}
			lastX = x;
			lastY = y;
		}
	}
}
