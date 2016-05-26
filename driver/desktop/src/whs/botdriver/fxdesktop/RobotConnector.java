package whs.botdriver.fxdesktop;

import javafx.application.Platform;
import javafx.scene.control.Label;
import whs.botdriver.Robot;
import whs.botdriver.RobotStub;

import java.io.IOException;

/**
 * Created by misson20000 on 5/23/16.
 */
public class RobotConnector {
  private final Label statusLabel;
  private RobotStub currentStub;
  private RobotStub targetStub;
  private Robot robot;
  private Thread thread;

  public RobotConnector(final Label statusLabel, final ConnectionListener connListener, final DisconnectionListener disconnListener) {
    this.statusLabel = statusLabel;
    this.currentStub = null;
    this.targetStub = null;
    this.robot = null;

    this.thread = new Thread(() -> {
      while(true) {
        RobotStub targetStub;
        synchronized(this) {
          targetStub = this.targetStub;
        }
        while(currentStub != targetStub) {
          if(targetStub == null) {
            setText("Disconnecting...");
            if(this.robot != null) {
              robot.dispose();
            }
            disconnListener.disconnected();
            setText("Disconnected.");
            synchronized(this) {
              currentStub = null;
              robot = null;
            }
          } else {
            if(currentStub != null) {
              setText("Disconnecting..");
              robot.dispose();
              synchronized(this) {
                currentStub = null;
                robot = null;
              }
              disconnListener.disconnected();
            }
            setText("Connecting...");
            try {
              robot = targetStub.connect();
              currentStub = targetStub;
              connListener.connected(robot);
              setText("Connected.");
            } catch(IOException e) {
              synchronized(this) {
                e.printStackTrace();
                robot = null;
                targetStub = null;
                currentStub = null;
                setText("Could not connect: " + e.getMessage());
              }
            }
          }
        }
        synchronized(this) {
          try {
            this.wait();
          } catch(InterruptedException e) {
          }
        }
      }
    });
    this.thread.setName("RobotConnector thread");
    this.thread.setDaemon(true);
    this.thread.start();
  }

  private void setText(String txt) {
    Platform.runLater(() -> statusLabel.setText(txt));
  }

  public synchronized void connectTo(RobotStub robotStub) {
    targetStub = robotStub;
    if(robot != null) {
      robot.dispose(); //cancel socket
      thread.interrupt();
    }
    this.notifyAll();
  }

  public interface ConnectionListener {
    void connected(Robot r);
  }

  public interface DisconnectionListener {
    void disconnected();
  }
}
