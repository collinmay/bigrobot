package whs.botdriver.events;

import whs.botdriver.Subsystem;

public class SubsystemEvent extends Event {
  protected Subsystem sub;

  public SubsystemEvent(Subsystem sub) {
    this.sub = sub;
  }

  public Subsystem getSubsystem() {
    return sub;
  }
}
