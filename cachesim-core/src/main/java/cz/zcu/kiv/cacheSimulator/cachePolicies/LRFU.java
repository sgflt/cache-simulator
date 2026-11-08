package cz.zcu.kiv.cacheSimulator.cachePolicies;

import cz.zcu.kiv.cacheSimulator.shared.FileKey;
import cz.zcu.kiv.cacheSimulator.shared.FileOnClient;
import cz.zcu.kiv.cacheSimulator.shared.GlobalVariables;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * class for LRFU algorithm
 * <p>
 * SOURCE: Adapted from article "LRFU: a spectrum of policies that subsumes the least recently used and least frequently used policies", by
 * D. Lee, J. Choi, J.-H. Kim, S. Noh, S. L. Min, Y. Cho and C. S. Kim
 *
 * @author Pavel Bžoch
 */
public class LRFU implements ICache {

  /**
   * konstanta p
   */
  private static double P = 2.0f;

  /**
   * konstanta lambda
   */
  private static double LAMBDA = 0.045f;

  /**
   * atribut pro pocitani casu
   */
  private long timeCounter = 1;

  /**
   * atribut pro uchovani souboru v cache
   */
  private final List<MetaData> fList = new ArrayList<>();

  /**
   * the same entries by name key, for lookups
   */
  private final Map<String, MetaData> byName = new HashMap<>();

  /**
   * struktura pro ukladani souboru, ktere jsou vetsi nez cache
   */
  private final List<FileOnClient> fOverCapacity = new ArrayList<>();


  /**
   * velikost cache v B
   */
  private long capacity;
  /**
   * the cache size set by setCapacity, restored when the download window closes
   */
  private long configuredCapacity;
  private long usedCapacity;

  /**
   * promenna pro urceni, zda je potreba setridit pole cachovanych souboru
   */
  private boolean needSort = true;

  /**
   * konstruktor - iniciace parametru
   */
  public LRFU() {
    this.capacity = GlobalVariables.getCacheCapacity();
    this.configuredCapacity = this.capacity;
  }

  @Override
  public FileOnClient get(final String fileName) {
    final MetaData metaData = this.byName.get(FileKey.of(fileName));
    if (metaData == null) {
      return null;
    }
    final long actTime = ++this.timeCounter;
    metaData.setPriority(calculateF(0) + metaData.getPriority() * calculateF(actTime - metaData.getLastAccessTime()));
    metaData.setLastAccessTime(actTime);
    this.needSort = true;
    return metaData.getFileOnClient();
  }

  @Override
  public long freeCapacity() {
    return this.capacity - this.usedCapacity;
  }

  @Override
  public void removeFile() {
    if (this.needSort) {
      this.fList.sort(Comparator.comparing(MetaData::getPriority));
    }
    this.needSort = false;
    if (!this.fList.isEmpty()) {
      final MetaData removedFile = this.fList.remove(0);
      this.byName.remove(FileKey.of(removedFile.getFileOnClient().getFileName()));
      this.usedCapacity -= removedFile.getFileOnClient().getFileSize();
    }
  }

  @Override
  public void insertFile(final FileOnClient fileOnClient) {
    //napred zkontrolujeme, jestli se soubor vejde do cache
    //pokud se nevejde, vztvorime pro nej okenko
    if (fileOnClient.getFileSize() > this.capacity) {
      if (!this.fOverCapacity.isEmpty()) {
        this.fOverCapacity.add(fileOnClient);
        return;
      }
      while (freeCapacity() < (long) ((double) this.capacity * GlobalVariables.getCacheCapacityForDownloadWindow())) {
        removeFile();
      }
      this.fOverCapacity.add(fileOnClient);
      this.capacity = (long) ((double) this.capacity * (1 - GlobalVariables.getCacheCapacityForDownloadWindow()));
      return;
    }

    if (!this.fOverCapacity.isEmpty()) {
      checkTimes();
    }

    //pokud se soubor vejde, fungujeme spravne
    this.needSort = true;
    while (freeCapacity() < fileOnClient.getFileSize()) {
      removeFile();
    }
    final MetaData entry = new MetaData(fileOnClient, ++this.timeCounter, calculateF(0));
    this.fList.add(entry);
    this.byName.put(FileKey.of(entry.getFileOnClient().getFileName()), entry);
    this.usedCapacity += fileOnClient.getFileSize();
  }

  /**
   * metoda pro vypocet priority
   *
   * @param x casovy parametr
   * @return priorita souboru
   */
  private static double calculateF(final long x) {
    return Math.pow((1.0 / P), (LAMBDA * x));
  }

  @Override
  public String toString() {
    return "LRFU";
  }

  @Override
  public boolean needServerStatistics() {
    return false;
  }

  @Override
  public void setCapacity(final long capacity) {
    this.capacity = capacity;
    this.configuredCapacity = capacity;
  }

  @Override
  public void reset() {
    this.capacity = this.configuredCapacity;
    this.timeCounter = 1;
    this.fList.clear();
    this.byName.clear();
    this.needSort = true;
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
      this.capacity = this.configuredCapacity;
    }
  }

  @Override
  public String cacheInfo() {
    return "LRFU;LRFU";
  }

  public static double getP() {
    return P;
  }

  public static void setP(final double P) {
    LRFU.P = P;
  }

  public static double getLAMBDA() {
    return LAMBDA;
  }

  public static void setLAMBDA(final double LAMBDA) {
    LRFU.LAMBDA = LAMBDA;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (int) (this.capacity ^ (this.capacity >>> 32));
    result = prime * result + ((toString() == null) ? 0 : toString().hashCode());
    return result;
  }


  private static class MetaData {

    private final FileOnClient fileOnClient;
    private long lastAccessTime;
    private double priority;

    MetaData(final FileOnClient fileOnClient, final long lastAccessTime, final double priority) {
      this.fileOnClient = fileOnClient;
      this.lastAccessTime = lastAccessTime;
      this.priority = priority;
    }

    FileOnClient getFileOnClient() {
      return this.fileOnClient;
    }

    long getLastAccessTime() {
      return this.lastAccessTime;
    }

    double getPriority() {
      return this.priority;
    }

    void setLastAccessTime(final long actualTime) {
      this.lastAccessTime = actualTime;
    }

    void setPriority(final double priority) {
      this.priority = priority;
    }
  }
}
