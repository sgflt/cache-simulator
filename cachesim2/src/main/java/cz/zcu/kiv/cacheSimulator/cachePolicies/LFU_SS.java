package cz.zcu.kiv.cacheSimulator.cachePolicies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import cz.zcu.kiv.cacheSimulator.server.Server;
import cz.zcu.kiv.cacheSimulator.shared.GlobalVariables;
import cz.zcu.kiv.cacheSimulator.shared.Pair;
import cz.zcu.kiv.cacheSimulator.simulation.FileOnClient;

/**
 * class for LFU-SS algorithm
 *
 * @author Pavel Bžoch
 */
public class LFU_SS implements ICache {

  /**
   * trida pro porovnani prvku
   *
   * @author Pavel Bzoch
   */
  protected class PairCompare implements Comparator<Pair<Double, FileOnClient>> {

    @Override
    public int compare(final Pair<Double, FileOnClient> o1, final Pair<Double, FileOnClient> o2) {
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
  protected List<Pair<Double, FileOnClient>> list;

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
  protected boolean needSort = true;

  /**
   * poromenna pro urceni globalniho poctu hitu na cteni
   */
  protected long globalReadCount = Long.MAX_VALUE;

  /**
   * promenna pro urceni poctu pristupu do cache a pro aktualizaci globalnich statistik
   */
  private long accessCount = 0;

  /**
   * promenna pro uchovani odkazu na server
   */
  private final Server server = Server.getInstance();


  /**
   * konstruktor - inicializace cache
   */
  public LFU_SS() {
    this.list = new LinkedList<Pair<Double, FileOnClient>>();
    this.fOverCapacity = new LinkedList<FileOnClient>();
  }


  @Override
  public boolean isInCache(final String fName) {
    for (final Pair<Double, FileOnClient> f : this.list) {
      if (f.getSecond().getFileName().equalsIgnoreCase(fName))
        return true;
    }
    return false;
  }


  @Override
  public FileOnClient getFileFromCache(final String fName) {
    this.accessCount++;
    if (this.accessCount % 20 == 0) {
      this.setGlobalReadCountServer(this.server.getGlobalReadHits(this));
    }

    for (final Pair<Double, FileOnClient> pair : this.list) {
      if (pair.getSecond().getFileName().equalsIgnoreCase(fName)) {
        pair.setFirst(pair.getFirst() + 1.0);
        this.needSort = true;
        return pair.getSecond();
      }
    }
    return null;
  }


  @Override
  public long freeCapacity() {
    long obsazeno = 0;
    for (final Pair<Double, FileOnClient> f : this.list) {
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

    if (this.list.size() > 2)
      if ((this.list.get(this.list.size() - 1)).getFirst() > 15) {
        for (final Pair<Double, FileOnClient> f : this.list) {
          f.setFirst(f.getFirst() / 2);
        }
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

    double localReadCount = 0;
    for (final Pair<Double, FileOnClient> files : this.list) {
      localReadCount += files.getFirst();
    }

    final double readHits = ((double) f.getReadHit() - (double) f.getWriteHit())
        / this.globalReadCount * localReadCount + 1.0;

    this.list.add(new Pair<Double, FileOnClient>(new Double(readHits), f));
    this.needSort = true;
  }


  /**
   * metoda pro nastaveni poctu globalnich hitu
   *
   * @param readCount
   *          pocet hitu
   */
  private void setGlobalReadCountServer(final long readCount) {
    this.globalReadCount = readCount;
  }


  @Override
  public String toString() {
    return "LFU-SS";
  }


  @Override
  public boolean needServerStatistics() {
    return true;
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
    this.globalReadCount = Long.MAX_VALUE;
    this.accessCount = 0;
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
    return "LFU_SS;LFU-SS";
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
    Pair<Double, FileOnClient> pair = null;
    for (final Pair<Double, FileOnClient> file : this.list) {
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
    final List<FileOnClient> list = new ArrayList<FileOnClient>(this.list.size());
    for (final Pair<Double, FileOnClient> file : this.list) {
      list.add(file.getSecond());
    }
    return list;
  }


  @Override
  public long getCacheCapacity() {
    return this.initialCapacity;
  }
}
