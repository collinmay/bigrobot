package whs.botdriver.fxdesktop;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import net.java.games.input.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by misson20000 on 5/25/16.
 */
public class WController {
  public static ObservableMap<Controller, WController> controllers;
  public static ObservableList<WController> wControllerList;
  private static boolean initialized = false;

  public static void initialize() {
    if(!initialized) {
      ControllerEnvironment env = ControllerEnvironment.getDefaultEnvironment();
      controllers = FXCollections.observableMap(Arrays.stream(env.getControllers()).collect(Collectors.toMap(Function.identity(), WController::new)));
      wControllerList = FXCollections.observableList(new ArrayList<>(controllers.values()));
      env.addControllerListener(new ControllerListener() {
        @Override
        public void controllerRemoved(ControllerEvent evt) {
          wControllerList.remove(controllers.get(evt.getController()));
          controllers.remove(evt.getController());
          System.out.println("controller removed");
        }

        @Override
        public void controllerAdded(ControllerEvent evt) {
          WController wController = new WController(evt.getController());
          controllers.put(evt.getController(), wController);
          wControllerList.add(wController);
          System.out.println("controller added");
        }
      });
      initialized = true;
    }
  }

  private final Thread thread;

  private Map<Component.Identifier, WComponent> componentMap = new HashMap<>();

  private Controller controller;

  public WController(Controller controller) {
    this.controller = controller;
    for(Component c : controller.getComponents()) {
      componentMap.put(c.getIdentifier(), new WComponent(c));
    }
    this.thread = new Thread(() -> {
      Event evt = new Event();
      while(true) {
        controller.poll();
        if(controller.getEventQueue().getNextEvent(evt)) {
          componentMap.get(evt.getComponent().getIdentifier()).setValue(evt.getValue());
        } else {
          try {
            Thread.sleep(16);
          } catch(InterruptedException e) {
          }
        }
      }
    });
    this.thread.setName(controller.getName() + " event thread");
    this.thread.setDaemon(true);
    this.thread.start();
  }

  public Collection<WComponent> getComponents() {
    return componentMap.values();
  }

  public WComponent getComponent(WComponent other) {
    return componentMap.get(other.getIdentifier());
  }

  public WComponent getComponent(Component.Identifier id) {
    return componentMap.get(id);
  }

  public String getName() {
    return controller.getName();
  }
}
