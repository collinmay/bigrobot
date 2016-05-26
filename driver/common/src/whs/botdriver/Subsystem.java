package whs.botdriver;

import whs.botdriver.events.SubsystemEvent;

/**
 * Created by misson20000 on 5/23/16.
 */
public interface Subsystem {
  int getId();

  String getName();

  String getDriver();

  void attemptBind();

  void unbind();

  void pushEvent(SubsystemEvent event);

  boolean hasBinding();

  boolean isBound();
}
