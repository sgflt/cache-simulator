package cz.zcu.kiv.cacheSimulator.simulation;

/**
 * @author Lukáš Kvídera
 */
public class Metrics {
  private int cacheHits;
  private int savedBandthwidth;

  public int getCacheHits() {
    return this.cacheHits;
  }

  public int getSavedBandthwidth() {
    return this.savedBandthwidth;
  }

  public void incrementCacheHits() {
    ++this.cacheHits;
  }

  public void incrementSavedBandthwidth(final long fileSize) {
    this.savedBandthwidth += fileSize;
  }
}
