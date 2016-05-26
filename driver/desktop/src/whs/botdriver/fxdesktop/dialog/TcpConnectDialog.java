package whs.botdriver.fxdesktop.dialog;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by misson20000 on 5/23/16.
 */
public class TcpConnectDialog extends Stage {

  @FXML
  public TextField addrField;

  @FXML
  public TextField portField;

  private boolean cancelled;

  public TcpConnectDialog() {
    setTitle("Manually enter robot address");

    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("dialog_tcp_connect.fxml"));
    fxmlLoader.setController(this);

    try {
      setScene(new Scene(fxmlLoader.load()));
    } catch(IOException e) {
      e.printStackTrace();
    }

    setMaxWidth(315);
    setMaxHeight(96);

    addrField.setText("127.0.0.1");
    portField.setText("25600");

    portField.textProperty().addListener(new ChangeListener<String>() {
      @Override
      public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        if(!newValue.matches("\\d*")) {
          portField.setText(oldValue);
        }
      }
    });
  }

  public InetAddress getAddress() throws UnknownHostException {
    return InetAddress.getByName(addrField.getText());
  }

  public int getPort() {
    return Integer.parseInt(portField.getText());
  }

  public boolean isCancelled() {
    return cancelled;
  }

  @FXML
  public void connect(ActionEvent e) {
    cancelled = false;
    close();
  }

  @FXML
  public void cancel(ActionEvent e) {
    cancelled = true;
    close();
  }
}
