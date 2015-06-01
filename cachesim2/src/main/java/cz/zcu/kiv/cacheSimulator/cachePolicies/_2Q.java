package cz.zcu.kiv.cacheSimulator.cachePolicies;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.function.Predicate;

import cz.zcu.kiv.cacheSimulator.shared.GlobalVariables;
import cz.zcu.kiv.cacheSimulator.simulation.FileOnClient;


/**
 * trida pro prezentaci cache algoritmu 2Q
 * class for 2Q algorithm
 *
 * SOURCE: Adapted from article "2Q: A Low Overhead High Performance Buffer Management Replacement Algorithm",
 * by Theodore Johnson, Dennis  Shasha
 *
 * @author Pavel Bzoch
 *
 */
public class _2Q implements ICache {

  /**
   * prvni fronta pro jen jednou referencovane soubory
   */
  private final Queue<FileOnClient> fQueueFIFO;

  /**
   * LRU fronta pro vicekrat referencovane soubory
   */
  private final Queue<FileOnClient> fQueueLRU;

  /**
   * struktura pro ukladani souboru, ktere jsou vetsi nez cache
   */
  private final List<FileOnClient> fOverCapacity;

  private long used = 0;

  /**
   * velikost cache v B
   */
  private long capacity = 0;

  /**
   * Cache capacity
   */
  private long initialCapacity = 0;

  /**
   * konstanta pro urceni, jak ma byt velka fifo pamet (v % velikosti cache)
   */
  private static double FIFO_CAPACITY = 0.50f;

  public _2Q() {
    this.fQueueFIFO = new LinkedList<>();
    this.fQueueLRU = new LinkedList<>();
    this.fOverCapacity = new LinkedList<>();
  }

  @Override
  public boolean contains(final String fName) {
    final Predicate<FileOnClient> predicate = f -> f.getFileName().equalsIgnoreCase(fName);

    if (this.fQueueFIFO.stream().anyMatch(predicate))
      return true;

    return this.fQueueLRU.stream().anyMatch(predicate);
  }

  @Override
  public FileOnClient getFile(final String fName) {
    for (final Iterator<FileOnClient> it = this.fQueueFIFO.iterator(); it.hasNext(); ) {
      final FileOnClient f = it.next();
      if (f.getFileName().equalsIgnoreCase(fName)) {
        it.remove();
        this.fQueueLRU.add(f);
        return f;
      }
    }

    for (final Iterator<FileOnClient> it = this.fQueueLRU.iterator(); it.hasNext(); ) {
      final FileOnClient f = it.next();
      if (f.getFileName().equalsIgnoreCase(fName)) {
        it.remove();
        this.fQueueLRU.add(f);
        break;
      }
    }

    return null;
  }

  @Override
  public long freeCapacity() {
    return this.capacity - this.used;
  }

  @Override
  public void removeFile() {
    if (!this.fQueueFIFO.isEmpty()) {
      this.used -= this.fQueueFIFO.remove().getFileSize();
    } else if (!this.fQueueLRU.isEmpty()) {
      this.used -= this.fQueueLRU.remove().getFileSize();
    }
  }

  @Override
  public void insertFile(final FileOnClient f) {
    // napred zkontrolujeme, jestli se soubor vejde do cache
    // pokud se nevejde, vztvorime pro nej okenko
    if (f.getFileSize() > this.capacity) {
      if (!this.fOverCapacity.isEmpty()) {
        this.fOverCapacity.add(f);
        return;
      }

      /* create window for download */
      while (this.freeCapacity() < (long) (this.capacity * GlobalVariables
          .getCacheCapacityForDownloadWindow())) {
        this.removeFile();
      }

      this.fOverCapacity.add(f);
      this.capacity = (long) (this.capacity * (1 - GlobalVariables
          .getCacheCapacityForDownloadWindow()));
      return;
    }

    if (!this.fOverCapacity.isEmpty())
      this.checkTimes();

    // pokud se soubor vejde, fungujeme spravne
    while (this.freeCapacity() < f.getFileSize()) {
      this.removeFile();
    }

    long fifoSize = this.fQueueFIFO.stream().mapToLong(file -> file.getFileSize()).sum();

    while (fifoSize > (int) (FIFO_CAPACITY * this.capacity)) {
      final long sizeRemoved = this.fQueueFIFO.remove().getFileSize();
      fifoSize -= sizeRemoved;
      this.used -= sizeRemoved;
    }

    this.fQueueFIFO.add(f);
    this.used += f.getFileSize();
  }

  /**
   * metoda pro kontrolu, zda jiz nejsou soubory s vetsi velikosti nez cache
   * stazene - pak odstranime okenko
   */
  private void checkTimes() {
    boolean hasBeenRemoved = true;
    while (hasBeenRemoved) {
      hasBeenRemoved = false;
      if (!this.fOverCapacity.isEmpty()
          && this.fOverCapacity.get(0).getFRemoveTime() < GlobalVariables
              .getActualTime()) {
        this.fOverCapacity.remove(0);
        hasBeenRemoved = true;
      }
    }
    if (this.fOverCapacity.isEmpty()) {
      this.capacity = this.initialCapacity;
    }
  }

  @Override
  public String toString() {
    return "2Q";
  }

  @Override
  public boolean needServerStatistics() {
    return false;
  }

  @Override
  public void setCapacity(final long capacity) {
    this.capacity = capacity;
    this.initialCapacity = capacity;

  }

  @Override
  public void reset() {
    this.fQueueFIFO.clear();
    this.fQueueLRU.clear();
    this.fOverCapacity.clear();
  }

  @Override
  public String cacheInfo() {
    return "_2Q;2 Queues";
  }

  public static double getFIFO_CAPACITY() {
    return FIFO_CAPACITY;
  }

  public static void setFIFO_CAPACITY(final double FIFO_CAPACITY) {
    _2Q.FIFO_CAPACITY = FIFO_CAPACITY;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (int) (this.initialCapacity ^ (this.initialCapacity >>> 32));
    result = prime * result + ((this.toString() == null) ? 0 : this.toString().hashCode());
    return result;
  }

  @Override
  public void removeFile(final FileOnClient f) {
    if (this.fQueueFIFO.contains(f)) {
      this.fQueueFIFO.remove(f);
      this.used -= f.getFileSize();
    }

    if (this.fQueueLRU.contains(f)) {
      this.fQueueLRU.remove(f);
      this.used -= f.getFileSize();
    }
  }

  @Override
  public List<FileOnClient> getCachedFiles() {
    final List<FileOnClient> list = new ArrayList<>(this.fQueueFIFO.size() + this.fQueueLRU.size());
    list.addAll(this.fQueueFIFO);
    list.addAll(this.fQueueLRU);
    return list;
  }

  @Override
  public long getCapacity() {
    return this.initialCapacity;
  }
}
