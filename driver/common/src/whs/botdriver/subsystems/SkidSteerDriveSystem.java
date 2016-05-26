package whs.botdriver.subsystems;

import whs.botdriver.AbstractSubsystem;
import whs.botdriver.Robot;

public abstract class SkidSteerDriveSystem extends AbstractSubsystem {
  public SkidSteerDriveSystem(Robot robot, int id, String name, String driver) {
    super(robot, id, name, driver);
  }

  public abstract void setPower(double l, double r);
}
