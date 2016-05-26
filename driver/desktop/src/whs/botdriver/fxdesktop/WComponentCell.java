package whs.botdriver.fxdesktop;

import javafx.beans.property.DoubleProperty;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.util.Callback;
import net.java.games.input.Component;

/**
 * Created by misson20000 on 5/25/16.
 */
public class WComponentCell extends ListCell<WComponent> {

  @Override
  protected void updateItem(WComponent component, boolean empty) {
    super.updateItem(component, empty);

    if(component == null || empty) {
      setText(null);
      setGraphic(null);
    } else {
      ProgressBar bar = new ProgressBar();
      bar.progressProperty().bind(component.getProperty().multiply(0.5).add(0.5));
      setText(component.getIdentifier().getName());
      setGraphic(bar);
    }
  }

  public static class Factory implements Callback<ListView<WComponent>, ListCell<WComponent>> {
    @Override
    public ListCell<WComponent> call(ListView<WComponent> param) {
      return new WComponentCell();
    }
  }
}
