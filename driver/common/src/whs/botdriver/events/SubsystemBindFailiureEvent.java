package whs.botdriver.events;

import whs.botdriver.Subsystem;

public class SubsystemBindFailiureEvent extends SubsystemEvent {
  public SubsystemBindFailiureEvent(Subsystem sub) {
    super(sub);
  }
}
