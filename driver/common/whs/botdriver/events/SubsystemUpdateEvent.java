package whs.botdriver.events;

import whs.botdriver.Subsystem;

public class SubsystemUpdateEvent extends Event {

	private Subsystem[] subsystems;
	
	public SubsystemUpdateEvent(Subsystem[] subsystems) {
		this.subsystems = subsystems;
	}

	public Subsystem[] getSubsystems() {
		return subsystems;
	}
}
