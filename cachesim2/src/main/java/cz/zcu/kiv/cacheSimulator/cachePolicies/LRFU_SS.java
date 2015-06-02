package cz.zcu.kiv.cacheSimulator.cachePolicies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import cz.zcu.kiv.cacheSimulator.server.Server;
import cz.zcu.kiv.cacheSimulator.shared.GlobalVariables;
import cz.zcu.kiv.cacheSimulator.shared.Quartet;
import cz.zcu.kiv.cacheSimulator.simulation.FileOnClient;

/**
 * class for LRFU-SS algorithm
 *
 * @author Pavel Bžoch
 * @author Lukáš Kvídera
 * @version 2.1
 */
public class LRFU_SS implements ICache {

  private static final Comparator<Quartet<FileOnClient, Long, Double, Integer>> comparator =
      (o1, o2) -> Double.compare(o1.getFourth(), o2.getFourth());

  /**
   * struktura pro uchovani souboru
   */
  private final List<Quartet<FileOnClient, Long, Double, Integer>> list;

  /**
   * velikost cache v B
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
   * promenna pro urceni, zda je potreba spocitat znovu priority
   */
  private boolean needRecalculate = true;

  /**
   * koeficienty pro urceni priority
   */
  private static double K1 = 0.35f, K2 = 1.1f;

  /**
   * poromenna pro urceni globalniho poctu hitu na cteni
   */
  private long globalReadCount = Long.MAX_VALUE;

  /**
   * promenna pro urceni poctu pristupu do cache a pro aktualizaci globalnich
   * statistik
   */
  private long accessCount = 0;

  /**
   * promenna pro uchovani odkazu na server
   */
  private final Server server = Server.getInstance();

  /**
   * struktura pro ukladani souboru, ktere jsou vetsi nez cache
   */
  private final List<FileOnClient> fOverCapacity;

  private long used;


  /**
   * konstruktor - inicializce cache
   */
  public LRFU_SS() {
    this.list = new ArrayList<>();
    this.fOverCapacity = new ArrayList<>();
  }


  @Override
  public boolean contains(final String fName) {
    for (final Quartet<FileOnClient, Long, Double, Integer> f : this.list) {
      if (f.getFirst().getFileName().equalsIgnoreCase(fName))
        return true;
    }
    return false;
  }


  @Override
  public FileOnClient getFile(final String fName) {
    this.accessCount++;
    if (this.accessCount % 20 == 0) {
      this.setGlobalReadCountServer(this.server.getGlobalReadHits(this));
    }

    for (final Quartet<FileOnClient, Long, Double, Integer> f : this.list) {
      if (f.getFirst().getFileName().equalsIgnoreCase(fName)) {
        f.setSecond(GlobalVariables.getActualTime());
        f.setThird(f.getThird() + 1);
        this.needSort = true;
        this.needRecalculate = true;
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
    if (this.needRecalculate) {
      this.recalculatePriorities();
      this.needRecalculate = false;
    }

    if (this.needSort) {
      Collections.sort(this.list, comparator);
      this.needSort = false;
    }


    if (!this.list.isEmpty()) {
      this.used -= this.list.remove(0).getFirst().getFileSize();
    }

  }


  /**
   * metoda pro rekalkulaci priorit
   */
  public void recalculatePriorities() {
    if (this.list.size() <= 1)
      return;

    long oldestTime = this.list.get(0).getSecond(), newestTime = this.list.get(0).getSecond();
    double maxReadHit = this.list.get(0).getThird(), minReadHit = this.list.get(0).getThird();

    // zjisteni lokalnich extremu
    for (final Quartet<FileOnClient, Long, Double, Integer> f : this.list) {
      if (f.getSecond() > newestTime)
        newestTime = f.getSecond();
      if (f.getSecond() < oldestTime)
        oldestTime = f.getSecond();
      if (maxReadHit < f.getThird())
        maxReadHit = f.getThird();
      if (minReadHit > f.getThird())
        minReadHit = f.getThird();
    }

    // vypocet priorit
    for (final Quartet<FileOnClient, Long, Double, Integer> f : this.list) {
      final int PLFU_SS = (int) ((f.getThird() - minReadHit) * 65535.0 / (maxReadHit - minReadHit));
      final int PLRU = (int) ((f.getSecond() - (double) oldestTime) * 65535.0 / ((double) newestTime - (double) oldestTime));
      f.setFourth((int) (K1 * PLRU + K2 * PLFU_SS));
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

      while (this.freeCapacity() < (long) (this.capacity * GlobalVariables.getCacheCapacityForDownloadWindow())) {
        this.removeFile();
      }

      this.fOverCapacity.add(f);
      this.capacity = (long) (this.capacity * (1 - GlobalVariables.getCacheCapacityForDownloadWindow()));
      return;
    }

    if (!this.fOverCapacity.isEmpty()) {
      this.checkTimes();
    }

    // pokud se soubor vejde, fungujeme spravne
    while (this.freeCapacity() < f.getFileSize()) {
      this.removeFile();
    }

    final double localReadCount = this.list.stream().mapToDouble(file -> file.getThird()).sum();

    double readHits = 0;
    if (this.globalReadCount > 0)
      readHits = ((double) f.getReadHit() - (double) f.getWriteHit()) / this.globalReadCount * localReadCount + 1;

    this.list.add(new Quartet<>(f, GlobalVariables.getActualTime(), readHits, 0));
    this.used += f.getFileSize();

    this.needSort = true;
    this.needRecalculate = true;
  }


  /**
   * metoda pro nastaveni poctu globalnich hitu
   *
   * @param readCount
   *            pocet hitu
   */
  public void setGlobalReadCountServer(final long readCount) {
    this.globalReadCount = readCount;
  }


  @Override
  public String toString() {
    return "LRFU-SS";
    // return "LRFU-SS algorithm (K1="+K1+", K2=" + K2 +") ";
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
    this.list.clear();
    this.needRecalculate = true;
    this.needSort = true;
    this.accessCount = 0;
    this.globalReadCount = Long.MAX_VALUE;
    this.fOverCapacity.clear();
  }


  /**
   * metoda pro kontrolu, zda jiz nejsou soubory s vetsi velikosti nez cache
   * stazene - pak odstranime okenko
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
      this.capacity = this.initialCapacity;
    }
  }


  @Override
  public String cacheInfo() {
    return "LRFU_SS;LRFU-SS";
  }


  public static double getK1() {
    return K1;
  }


  public static void setK1(final double K1) {
    LRFU_SS.K1 = K1;
  }


  public static double getK2() {
    return K2;
  }


  public static void setK2(final double K2) {
    LRFU_SS.K2 = K2;
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
    for (final Quartet<FileOnClient, Long, Double, Integer> file : this.list) {
      if (file.getFirst() == f) {
        this.list.remove(file);
        this.used -= file.getFirst().getFileSize();
        break;
      }
    }
  }


  @Override
  public List<FileOnClient> getCachedFiles() {
    return this.list.stream().map(quartet -> quartet.getFirst()).collect(Collectors.toList());
  }


  @Override
  public long getCapacity() {
    return this.initialCapacity;
  }
}
