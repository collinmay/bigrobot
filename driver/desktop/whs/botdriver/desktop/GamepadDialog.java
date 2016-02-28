package whs.botdriver.desktop;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.MutableComboBoxModel;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.ControllerEvent;
import net.java.games.input.ControllerListener;
import net.java.games.input.Event;
import net.java.games.input.EventQueue;

public class GamepadDialog extends JDialog implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2269920014829612375L;
	
	private static Controller lastController = null;
	private InputType inputType;
	private JComboBox<Controller> controllerSelector;
	private MutableComboBoxModel<Controller> controllerSelectorModel;
	
	private ControllerEnvironment controllerEnv;
	private Controller controller;
	private Map<Component.Identifier, JLabel> componentLabels;
	private JPanel labelPane;
	
	private Thread eventThread;
	private boolean isDead = false;
	
	public GamepadDialog(InputType inputType) {
		super();
		
		switch(inputType) {
		case SINGLE_AXIS:
			this.setTitle("Select one axis"); break;
		case DUAL_AXIS:
			this.setTitle("Select two axes"); break;
		case BUTTON:
			this.setTitle("Select a button"); break;
		}
		
		componentLabels = new HashMap<Component.Identifier, JLabel>();
		
		controllerEnv = ControllerEnvironment.getDefaultEnvironment();
		
		controllerSelectorModel = new DefaultComboBoxModel<Controller>();
		Controller[] controllers = controllerEnv.getControllers();
		for(int i = 0; i < controllers.length; i++) {
			controllerSelectorModel.addElement(controllers[i]);
		}
		controllerSelector = new JComboBox<Controller>(controllerSelectorModel);
		controllerSelector.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				rebuildGui();
			}
		});
		
		controllerEnv.addControllerListener(new ControllerListener() {
			@Override
			public void controllerAdded(ControllerEvent ev) {
				System.out.println("added controller");
				controllerSelectorModel.addElement(ev.getController());
				rebuildGui();
			}

			@Override
			public void controllerRemoved(ControllerEvent ev) {
				System.out.println("removed controller");
				controllerSelectorModel.removeElement(ev.getController());
				rebuildGui();
			}
		});
		
		JPanel contentPane = new JPanel();
		contentPane.setLayout(new BorderLayout());
		this.getContentPane().add(contentPane);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.add(controllerSelector, BorderLayout.NORTH);
		
		labelPane = new JPanel();
		labelPane.setLayout(new BoxLayout(labelPane, BoxLayout.Y_AXIS));
		contentPane.add(labelPane, BorderLayout.CENTER);

		this.rebuildGui();

		Timer timer = new Timer(10, this);
		timer.start();
		
		this.setLocation(200, 200);
	}
	
	private synchronized void rebuildGui() {
		controller = (Controller) controllerSelector.getSelectedItem();
		labelPane.removeAll();
		if(controller != null) {
			Component[] components = controller.getComponents();
			componentLabels.clear();
			for(int i = 0; i < components.length; i++) {
				JLabel label = new JLabel();
				label.setText("placeholder");
				componentLabels.put(components[i].getIdentifier(), label);
				labelPane.add(label);
			}
		} else {
			labelPane.add(new JLabel("no controller selected"));
		}
		this.pack();
	}

	public enum InputType {
		SINGLE_AXIS, DUAL_AXIS, BUTTON
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		synchronized(GamepadDialog.this) {
			if(controller != null) {
				if(controller.poll()) {
					EventQueue queue = controller.getEventQueue();
					Event event = new Event();
					while(queue.getNextEvent(event)) {
						Component.Identifier id = event.getComponent().getIdentifier();
						componentLabels.get(id).setText(id.toString() + ": " + event.getValue());
					}
					this.repaint();
				} else {
					controllerSelectorModel.removeElement(controller);
					controller = null;
					rebuildGui();
				}
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException ex) { }
		}
	}
}
