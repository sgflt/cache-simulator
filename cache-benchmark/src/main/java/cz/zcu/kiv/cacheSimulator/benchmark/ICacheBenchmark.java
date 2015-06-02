package cz.zcu.kiv.cacheSimulator.benchmark;

import cz.zcu.kiv.cacheSimulator.cachePolicies.ICache;


/**
 * ICacheBenchamrk.java
 *    2. 6. 2015
 * @author Lukáš Kvídera
 * @version 0.0
 */
public interface ICacheBenchmark {

  /**
   * Test efficiency of contains method.
   * @return
   */
  ICache contains(DataProvider cacheProvider);

  /**
   * Test efficiency of insertion
   * @return
   */
  ICache insert(DataProvider cacheProvider);

  /**
   * Test efficiency of removal
   * @return
   */
  ICache remove(DataProvider cacheProvider);
}
