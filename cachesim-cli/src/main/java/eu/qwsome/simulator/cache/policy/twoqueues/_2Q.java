package eu.qwsome.simulator.cache.policy.twoqueues;

import eu.qwsome.simulator.cache.core.FileTraffic;
import eu.qwsome.simulator.cache.core.SimulationCacheStub;

import java.util.LinkedList;
import java.util.Queue;


/**
 * @author Lukáš Kvídera
 */
class _2Q extends SimulationCacheStub {

  /**
   * A queue for files that are accessed for the first time.
   */
  private final Queue<FileTraffic> fifo = new LinkedList<>();

  /**
   * LRU queue for files that are accessed repeatedly.
   */
  private final Queue<FileTraffic> lru = new LinkedList<>();

  /**
   * How many bytes we can use for caching
   */
  private final long capacity;
  /**
   * FIFO cache coefficient
   */
  private final double fifoRatio;
  /**
   * How many bytes is used by cached files
   */
  private long used;

  _2Q(final long capacity, final double fifoRatio) {
    this.capacity = capacity;
    this.fifoRatio = fifoRatio;
  }

  @Override
  public FileTraffic get(final String fileName) {
    for (final var it = this.lru.iterator(); it.hasNext(); ) {
      final var cachedFile = it.next();
      if (cachedFile.getFileName().equalsIgnoreCase(fileName)) {
        it.remove();
        this.lru.add(cachedFile);
        return cachedFile;
      }
    }

    for (final var it = this.fifo.iterator(); it.hasNext(); ) {
      final var cachedFile = it.next();
      if (cachedFile.getFileName().equalsIgnoreCase(fileName)) {
        it.remove();
        this.lru.add(cachedFile);
        return cachedFile;
      }
    }

    return null;
  }

  @Override
  public void put(final String key, final FileTraffic value) {
    if (value.getFileSize() > this.capacity) {
      return;
    }

    while (value.getFileSize() + this.used > this.capacity) {
      removeFile();
    }

    rebalanceQueues();

    this.fifo.add(value);
    this.used += value.getFileSize();
  }

  private void removeFile() {
    if (!this.fifo.isEmpty()) {
      recalculateUsedCapacity(this.fifo.remove());
    } else if (!this.lru.isEmpty()) {
      recalculateUsedCapacity(this.lru.remove());
    }
  }

  private void recalculateUsedCapacity(final FileTraffic removedFile) {
    this.used -= removedFile.getFileSize();
  }

  private void rebalanceQueues() {
    long fifoSize = this.fifo.stream().mapToLong(FileTraffic::getFileSize).sum();
    while (fifoSize > (long) (this.fifoRatio * this.capacity)) {
      final var removedFile = this.fifo.remove();
      fifoSize -= removedFile.getFileSize();
      recalculateUsedCapacity(removedFile);
    }
  }

  @Override
  public long getCapacity() {
    return this.capacity;
  }
}
