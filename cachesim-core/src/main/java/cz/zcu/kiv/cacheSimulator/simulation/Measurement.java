package cz.zcu.kiv.cacheSimulator.simulation;

import cz.zcu.kiv.cacheSimulator.cachePolicies.ICache;

/**
 * @author Lukáš Kvídera
 */
public class Measurement {
  private final ICache cache;
  private final Metrics metrics = new Metrics();

  public Measurement(final ICache cache) {
    this.cache = cache;
  }

  public ICache getCache() {
    return this.cache;
  }

  public Metrics getMetrics() {
    return this.metrics;
  }
}
