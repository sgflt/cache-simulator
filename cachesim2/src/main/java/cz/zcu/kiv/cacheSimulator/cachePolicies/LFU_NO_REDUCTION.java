package cz.zcu.kiv.cacheSimulator.cachePolicies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cz.zcu.kiv.cacheSimulator.shared.GlobalVariables;
import cz.zcu.kiv.cacheSimulator.shared.Pair;
import cz.zcu.kiv.cacheSimulator.simulation.FileOnClient;

/**
 * class for LFU with no reduction of references' count algorithm
 * SOURCE: Adapted from article "Analysis of caching algorithms for distributed file systems", by
 * B. Reed and D. D. E. Long
 *
 * @author Pavel Bžoch
 */
public class LFU_NO_REDUCTION implements ICache {

  /**
   * trida pro porovnani prvku
   *
   * @author Pavel Bzoch
   */
  private class PairCompare implements Comparator<Pair<Integer, FileOnClient>> {

    @Override
    public int compare(final Pair<Integer, FileOnClient> o1, final Pair<Integer, FileOnClient> o2) {
      if (o1.getFirst() > o2.getFirst())
        return 1;
      else if (o1.getFirst() < o2.getFirst())
        return -1;
      return 0;
    }
  }

  /**
   * struktura pro uchovani souboru
   */
  private final List<Pair<Integer, FileOnClient>> list;

  /**
   * struktura pro ukladani souboru, ktere jsou vetsi nez cache
   */
  private final List<FileOnClient> fOverCapacity;

  /**
   * velikost cache v kB
   */
  private long capacity = 0;

  /**
   * pocatecni kapacita cache
   */
  private long initialCapacity = 0;

  /**
   * promenne pro urceni, jestli je treba tridit
   */
  private boolean needSort = true;


  /**
   * konstruktor - inicializace cache
   */
  public LFU_NO_REDUCTION() {
    this.list = new ArrayList<>();
    this.fOverCapacity = new ArrayList<>();
  }


  @Override
  public boolean isInCache(final String fName) {
    for (final Pair<Integer, FileOnClient> f : this.list) {
      if (f.getSecond().getFileName().equalsIgnoreCase(fName))
        return true;
    }
    return false;
  }


  @Override
  public FileOnClient getFileFromCache(final String fName) {
    for (final Pair<Integer, FileOnClient> f : this.list) {
      if (f.getSecond().getFileName().equalsIgnoreCase(fName)) {
        f.setFirst(f.getFirst() + 1);
        this.needSort = true;
        return f.getSecond();
      }
    }
    return null;
  }


  @Override
  public long freeCapacity() {
    long obsazeno = 0;
    for (final Pair<Integer, FileOnClient> f : this.list) {
      obsazeno += f.getSecond().getFileSize();
    }
    return this.capacity - obsazeno;
  }


  @Override
  public void removeFile() {
    if (this.needSort) {
      Collections.sort(this.list, new PairCompare());
    }
    this.needSort = false;
    if (this.list.size() > 0)
      this.list.remove(0);
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
    this.list.add(new Pair<>(new Integer(1), f));
    this.needSort = true;
  }


  @Override
  public String toString() {
    return "Standard LFU";
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
    this.needSort = true;
    this.list.clear();
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
    return "LFU_NO_REDUCTION;Standard LFU";
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
    Pair<Integer, FileOnClient> pair = null;
    for (final Pair<Integer, FileOnClient> file : this.list) {
      if (file.getSecond() == f) {
        pair = file;
        break;
      }
    }
    if (pair != null) {
      this.list.remove(pair);
    }
  }


  @Override
  public List<FileOnClient> getCachedFiles() {
    final List<FileOnClient> list = new ArrayList<>(this.list.size());
    for (final Pair<Integer, FileOnClient> file : this.list) {
      list.add(file.getSecond());
    }
    return list;
  }


  @Override
  public long getCacheCapacity() {
    return this.initialCapacity;
  }
}
