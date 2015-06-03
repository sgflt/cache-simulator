package cz.zcu.kiv.cacheSimulator.benchmark;

import cz.zcu.kiv.cacheSimulator.cachePolicies.LRDv2;

/**
 * LRDv2_Benchmark.java
 * 3. 6. 2015
 *
 * @author Lukáš Kvídera
 * @version 0.0
 */
public class LRDv2_Benchmark extends ACacheBenchmark {

  public LRDv2_Benchmark() {
    this.clazz = LRDv2.class;
  }
}
