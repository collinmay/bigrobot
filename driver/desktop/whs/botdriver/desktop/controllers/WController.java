package whs.botdriver.desktop.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;

public class WController {
	private static Map<Controller, WController> cache = new HashMap<Controller, WController>();
	private static Thread updateThread = new Thread(() -> {
		while(true) {
			updateAll();
			try { Thread.sleep(10); } catch(InterruptedException e) {}
		}
	});
	
	static {
		updateThread.setDaemon(true);
		updateThread.start();
	}
	
	protected Controller controller;
	private List<EventListener> listeners;
	
	public WController(Controller controller) {
		this.controller = controller;
		this.listeners = new ArrayList<EventListener>();
	}

	public Joystick[] getJoysticks() {
		return new Joystick[0];
	}
	
	public String getName() {
		return controller.getName();
	}
	
	public synchronized void addListener(EventListener l) {
		listeners.add(l);
	}
	
	public synchronized void removeListener(EventListener l) {
		listeners.remove(l);
	}
	
	public synchronized static WController[] getControllers() {
		Controller[] controllers = ControllerEnvironment.getDefaultEnvironment().getControllers();
		WController[] wControllers = new WController[controllers.length];
		for(int i = 0; i < controllers.length; i++) {
			if(cache.containsKey(controllers[i])) {
				wControllers[i] = cache.get(controllers[i]);
			} else {
				Controller controller = controllers[i];
				WController wController = null;
				if(controller.getName().equals("Logitech Logitech Dual Action")) {
					wController = new LogitechDualActionController(controllers[i]);
				} else if(controller.getName().equals("Microsoft X-Box 360 pad")) {
					wController = new Xbox360Controller(controllers[i]);
				} else {
					System.out.println("Unrecognized controller: " + controllers[i].getName());
					wController = new WController(controllers[i]);
				}
				
				cache.put(controllers[i], wController);
				wControllers[i] = wController;
			}
		}
		return wControllers;
	}
	
	public boolean update() {
		return true;
	}
	
	public synchronized static void updateAll() {
		cache.forEach((Controller c, WController w) -> {
			w.update();
		});
	}
	
	public interface EventListener {
		// called on controller update thread
		// @returns keep this listener
		public boolean update(WController c);
	}

	protected void triggerUpdate() {
		for(Iterator<EventListener> i = listeners.iterator(); i.hasNext();) {
			if(!i.next().update(this)) {
				i.remove();
			}
		}
	}
}
