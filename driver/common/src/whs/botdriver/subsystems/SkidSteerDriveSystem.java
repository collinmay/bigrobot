package whs.botdriver.subsystems;

import whs.botdriver.AbstractSubsystem;
import whs.botdriver.Motor;
import whs.botdriver.MutableMotor;
import whs.botdriver.Robot;

public abstract class SkidSteerDriveSystem extends AbstractSubsystem {
  public SkidSteerDriveSystem(Robot robot, int id, String name, String driver) {
    super(robot, id, name, driver);
  }

  protected MutableMotor left = new MutableMotor();
  protected MutableMotor right = new MutableMotor();

  public Motor getLeftMotor() {
    return left;
  }

  public Motor getRightMotor() {
    return right;
  }

  public abstract void setPower(double l, double r);
}
