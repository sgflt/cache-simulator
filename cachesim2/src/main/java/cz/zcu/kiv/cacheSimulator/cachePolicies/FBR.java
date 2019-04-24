package cz.zcu.kiv.cacheSimulator.cachePolicies;

import cz.zcu.kiv.cacheSimulator.shared.FileOnClient;
import cz.zcu.kiv.cacheSimulator.shared.GlobalVariables;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;


/**
 * class for FBR algorithm
 * <p>
 * SOURCE: Adapted from article "Towards building a fault tolerant and conflict-free distributed file system for mobile clients",
 * by A. Boukerche and R. Al-Shaikh
 *
 * @author Pavel Bžoch
 */
public class FBR implements ICache {
  /**
   * struktura pro uchovani souboru
   */
  private final List<MetaData> fQueue = new ArrayList<>();

  private final Set<String> files = new HashSet<>();

  /**
   * struktura pro ukladani souboru, ktere jsou vetsi nez cache
   */
  private final List<FileOnClient> fOverCapacity = new ArrayList<>();


  /**
   * velikost cache v B
   */
  private long capacity;
  private long usedCapacity;

  /**
   * konstanta pro urceni stare sekce
   */
  private static double OLD_SECTION = 0.3;

  /**
   * konstanta pro urceni nove sekce - neikrementuje se pocet hitu pri zasahu
   */

  private static double NEW_SECTION = 0.6;

  /**
   * konstruktor - inicializace cache
   */
  public FBR() {
    this.capacity = GlobalVariables.getCacheCapacity();
  }

  @Override
  public FileOnClient get(final String fileName) {
    if (!this.files.contains(fileName)) {
      return null;
    }
    for (int i = 0; i < this.fQueue.size(); i++) {
      if (this.fQueue.get(i).getFileOnClient().getFileName().equalsIgnoreCase(fileName)) {
        final MetaData foundFile = this.fQueue.get(i);
        //rozdeleni cache podle indexu
        reorder(i, foundFile);
        this.fQueue.remove(foundFile);
        this.fQueue.add(foundFile);
        return foundFile.getFileOnClient();
      }
    }
    return null;
  }

  private void reorder(final int i, final MetaData foundFile) {
    long sumCap = 0;
    int newIndex = 0;
    for (int j = 0; j < this.fQueue.size(); j++) {
      sumCap += this.fQueue.get(j).getFileOnClient().getFileSize();
      if (sumCap > (NEW_SECTION) * this.capacity) {
        newIndex = j;
        break;
      }
    }

    if (newIndex < i) {
      foundFile.incrementReadHits();
    }
  }

  @Override
  public long freeCapacity() {
    return this.capacity - this.usedCapacity;
  }

  @Override
  public void removeFile() {
    long sumCap = 0;
    int oldIndex = -1;

    if (this.fQueue.isEmpty()) {
      return;
    }

    for (int i = 0; i < this.fQueue.size(); i++) {
      sumCap += this.fQueue.get(i).getFileOnClient().getFileSize();
      if (sumCap > (1 - OLD_SECTION) * this.capacity) {
        oldIndex = i;
        break;
      }
    }
    //odebereme podle LRU
    if (oldIndex == -1) {
      final MetaData removedFile = this.fQueue.remove(this.fQueue.size() - 1);
      this.usedCapacity -= removedFile.getFileOnClient().getFileSize();
      this.files.remove(removedFile.getFileOnClient().getFileName());
      return;
    }
    //odebereme podle LFU z OLD section
    final Optional<MetaData> min = this.fQueue.stream().skip(oldIndex).min(Comparator.comparing(MetaData::getReadHits));
    final MetaData metaData = min.get();
    this.fQueue.remove(metaData);
    this.usedCapacity -= metaData.getFileOnClient().getFileSize();
    this.files.remove(metaData.getFileOnClient().getFileName());
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
    this.fQueue.add(new MetaData(fileOnClient));
    this.files.add(fileOnClient.getFileName());
    this.usedCapacity += fileOnClient.getFileSize();
  }

  @Override
  public String toString() {
    return "FBR";
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
    this.fQueue.clear();
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
    return "FBR;FBR";
  }

  public static double getOLD_SECTION() {
    return OLD_SECTION;
  }

  public static void setOLD_SECTION(final double OLD_SECTION) {
    FBR.OLD_SECTION = OLD_SECTION;
  }

  public static double getNEW_SECTION() {
    return NEW_SECTION;
  }

  public static void setNEW_SECTION(final double NEW_SECTION) {
    FBR.NEW_SECTION = NEW_SECTION;
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
    private int readHits = 1;

    MetaData(final FileOnClient fileOnClient) {
      this.fileOnClient = fileOnClient;
    }

    FileOnClient getFileOnClient() {
      return this.fileOnClient;
    }

    int getReadHits() {
      return this.readHits;
    }

    void incrementReadHits() {
      ++this.readHits;
    }
  }
}
