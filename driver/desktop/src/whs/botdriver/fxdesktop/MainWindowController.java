package whs.botdriver.fxdesktop;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;
import whs.botdriver.LogEvent;
import whs.botdriver.Robot;
import whs.botdriver.RobotStub;
import whs.botdriver.Subsystem;
import whs.botdriver.events.Event;
import whs.botdriver.events.SubsystemEvent;
import whs.botdriver.events.SubsystemUpdateEvent;
import whs.botdriver.fxdesktop.dialog.TcpConnectDialog;
import whs.botdriver.fxdesktop.subsystemview.SubsystemPane;

import java.net.UnknownHostException;

/**
 * Created by misson20000 on 5/23/16.
 */
public class MainWindowController implements RobotConnector.ConnectionListener, RobotConnector.DisconnectionListener {
  @FXML
  public Menu robotMenu;

  @FXML
  public Label status;

  @FXML
  public VBox mainContainer;

  @FXML
  public ListView<String> console;

  @FXML
  public VBox sensorPanel;

  private RobotConnector robotConnector;
  private Robot robot;

  private Thread eventThread;

  private SubsystemPane subsystemPanes[];

  private TcpConnectDialog tcpConnectDialog = new TcpConnectDialog();


  @FXML private MenuItem firmwareVersionMenuItem;
  @FXML private MenuItem upgradeFirmwareMenuItem;

  public void initialize(Parent root) {
    this.robotConnector = new RobotConnector(status, this, this);
    this.eventThread = new Thread(() -> {
      while(true) {
        Robot r = robot;
        if(r != null) {
          try {
            Event evt = r.getEventQueue().take();
            if(evt instanceof SubsystemUpdateEvent) {
              SubsystemUpdateEvent sue = (SubsystemUpdateEvent) evt;
              Platform.runLater(() -> {
                System.out.println("got subsystem update event");
                Subsystem[] subsystems = sue.getSubsystems();
                subsystemPanes = new SubsystemPane[subsystems.length];
                for(int i = 0; i < subsystems.length; i++) {
                  subsystemPanes[i] = SubsystemPane.createView(subsystems[i]);
                }

                mainContainer.getChildren().clear();
                mainContainer.getChildren().addAll(subsystemPanes);
              });
            }
            if(evt instanceof SubsystemEvent) {
              SubsystemEvent se = (SubsystemEvent) evt;
              subsystemPanes[se.getSubsystem().getId()].handleEvent(se);
            }
            if(evt instanceof LogEvent) {
              Platform.runLater(() -> console.getItems().add(((LogEvent) evt).getMessage()));
            }
          } catch(InterruptedException e) {
          }
        } else {
          synchronized(this) {
            try {
              wait();
            } catch(InterruptedException e) {
            }
          }
        }
      }
    });
    this.eventThread.setName("MainWindowController event thread");
    this.eventThread.setDaemon(true);
    this.eventThread.start();
  }

  @FXML
  public void directTcpConnect(ActionEvent e) {
    tcpConnectDialog.showAndWait();
    if(!tcpConnectDialog.isCancelled()) {
      try {
        robotConnector.connectTo(new RobotStub(tcpConnectDialog.getAddress(), tcpConnectDialog.getPort(), "direct connect bot"));
      } catch(UnknownHostException e1) {
        e1.printStackTrace();
      }
    }
  }

  @FXML
  public void showFirmwareVersion(ActionEvent e) {

  }

  @FXML
  public void upgradeFirmware(ActionEvent e) {

  }

  @Override
  public synchronized void connected(Robot r) {
    this.robot = r;
    this.notifyAll();
    robot.querySubsystems();
    robot.registerDriver(System.getProperty("user.name"));
  }

  @Override
  public synchronized void disconnected() {
    this.robot = null;
  }
}
