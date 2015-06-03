package cz.zcu.kiv.cacheSimulator.benchmark;

import cz.zcu.kiv.cacheSimulator.cachePolicies.LFU_NO_REDUCTION;


/**
 * LFUBenchmark.java
 *    2. 6. 2015
 * @author Lukáš Kvídera
 * @version 0.0
 */
public class LFU_NO_REDUCTION_Benchmark extends ACacheBenchmark {

  public LFU_NO_REDUCTION_Benchmark() {
    this.clazz = LFU_NO_REDUCTION.class;
  }
}
