package eu.qwsome.simulator.cache.core;

import javax.cache.Cache;

/**
 * @author Lukáš Kvídera
 */
class StatisticsCalculatingCacheDecorator extends SimulationCacheStub implements Cache<String, FileTraffic> {

  private final Cache<String, FileTraffic> delegate;

  private long hit;
  private long miss;
  /**
   * in bytes
   */
  private long savedTraffic;

  StatisticsCalculatingCacheDecorator(final Cache<String, FileTraffic> delegate) {
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
  public boolean remove(final String key) {
    return this.delegate.remove(key);
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
}
