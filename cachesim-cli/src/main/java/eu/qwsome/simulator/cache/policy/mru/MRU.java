package eu.qwsome.simulator.cache.policy.mru;

import eu.qwsome.simulator.cache.core.FileTraffic;
import eu.qwsome.simulator.cache.core.SimulationCacheStub;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Lukáš Kvídera
 */
class MRU extends SimulationCacheStub {

  private final List<FileTraffic> queue = new ArrayList<>();

  private final long capacity;
  private long used;

  /**
   * konstruktor - inicializace cache
   */
  MRU(final long capacity) {
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
    if (value.getFileSize() > capacity) {
      return;
    }
    
    while (value.getFileSize() + this.used > this.capacity) {
      removeFile();
    }

    this.used += value.getFileSize();
    this.queue.add(value);
  }

  private void removeFile() {
    final var removedFile = this.queue.remove(this.queue.size() - 1);
    this.used -= removedFile.getFileSize();
  }

  @Override
  public long getCapacity() {
    return this.capacity;
  }
}
