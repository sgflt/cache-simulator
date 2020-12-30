package eu.qwsome.simulator.cache.policy.lru;

import eu.qwsome.simulator.cache.core.FileTraffic;
import eu.qwsome.simulator.cache.core.SimulationCacheStub;

import java.util.LinkedList;
import java.util.Queue;


/**
 * @author Lukáš Kvídera
 */
public class LRU extends SimulationCacheStub {

  private final Queue<FileTraffic> queue = new LinkedList<>();

  /**
   * cache size in bytes
   */
  private final long capacity;
  private long used;

  public LRU(final long capacity) {
    this.capacity = capacity;
  }

  @Override
  public FileTraffic get(final String fileName) {
    FileTraffic cachedFile = null;
    for (final var file : this.queue) {
      if (file.getFileName().equalsIgnoreCase(fileName)) {
        cachedFile = file;
        break;
      }
    }

    if (cachedFile == null) {
      return null;
    }

    this.queue.remove(cachedFile);
    this.queue.add(cachedFile);
    return cachedFile;
  }

  @Override
  public void put(final String key, final FileTraffic value) {
    if (value.getFileSize() > this.capacity) {
      return;
    }

    while (value.getFileSize() + this.used > this.capacity) {
      final var removedFile = this.queue.remove();
      this.used -= removedFile.getFileSize();
    }

    this.used += value.getFileSize();
    this.queue.add(value);
  }

  @Override
  public long getCapacity() {
    return this.capacity;
  }
}
