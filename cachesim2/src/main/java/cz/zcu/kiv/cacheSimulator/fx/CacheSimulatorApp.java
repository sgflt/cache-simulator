package cz.zcu.kiv.cacheSimulator.fx;

import cz.zcu.kiv.cacheSimulator.shared.ConfigReaderWriter;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * JavaFX application: the main window.
 */
public class CacheSimulatorApp extends Application {

  @Override
  public void start(final Stage stage) throws IOException {
    final Parent root = FXMLLoader.load(CacheSimulatorApp.class.getResource("/fx/main.fxml"));
    final Scene scene = new Scene(root, 1120, 740);
    scene.getStylesheets().add(stylesheet());
    stage.setTitle("Cache Simulator");
    stage.getIcons().add(new Image(CacheSimulatorApp.class.getResource("/ico/simulation.png").toExternalForm()));
    stage.setMinWidth(900);
    stage.setMinHeight(600);
    stage.setScene(scene);
    stage.show();
  }

  /**
   * saves the settings on every way out (menu, toolbar, closing the window)
   */
  @Override
  public void stop() {
    ConfigReaderWriter.write();
  }

  static String stylesheet() {
    return CacheSimulatorApp.class.getResource("/fx/app.css").toExternalForm();
  }
}
