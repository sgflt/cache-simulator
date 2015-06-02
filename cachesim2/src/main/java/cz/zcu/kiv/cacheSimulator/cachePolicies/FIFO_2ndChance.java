package cz.zcu.kiv.cacheSimulator.cachePolicies;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import cz.zcu.kiv.cacheSimulator.shared.GlobalVariables;
import cz.zcu.kiv.cacheSimulator.shared.Pair;
import cz.zcu.kiv.cacheSimulator.simulation.FileOnClient;

/**
 * class for FIFO with second chance algorithm
 * SOURCE: Adapted from article "Page Replacement and Reference Bit Emulation in Mach",
 * by R. P. Draves
 *
 * @author Pavel Bžoch
 * @author Lukáš Kvídera
 * @version 2.1
 */
public class FIFO_2ndChance implements ICache {

  /**
   * struktura pro uchovani souboru
   */
  private final Queue<Pair<FileOnClient, Boolean>> fQueue;

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

  private long used;


  /**
   * konstruktor - inicializace cache
   */
  public FIFO_2ndChance() {
    this.fQueue = new LinkedList<>();
    this.fOverCapacity = new ArrayList<>();
  }


  @Override
  public boolean contains(final String fName) {
    for (final Pair<FileOnClient, Boolean> f : this.fQueue) {
      if (f.getFirst().getFileName().equalsIgnoreCase(fName))
        return true;
    }
    return false;
  }


  @Override
  public FileOnClient getFile(final String fName) {
    for (final Pair<FileOnClient, Boolean> f : this.fQueue) {
      if (f.getFirst().getFileName().equalsIgnoreCase(fName)) {
        f.setSecond(true);
        return f.getFirst();
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
    Pair<FileOnClient, Boolean> file = this.fQueue.remove();

    while (file.getSecond() == true) {
      file.setSecond(false);
      this.fQueue.add(file);
      file = this.fQueue.remove();
    }

    this.used -= file.getFirst().getFileSize();
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

    this.fQueue.add(new Pair<>(f, false));
    this.used += f.getFileSize();
  }


  @Override
  public String toString() {
    return "FiFO 2nd chance";
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
    this.fQueue.clear();
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
    return "FIFO_2ndChance;FIFO 2nd chance";
  }


  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (int) (this.initialCapacity ^ (this.initialCapacity >>> 32));
    result = prime * result + this.toString().hashCode();
    return result;
  }


  @Override
  public void removeFile(final FileOnClient f) {
    for (final Pair<FileOnClient, Boolean> file : this.fQueue) {
      if (file.getFirst() == f) {
        this.fQueue.remove(file);
        this.used -= file.getFirst().getFileSize();
        break;
      }
    }
  }


  @Override
  public List<FileOnClient> getCachedFiles() {
    final List<FileOnClient> list = new ArrayList<>(this.fQueue.size());
    for (final Pair<FileOnClient, Boolean> file : this.fQueue) {
      list.add(file.getFirst());
    }
    return list;
  }


  @Override
  public long getCapacity() {
    return this.initialCapacity;
  }
}
