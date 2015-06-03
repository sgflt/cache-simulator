package cz.zcu.kiv.cacheSimulator.benchmark;

import cz.zcu.kiv.cacheSimulator.cachePolicies.FIFO;

/**
 * FIFO_Benchmark.java
 * 3. 6. 2015
 *
 * @author Lukáš Kvídera
 * @version 0.0
 */
public class FIFO_Benchmark extends ACacheBenchmark {

  /**
   *
   */
  public FIFO_Benchmark() {
    this.clazz = FIFO.class;
  }
}
