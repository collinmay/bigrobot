package whs.botdriver.fxdesktop.subsystemview;

import javafx.scene.Node;
import javafx.scene.control.Label;
import whs.botdriver.Subsystem;

import java.io.IOException;

/**
 * Created by misson20000 on 5/24/16.
 */
public class UnknownSubsystemPane extends SubsystemPane {
  public UnknownSubsystemPane(Subsystem subsystem) {
    super(subsystem);
  }

  @Override
  protected Node buildMainView() throws IOException {
    return new Label("UNKNOWN SUBSYSTEM TYPE '" + subsystem.getClass().getName());
  }
}
