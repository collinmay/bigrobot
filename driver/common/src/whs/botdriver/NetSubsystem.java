package whs.botdriver;

import whs.botdriver.subsystems.SkidSteerDriveNetSystem;

import java.nio.ByteBuffer;

public interface NetSubsystem extends Subsystem {
  static NetSubsystem read(NetRobot robot, int id, ByteBuffer buffer) {
    int type = buffer.get();

    int name_len = buffer.getShort();
    byte[] name_bytes = new byte[name_len];
    buffer.get(name_bytes);
    String name = new String(name_bytes);

    int driver_len = buffer.getShort();
    byte[] driver_bytes = new byte[driver_len];
    buffer.get(driver_bytes);
    String driver = new String(driver_bytes);

    switch(type) {
      case 0:
        return null; //invalid subsystem
      case 1:
        return new SkidSteerDriveNetSystem(robot, id, name, driver);
    }
    return null;
  }

  void netTick();
}
