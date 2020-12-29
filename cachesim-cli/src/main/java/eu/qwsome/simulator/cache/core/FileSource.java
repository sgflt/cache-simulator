package eu.qwsome.simulator.cache.core;

/**
 * @author Lukáš Kvídera
 */
@FunctionalInterface
public interface FileSource {

  /**
   * @return null if there is no file available (simulation end)
   */
  FileTraffic get();
}
