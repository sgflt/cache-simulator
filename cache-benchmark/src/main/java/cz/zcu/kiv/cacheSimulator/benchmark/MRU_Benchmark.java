package cz.zcu.kiv.cacheSimulator.benchmark;

import cz.zcu.kiv.cacheSimulator.cachePolicies.MRU;

/**
 * MRU_Benchmark.java
 * 3. 6. 2015
 *
 * @author Lukáš Kvídera
 * @version 0.0
 */
public class MRU_Benchmark extends ACacheBenchmark {

  /**
   *
   */
  public MRU_Benchmark() {
    this.clazz = MRU.class;
  }
}
