package whs.botdriver.fxdesktop;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import net.java.games.input.Controller;

/**
 * Created by misson20000 on 5/24/16.
 */
public class WControllerCell extends ListCell<WController> {

  @Override
  protected void updateItem(WController item, boolean empty) {
    super.updateItem(item, empty);

    if(item == null || empty) {
      setText(null);
      setGraphic(null);
    } else {
      setText(item.getName());
    }
  }

  public static class Factory implements Callback<ListView<WController>,ListCell<WController>> {
    @Override
    public ListCell<WController> call(ListView<WController> param) {
      return new WControllerCell();
    }
  }
}
