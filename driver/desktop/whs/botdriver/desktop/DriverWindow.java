package whs.botdriver.desktop;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.lang.reflect.InvocationTargetException;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import whs.botdriver.Robot;
import whs.botdriver.RobotKilledException;
import whs.botdriver.Subsystem;
import whs.botdriver.desktop.subsystemviews.SubsystemView;
import whs.botdriver.desktop.widgets.GraphWidget;
import whs.botdriver.events.Event;
import whs.botdriver.events.PingEvent;
import whs.botdriver.events.SubsystemUpdateEvent;

public class DriverWindow extends JFrame {

	private Robot robot;
	private JTextArea console;
	private GraphWidget pingGraph;
	private JPanel mainArea;
	private long lastGraphUpdate = System.currentTimeMillis();
	
	public DriverWindow(Robot robot) {
		super("Driver Window");
		this.robot = robot;
		
		JPanel content = new JPanel();
		content.setLayout(new BorderLayout());
		this.setLayout(new BorderLayout());
		this.add(content, BorderLayout.CENTER);
		
		JPanel status = new JPanel();
		status.setLayout(new BoxLayout(status, BoxLayout.Y_AXIS));
		status.add(pingGraph = new GraphWidget("Ping (ms)"));
		
		JProgressBar batteryA = new JProgressBar();
		batteryA.setStringPainted(true);
		batteryA.setMinimum(0);
		batteryA.setMaximum(24000);
		batteryA.setString("Main Battery: 21.3v");
		batteryA.setValue(21300);
		status.add(batteryA);
		
		status.setBorder(new EmptyBorder(10, 10, 10, 10));
		content.add(status, BorderLayout.WEST);
		
		console = new JTextArea(20, 50);
		console.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		console.setEditable(false);
		console.append("Ready.\n");
		JScrollPane consoleScroller = new JScrollPane(console);
		consoleScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		content.add(consoleScroller, BorderLayout.EAST);
		
		mainArea = new JPanel();
		mainArea.setLayout(new BoxLayout(mainArea, BoxLayout.Y_AXIS));
		
		JPanel mainAreaFrame = new JPanel();
		mainAreaFrame.setLayout(new BorderLayout());
		mainAreaFrame.add(mainArea, BorderLayout.NORTH);
		mainAreaFrame.setBackground(new Color(80, 80, 80));
		
		JScrollPane mainAreaScroller = new JScrollPane(mainAreaFrame);

		
		content.add(mainAreaScroller, BorderLayout.CENTER);
		
		pack();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 5754061896005251231L;

	public void runEventLoop() {
		try {
			while(robot.checkStatus()) {
				Event e;
				synchronized(robot) {
					while((e = robot.getEventQueue().poll()) != null) {
						if(e instanceof PingEvent) {
							PingEvent p = (PingEvent) e;
							pingGraph.shift(System.currentTimeMillis() - this.lastGraphUpdate);
							pingGraph.push(0, p.pingTime());
							this.lastGraphUpdate = System.currentTimeMillis();
						}
						if(e instanceof SubsystemUpdateEvent) {
							SubsystemUpdateEvent s = (SubsystemUpdateEvent) e;
							SwingUtilities.invokeAndWait(() -> {
								Subsystem[] subs = s.getSubsystems();
								System.out.println("EDT? " + SwingUtilities.isEventDispatchThread());
								mainArea.removeAll();
								for(int i = 0; i < subs.length; i++) {
									JPanel frame = new JPanel();
									frame.setBorder(new EmptyBorder(10, 10, 10, 10));
									frame.setBackground(new Color(80, 80, 80));
									frame.setLayout(new BorderLayout());
									frame.add(SubsystemView.createView(subs[i]), BorderLayout.CENTER);

									mainArea.add(frame);
								}
							});
						}
					}
					try {
						robot.wait();
					} catch (InterruptedException e1) { }
				}
			}
		} catch(RobotKilledException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e1) {
			e1.printStackTrace();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
	}

}
