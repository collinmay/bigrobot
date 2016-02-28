package whs.botdriver.desktop.controllers;

import net.java.games.input.Component.Identifier;
import net.java.games.input.Event;

public class DualAxisJoystick extends AbstractJoystick {
	private float x;
	private float y;
	private Identifier xId;
	private Identifier yId;
	private final String name;
	
	public DualAxisJoystick(String name, Identifier xId, Identifier yId) {
		this.xId = xId;
		this.yId = yId;
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
		Identifier id = e.getComponent().getIdentifier();
		if(id == xId) {
			this.x = e.getComponent().getPollData();
			triggerEvent();
			return true;
		}
		if(id == yId) {
			this.y = e.getComponent().getPollData();
			triggerEvent();
			return true;
		}
		return false;
	}
}
