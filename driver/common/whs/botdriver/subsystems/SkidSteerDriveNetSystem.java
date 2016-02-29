package whs.botdriver.subsystems;

import java.nio.ByteBuffer;

import whs.botdriver.NetRobot;

public class SkidSteerDriveNetSystem extends SkidSteerDriveSystem {
	private NetRobot netbot;
	private ByteBuffer outBuffer;
	
	public SkidSteerDriveNetSystem(NetRobot robot, int id, String name, String driver) {
		super(robot, id, name, driver);
		this.netbot = robot;
		this.outBuffer = ByteBuffer.allocate(4);
	}

	@Override
	public void setPower(double l, double r) {
		synchronized(netbot) {
			short sL = (short) (l*2048);
			short sR = (short) (r*2048);
			if(sL < -2048) { sL = -2048; }
			if(sL >  2048) { sL =  2048; }
			if(sR < -2048) { sR = -2048; }
			if(sR >  2048) { sR =  2048; }
			outBuffer.putShort(sL);
			outBuffer.putShort(sR);
			netbot.sendSubsystemPacket(this, outBuffer);
		}
	}
}
