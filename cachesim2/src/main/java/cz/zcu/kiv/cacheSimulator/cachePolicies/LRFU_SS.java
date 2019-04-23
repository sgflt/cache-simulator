package cz.zcu.kiv.cacheSimulator.cachePolicies;

import cz.zcu.kiv.cacheSimulator.server.Server;
import cz.zcu.kiv.cacheSimulator.shared.FileOnClient;
import cz.zcu.kiv.cacheSimulator.shared.GlobalVariables;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


/**
 * class for LRFU-SS algorithm
 *
 * @author Pavel Bžoch
 */
public class LRFU_SS implements ICache {

  /**
   * struktura pro uchovani souboru
   */
  private final List<MetaData> cachedFiles = new ArrayList<>();

  /**
   * velikost cache v B
   */
  private long capacity;

  private long usedCapacity;

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
  private static double K1 = 0.35f;
  private static double K2 = 1.1f;

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
   * struktura pro ukladani souboru, ktere jsou vetsi nez cache
   */
  private final List<FileOnClient> fOverCapacity = new ArrayList<>();


  public LRFU_SS() {
    this.capacity = GlobalVariables.getCacheCapacity();
  }

  @Override
  public boolean contains(final String fileName) {
    this.accessCount++;
    if (this.accessCount % 20 == 0) {
      setGlobalReadCountServer(this.server.getGlobalReadRequests(this));
    }
    for (final var metaData : this.cachedFiles) {
      if (metaData.getFileOnClient().getFileName().equalsIgnoreCase(fileName)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public FileOnClient get(final String fileName) {
    for (final MetaData metaData : this.cachedFiles) {
      if (metaData.getFileOnClient().getFileName().equalsIgnoreCase(fileName)) {
        metaData.updateAccesTime();
        metaData.increaseHits();
        this.needSort = true;
        this.needRecalculate = true;
        return metaData.getFileOnClient();
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
    if (this.needRecalculate) {
      recalculatePriorities();
    }
    if (this.needSort) {
      this.cachedFiles.sort(Comparator.comparing(MetaData::getPriority));
    }
    this.needSort = false;
    this.needRecalculate = false;
    if (!this.cachedFiles.isEmpty()) {
      final MetaData removedFile = this.cachedFiles.remove(0);
      this.usedCapacity -= removedFile.getFileOnClient().getFileSize();
    }
  }

  /**
   * metoda pro rekalkulaci priorit
   */
  private void recalculatePriorities() {
    if (this.cachedFiles.size() <= 1) {
      return;
    }
    long oldestTime = this.cachedFiles.get(0).getLastAccessTime();
    long newestTime = this.cachedFiles.get(0).getLastAccessTime();
    double maxReadHit = this.cachedFiles.get(0).getReadHits();
    double minReadHit = this.cachedFiles.get(0).getReadHits();
    // zjisteni lokalnich extremu
    for (final var metaData : this.cachedFiles) {
      if (metaData.getLastAccessTime() > newestTime) {
        newestTime = metaData.getLastAccessTime();
      } else if (metaData.getLastAccessTime() < oldestTime) {
        oldestTime = metaData.getLastAccessTime();
      }
      if (maxReadHit < metaData.getReadHits()) {
        maxReadHit = metaData.getReadHits();
      } else if (minReadHit > metaData.getReadHits()) {
        minReadHit = metaData.getReadHits();
      }
    }

    for (final var metaData : this.cachedFiles) {
      final int lfussPriority = (int) ((metaData.getReadHits() - minReadHit) * 65535.0 / (maxReadHit - minReadHit));
      final int lruPriority = (int) ((metaData.getLastAccessTime() - oldestTime) * 65535.0 / (newestTime - oldestTime));
      metaData.setPriority((int) (K1 * lruPriority + K2 * lfussPriority));
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
    while (freeCapacity() < fileOnClient.getFileSize()) {
      removeFile();
    }
    double localReadCount = 0.0;
    for (final MetaData files : this.cachedFiles) {
      localReadCount += files.getReadHits();
    }
    double readHits = 0.0;
    if (this.globalReadCount > 0.0) {
      readHits = ((double) fileOnClient.getCountOfReadRequests() - (double) fileOnClient.getCountOfWriteRequests()) / (double) this.globalReadCount
        * localReadCount + 1;
    }
    this.cachedFiles.add(new MetaData(fileOnClient, readHits));
    this.needSort = true;
    this.needRecalculate = true;
    this.usedCapacity += fileOnClient.getFileSize();
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
    return "LRFU-SS";
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
    this.cachedFiles.clear();
    this.needRecalculate = true;
    this.needSort = true;
    this.accessCount = 0;
    this.globalReadCount = Long.MAX_VALUE;
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
    result = prime * result + (int) (this.capacity ^ (this.capacity >>> 32));
    result = prime * result + ((toString() == null) ? 0 : toString().hashCode());
    return result;
  }

  private static class MetaData {

    private final FileOnClient fileOnClient;
    private long lastAccessTime;
    private double readHits;
    private double priority;

    MetaData(final FileOnClient fileOnClient, final double readHits) {
      this.fileOnClient = fileOnClient;
      this.lastAccessTime = System.nanoTime();
      this.readHits = readHits;
    }

    FileOnClient getFileOnClient() {
      return this.fileOnClient;
    }

    long getLastAccessTime() {
      return this.lastAccessTime;
    }

    double getReadHits() {
      return this.readHits;
    }

    void increaseHits() {
      ++this.readHits;
    }

    double getPriority() {
      return this.priority;
    }

    void setPriority(final int priority) {
      this.priority = priority;
    }

    void updateAccesTime() {
      this.lastAccessTime = System.nanoTime();
    }
  }
}
