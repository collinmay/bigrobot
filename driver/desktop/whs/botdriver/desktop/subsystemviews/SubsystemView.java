package whs.botdriver.desktop.subsystemviews;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import whs.botdriver.Subsystem;
import whs.botdriver.subsystems.SkidSteerDriveSystem;

public class SubsystemView extends JPanel {

	private Subsystem sub;
	private boolean isExpanded = true;
	protected JPanel mainArea;
	
	protected SubsystemView(Subsystem sub, String label) {
		this.sub = sub;
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
		header.add(new JButton("Bind"));
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

}
