package cz.zcu.kiv.cacheSimulator.benchmark;

import cz.zcu.kiv.cacheSimulator.cachePolicies.LRU;


/**
 * LRUBenchmark.java
 *    2. 6. 2015
 * @author Lukáš Kvídera
 * @version 0.0
 */
public class LRU_Benchmark extends ACacheBenchmark {

  public LRU_Benchmark() {
    this.clazz = LRU.class;
  }
}
