package whs.botdriver.events;

public class PingEvent extends Event {
  private long sent;
  private long received;

  public PingEvent(long sent, long received) {
    this.sent = sent;
    this.received = received;
  }

  public long pingTime() {
    return this.received - this.sent;
  }
}
