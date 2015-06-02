package cz.zcu.kiv.cacheSimulator.benchmark;

import cz.zcu.kiv.cacheSimulator.cachePolicies.ICache;
import cz.zcu.kiv.cacheSimulator.server.FileOnServer;


/**
 * IDataProvider.java
 *    2. 6. 2015
 * @author Lukáš Kvídera
 * @version 0.0
 */
public interface IDataProvider {

  /**
   *
   * @return
   */
  FileOnServer getNextFile();

  /**
   * @param clazz
   * @return
   */
  ICache getCache(Class<?> clazz);

  /**
   * @param clazz
   * @return
   */
  ICache getCache(Class<?> clazz, int size);
}
