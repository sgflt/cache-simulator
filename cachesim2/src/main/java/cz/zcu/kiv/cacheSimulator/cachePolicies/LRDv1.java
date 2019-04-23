package cz.zcu.kiv.cacheSimulator.cachePolicies;

import cz.zcu.kiv.cacheSimulator.shared.FileOnClient;
import cz.zcu.kiv.cacheSimulator.shared.GlobalVariables;
import cz.zcu.kiv.cacheSimulator.shared.LRDMetaData;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


/**
 * class for LRDv1 algorithm
 * <p>
 * SOURCE: Adapted from article "Principles of database buffer management", by
 * W. Effelsberg and T. Haerder
 *
 * @author Pavel Bžoch
 */
public class LRDv1 implements ICache {

  /**
   * List of cached files
   */
  private final List<LRDMetaData> files;

  /**
   * struktura pro ukladani souboru, ktere jsou vetsi nez cache
   */
  private final List<FileOnClient> fOverCapacity;

  /**
   * kapacita cache
   */
  private long capacity;

  /**
   * global counter
   */
  private long GC;

  /**
   * promenna pro nastaveni, zda se ma znovu pocitat RD
   */
  private boolean needRecalculate = true;



  /**
   * konstruktor - iniciace promennych
   */
  public LRDv1() {
    this.capacity = GlobalVariables.getCacheCapacity();
    this.files = new ArrayList<>();
    this.fOverCapacity = new ArrayList<>();
  }

  @Override
  public boolean isInCache(final String fName) {
    for (final LRDMetaData file : this.files) {
      if (file.getFileOnClient().getFileName().equalsIgnoreCase(fName)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public FileOnClient getFileFromCache(final String fName) {
    for (final LRDMetaData file : this.files) {
      if (file.getFileOnClient().getFileName().equalsIgnoreCase(fName)) {
        file.incrementHit();
        this.needRecalculate = true;
        this.GC++;
        return file.getFileOnClient();
      }
    }
    return null;
  }

  @Override
  public long freeCapacity() {
    long sumCap = 0;
    for (final LRDMetaData file : this.files) {
      sumCap += file.getFileOnClient().getFileSize();
    }
    return this.capacity - sumCap;
  }

  @Override
  public void removeFile() {
    if (this.needRecalculate) {
      for (final LRDMetaData file : this.files) {
        file.recalculateReferenceDensity(this.GC);
      }
      this.files.sort(Comparator.comparing(LRDMetaData::getReferenceDensity));
    }
    this.needRecalculate = false;
    this.files.remove(0);
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
    while (freeCapacity() < fileOnClient.getFileSize()) {
      removeFile();
    }
    this.files.add(new LRDMetaData(fileOnClient, this.GC++));
    this.needRecalculate = true;
  }

  @Override
  public String toString() {
    return "LRDv1";
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
    this.files.clear();
    this.GC = 0;
    this.needRecalculate = true;
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
    return "LRDv1;LRD version 1";
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
