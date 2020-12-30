package eu.qwsome.simulator.cache.policy.arc;

import eu.qwsome.simulator.cache.core.FileTraffic;
import eu.qwsome.simulator.cache.core.SimulationCacheStub;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Lukáš Kvídera
 */
class ARC extends SimulationCacheStub {

  private final List<FileTraffic> B1 = new ArrayList<>();
  private final List<FileTraffic> T1 = new ArrayList<>();
  private final List<FileTraffic> T2 = new ArrayList<>();
  private final List<FileTraffic> B2 = new ArrayList<>();
  private final long capacity;
  private long p = 0L;
  private long B1used;
  private long T1used;
  private long T2used;
  private long B2used;

  ARC(final long capacity) {
    this.capacity = capacity;
  }

  @Override
  public FileTraffic get(final String fileName) {
    FileTraffic cachedFile = null;
    for (final var file : this.T1) {
      if (file.getFileName().equalsIgnoreCase(fileName)) {
        cachedFile = file;
        break;
      }
    }

    if (cachedFile != null) {
      this.T1.remove(cachedFile);
      this.T2.add(cachedFile);
      this.T1used -= cachedFile.getFileSize();
      this.T2used += cachedFile.getFileSize();
    } else {
      for (final var file : this.T2) {
        if (file.getFileName().equalsIgnoreCase(fileName)) {
          cachedFile = file;
          break;
        }
      }

      if (cachedFile != null) {
        this.T2.remove(cachedFile);
        this.T2.add(cachedFile);
      }
    }

    return cachedFile;
  }

  @Override
  public void put(final String key, final FileTraffic value) {
    if (value.getFileSize() > this.capacity) {
      return;
    }

    if (this.B1.contains(value)) {
      onB1Hit(value);
    } else if (this.B2.contains(value)) {
      onB2Hit(value);
    } else {
      onMiss(value);
    }
  }

  private void onMiss(final FileTraffic value) {
    if (getL1UsedBytes() == this.capacity) {
      if (this.T1used < this.capacity) {
        final var B1removedFile = this.B1.remove(0);
        this.B1used -= B1removedFile.getFileSize();
        replace(this.p, false);
      } else {
        final var T1removedFile = this.T1.remove(0);
        this.T1used -= T1removedFile.getFileSize();
      }
    } else if (getL1UsedBytes() < this.capacity && getUsedBytes() >= this.capacity) {
      if (getUsedBytes() == 2 * this.capacity) {
        final var B2removedFile = this.B2.remove(0);
        this.B2used -= B2removedFile.getFileSize();
      }
      replace(this.p, false);
    }

    this.T1.add(value);
    this.T1used += value.getFileSize();
  }

  private void onB1Hit(final FileTraffic value) {
    this.p = Math.min(this.capacity, this.p + Math.max(this.B2used / this.B1used, 1L));
    replace(this.p, false);

    insertIntoT2(value);
  }

  private void onB2Hit(final FileTraffic value) {
    this.p = Math.max(0, this.p - Math.max(this.B1used / this.B2used, 1L));
    replace(this.p, true);

    insertIntoT2(value);

  }

  private void insertIntoT2(final FileTraffic value) {
    this.T2.add(value);
    this.T2used += value.getFileSize();
  }

  private long getL1UsedBytes() {
    return this.T1used + this.B1used;
  }

  private long getUsedBytes() {
    return getL1UsedBytes() + this.T2used + this.B2used;
  }

  private void replace(final long p, final boolean inB2) {
    if (!this.T1.isEmpty() && ((inB2 && this.T1used == p) || this.T1used > p)) {
      final var T1removedFile = this.T1.remove(0);
      this.B1.add(T1removedFile);
      this.T1used -= T1removedFile.getFileSize();
      this.B1used += T1removedFile.getFileSize();
    } else {
      final var T2removedFile = this.T2.remove(0);
      this.B2.add(T2removedFile);
      this.T2used -= T2removedFile.getFileSize();
      this.B2used += T2removedFile.getFileSize();
    }
  }

  @Override
  public long getCapacity() {
    return this.capacity;
  }
}
