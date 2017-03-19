package whs.botdriver.subsystems;

import whs.botdriver.NetRobot;
import whs.botdriver.NetSubsystem;

import java.nio.ByteBuffer;

public class SkidSteerDriveNetSystem extends SkidSteerDriveSystem implements NetSubsystem {
  private NetRobot netbot;
  private ByteBuffer outBuffer;

  private double inL = 0;
  private double inR = 0;
  private double outL = 0;
  private double outR = 0;

  public SkidSteerDriveNetSystem(NetRobot robot, int id, String name, String driver, ByteBuffer buffer) {
    super(robot, id, name, driver);
    this.netbot = robot;
    this.outBuffer = ByteBuffer.allocate(4);
  }

  @Override
  public synchronized void setPower(double l, double r) {
    this.inL = l;
    this.inR = r;
  }

  @Override
  public synchronized void netTick() {
    if(hasBinding() && (inL != outL || inR != outR)) {
      short sL = (short) (inL * 2048);
      short sR = (short) (inR * 2048);
      if(sL < -2048) {
        sL = -2048;
      }
      if(sL > 2048) {
        sL = 2048;
      }
      if(sR < -2048) {
        sR = -2048;
      }
      if(sR > 2048) {
        sR = 2048;
      }
      outBuffer.putShort(sL);
      outBuffer.putShort(sR);
      netbot.sendSubsystemPacket(this, outBuffer);

      outL = inL;
      outR = inR;
    }
  }
}
