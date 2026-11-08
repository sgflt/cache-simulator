package cz.zcu.kiv.cacheSimulator.cachePolicies;

import cz.zcu.kiv.cacheSimulator.shared.FileKey;
import cz.zcu.kiv.cacheSimulator.shared.FileOnClient;
import cz.zcu.kiv.cacheSimulator.shared.GlobalVariables;
import cz.zcu.kiv.cacheSimulator.shared.LRDMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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
   * the same entries by name key, for lookups
   */
  private final Map<String, LRDMetaData> byName = new HashMap<>();

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
   * the cache size set by setCapacity, restored when the download window closes
   */
  private long configuredCapacity;

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
    this.configuredCapacity = this.capacity;
    this.files = new ArrayList<>();
    this.fOverCapacity = new ArrayList<>();
  }

  @Override
  public FileOnClient get(final String fileName) {
    final LRDMetaData file = this.byName.get(FileKey.of(fileName));
    if (file == null) {
      return null;
    }
    file.incrementHit();
    this.GC++;
    this.needRecalculate = true;
    recalculateReferences();
    return file.getFileOnClient();
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
    this.byName.remove(FileKey.of(removedFile.getFileOnClient().getFileName()));
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
    final LRDMetaData entry = new LRDMetaData(fileOnClient, this.GC++);
    this.files.add(entry);
    this.byName.put(FileKey.of(entry.getFileOnClient().getFileName()), entry);
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
    this.configuredCapacity = capacity;
  }

  @Override
  public void reset() {
    this.capacity = this.configuredCapacity;
    this.files.clear();
    this.byName.clear();
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
      this.capacity = this.configuredCapacity;
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