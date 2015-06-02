package cz.zcu.kiv.cacheSimulator.cachePolicies;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import cz.zcu.kiv.cacheSimulator.shared.GlobalVariables;
import cz.zcu.kiv.cacheSimulator.shared.Triplet;
import cz.zcu.kiv.cacheSimulator.simulation.FileOnClient;

/**
 * class for LRU-K algorithm
 * SOURCE: Adapted from article
 * "The LRU-K page replacement algorithm for database disk buffering", by E. J.
 * O'Neil, P. E. O'Neil and G. Weikum
 *
 * @author Pavel Bžoch
 * @author Lukáš Kvídera
 * @version 2.1
 */
public class LRU_K implements ICache {

  /**
   * Korelace pro vyhazovani souboru z cache
   */
  private static int CORRELATED_REFERENCE_PERIOD = 7;

  /**
   * urcuje, kolik casu si budeme pamatovat
   */
  private static int K = 3;

  /**
   * promenna pro pocitani logickeho casu prichodu souboru
   */
  private long timeCounter = 0;

  /**
   * velikost cache v kB
   */
  private long capacity = 0;

  /**
   * pocatecni kapacita cache
   */
  private long initialCapacity = 0;

  /**
   * struktura pro ukladani souboru
   */
  private final List<Triplet<FileOnClient, Long[], Long>> fList;

  /**
   * struktura pro ukladani souboru, ktere jsou vetsi nez cache
   */
  private final List<FileOnClient> fOverCapacity;

  private long used;


  public LRU_K() {
    super();
    this.fList = new ArrayList<>();
    this.fOverCapacity = new ArrayList<>();
  }


  @Override
  public boolean contains(final String fName) {
    for (final Triplet<FileOnClient, Long[], Long> files : this.fList) {
      if (files.getFirst().getFileName().equalsIgnoreCase(fName))
        return true;
    }
    return false;
  }


  @Override
  public FileOnClient getFile(final String fName) {
    Triplet<FileOnClient, Long[], Long> fileInCache = null;
    for (final Triplet<FileOnClient, Long[], Long> files : this.fList) {
      if (files.getFirst().getFileName().equalsIgnoreCase(fName)) {
        fileInCache = files;
        break;
      }
    }

    if (fileInCache == null)
      return null;
    final long actTime = ++this.timeCounter;
    if (actTime - fileInCache.getThird() > CORRELATED_REFERENCE_PERIOD) {
      final long correlPeriodOfRefPage = fileInCache.getThird() - fileInCache.getSecond()[0];
      for (int i = 1; i < K; i++) {
        fileInCache.getSecond()[i] = fileInCache.getSecond()[i - 1] + correlPeriodOfRefPage;
      }
      fileInCache.getSecond()[0] = actTime;
      fileInCache.setThird(actTime);
    } else {
      fileInCache.setThird(actTime);
    }
    return fileInCache.getFirst();
  }


  @Override
  public long freeCapacity() {
    return this.capacity - this.used;
  }


  @Override
  public void removeFile() {
    long min = ++this.timeCounter;
    Triplet<FileOnClient, Long[], Long> victim = null;

    for (final Triplet<FileOnClient, Long[], Long> files : this.fList) {
      if (this.timeCounter - files.getThird() > CORRELATED_REFERENCE_PERIOD)
        if (files.getSecond()[K - 1] < min) {
          victim = files;
          min = files.getSecond()[K - 1];
        }
    }

    if (victim != null) {
      this.fList.remove(victim);
      this.used -= victim.getFirst().getFileSize();
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

    if (!this.fOverCapacity.isEmpty())
      this.checkTimes();

    // pokud se soubor vejde, fungujeme spravne
    while (this.freeCapacity() < f.getFileSize()) {
      this.removeFile();
    }

    final long actTime = ++this.timeCounter;
    final Long[] lastTimes = new Long[K];
    for (int i = 0; i < lastTimes.length; i++) {
      lastTimes[i] = 0L;
    }

    lastTimes[0] = actTime;
    this.fList.add(new Triplet<>(f, lastTimes, actTime));
    this.used += f.getFileSize();
  }


  @Override
  public String toString() {
    return "LRU-K";
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
    this.timeCounter = 0;
    this.fList.clear();
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
    return "LRU_K;LRU-K";
  }


  public static int getCORRELATED_REFERENCE_PERIOD() {
    return CORRELATED_REFERENCE_PERIOD;
  }


  public static void setCORRELATED_REFERENCE_PERIOD(final int CORRELATED_REFERENCE_PERIOD) {
    LRU_K.CORRELATED_REFERENCE_PERIOD = CORRELATED_REFERENCE_PERIOD;
  }


  public static int getK() {
    return K;
  }


  public static void setK(final int K) {
    LRU_K.K = K;
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
    for (final Triplet<FileOnClient, Long[], Long> file : this.fList) {
      if (file.getFirst() == f) {
        this.fList.remove(file);
        this.used -= file.getFirst().getFileSize();
        break;
      }
    }
  }


  @Override
  public List<FileOnClient> getCachedFiles() {
    return this.fList.stream().map(triplet -> triplet.getFirst()).collect(Collectors.toList());
  }


  @Override
  public long getCapacity() {
    return this.initialCapacity;
  }

}
