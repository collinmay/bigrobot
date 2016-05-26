package whs.botdriver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;

public class RobotStub {
  private int port;
  private InetAddress addr;
  private String name;

  public RobotStub(DatagramPacket packet) {
    this(packet.getAddress(), new String(packet.getData()).substring(0, packet.getLength()));
  }

  public RobotStub(InetAddress addr, String name) {
    this(addr, 25600, name);
  }

  public RobotStub(InetAddress addr, int port, String name) {
    this.addr = addr;
    this.port = port;
    this.name = name;
  }

  public InetAddress getAddr() {
    return this.addr;
  }

  public int getPort() {
    return this.port;
  }

  public String getName() {
    return this.name;
  }

  @Override
  public String toString() {
    return this.name + " (" + this.addr.getHostAddress() + ":" + this.getPort() + ")";
  }

  public Robot connect() throws IOException {
    return new NetRobot(this);
  }
}
