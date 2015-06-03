package cz.zcu.kiv.cacheSimulator.benchmark;

import cz.zcu.kiv.cacheSimulator.cachePolicies.LRDv1;

/**
 * LRDv1_Benchmark.java
 * 3. 6. 2015
 *
 * @author Lukáš Kvídera
 * @version 0.0
 */
public class LRDv1_Benchmark extends ACacheBenchmark {

  /**
   *
   */
  public LRDv1_Benchmark() {
    this.clazz = LRDv1.class;
  }
}
