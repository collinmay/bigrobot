package whs.botdriver;

import java.util.Queue;

import whs.botdriver.events.Event;

public interface Robot {

	public void querySubsystems(); //send a request to access subsystems
	public void registerDriver(String name);
	public boolean checkStatus() throws RobotKilledException; //returns true while the robot is still alive
	public Queue<Event> getEventQueue();	
}
