package whs.botdriver;

public class Battery {
  private int charge;
  private int full;
  private int id;
  private String name;

  public Battery(int id, int full, int now, String name) {
    this.id = id;
    this.full = full;
    this.charge = now;
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public int getFullCharge() {
    return full;
  }

  public int getCurrentCharge() {
    return charge;
  }

  public boolean setCharge(int charge) {
    int prev = this.charge;
    this.charge = charge;
    return prev != charge;
  }

  @Override
  public int hashCode() {
    return id;
  }
}
