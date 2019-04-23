package cz.zcu.kiv.cacheSimulator.cachePolicies;

import cz.zcu.kiv.cacheSimulator.shared.FileOnClient;
import cz.zcu.kiv.cacheSimulator.shared.GlobalVariables;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;


/**
 * trida pro prezentaci cache algoritmu 2Q
 * class for 2Q algorithm
 * <p>
 * SOURCE: Adapted from article "2Q: A Low Overhead High Performance Buffer Management Replacement Algorithm",
 * by Theodore Johnson, Dennis  Shasha
 *
 * @author Pavel Bzoch
 */
public class _2Q implements ICache {

  /**
   * A queue for files that are accessed for the first time.
   */
  private final Queue<FileOnClient> fQueueFIFO;

  /**
   * LRU queue for files that are accessed repeatedly.
   */
  private final Queue<FileOnClient> fQueueLRU;

  /**
   * struktura pro ukladani souboru, ktere jsou vetsi nez cache
   */
  private final ArrayList<FileOnClient> fOverCapacity;

  /**
   * How many bytes we can use for caching
   */
  private long capacity;

  /**
   * How many bytes is used by cached files
   */
  private long usedCapacity;

  /**
   * konstanta pro urceni, jak ma byt velka fifo pamet (v % velikosti cache)
   */
  private static double FIFO_CAPACITY = 0.50f;

  public _2Q() {
    this.capacity = GlobalVariables.getCacheCapacity();
    this.fQueueFIFO = new LinkedList<>();
    this.fQueueLRU = new LinkedList<>();
    this.fOverCapacity = new ArrayList<>();
  }

  @Override
  public boolean contains(final String fileName) {
    for (final FileOnClient f : this.fQueueFIFO) {
      if (f.getFileName().equalsIgnoreCase(fileName)) {
        return true;
      }
    }
    for (final FileOnClient f : this.fQueueLRU) {
      if (f.getFileName().equalsIgnoreCase(fileName)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public FileOnClient get(final String fileName) {
    for (final var it = this.fQueueFIFO.iterator(); it.hasNext(); ) {
      final FileOnClient cachedFile = it.next();
      if (cachedFile.getFileName().equalsIgnoreCase(fileName)) {
        it.remove();
        this.fQueueLRU.add(cachedFile);
        return cachedFile;
      }
    }

    for (final var it = this.fQueueLRU.iterator(); it.hasNext(); ) {
      final FileOnClient cachedFile = it.next();
      if (cachedFile.getFileName().equalsIgnoreCase(fileName)) {
        it.remove();
        this.fQueueLRU.add(cachedFile);
        return cachedFile;
      }
    }
    return null;
  }

  @Override
  public long freeCapacity() {
    return this.capacity - this.usedCapacity;
  }

  @Override
  public void removeFile() {
    if (!this.fQueueFIFO.isEmpty()) {
      recalculateUsedCapacity(this.fQueueFIFO.remove());
    } else if (!this.fQueueLRU.isEmpty()) {
      recalculateUsedCapacity(this.fQueueLRU.remove());
    }
  }

  private void recalculateUsedCapacity(final FileOnClient removedFile) {
    this.usedCapacity -= removedFile.getFileSize();
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
      while (freeCapacity() < (long) ((double) this.capacity * GlobalVariables
        .getCacheCapacityForDownloadWindow())) {
        removeFile();
      }
      this.fOverCapacity.add(f);
      this.capacity = (long) ((double) this.capacity * (1 - GlobalVariables
        .getCacheCapacityForDownloadWindow()));
      return;
    }

    if (!this.fOverCapacity.isEmpty()) {
      checkTimes();
    }

    // pokud se soubor vejde, fungujeme spravne
    while (freeCapacity() < f.getFileSize()) {
      removeFile();
    }

    long fifoSize = this.fQueueFIFO.stream().mapToLong(FileOnClient::getFileSize).sum();
    while (fifoSize > (int) (FIFO_CAPACITY * (double) this.capacity)) {
      final FileOnClient removedFile = this.fQueueFIFO.remove();
      fifoSize -= removedFile.getFileSize();
      recalculateUsedCapacity(removedFile);
    }
    this.fQueueFIFO.add(f);
    this.usedCapacity += f.getFileSize();
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
      this.capacity = GlobalVariables.getCacheCapacity();
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
    result = prime * result + (int) (this.capacity ^ (this.capacity >>> 32));
    result = prime * result + ((toString() == null) ? 0 : toString().hashCode());
    return result;
  }
}
