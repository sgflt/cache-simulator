package eu.qwsome.simulator.cache.policy.clock;

import eu.qwsome.simulator.cache.core.FileTraffic;
import eu.qwsome.simulator.cache.core.SimulationCacheStub;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * @author Lukáš Kvídera
 */
class Clock extends SimulationCacheStub {

  private final List<Entry> data = new ArrayList<>();
  private final long capacity;
  private int index;
  private long used;

  Clock(final long capacity) {
    this.capacity = capacity;
  }

  @Override
  public FileTraffic get(final String fileName) {
    for (final var entry : this.data) {
      if (entry.file.getFileName().equalsIgnoreCase(fileName)) {
        entry.referenced();
        return entry.file;
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

    this.data.add(this.index, new Entry(value));
    this.index = (this.index + 1);
    this.used += value.getFileSize();
  }

  private void removeFile() {
    this.index = this.index % this.data.size();
    var entry = this.data.get(this.index);

    while (entry.referenced) {
      entry.clocked();
      this.index = (this.index + 1) % this.data.size();
      entry = this.data.get(this.index);
    }

    this.data.remove(entry);
    this.used -= entry.file.getFileSize();
  }

  @Override
  public long getCapacity() {
    return this.capacity;
  }


  private static class Entry {
    private final FileTraffic file;
    private boolean referenced = true;

    Entry(final FileTraffic file) {
      this.file = Objects.requireNonNull(file);
    }

    void referenced() {
      this.referenced = true;
    }

    void clocked() {
      this.referenced = false;
    }
  }
}
