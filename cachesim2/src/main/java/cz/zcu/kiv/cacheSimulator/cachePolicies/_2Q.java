package cz.zcu.kiv.cacheSimulator.cachePolicies;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

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
  private final ArrayList<FileOnClient> fOverCapacity;

  /**
   * velikost cache v B
   */
  private long capacity = 0;

  /**
   * pocatecni kapacita cache
   */
  private long initialCapacity = 0;

  /**
   * konstanta pro urceni, jak ma byt velka fifo pamet (v % velikosti cache)
   */
  private static double FIFO_CAPACITY = 0.50f;

  public _2Q() {
    super();
    this.fQueueFIFO = new LinkedList<>();
    this.fQueueLRU = new LinkedList<>();
    this.fOverCapacity = new ArrayList<>();
  }

  @Override
  public boolean contains(final String fName) {
    for (final FileOnClient f : this.fQueueFIFO) {
      if (f.getFileName().equalsIgnoreCase(fName))
        return true;
    }
    for (final FileOnClient f : this.fQueueLRU) {
      if (f.getFileName().equalsIgnoreCase(fName))
        return true;
    }
    return false;
  }

  @Override
  public FileOnClient getFile(final String fName) {
    FileOnClient fromCache = null;
    for (final FileOnClient f : this.fQueueFIFO) {
      if (f.getFileName().equalsIgnoreCase(fName)) {
        fromCache = f;
        break;
      }
    }

    if (fromCache != null) {
      this.fQueueFIFO.remove(fromCache);
      this.fQueueLRU.add(fromCache);
      return fromCache;
    }

    for (final FileOnClient f : this.fQueueLRU) {
      if (f.getFileName().equalsIgnoreCase(fName)) {
        fromCache = f;
        break;
      }
    }

    if (fromCache != null) {
      this.fQueueLRU.remove(fromCache);
      this.fQueueLRU.add(fromCache);
      return fromCache;
    }

    return null;
  }

  @Override
  public long freeCapacity() {
    long obsazeno = 0;
    for (final FileOnClient f : this.fQueueFIFO) {
      obsazeno += f.getFileSize();
    }
    for (final FileOnClient f : this.fQueueLRU) {
      obsazeno += f.getFileSize();
    }
    return this.capacity - obsazeno;
  }

  @Override
  public void removeFile() {
    if (!this.fQueueFIFO.isEmpty())
      this.fQueueFIFO.remove();
    else {
      if (!this.fQueueLRU.isEmpty())
        this.fQueueLRU.remove();
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
      while (this.freeCapacity() < (long) (this.capacity * GlobalVariables
          .getCacheCapacityForDownloadWindow()))
        this.removeFile();
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
    long fifoSize = 0;
    for (final FileOnClient fifo : this.fQueueFIFO) {
      fifoSize += fifo.getFileSize();
    }
    while (fifoSize > (int) (FIFO_CAPACITY * this.capacity)) {
      fifoSize -= this.fQueueFIFO.remove().getFileSize();
    }
    this.fQueueFIFO.add(f);

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
    if (this.fQueueFIFO.contains(f))
      this.fQueueFIFO.remove(f);
    if (this.fQueueLRU.contains(f))
      this.fQueueLRU.remove(f);
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
