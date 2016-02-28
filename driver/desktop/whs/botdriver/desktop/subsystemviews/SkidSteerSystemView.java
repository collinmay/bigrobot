package whs.botdriver.desktop.subsystemviews;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;

import whs.botdriver.desktop.widgets.CoordWidget;
import whs.botdriver.subsystems.SkidSteerDriveSystem;

public class SkidSteerSystemView extends SubsystemView {
	private SkidSteerDriveSystem system;
	private CoordWidget joyCoords;
	
	public SkidSteerSystemView(SkidSteerDriveSystem sub) {
		super(sub, "Skid Steer Drive");
		
		mainArea.setLayout(new BoxLayout(mainArea, BoxLayout.X_AXIS));
		joyCoords = new CoordWidget();
		joyCoords.setPos(0.5, 0.5);
		joyCoords.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				
			}
		});
		mainArea.add(CoordWidget.withLabel(joyCoords, "Joystick Input"));
	}
}
