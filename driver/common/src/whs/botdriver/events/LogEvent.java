package whs.botdriver;

import whs.botdriver.events.Event;

public class LogEvent extends Event {
  private String msg;

  public LogEvent(String msg) {
    this.msg = msg;
  }

  public String getMessage() {
    return msg;
  }
}
