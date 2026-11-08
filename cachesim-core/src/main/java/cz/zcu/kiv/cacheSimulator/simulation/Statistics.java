package cz.zcu.kiv.cacheSimulator.simulation;

import cz.zcu.kiv.cacheSimulator.cachePolicies.ICache;

/**
 * @author Lukáš Kvídera
 */
public class Statistics {
  private final long cacheHit;
  private final long savedTraffic;
  private final double hitRatio;
  private final double savedTrafRatio;
  private final ICache cache;

  public Statistics(final ICache cache, final long cacheHit, final long savedTraffic, final double hitRatio, final double savedTrafRatio) {
    this.cache = cache;
    this.cacheHit = cacheHit;
    this.savedTraffic = savedTraffic;
    this.hitRatio = hitRatio;
    this.savedTrafRatio = savedTrafRatio;
  }

  public long getCacheHit() {
    return this.cacheHit;
  }

  public long getSavedTraffic() {
    return this.savedTraffic;
  }

  public double getHitRatio() {
    return this.hitRatio;
  }

  public double getSavedTrafRatio() {
    return this.savedTrafRatio;
  }

  public ICache getCache() {
    return this.cache;
  }
}
