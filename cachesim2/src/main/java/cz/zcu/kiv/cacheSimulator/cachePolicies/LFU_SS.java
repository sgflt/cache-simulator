package cz.zcu.kiv.cacheSimulator.cachePolicies;

import cz.zcu.kiv.cacheSimulator.server.Server;
import cz.zcu.kiv.cacheSimulator.shared.FileOnClient;
import cz.zcu.kiv.cacheSimulator.shared.GlobalVariables;
import cz.zcu.kiv.cacheSimulator.shared.Pair;

import java.util.ArrayList;
import java.util.Comparator;


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
  private class PairCompare implements Comparator<Pair<Double, FileOnClient>> {

    @Override
    public int compare(final Pair<Double, FileOnClient> o1, final Pair<Double, FileOnClient> o2) {
      if (o1.getFirst() > o2.getFirst()) {
        return 1;
      } else if (o1.getFirst() < o2.getFirst()) {
        return -1;
      }
      return 0;
    }
  }

  /**
   * struktura pro uchovani souboru
   */
  private final ArrayList<Pair<Double, FileOnClient>> list;

  /**
   * struktura pro ukladani souboru, ktere jsou vetsi nez cache
   */
  private final ArrayList<FileOnClient> fOverCapacity;


  /**
   * velikost cache v kB
   */
  private long capacity;

  /**
   * promenne pro urceni, jestli je treba tridit
   */
  private boolean needSort = true;

  /**
   * poromenna pro urceni globalniho poctu hitu na cteni
   */
  private long globalReadCount = Long.MAX_VALUE;

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
    this.list = new ArrayList<>();
    this.capacity = GlobalVariables.getCacheCapacity();
    this.fOverCapacity = new ArrayList<>();
  }

  @Override
  public FileOnClient get(final String fileName) {
    final Pair<Double, FileOnClient> pair;
    for (final Pair<Double, FileOnClient> f : this.list) {
      if (f.getSecond().getFileName().equalsIgnoreCase(fileName)) {
        pair = f;
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
      this.list.sort(new PairCompare());
    }
    this.needSort = false;
    if (!this.list.isEmpty()) {
      this.list.remove(0);
    }

    if (this.list.size() > 2 && this.list.get(this.list.size() - 1).getFirst() > 15) {
      for (final Pair<Double, FileOnClient> f : this.list) {
        f.setFirst(f.getFirst() / 2);
      }
    }
  }

  @Override
  public void insertFile(final FileOnClient f) {
    //napred zkontrolujeme, jestli se soubor vejde do cache
    //pokud se nevejde, vztvorime pro nej okenko
    if (f.getFileSize() > this.capacity) {
      if (!this.fOverCapacity.isEmpty()) {
        this.fOverCapacity.add(f);
        return;
      }
      while (freeCapacity() < (long) ((double) this.capacity * GlobalVariables.getCacheCapacityForDownloadWindow())) {
        removeFile();
      }
      this.fOverCapacity.add(f);
      this.capacity = (long) ((double) this.capacity * (1 - GlobalVariables.getCacheCapacityForDownloadWindow()));
      return;
    }

    if (!this.fOverCapacity.isEmpty()) {
      checkTimes();
    }

    //pokud se soubor vejde, fungujeme spravne
    while (freeCapacity() < f.getFileSize()) {
      removeFile();
    }
    double localReadCount = 0;
    for (final Pair<Double, FileOnClient> files : this.list) {
      localReadCount += files.getFirst();
    }
    final double readHits = ((double) f.getCountOfReadRequests() - (double) f.getCountOfWriteRequests())
      / (double) this.globalReadCount * localReadCount + 1.0;
    this.list.add(new Pair<>(readHits, f));
    this.needSort = true;
  }

  /**
   * metoda pro nastaveni poctu globalnich hitu
   *
   * @param readCount pocet hitu
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
   * metoda pro kontrolu, zda jiz nejsou soubory s vetsi velikosti nez cache stazene - pak odstranime okenko
   */
  private void checkTimes() {
    boolean hasBeenRemoved = true;
    while (hasBeenRemoved) {
      hasBeenRemoved = false;
      if (!this.fOverCapacity.isEmpty() && this.fOverCapacity.get(0).getFRemoveTime() < GlobalVariables.getActualTime()) {
        this.fOverCapacity.remove(0);
        hasBeenRemoved = true;
      }
    }
    if (this.fOverCapacity.isEmpty()) {
      this.capacity = GlobalVariables.getCacheCapacity();
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
    result = prime * result + (int) (this.capacity ^ (this.capacity >>> 32));
    result = prime * result + ((toString() == null) ? 0 : toString().hashCode());
    return result;
  }

}
