package whs.botdriver;

public class MutableMotor implements Motor {

  public short current;
  public short temperature;
  public short speed;

  @Override
  public double getCurrent() {
    return current;
  }

  @Override
  public double getTemperature() {
    return temperature;
  }

  @Override
  public double getSpeed() {
    return ((double) speed) / 2048.0;
  }

}
