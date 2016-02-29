package whs.botdriver.desktop.subsystemviews;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BoxLayout;

import whs.botdriver.desktop.GamepadDialog;
import whs.botdriver.desktop.controllers.Joystick;
import whs.botdriver.desktop.widgets.AxisWidget;
import whs.botdriver.desktop.widgets.CoordWidget;
import whs.botdriver.events.SubsystemEvent;
import whs.botdriver.subsystems.SkidSteerDriveSystem;

public class SkidSteerSystemView extends SubsystemView implements Joystick.EventListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -35365204530681309L;
	
	private SkidSteerDriveSystem system;
	private CoordWidget joyCoords;
	private AxisWidget leftPowerView;
	private AxisWidget rightPowerView;
	private Joystick input;
	
	public SkidSteerSystemView(SkidSteerDriveSystem sub) {
		super(sub, "Skid Steer Drive");
		
		this.system = sub;
		
		mainArea.setLayout(new BoxLayout(mainArea, BoxLayout.X_AXIS));
		input = null;
		joyCoords = new CoordWidget();
		joyCoords.setMessage("not bound");
		joyCoords.setAxisLines(CoordWidget.AxisLines.NORMAL);
		joyCoords.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				new GamepadDialog().getJoystick(new GamepadDialog.JoystickTarget() {
					@Override
					public void noJoystick() {
						input = null;
						joystickMoved(null, 0, 0);
						updateWidgetMessage();
					}
					
					@Override
					public void gotJoystick(Joystick j) {
						input = j;
						j.addEventListener(SkidSteerSystemView.this);
						updateWidgetMessage();
					}
				});
			}
		});
		mainArea.add(CoordWidget.withLabel(joyCoords, "Joystick Input"));
		
		mainArea.add(leftPowerView = new AxisWidget());
		mainArea.add(rightPowerView = new AxisWidget());
	}

	private void updateWidgetMessage() {
		if(!bound) {
			joyCoords.setMessage("not bound");
		} else if(input == null) {
			joyCoords.setMessage("no joystick");
		} else {
			joyCoords.setMessage(null);
		}
	}
	
	@Override
	public boolean joystickMoved(Joystick j, float x, float y) {
		if(input == j) {
			x*= 1.1;
			y*= 1.1;
			double r = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
			double t = Math.atan2(y, x);
			if(r > 1) {
				x/= r;
				y/= r;
				r = 1;
			}
			
			joyCoords.setPolar(t, r);			
			t+= (3.0*Math.PI/4.0);
			
			double sqRad;
			if((t >= Math.PI*3.0/4.0 && t < Math.PI*5.0/4.0) || t < Math.PI/4.0 || t > Math.PI*7.0/4.0) {
				sqRad = 1.0/Math.cos(t);
			} else {
				sqRad = 1.0/Math.sin(t);
			}
			sqRad = Math.abs(sqRad);
			double dr = r * sqRad;			
			double leftPower = dr * Math.sin(t);
			double rightPower = dr * Math.cos(t);
			
			leftPowerView.setValue(leftPower);
			rightPowerView.setValue(rightPower);
			
			system.setPower(leftPower, rightPower);
		}
		return input == j;
	}
	
	@Override
	protected void subsystemBound() {
		updateWidgetMessage();
	}
	
	@Override
	protected void subsystemUnbound() {
		updateWidgetMessage();
	}
	
	@Override
	public void handleEvent(SubsystemEvent e) {
		super.handleEvent(e);
		
	}
}
