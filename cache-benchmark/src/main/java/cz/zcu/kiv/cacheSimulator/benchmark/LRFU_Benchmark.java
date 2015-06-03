package cz.zcu.kiv.cacheSimulator.benchmark;

import cz.zcu.kiv.cacheSimulator.cachePolicies.LRFU;

/**
 * LRFU_Benchmark.java
 * 3. 6. 2015
 *
 * @author Lukáš Kvídera
 * @version 0.0
 */
public class LRFU_Benchmark extends ACacheBenchmark {

  /**
   *
   */
  public LRFU_Benchmark() {
    this.clazz = LRFU.class;
  }
}
