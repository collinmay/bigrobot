package whs.botdriver.fxdesktop.subsystemview;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import whs.botdriver.Subsystem;
import whs.botdriver.events.SubsystemBindFailiureEvent;
import whs.botdriver.events.SubsystemBindSuccessEvent;
import whs.botdriver.events.SubsystemEvent;
import whs.botdriver.events.SubsystemUnboundEvent;
import whs.botdriver.subsystems.SkidSteerDriveSystem;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * Created by misson20000 on 5/24/16.
 */
public abstract class SubsystemPane extends BorderPane {
  private Node mainView;
  protected Subsystem subsystem;

  @FXML
  private Label subsystemLabel;

  @FXML
  private Button collapseButton;

  @FXML
  private Button bindButton;

  @FXML
  private BorderPane mainPane;

  private boolean expanded;

  protected BooleanProperty boundProperty;

  private boolean boundPropertyTarget = false;

  public SubsystemPane(Subsystem system) {
    try {
      this.boundProperty = new SimpleBooleanProperty(false);
      this.subsystem = system;
      this.mainView = buildMainView();


      this.setMaxWidth(Double.MAX_VALUE);
      this.setMaxHeight(Region.USE_PREF_SIZE);

      FXMLLoader loader = new FXMLLoader(getClass().getResource("subsystem_frame.fxml"));
      loader.setController(this);

      loader.load();

      this.subsystemLabel.setText(system.getName());
      this.setCenter(loader.getRoot());
      this.expand();
      this.mainPane.setCenter(mainView);
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected abstract Node buildMainView() throws IOException;

  public void expand() {
    this.mainPane.setCenter(mainView);
    this.collapseButton.setText("-");
    expanded = true;
  }

  public void collapse() {
    this.mainPane.setCenter(null);
    this.collapseButton.setText("+");
    expanded = false;
  }

  @FXML
  private void collapseButtonClicked(ActionEvent e) {
    if(expanded) {
      collapse();
    } else {
      expand();
    }
  }

  @FXML
  private void bindButtonClicked(ActionEvent e) {
    if(this.subsystem.hasBinding()) {
      this.subsystem.unbind();
      bindButton.setText("Bind");
    } else {
      this.subsystem.attemptBind();
      bindButton.setText("Binding...");
      bindButton.setDisable(true);
    }
    if(subsystem.hasBinding() != boundPropertyTarget) {
      boundPropertyTarget = subsystem.hasBinding();
      final boolean tgt = boundPropertyTarget;
      Platform.runLater(() -> boundProperty.setValue(tgt));
    }
  }

  public void handleEvent(SubsystemEvent evt) {
    if(evt instanceof SubsystemBindSuccessEvent) {
      Platform.runLater(() -> {
        bindButton.setText("Unbind");
        bindButton.setDisable(false);
      });
    }
    if(subsystem.hasBinding() != boundPropertyTarget) {
      boundPropertyTarget = subsystem.hasBinding();
      final boolean tgt = boundPropertyTarget;
      Platform.runLater(() -> boundProperty.setValue(tgt));
    }
  }

  public static SubsystemPane createView(Subsystem subsystem) {
    if(subsystem instanceof SkidSteerDriveSystem) {
      return new SkidSteerDriveSystemPane((SkidSteerDriveSystem) subsystem);
    } else {
      return new UnknownSubsystemPane(subsystem);
    }
  }
}
