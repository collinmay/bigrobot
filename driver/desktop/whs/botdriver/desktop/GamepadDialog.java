package whs.botdriver.desktop;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import whs.botdriver.desktop.controllers.Joystick;
import whs.botdriver.desktop.controllers.WController;
import whs.botdriver.desktop.widgets.CoordWidget;

public class GamepadDialog extends JDialog implements WindowListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8963674797083185219L;
	private JoystickTarget joystickTarget;
	private List<ControllerPanel> controllerPanels;
	private boolean active;
	
	public GamepadDialog() {
		super();
		
		this.controllerPanels = new ArrayList<ControllerPanel>();
		this.active = true;
		
		JPanel mainAreaFrame = new JPanel();
		mainAreaFrame.setLayout(new BorderLayout());
		
		JPanel mainArea = new JPanel();
		mainArea.setBorder(new EmptyBorder(5, 5, 5, 5));
		mainArea.setLayout(new BoxLayout(mainArea, BoxLayout.Y_AXIS));
		
		mainAreaFrame.add(mainArea, BorderLayout.NORTH);
		
		for(WController c : WController.getControllers()) {
			ControllerPanel panel = new ControllerPanel(c);
			mainArea.add(panel);
			controllerPanels.add(panel);
		}
				
		this.getContentPane().add(new JScrollPane(mainAreaFrame));
		this.pack();
		this.setLocation(200, 200);
		this.addWindowListener(this);
	}
	
	// only valid to call once per instance
	public synchronized void getJoystick(JoystickTarget joystickTarget) {
		if(!this.active) {
			joystickTarget.noJoystick();
			return;
		}
		this.setVisible(true);
		this.setTitle("Select a joystick...");
		this.joystickTarget = joystickTarget;
	}
	
	private class ControllerPanel extends JPanel implements WController.EventListener {

		/**
		 * 
		 */
		private static final long serialVersionUID = -3198999028574714405L;
		private WController controller;
		private Joystick[] joysticks;
		private CoordWidget[] coordWidgets;
		
		public ControllerPanel(WController c) {
			this.setBorder(new LineBorder(new Color(100, 100, 100)));
			this.setLayout(new BorderLayout());
			this.add(new JLabel(c.getName()), BorderLayout.NORTH);
			
			JPanel mainPane = new JPanel();
			this.add(mainPane, BorderLayout.CENTER);
			mainPane.setLayout(new BoxLayout(mainPane, BoxLayout.X_AXIS));
			
			this.controller = c;
			this.joysticks = c.getJoysticks();
			this.coordWidgets = new CoordWidget[joysticks.length];
			
			for(int i = 0; i < joysticks.length; i++) {
				coordWidgets[i] = new CoordWidget();
				coordWidgets[i].setHasJoystick(true);
				
				final Joystick j = joysticks[i];
				
				coordWidgets[i].addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						synchronized(GamepadDialog.this) {
							joystickTarget.gotJoystick(j);
							joystickTarget = null;
							GamepadDialog.this.setVisible(false);
							active = false;
						}
					}
				});
				mainPane.add(CoordWidget.withLabel(coordWidgets[i], joysticks[i].getName()));
			}
			
			this.controller.addListener(this);
		}
		
		public void update() {
			for(int i = 0; i < joysticks.length; i++) {
				coordWidgets[i].setPos(joysticks[i].getX(), joysticks[i].getY());
			}
		}

		@Override
		public boolean update(WController c) {
			this.update();
			return active;
		}
	}

	@Override public void windowOpened(WindowEvent e) { }
	@Override public synchronized void windowClosing(WindowEvent e) {
		joystickTarget.noJoystick();
		joystickTarget = null;
		active = false;
	}
	@Override public void windowClosed(WindowEvent e) { }
	@Override public void windowIconified(WindowEvent e) { }
	@Override public void windowDeiconified(WindowEvent e) { }
	@Override public void windowActivated(WindowEvent e) { }
	@Override public void windowDeactivated(WindowEvent e) { }
	
	public interface JoystickTarget {
		public void gotJoystick(Joystick j);
		public void noJoystick();
	}
}
