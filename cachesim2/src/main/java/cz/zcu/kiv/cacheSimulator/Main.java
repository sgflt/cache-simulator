package cz.zcu.kiv.cacheSimulator;

import java.io.File;

import cz.zcu.kiv.cacheSimulator.fx.CacheSimulatorApp;
import cz.zcu.kiv.cacheSimulator.shared.ConfigReaderWriter;
import javafx.application.Application;

/**
 * trida, ktera obsahuje metodu main, vstupni bod simulace
 *
 * class with main method, entry point to the simulation
 *
 * Note: this class must not extend {@link Application}, otherwise JavaFX
 * refuses to start from the classpath (fat jar).
 *
 * @author Pavel Bzoch
 */
public class Main {

  /**
   * constant for config file
   */
  static final String CONFIG_FILE = "config.xml";

  /**
   * entry point to the simulation, reads config file, shows gui
   *
   * @param args the command line arguments
   */
  public static void main(final String[] args) {
    final File config = new File(CONFIG_FILE);
    if (config.exists() && config.isFile()) {
      ConfigReaderWriter.read();
    }
    Application.launch(CacheSimulatorApp.class, args);
  }
}
