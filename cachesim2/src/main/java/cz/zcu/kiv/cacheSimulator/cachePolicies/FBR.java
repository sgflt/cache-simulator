package cz.zcu.kiv.cacheSimulator.cachePolicies;

import java.util.ArrayList;

import cz.zcu.kiv.cacheSimulator.shared.FileOnClient;
import cz.zcu.kiv.cacheSimulator.shared.GlobalVariables;
import cz.zcu.kiv.cacheSimulator.shared.Pair;


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
  private final ArrayList<Pair<FileOnClient, Integer>> fQueue;

  /**
   * struktura pro ukladani souboru, ktere jsou vetsi nez cache
   */
  private final ArrayList<FileOnClient> fOverCapacity;


  /**
   * velikost cache v B
   */
  private long capacity = 0;

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
    this.fQueue = new ArrayList<>();
    this.capacity = GlobalVariables.getCacheCapacity();
    this.fOverCapacity = new ArrayList<>();
  }

  @Override
  public boolean contains(final String fileName) {
    for (final Pair<FileOnClient, Integer> pair : this.fQueue) {
      if (pair.getFirst().getFileName().equalsIgnoreCase(fileName)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public FileOnClient get(final String fileName) {
    Pair<FileOnClient, Integer> foundFile = null;
    int pairIndex = 0;
    for (int i = 0; i < this.fQueue.size(); i++) {
      if (this.fQueue.get(i).getFirst().getFileName().equalsIgnoreCase(fileName)) {
        foundFile = this.fQueue.get(i);
        pairIndex = i;
      }
    }
    if (foundFile == null) {
      return null;
    } else {
      //rozdeleni cache podle indexu
      long sumCap = 0;
      int newIndex = 0;
      for (int i = 0; i < this.fQueue.size(); i++) {
        sumCap += this.fQueue.get(i).getFirst().getFileSize();
        if (sumCap > (NEW_SECTION) * this.capacity) {
          newIndex = i;
          break;
        }
      }

      if (newIndex < pairIndex) {
        foundFile.setSecond(foundFile.getSecond() + 1);
      }

      this.fQueue.remove(foundFile);
      this.fQueue.add(foundFile);
      return foundFile.getFirst();
    }
  }

  @Override
  public long freeCapacity() {
    long obsazeno = 0;
    for (final Pair<FileOnClient, Integer> pair : this.fQueue) {
      obsazeno += pair.getFirst().getFileSize();
    }
    return this.capacity - obsazeno;
  }

  @Override
  public void removeFile() {
    long sumCap = 0;
    int oldIndex = -1;

    if (this.fQueue.isEmpty()) {
      return;
    }

    for (int i = 0; i < this.fQueue.size(); i++) {
      sumCap += this.fQueue.get(i).getFirst().getFileSize();
      if (sumCap > (1 - OLD_SECTION) * this.capacity) {
        oldIndex = i;
        break;
      }
    }
    //odebereme podle LRU
    if (oldIndex == -1) {
      this.fQueue.remove(this.fQueue.size() - 1);
      return;
    }
    //odebereme podle LFU z OLD section
    Pair<FileOnClient, Integer> file = this.fQueue.get(oldIndex);
    for (int i = oldIndex; i < this.fQueue.size(); i++) {
      if (this.fQueue.get(i).getSecond() < file.getSecond()) {
        file = this.fQueue.get(i);
      }
    }
    this.fQueue.remove(file);

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
    this.fQueue.add(new Pair<>(f, 1));
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
}
