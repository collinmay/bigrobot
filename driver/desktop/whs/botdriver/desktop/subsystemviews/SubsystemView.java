package whs.botdriver.desktop.subsystemviews;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import whs.botdriver.Subsystem;
import whs.botdriver.desktop.DriverWindow;
import whs.botdriver.events.SubsystemBindFailiureEvent;
import whs.botdriver.events.SubsystemBindSuccessEvent;
import whs.botdriver.events.SubsystemEvent;
import whs.botdriver.subsystems.SkidSteerDriveSystem;

public class SubsystemView extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5926144703841923998L;
	
	protected Subsystem subsystem;
	private boolean isExpanded = true;
	protected JPanel mainArea;
	private JButton bindButton;
	protected boolean bound = false;
	
	protected SubsystemView(Subsystem sub, String label) {
		this.subsystem = sub;
		this.setLayout(new BorderLayout());
		
		JPanel header = new JPanel();
		header.setLayout(new BoxLayout(header, BoxLayout.X_AXIS));
		
		final JButton shrink = new JButton("-");
		shrink.addActionListener((ActionEvent e) -> {
			isExpanded = !isExpanded;
			if(isExpanded) {
				shrink.setText("-");
			} else {
				shrink.setText("+");
			}
			mainArea.setVisible(isExpanded);
		});
		header.add(shrink);
		header.add(Box.createHorizontalStrut(5));
		header.add(new JLabel(label));
		header.add(Box.createHorizontalGlue());
		bindButton = new JButton("Bind");
		bindButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(!bound) {
					bindButton.setEnabled(false);
					bindButton.setText("Binding...");
					sub.attemptBind();
				} else {
					bindButton.setText("Bind");
					sub.unbind();
					subsystemUnbound();
					bound = false;
				}
			}
		});
		header.add(bindButton);
		this.add(header, BorderLayout.NORTH);
		
		mainArea = new JPanel();
		mainArea.setBackground(new Color(180, 180, 180));
		this.add(mainArea, BorderLayout.CENTER);
		
		this.setBorder(new EmptyBorder(5, 5, 5, 5));
	}
	
	public static SubsystemView createView(Subsystem sub) {
		if(sub instanceof SkidSteerDriveSystem) {
			return new SkidSteerSystemView((SkidSteerDriveSystem) sub);
		} else {
			return new SubsystemView(sub, "Unknown Subsystem: " + sub.getClass().getName());
		}
	}

	public void handleEvent(SubsystemEvent e) {
		if(e instanceof SubsystemBindFailiureEvent) {
			bindButton.setText("Bind");
			bindButton.setEnabled(true);
			DriverWindow.console.append("Subsystem bind for " + e.getSubsystem().getName() + " failed\n");
			if(bound) {
				bound = false;
				subsystemUnbound();
			}
		}
		if(e instanceof SubsystemBindSuccessEvent) {
			bindButton.setText("Unbind");
			bindButton.setEnabled(true);
			DriverWindow.console.append("Subsystem bind for " + e.getSubsystem().getName() + " succeeded\n");
			if(!bound) {
				bound = true;
				subsystemBound();
			}
		}
	}
	
	// override these if you need to know when the subsystem is bound or unbound
	protected void subsystemBound() { }
	protected void subsystemUnbound() { }
}
