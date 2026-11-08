package cz.zcu.kiv.cacheSimulator.shared;

/**
 * @author Lukáš Kvídera
 */
public class LRDMetaData {
  private final FileOnClient fileOnClient;
  private final long counter;
  private long hits;

  /**
   * Determines how often was file accessed between creation and current time defined by global counter.
   */
  private double referenceDensity = 1.0;

  public LRDMetaData(final FileOnClient fileOnClient, final long counter) {
    this.fileOnClient = fileOnClient;
    this.counter = counter;
  }

  public FileOnClient getFileOnClient() {
    return this.fileOnClient;
  }

  public long getHits() {
    return this.hits;
  }

  public long getCounter() {
    return this.counter;
  }

  public double getReferenceDensity() {
    return this.referenceDensity;
  }

  public void incrementHit() {
    ++this.hits;
  }

  public void recalculateReferenceDensity(final double gc) {
    this.referenceDensity = getHits() / (gc - getCounter());
  }

  public void reduceReferenceDensityBy(final double k1) {
    this.hits = (long) (getHits() / k1);
  }
}
