package whs.botdriver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;

public class RobotStub {
	private InetAddress addr;
	private String name;
	
	public RobotStub(DatagramPacket packet) {
		this(packet.getAddress(), new String(packet.getData()).substring(0, packet.getLength()));
	}
	
	public RobotStub(InetAddress addr, String name) {
		this.addr = addr;
		this.name = name;
	}
	
	public InetAddress getAddr() {
		return this.addr;
	}
	
	public String getName() {
		return this.name;
	}
	
	@Override
	public String toString() {
		return this.name + " (" + this.addr.getHostAddress() + ")";
	}
	
	public NetRobot connect() throws IOException {
		return new NetRobot(this);
	}
}
