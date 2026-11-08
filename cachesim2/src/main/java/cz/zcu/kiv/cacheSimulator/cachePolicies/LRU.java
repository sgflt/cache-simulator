package cz.zcu.kiv.cacheSimulator.cachePolicies;

import cz.zcu.kiv.cacheSimulator.shared.FileKey;
import cz.zcu.kiv.cacheSimulator.shared.FileOnClient;
import cz.zcu.kiv.cacheSimulator.shared.GlobalVariables;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.NoSuchElementException;


/**
 * class for LRU algorithm
 * <p>
 * SOURCE: Adapted from article "Analysis of caching algorithms for distributed file systems", by
 * B. Reed and D. D. E. Long
 *
 * @author Pavel Bžoch
 */
public class LRU implements ICache {

  /**
   * cached files by name key, least recently used first
   */
  private final LinkedHashMap<String, FileOnClient> files = new LinkedHashMap<>();

  /**
   * sum of sizes of the cached files
   */
  private long usedCapacity;

  /**
   * struktura pro ukladani souboru, ktere jsou vetsi nez cache
   */
  private final ArrayList<FileOnClient> fOverCapacity;


  /**
   * velikost cache v B
   */
  private long capacity;

  /**
   * konstruktor - inicializace cache
   */
  public LRU() {
    this.capacity = GlobalVariables.getCacheCapacity();
    this.fOverCapacity = new ArrayList<>();
  }

  @Override
  public FileOnClient get(final String fileName) {
    final String key = FileKey.of(fileName);
    final FileOnClient file = this.files.get(key);
    if (file != null) {
      this.files.putLast(key, file);
    }
    return file;
  }

  @Override
  public long freeCapacity() {
    return this.capacity - this.usedCapacity;
  }

  @Override
  public void removeFile() {
    final var oldest = this.files.pollFirstEntry();
    if (oldest == null) {
      throw new NoSuchElementException();
    }
    this.usedCapacity -= oldest.getValue().getFileSize();
  }

  @Override
  public void insertFile(final FileOnClient f) {
    //napred zkontrolujeme, jestli se soubor vejde do cache
    //pokud se nevejde, vztvorime pro nej okenko
    if (f.getFileSize() > this.capacity) {
      if (!this.fOverCapacity.isEmpty()) {
        this.fOverCapacity.add(f);
        return;
      }
      while (freeCapacity() < (long) ((double) this.capacity * GlobalVariables.getCacheCapacityForDownloadWindow())) {
        removeFile();
      }
      this.fOverCapacity.add(f);
      this.capacity = (long) ((double) this.capacity * (1 - GlobalVariables.getCacheCapacityForDownloadWindow()));
      return;
    }

    if (!this.fOverCapacity.isEmpty()) {
      checkTimes();
    }

    //pokud se soubor vejde, fungujeme spravne
    while (freeCapacity() < f.getFileSize()) {
      removeFile();
    }
    this.files.put(FileKey.of(f.getFileName()), f);
    this.usedCapacity += f.getFileSize();
  }

  @Override
  public String toString() {
    return "LRU";
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
    this.usedCapacity = 0;
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
    return "LRU;LRU";
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
