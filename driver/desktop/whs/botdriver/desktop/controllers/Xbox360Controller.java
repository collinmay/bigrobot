package whs.botdriver.desktop.controllers;

import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.Event;
import net.java.games.input.EventQueue;

public class Xbox360Controller extends WController {

	private DualAxisJoystick leftStick;
	private DualAxisJoystick rightStick;
	private DpadJoystick dPad;
	private Joystick[] joysticks;
	
	private Event event = new Event();
	
	public Xbox360Controller(Controller c) {
		super(c);
		
		this.leftStick = new DualAxisJoystick("Left Joystick", Component.Identifier.Axis.X, Component.Identifier.Axis.Y);
		this.rightStick = new DualAxisJoystick("Right Joystick", Component.Identifier.Axis.RX, Component.Identifier.Axis.RY);
		this.dPad = new DpadJoystick("D-Pad", Component.Identifier.Axis.POV);
		this.joysticks = new Joystick[] {leftStick, rightStick, dPad};
	}
	
	@Override
	public synchronized boolean update() {
		if(!this.controller.poll()) { return false; }
		
		EventQueue queue = this.controller.getEventQueue();
		boolean updated = false;
		while(queue.getNextEvent(event)) {
			updated|= leftStick.handleEvent(event);
			updated|= rightStick.handleEvent(event);
			updated|= dPad.handleEvent(event);
		}
		if(updated) {
			super.triggerUpdate();
		}
		
		return true;
	}

	@Override
	public synchronized Joystick[] getJoysticks() {
		return joysticks;
	}
	
	@Override
	public String getName() {
		return "Xbox 360 Controller";
	}
}
