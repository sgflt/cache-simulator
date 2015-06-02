package cz.zcu.kiv.cacheSimulator.cachePolicies;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import cz.zcu.kiv.cacheSimulator.shared.GlobalVariables;
import cz.zcu.kiv.cacheSimulator.simulation.FileOnClient;

/**
 * class for LRU algorithm
 * SOURCE: Adapted from article "Analysis of caching algorithms for distributed file systems", by
 * B. Reed and D. D. E. Long
 *
 * @author Pavel Bžoch
 * @author Lukáš Kvídera
 * @version 2.1
 */
public class LRU implements ICache {

  /**
   * struktura pro uchovani souboru
   */
  private final List<FileOnClient> lruQueue;

  /**
   * struktura pro ukladani souboru, ktere jsou vetsi nez cache
   */
  private final List<FileOnClient> fOverCapacity;

  /**
   * velikost cache v B
   */
  private long capacity = 0;

  /**
   * pocatecni kapacita cache
   */
  private long initialCapacity = 0;


  /**
   * konstruktor - inicializace cache
   */
  public LRU() {
    this.lruQueue = new LinkedList<>();
    this.fOverCapacity = new LinkedList<>();
  }


  @Override
  public boolean contains(final String fName) {
    for (final FileOnClient f : this.lruQueue) {
      if (f.getFileName().equalsIgnoreCase(fName))
        return true;
    }
    return false;
  }


  @Override
  public FileOnClient getFile(final String fName) {
    FileOnClient fileForGetting = null;
    for (final FileOnClient f : this.lruQueue) {
      if (f.getFileName().equalsIgnoreCase(fName)) {
        fileForGetting = f;
        break;
      }
    }
    if (fileForGetting == null)
      return null;

    this.lruQueue.remove(fileForGetting);
    this.lruQueue.add(fileForGetting);
    return fileForGetting;
  }


  @Override
  public long freeCapacity() {
    long obsazeno = 0;
    for (final FileOnClient f : this.lruQueue) {
      obsazeno += f.getFileSize();
    }
    return this.capacity - obsazeno;
  }


  @Override
  public void removeFile() {
    this.lruQueue.remove(0);
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
    this.lruQueue.add(f);
  }


  @Override
  public String toString() {
    return "LRU";
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
    this.lruQueue.clear();
    this.fOverCapacity.clear();
  }


  /**
   * metoda pro kontrolu, zda jiz nejsou soubory s vetsi velikosti nez cache stazene - pak
   * odstranime okenko
   */
  private void checkTimes() {
    boolean hasBeenRemoved = true;
    while (hasBeenRemoved) {
      hasBeenRemoved = false;
      if (!this.fOverCapacity.isEmpty()
          && this.fOverCapacity.get(0).getFRemoveTime() < GlobalVariables.getActualTime()) {
        this.fOverCapacity.remove(0);
        hasBeenRemoved = true;
      }
    }
    if (this.fOverCapacity.isEmpty()) {
      this.capacity = this.initialCapacity;
    }
  }


  @Override
  public String cacheInfo() {
    return "LRU;LRU";
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
    this.lruQueue.remove(f);
  }


  @Override
  public List<FileOnClient> getCachedFiles() {
    final List<FileOnClient> list = new ArrayList<>(this.lruQueue.size());
    this.lruQueue.addAll(this.lruQueue);
    return list;
  }


  @Override
  public long getCapacity() {
    return this.initialCapacity;
  }
}
