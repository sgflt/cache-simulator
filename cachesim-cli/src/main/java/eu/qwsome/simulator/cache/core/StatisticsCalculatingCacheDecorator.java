package eu.qwsome.simulator.cache.core;

/**
 * @author Lukáš Kvídera
 */
class StatisticsCalculatingCacheDecorator extends SimulationCacheStub {

  private final SimulationCacheStub delegate;

  private long hit;
  private long miss;
  /**
   * in bytes
   */
  private long savedTraffic;

  StatisticsCalculatingCacheDecorator(final SimulationCacheStub delegate) {
    this.delegate = delegate;
  }

  @Override
  public FileTraffic get(final String key) {
    final var fileTraffic = this.delegate.get(key);
    if (fileTraffic == null) {
      ++this.miss;
    } else {
      ++this.hit;
      this.savedTraffic += fileTraffic.getFileSize();
    }
    return fileTraffic;
  }

  @Override
  public void put(final String key, final FileTraffic value) {
    this.delegate.put(key, value);
  }

  @Override
  public long getCapacity() {
    return this.delegate.getCapacity();
  }

  @Override
  public boolean remove(final String key) {
    return this.delegate.remove(key);
  }

  @Override
  public String getName() {
    return this.delegate.getClass().getSimpleName();
  }

  long getHit() {
    return this.hit;
  }

  long getMiss() {
    return this.miss;
  }

  long getSavedTraffic() {
    return this.savedTraffic;
  }

  @Override
  public String toString() {
    return new org.apache.commons.lang3.builder.ToStringBuilder(this)
      .append("delegate", this.delegate)
      .append("hit", this.hit)
      .append("miss", this.miss)
      .append("savedTraffic", this.savedTraffic)
      .toString();
  }

  double getHitRatio() {
    return (double) getHit() / (getHit() + getMiss());
  }
}
