package whs.botdriver.events;

import whs.botdriver.Motor;
import whs.botdriver.Subsystem;

public class SkidSteerSystemUpdateEvent extends SubsystemEvent {
	private Motor left;
	private Motor right;
	
	public SkidSteerSystemUpdateEvent(Subsystem sub, Motor left, Motor right) {
		super(sub);
		this.left = left;
		this.right = right;
	}

	public Motor getLeftMotor() { return left; }
	public Motor getRightMotor() { return right; }
}
