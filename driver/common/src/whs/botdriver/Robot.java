package whs.botdriver;

import whs.botdriver.events.Event;

import java.util.concurrent.BlockingQueue;

public interface Robot {

  public void querySubsystems(); //send a request to access subsystems

  public void registerDriver(String name);

  void dispose();

  void kill(Throwable t);

  public boolean checkStatus() throws RobotKilledException; //returns true while the robot is still alive

  public BlockingQueue<Event> getEventQueue();

  public void bindSubsystem(Subsystem subsystem);    //send a request to bind subsystem

  public void unbindSubsystem(Subsystem subsystem); //send a request to unbind subsystem

  public void pushEvent(Event evt);
}
