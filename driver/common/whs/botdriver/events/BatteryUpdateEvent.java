package whs.botdriver.events;

import whs.botdriver.Battery;

public class BatteryUpdateEvent extends Event {
	public int id;
	public Battery battery;
	
	public BatteryUpdateEvent(int id, Battery bat) {
		this.battery = bat;
		this.id = id;
	}
}
