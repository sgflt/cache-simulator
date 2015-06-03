package cz.zcu.kiv.cacheSimulator.benchmark;

import cz.zcu.kiv.cacheSimulator.cachePolicies.FIFO_2ndChance;

/**
 * FIFO_2ndChance_Benchmark.java
 * 3. 6. 2015
 *
 * @author Lukáš Kvídera
 * @version 0.0
 */
public class FIFO_2ndChance_Benchmark extends ACacheBenchmark {

  /**
   *
   */
  public FIFO_2ndChance_Benchmark() {
    this.clazz = FIFO_2ndChance.class;
  }
}
