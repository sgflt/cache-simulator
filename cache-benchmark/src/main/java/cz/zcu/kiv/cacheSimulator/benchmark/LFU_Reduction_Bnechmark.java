package cz.zcu.kiv.cacheSimulator.benchmark;

import cz.zcu.kiv.cacheSimulator.cachePolicies.LFU_REDUCTION;


/**
 * LFU_Reduction_Bnechmark.java
 *    2. 6. 2015
 * @author Lukáš Kvídera
 * @version 0.0
 */
public class LFU_Reduction_Bnechmark extends ACacheBenchmark {

  public LFU_Reduction_Bnechmark() {
    this.clazz = LFU_REDUCTION.class;
  }
}
