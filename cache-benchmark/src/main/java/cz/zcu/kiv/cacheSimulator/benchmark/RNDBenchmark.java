package cz.zcu.kiv.cacheSimulator.benchmark;

import cz.zcu.kiv.cacheSimulator.cachePolicies.RND;

/**
 * CacheBenchmark.java
 * 2. 6. 2015
 *
 * @author Lukáš Kvídera
 * @version 0.0
 */
public class RNDBenchmark extends ACacheBenchmark {

  /**
   *
   */
  public RNDBenchmark() {
    this.clazz = RND.class;
  }
}
