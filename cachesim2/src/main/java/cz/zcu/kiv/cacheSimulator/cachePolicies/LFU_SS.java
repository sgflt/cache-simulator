package cz.zcu.kiv.cacheSimulator.cachePolicies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import cz.zcu.kiv.cacheSimulator.server.Server;
import cz.zcu.kiv.cacheSimulator.shared.GlobalVariables;
import cz.zcu.kiv.cacheSimulator.shared.Pair;
import cz.zcu.kiv.cacheSimulator.simulation.FileOnClient;

/**
 * class for LFU-SS algorithm
 *
 * @author Pavel Bžoch
 * @author Lukáš Kvídera
 * @version 2.1
 */
public class LFU_SS implements ICache {

  protected static final Comparator<Pair<Double, FileOnClient>> comparator =
      (o1, o2) -> Double.compare(o1.getFirst(), o2.getFirst());
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

  protected long used = 0;


  /**
   * konstruktor - inicializace cache
   */
  public LFU_SS() {
    this.list = new LinkedList<>();
    this.fOverCapacity = new LinkedList<>();
  }


  @Override
  public boolean contains(final String fName) {
    return this.list.stream().anyMatch(pair -> pair.getSecond().getFileName().equals(fName));
  }


  @Override
  public FileOnClient getFile(final String fName) {
    this.accessCount++;
    if (this.accessCount % 20 == 0) {
      this.setGlobalReadCountServer(this.server.getGlobalReadHits(this));
    }

    final Optional<Pair<Double, FileOnClient>> opt =
        this.list.stream().filter(pair -> pair.getSecond().getFileName().equalsIgnoreCase(fName)).findFirst();

    if (opt.isPresent()) {
      final Pair<Double, FileOnClient> pair = opt.get();
      pair.setFirst(pair.getFirst() + 1.0);
      this.needSort = true;
      return pair.getSecond();
    }

    return null;
  }


  @Override
  public long freeCapacity() {
    return this.capacity - this.used;
  }


  @Override
  public void removeFile() {
    if (this.needSort) {
      Collections.sort(this.list, comparator);
      this.needSort = false;
    }

    if (!this.list.isEmpty()) {
      this.used -= this.list.remove(0).getSecond().getFileSize();
    }

    if (this.list.size() > 2) {
      if ((this.list.get(this.list.size() - 1)).getFirst() > 15) {
        this.list.forEach(occurence -> occurence.setFirst(occurence.getFirst() / 2));
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

    this.list.add(new Pair<>(readHits, f));
    this.used += f.getFileSize();
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
    result = prime * result + this.toString().hashCode();
    return result;
  }


  @Override
  public void removeFile(final FileOnClient f) {
    for (final Pair<Double, FileOnClient> file : this.list) {
      if (file.getSecond() == f) {
        this.list.remove(file);
        this.used -= file.getSecond().getFileSize();
        break;
      }
    }
  }


  @Override
  public List<FileOnClient> getCachedFiles() {
    return this.list.stream().map(pair -> pair.getSecond()).collect(Collectors.toList());
  }


  @Override
  public long getCapacity() {
    return this.initialCapacity;
  }
}
