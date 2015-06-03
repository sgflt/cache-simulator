package cz.zcu.kiv.cacheSimulator.benchmark;

import cz.zcu.kiv.cacheSimulator.cachePolicies.LFU_SS;


/**
 * LFU_SSBenchmark.java
 *    2. 6. 2015
 * @author Lukáš Kvídera
 * @version 0.0
 */
public class LFU_SS_Benchmark extends ACacheBenchmark {

  public LFU_SS_Benchmark() {
    this.clazz = LFU_SS.class;
  }
}
