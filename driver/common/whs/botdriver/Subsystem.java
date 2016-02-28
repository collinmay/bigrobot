package whs.botdriver;

import java.nio.ByteBuffer;

import whs.botdriver.subsystems.SkidSteerDriveSystem;

public abstract class Subsystem {
	
	private int id;
	private String name;
	private String driver;
	
	protected Subsystem(int id, String name, String driver) {
		this.id = id;
		this.name = name;
		this.driver = driver;
	}
	
	public static Subsystem read(int id, ByteBuffer buffer) {
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
			return new SkidSteerDriveSystem(id, name, driver);
		}
		return null;
	}
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public String getDriver() {
		return driver;
	}
}
