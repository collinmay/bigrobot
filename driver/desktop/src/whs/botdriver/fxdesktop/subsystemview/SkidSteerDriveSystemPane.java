package whs.botdriver.fxdesktop.subsystemview;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleExpression;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TabPane;
import whs.botdriver.Motor;
import whs.botdriver.events.SkidSteerSystemUpdateEvent;
import whs.botdriver.events.SubsystemEvent;
import whs.botdriver.fxdesktop.WComponent;
import whs.botdriver.fxdesktop.WComponentCell;
import whs.botdriver.fxdesktop.WController;
import whs.botdriver.fxdesktop.WControllerCell;
import whs.botdriver.subsystems.SkidSteerDriveSystem;

import java.io.IOException;

/**
 * Created by misson20000 on 5/24/16.
 */
public class SkidSteerDriveSystemPane extends SubsystemPane {

  @FXML private BarChart<String, Number> chart;

  private XYChart.Series<String, Number> left;
  private XYChart.Series<String, Number> right;

  private XYChart.Data<String, Number> leftIn;
  private XYChart.Data<String, Number> rightIn;
  private XYChart.Data<String, Number> leftOut;
  private XYChart.Data<String, Number> rightOut;

  @FXML private ComboBox<WController> xControllerBox;
  @FXML private ComboBox<WController> yControllerBox;
  @FXML private ComboBox<WComponent> xAxisBox;
  @FXML private ComboBox<WComponent> yAxisBox;

  @FXML private ComboBox<WController> lControllerBox;
  @FXML private ComboBox<WController> rControllerBox;
  @FXML private ComboBox<WComponent> lAxisBox;
  @FXML private ComboBox<WComponent> rAxisBox;

  @FXML private CheckBox lReverse;
  @FXML private CheckBox rReverse;
  @FXML private CheckBox xReverse;
  @FXML private CheckBox yReverse;

  private WComponent xAxisComponent;
  private WComponent yAxisComponent;

  private DoubleProperty xAxisProperty;
  private DoubleProperty yAxisProperty;
  private DoubleExpression xAxisDeadZoner;
  private DoubleExpression yAxisDeadZoner;
  private DoubleProperty leftIntuitiveProperty;
  private DoubleProperty rightIntuitiveProperty;


  private WComponent lAxisComponent;
  private WComponent rAxisComponent;
  private DoubleProperty leftTankProperty;
  private DoubleProperty rightTankProperty;
  private DoubleExpression leftTankDeadZoner;
  private DoubleExpression rightTankDeadZoner;

  private DoubleProperty leftOutputProperty;
  private DoubleProperty rightOutputProperty;

  @FXML private TabPane tabPane;

  private Mode mode;

  private SkidSteerDriveSystem subsystem;

  public SkidSteerDriveSystemPane(SkidSteerDriveSystem subsystem) {
    super(subsystem);
    this.subsystem = subsystem;
  }

  protected Node buildMainView() throws IOException {
    WController.initialize();
    FXMLLoader loader = new FXMLLoader(getClass().getResource("subsystem_skidsteer.fxml"));
    loader.setController(this);
    loader.load();

    left = new XYChart.Series<>();
    right = new XYChart.Series<>();

    left.setName("Left");
    right.setName("Right");

    leftIn = new XYChart.Data<>("In", 1.0);
    rightIn = new XYChart.Data<>("In", 0.7);
    leftOut = new XYChart.Data<>("Out", 0.5);
    rightOut = new XYChart.Data<>("Out", 0.2);

    this.xAxisProperty = new SimpleDoubleProperty();
    this.yAxisProperty = new SimpleDoubleProperty();

    this.xAxisDeadZoner = createDeadzoner(xAxisProperty);
    this.yAxisDeadZoner = createDeadzoner(yAxisProperty);

    this.leftIntuitiveProperty = new SimpleDoubleProperty();
    this.rightIntuitiveProperty = new SimpleDoubleProperty();

    this.leftTankProperty = new SimpleDoubleProperty();
    this.rightTankProperty = new SimpleDoubleProperty();

    this.leftTankDeadZoner = createDeadzoner(leftTankProperty);
    this.rightTankDeadZoner = createDeadzoner(rightTankProperty);

    this.leftOutputProperty = new SimpleDoubleProperty();
    this.rightOutputProperty = new SimpleDoubleProperty();

    ChangeListener<? super Number> outListener = (o, old_val, new_val) -> {
      subsystem.setPower(leftOutputProperty.getValue(), rightOutputProperty.getValue());
    };

    leftOutputProperty.addListener(outListener);
    rightOutputProperty.addListener(outListener);

    ChangeListener<? super Number> listener = (observable, oldValue, newValue) -> {
      if(xAxisDeadZoner.getValue() == 0 && yAxisDeadZoner.getValue() == 0) {
        leftIntuitiveProperty.setValue(0);
        rightIntuitiveProperty.setValue(0);
      } else {
        double angle = Math.atan2(yAxisDeadZoner.getValue(), xAxisDeadZoner.getValue()) + Math.PI;
        double radius = Math.sqrt(Math.pow(yAxisDeadZoner.getValue(), 2) + Math.pow(xAxisDeadZoner.getValue(), 2));

        double l = 0;
        double r = 0;

        if(angle >= 0 && angle < 1 * Math.PI / 2) {
          l = radius;
          r = radius * ((4.0 * angle / Math.PI) - 1.0);
        } else if(angle < 2 * Math.PI / 2) {
          l = -radius * ((4.0 * angle / Math.PI) - 3.0);
          r = radius;
        } else if(angle < 3 * Math.PI / 2) {
          l = -radius;
          r = -radius * ((4.0 * angle / Math.PI) - 5.0);
        } else {
          l = radius * ((4.0 * angle / Math.PI) - 7.0);
          r = -radius;
        }

        leftIntuitiveProperty.setValue(l);
        rightIntuitiveProperty.setValue(r);
      }
    };

    this.xAxisDeadZoner.addListener(listener);
    this.yAxisDeadZoner.addListener(listener);

    leftOutputProperty.bind(leftIntuitiveProperty);
    rightOutputProperty.bind(rightIntuitiveProperty);

    leftIn.YValueProperty().bind(leftOutputProperty);
    rightIn.YValueProperty().bind(rightOutputProperty);

    left.getData().add(leftIn);
    left.getData().add(leftOut);

    right.getData().add(rightIn);
    right.getData().add(rightOut);

    chart.getData().setAll(left, right);

    chart.setAnimated(false);

    BooleanBinding disableProperty = boundProperty.not();
    
    WControllerCell.Factory controllerCellFactory = new WControllerCell.Factory();
    xControllerBox.itemsProperty().bind(new ReadOnlyListWrapper<>(WController.wControllerList));
    yControllerBox.itemsProperty().bind(new ReadOnlyListWrapper<>(WController.wControllerList));
    lControllerBox.itemsProperty().bind(new ReadOnlyListWrapper<>(WController.wControllerList));
    rControllerBox.itemsProperty().bind(new ReadOnlyListWrapper<>(WController.wControllerList));
    xControllerBox.setCellFactory(controllerCellFactory);
    yControllerBox.setCellFactory(controllerCellFactory);
    lControllerBox.setCellFactory(controllerCellFactory);
    rControllerBox.setCellFactory(controllerCellFactory);
    xControllerBox.setButtonCell(new WControllerCell());
    yControllerBox.setButtonCell(new WControllerCell());
    lControllerBox.setButtonCell(new WControllerCell());
    rControllerBox.setButtonCell(new WControllerCell());
    xControllerBox.disableProperty().bind(disableProperty);
    yControllerBox.disableProperty().bind(disableProperty);
    lControllerBox.disableProperty().bind(disableProperty);
    rControllerBox.disableProperty().bind(disableProperty);


    WComponentCell.Factory componentCellFactory = new WComponentCell.Factory();
    xAxisBox.setCellFactory(componentCellFactory);
    yAxisBox.setCellFactory(componentCellFactory);
    lAxisBox.setCellFactory(componentCellFactory);
    rAxisBox.setCellFactory(componentCellFactory);
    xAxisBox.setButtonCell(new WComponentCell());
    yAxisBox.setButtonCell(new WComponentCell());
    lAxisBox.setButtonCell(new WComponentCell());
    rAxisBox.setButtonCell(new WComponentCell());
    xAxisBox.disableProperty().bind(disableProperty);
    yAxisBox.disableProperty().bind(disableProperty);
    lAxisBox.disableProperty().bind(disableProperty);
    rAxisBox.disableProperty().bind(disableProperty);

    tabPane.getSelectionModel().selectedIndexProperty().addListener((o, old_val, new_val) -> {
      mode = Mode.values()[(int) new_val];
      switch(mode) {
        case INTUITIVE:
          leftOutputProperty.bind(leftIntuitiveProperty);
          rightOutputProperty.bind(rightIntuitiveProperty);
          break;
        case TANK:
          leftOutputProperty.bind(leftTankDeadZoner);
          rightOutputProperty.bind(rightTankDeadZoner);
          break;
      }
    });

    return loader.getRoot();
  }

  private DoubleExpression createDeadzoner(DoubleExpression in) {
    SimpleDoubleProperty out = new SimpleDoubleProperty();
    in.addListener((obs, oldV, newVN) -> {
      double newV = newVN.doubleValue();
      if(Math.abs(newV) < 0.05) {
        out.set(0);
      } else {
        if(newV > 0) {
          out.set((newV - 0.05) * (1.0/0.95));
        } else {
          out.set((newV + 0.05) * (1.0/0.95));
        }
      }
    });
    return out;
  }

  @FXML
  private void xControllerSet(ActionEvent e) {
    xAxisBox.getItems().setAll(xControllerBox.getValue().getComponents());
    if(xAxisComponent != null) {
      xAxisBox.setValue(xControllerBox.getValue().getComponent(xAxisComponent));
    }
  }

  @FXML
  private void yControllerSet(ActionEvent e) {
    yAxisBox.getItems().setAll(yControllerBox.getValue().getComponents());
    if(yAxisComponent != null) {
      yAxisBox.setValue(yControllerBox.getValue().getComponent(yAxisComponent));
    }
  }

  @FXML
  public void xAxisSet(ActionEvent e) {
    xAxisProperty.unbind();
    xAxisComponent = xAxisBox.getValue();
    xAxisProperty.bind(xAxisComponent.getProperty().multiply(Bindings.when(xReverse.selectedProperty()).then(-1).otherwise(1)));
  }

  @FXML
  public void yAxisSet(ActionEvent e) {
    yAxisProperty.unbind();
    yAxisComponent = yAxisBox.getValue();
    yAxisProperty.bind(yAxisComponent.getProperty().multiply(Bindings.when(yReverse.selectedProperty()).then(-1).otherwise(1)));
  }

  @FXML
  private void lControllerSet(ActionEvent e) {
    lAxisBox.getItems().setAll(lControllerBox.getValue().getComponents());
    if(lAxisComponent != null) {
      lAxisBox.setValue(lControllerBox.getValue().getComponent(lAxisComponent));
    }
  }

  @FXML
  private void rControllerSet(ActionEvent e) {
    rAxisBox.getItems().setAll(rControllerBox.getValue().getComponents());
    if(rAxisComponent != null) {
      rAxisBox.setValue(rControllerBox.getValue().getComponent(rAxisComponent));
    }
  }

  @FXML
  public void lAxisSet(ActionEvent e) {
    leftTankProperty.unbind();
    lAxisComponent = lAxisBox.getValue();
    leftTankProperty.bind(lAxisComponent.getProperty().multiply(Bindings.when(lReverse.selectedProperty()).then(-1).otherwise(1)));
  }

  @FXML
  public void rAxisSet(ActionEvent e) {
    rightTankProperty.unbind();
    rAxisComponent = rAxisBox.getValue();
    rightTankProperty.bind(rAxisComponent.getProperty().multiply(Bindings.when(rReverse.selectedProperty()).then(-1).otherwise(1)));
  }

  @Override
  public void handleEvent(SubsystemEvent evt) {
    super.handleEvent(evt);

    if(evt instanceof SkidSteerSystemUpdateEvent) {
      Motor left = ((SkidSteerSystemUpdateEvent) evt).getLeftMotor();
      Motor right = ((SkidSteerSystemUpdateEvent) evt).getRightMotor();
      leftOut.setYValue(left.getSpeed());
      rightOut.setYValue(right.getSpeed());
    }
  }

  private enum Mode {
    INTUITIVE, TANK
  }
}
