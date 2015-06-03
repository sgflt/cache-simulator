package cz.zcu.kiv.cacheSimulator.benchmark;

import cz.zcu.kiv.cacheSimulator.cachePolicies.LIRS;

/**
 * LIRS_Benchmark.java
 * 3. 6. 2015
 *
 * @author Lukáš Kvídera
 * @version 0.0
 */
public class LIRS_Benchmark extends ACacheBenchmark {

  /**
   *
   */
  public LIRS_Benchmark() {
    this.clazz = LIRS.class;
  }
}
