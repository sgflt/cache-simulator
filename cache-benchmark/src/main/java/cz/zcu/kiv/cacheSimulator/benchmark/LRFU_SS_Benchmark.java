package cz.zcu.kiv.cacheSimulator.benchmark;

import cz.zcu.kiv.cacheSimulator.cachePolicies.LRFU_SS;

/**
 * LRFU_SS_Benchmark.java
 * 3. 6. 2015
 *
 * @author Lukáš Kvídera
 * @version 0.0
 */
public class LRFU_SS_Benchmark extends ACacheBenchmark {

  public LRFU_SS_Benchmark() {
    this.clazz = LRFU_SS.class;
  }
}
