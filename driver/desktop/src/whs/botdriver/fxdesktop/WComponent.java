package whs.botdriver.fxdesktop;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import net.java.games.input.Component;

/**
 * Created by misson20000 on 5/25/16.
 */
public class WComponent {
  private Component component;
  private DoubleProperty value = new SimpleDoubleProperty();

  public WComponent(Component c) {
    component = c;
  }

  public void setValue(float value) {
    Platform.runLater(() -> this.value.setValue(value));
  }

  public DoubleProperty getProperty() {
    return value;
  }

  public Component.Identifier getIdentifier() {
    return component.getIdentifier();
  }
}
