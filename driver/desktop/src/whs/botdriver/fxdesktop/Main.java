package whs.botdriver.fxdesktop;/**
 * Created by misson20000 on 5/23/16.
 */

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void start(Stage primaryStage) throws IOException {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("main_window.fxml"));
    Parent root = loader.load();
    MainWindowController c = loader.getController();
    c.initialize(root);

    primaryStage.setTitle("WHS DesktopDriver v5");
    primaryStage.setScene(new Scene(root, 800, 600));
    primaryStage.show();
  }
}
