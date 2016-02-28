package whs.botdriver.desktop;

import whs.botdriver.desktop.controllers.Joystick;

public class GamepadTest {
	public static void main(String[] args) {
		new GamepadDialog().getJoystick(new GamepadDialog.JoystickTarget() {
			@Override
			public void gotJoystick(Joystick j) {
				System.out.println("got joystick");
				System.exit(0);
			}
			
			@Override
			public void noJoystick() {
				System.out.println("no joystick");
				System.exit(1);
			}
		});
	}
}
