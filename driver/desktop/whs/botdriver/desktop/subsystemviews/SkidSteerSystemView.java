package whs.botdriver.desktop.subsystemviews;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;

import whs.botdriver.desktop.GamepadDialog;
import whs.botdriver.desktop.controllers.Joystick;
import whs.botdriver.desktop.widgets.CoordWidget;
import whs.botdriver.subsystems.SkidSteerDriveSystem;

public class SkidSteerSystemView extends SubsystemView implements Joystick.EventListener {
	private SkidSteerDriveSystem system;
	private CoordWidget joyCoords;
	private Joystick input;
	
	public SkidSteerSystemView(SkidSteerDriveSystem sub) {
		super(sub, "Skid Steer Drive");
		
		mainArea.setLayout(new BoxLayout(mainArea, BoxLayout.X_AXIS));
		input = null;
		joyCoords = new CoordWidget();
		joyCoords.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				new GamepadDialog().getJoystick(new GamepadDialog.JoystickTarget() {
					@Override
					public void noJoystick() {
						System.out.println("no joystick");
						input = null;
						joyCoords.setHasJoystick(false);
					}
					
					@Override
					public void gotJoystick(Joystick j) {
						System.out.println("yes joystick");
						input = j;
						joyCoords.setHasJoystick(true);
						j.addEventListener(SkidSteerSystemView.this);
					}
				});
			}
		});
		mainArea.add(CoordWidget.withLabel(joyCoords, "Joystick Input"));
	}

	@Override
	public boolean joystickMoved(Joystick j, float x, float y) {
		if(input == j) {
			joyCoords.setPos(x, y);
		}
		return input == j;
	}
}
