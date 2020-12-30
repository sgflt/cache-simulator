package eu.qwsome.simulator.cache.policy.fifo;

import eu.qwsome.simulator.cache.core.FileTraffic;
import eu.qwsome.simulator.cache.core.SimulationCacheStub;

import java.util.LinkedList;
import java.util.Queue;


/**
 * @author Lukáš Kvídera
 */
class FIFO extends SimulationCacheStub {

  private final Queue<FileTraffic> queue = new LinkedList<>();


  /**
   * cache size in bytes
   */
  private final long capacity;
  private long used;

  FIFO(final long capacity) {
    this.capacity = capacity;
  }

  @Override
  public FileTraffic get(final String key) {
    for (final var file : this.queue) {
      if (file.getFileName().equalsIgnoreCase(key)) {
        return file;
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
      final var removed = this.queue.remove();
      this.used -= removed.getFileSize();
    }

    this.used += value.getFileSize();
    this.queue.add(value);
  }

  @Override
  public long getCapacity() {
    return this.capacity;
  }
}
