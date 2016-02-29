package whs.botdriver.subsystems;

import whs.botdriver.Robot;
import whs.botdriver.Subsystem;

public abstract class SkidSteerDriveSystem extends Subsystem {
	public SkidSteerDriveSystem(Robot robot, int id, String name, String driver) {
		super(robot, id, name, driver);
	}

	public abstract void setPower(double l, double r);
}
