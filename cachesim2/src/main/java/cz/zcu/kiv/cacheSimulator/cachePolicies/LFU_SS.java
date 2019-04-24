package cz.zcu.kiv.cacheSimulator.cachePolicies;

import cz.zcu.kiv.cacheSimulator.server.Server;
import cz.zcu.kiv.cacheSimulator.shared.FileOnClient;
import cz.zcu.kiv.cacheSimulator.shared.GlobalVariables;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


/**
 * class for LFU-SS algorithm
 *
 * @author Pavel Bžoch
 */
public class LFU_SS implements ICache {

  /**
   * struktura pro uchovani souboru
   */
  private final List<MetaData> list = new ArrayList<>();

  /**
   * struktura pro ukladani souboru, ktere jsou vetsi nez cache
   */
  private final List<FileOnClient> fOverCapacity = new ArrayList<>();


  /**
   * velikost cache v kB
   */
  private long capacity;
  private long usedCapacity;

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
  private long accessCount;

  /**
   * promenna pro uchovani odkazu na server
   */
  private final Server server = Server.getInstance();

  /**
   * konstruktor - inicializace cache
   */
  public LFU_SS() {
    this.capacity = GlobalVariables.getCacheCapacity();
  }

  @Override
  public FileOnClient get(final String fileName) {
    if (++this.accessCount % 20 == 0) {
      setGlobalReadCountServer(this.server.getGlobalReadRequests(this));
    }
    for (final var metaData : this.list) {
      if (metaData.getFileOnClient().getFileName().equalsIgnoreCase(fileName)) {
        metaData.increaseReadHits();
        this.needSort = true;
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
    if (this.needSort) {
      this.list.sort(Comparator.comparing(MetaData::getReadHits));
    }
    this.needSort = false;
    if (!this.list.isEmpty()) {
      final MetaData removedFile = this.list.remove(0);
      this.usedCapacity -= removedFile.getFileOnClient().getFileSize();
    }

    if (this.list.size() > 2 && this.list.get(this.list.size() - 1).getReadHits() > 15) {
      this.list.forEach(MetaData::reduceReadHits);
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
    double localReadCount = 0;
    for (final var metaData : this.list) {
      localReadCount += metaData.getReadHits();
    }
    final double readHits = ((double) fileOnClient.getCountOfReadRequests() - (double) fileOnClient.getCountOfWriteRequests())
      / (double) this.globalReadCount * localReadCount + 1.0;
    this.list.add(new MetaData(fileOnClient, readHits));
    this.usedCapacity += fileOnClient.getFileSize();
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


  private static class MetaData {

    private final FileOnClient fileOnClient;
    private double readHits;

    MetaData(final FileOnClient fileOnClient, final double readHits) {
      this.fileOnClient = fileOnClient;
      this.readHits = readHits;
    }

    FileOnClient getFileOnClient() {
      return this.fileOnClient;
    }

    double getReadHits() {
      return this.readHits;
    }

    void increaseReadHits() {
      ++this.readHits;
    }

    void reduceReadHits() {
      this.readHits /= 2.0;
    }
  }
}
