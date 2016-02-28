package whs.botdriver.desktop;

import java.lang.reflect.InvocationTargetException;

import javax.swing.JDialog;
import javax.swing.SwingUtilities;

import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;

public class GamepadTest {
	public static void main(String[] args) {
		try {
			SwingUtilities.invokeAndWait(() -> {
				GamepadDialog d = new GamepadDialog(GamepadDialog.InputType.DUAL_AXIS);
				d.setVisible(true);
			});
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
