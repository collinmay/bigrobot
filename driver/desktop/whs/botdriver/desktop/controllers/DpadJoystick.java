package whs.botdriver.desktop.controllers;

import net.java.games.input.Component.Identifier;
import net.java.games.input.Component.POV;
import net.java.games.input.Event;

public class DpadJoystick extends AbstractJoystick {

	private Identifier povId;
	private float x;
	private float y;
	private final String name;
	
	public DpadJoystick(String name, Identifier povId) {
		this.povId = povId;
		this.name = name;
	}
	
	@Override
	public synchronized float getX() {
		return x;
	}

	@Override
	public synchronized float getY() {
		return y;
	}

	@Override
	public String getName() {
		return name;
	}
	
	public synchronized boolean handleEvent(Event e) {
		if(e.getComponent().getIdentifier() == povId) {
			float pov = e.getComponent().getPollData();
			if(pov == POV.CENTER) {	x = 0; y = 0; }
			if(pov == POV.DOWN) { x = 0; y = 1; }
			if(pov == POV.DOWN_LEFT) { x = -1; y = 1; }
			if(pov == POV.DOWN_RIGHT) { x = 1; y = 1; }
			if(pov == POV.LEFT) { x = -1; y = 0; }
			if(pov == POV.RIGHT) { x = 1; y = 0; }
			if(pov == POV.UP) { x = 0; y = -1; }
			if(pov == POV.UP_LEFT) { x = -1; y = -1; }
			if(pov == POV.UP_RIGHT) { x = 1; y = -1; }
			triggerEvent();
			return true;
		}
		return false;
	}
}
