package cz.zcu.kiv.cacheSimulator.cachePolicies;

import cz.zcu.kiv.cacheSimulator.shared.FileOnClient;
import cz.zcu.kiv.cacheSimulator.shared.GlobalVariables;
import cz.zcu.kiv.cacheSimulator.shared.LRDMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


/**
 * class for LRDv2 algorithm
 * <p>
 * SOURCE: Adapted from article "Principles of database buffer management", by
 * W. Effelsberg and T. Haerder
 *
 * @author Pavel Bžoch
 */
public class LRDv2 implements ICache {

  private static final Logger LOG = LoggerFactory.getLogger(LRDv2.class);

  /**
   * List of cached files
   */
  private final List<LRDMetaData> files;

  /**
   * struktura pro ukladani souboru, ktere jsou vetsi nez cache
   */
  private final List<FileOnClient> fOverCapacity;


  /**
   * konstanta pro snizovani poctu referenci
   */
  private static double K1 = 1.8f;

  /**
   * interval pro snizovani poctu referenci
   */
  private static int INTERVAL = 20;


  /**
   * kapacita cache
   */
  private long capacity;

  /**
   * How many bytes is consubed by cached files
   */
  private long usedCapacity;

  /**
   * global counter
   */
  private long GC;

  /**
   * promenna pro nastaveni, zda se ma znovu pocitat RD
   */
  private boolean needRecalculate = true;


  public LRDv2() {
    this.capacity = GlobalVariables.getCacheCapacity();
    this.files = new ArrayList<>();
    this.fOverCapacity = new ArrayList<>();
  }

  @Override
  public FileOnClient get(final String fileName) {
    for (final LRDMetaData file : this.files) {
      if (file.getFileOnClient().getFileName().equalsIgnoreCase(fileName)) {
        file.incrementHit();
        this.GC++;
        this.needRecalculate = true;
        recalculateReferences();
        return file.getFileOnClient();
      }
    }
    return null;
  }

  @Override
  public long freeCapacity() {
    return this.capacity - this.usedCapacity;
  }

  @Override
  public void removeFile() {
    LOG.debug("removeFile()");
    if (this.needRecalculate) {
      for (final LRDMetaData file : this.files) {
        file.recalculateReferenceDensity(this.GC);
      }
      this.files.sort(Comparator.comparing(LRDMetaData::getReferenceDensity));
    }
    this.needRecalculate = false;
    final LRDMetaData removedFile = this.files.remove(0);
    this.usedCapacity -= removedFile.getFileOnClient().getFileSize();
  }

  @Override
  public void insertFile(final FileOnClient fileOnClient) {
    LOG.debug("insertFile(fileOnClient={}", fileOnClient);
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
    this.usedCapacity += fileOnClient.getFileSize();
    this.needRecalculate = true;
    recalculateReferences();
  }

  /**
   * metoda pro rekalkulkaci poctu referenci
   */
  private void recalculateReferences() {
    if (this.GC % INTERVAL == 0) {
      for (final LRDMetaData file : this.files) {
        file.reduceReferenceDensityBy(K1);
      }
    }
  }

  @Override
  public String toString() {
    return "LRDv2";
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
    return "LRDv2;LRD version 2";
  }

  public static double getK1() {
    return K1;
  }

  public static void setK1(final double K1) {
    LRDv2.K1 = K1;
  }

  public static int getINTERVAL() {
    return INTERVAL;
  }

  public static void setINTERVAL(final int INTERVAL) {
    LRDv2.INTERVAL = INTERVAL;
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