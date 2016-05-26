package whs.botdriver;

import whs.botdriver.events.SubsystemBindFailiureEvent;
import whs.botdriver.events.SubsystemBindSuccessEvent;
import whs.botdriver.events.SubsystemEvent;

public abstract class AbstractSubsystem implements Subsystem {
  private int id;
  private String name;
  private String driver;
  private Robot robot;
  private boolean hasBinding;
  private boolean bound;

  protected AbstractSubsystem(Robot robot, int id, String name, String driver) {
    this.id = id;
    this.name = name;
    this.driver = driver;
    this.robot = robot;
    this.hasBinding = false;
    this.bound = driver.length() > 0;
  }

  @Override
  public int getId() {
    return id;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getDriver() {
    return driver;
  }

  @Override
  public void attemptBind() {
    this.robot.bindSubsystem(this);
  }

  @Override
  public void unbind() {
    this.robot.unbindSubsystem(this);
    this.bound = false;
    this.hasBinding = false;
  }

  @Override
  public boolean hasBinding() {
    return hasBinding;
  }

  @Override
  public boolean isBound() {
    return bound;
  }

  @Override
  public synchronized void pushEvent(SubsystemEvent evt) {
    this.notifyAll();
    if(evt instanceof SubsystemBindFailiureEvent) {
      hasBinding = false;
    }
    if(evt instanceof SubsystemBindSuccessEvent) {
      hasBinding = true;
      bound = true;
    }
    this.robot.pushEvent(evt);
  }

  @Override
  public int hashCode() {
    return id;
  }
}
