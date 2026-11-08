package cz.zcu.kiv.cacheSimulator.simulation;

/**
 * @author Lukáš Kvídera
 */
public class Metrics {
  private int cacheHits;
  private long savedBandthwidth;

  public int getCacheHits() {
    return this.cacheHits;
  }

  public long getSavedBandthwidth() {
    return this.savedBandthwidth;
  }

  public void incrementCacheHits() {
    ++this.cacheHits;
  }

  public void incrementSavedBandthwidth(final long fileSize) {
    this.savedBandthwidth += fileSize;
  }
}
