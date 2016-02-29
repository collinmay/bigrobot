package whs.botdriver;

import whs.botdriver.events.SubsystemEvent;

public abstract class Subsystem {
	private int id;
	private String name;
	private String driver;
	private Robot robot;
	
	protected Subsystem(Robot robot, int id, String name, String driver) {
		this.id = id;
		this.name = name;
		this.driver = driver;
		this.robot = robot;
	}
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public String getDriver() {
		return driver;
	}

	public void attemptBind() {
		this.robot.bindSubsystem(this);
	}

	public void unbind() {
		this.robot.unbindSubsystem(this);
	}
	
	public synchronized void pushEvent(SubsystemEvent evt) {
		this.notifyAll();
		this.robot.pushEvent(evt);
	}
	
	@Override
	public int hashCode() {
		return id;
	}
}
