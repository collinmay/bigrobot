package whs.botdriver.desktop;

import javax.swing.SwingUtilities;

import com.ezware.dialog.task.TaskDialogs;

import whs.botdriver.Robot;
import whs.botdriver.RobotStub;

public class Main {	
	public static void main(String[] args) {
		try {
			RobotStub stub = new RobotSelectionDialog().open();
			if(stub == null) {
				System.exit(0);
			}
			Robot robot = stub.connect();
			robot.querySubsystems();
			robot.registerDriver(System.getProperty("user.name"));
			
			SwingUtilities.invokeAndWait(() -> {
				final DriverWindow window = new DriverWindow(robot);
	
				window.setVisible(true);
				new Thread(() -> {
					window.runEventLoop();
					window.setVisible(false);
				}).start();
			});
		} catch (Throwable t) {
			TaskDialogs.showException(t);
		}
	}
}
