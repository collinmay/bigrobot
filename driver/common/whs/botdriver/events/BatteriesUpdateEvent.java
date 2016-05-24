package whs.botdriver.events;

import whs.botdriver.Battery;

public class BatteriesUpdateEvent extends Event {
	
	private Battery[] batteries;
	
	public BatteriesUpdateEvent(Battery[] batteries) {
		this.batteries = batteries;
	}

	public Battery[] getBatteries() {
		return batteries;
	}
}
