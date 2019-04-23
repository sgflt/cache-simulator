package cz.zcu.kiv.cacheSimulator.cachePolicies;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import cz.zcu.kiv.cacheSimulator.shared.FileOnClient;
import cz.zcu.kiv.cacheSimulator.shared.GlobalVariables;
import cz.zcu.kiv.cacheSimulator.shared.Triplet;


/**
 * class for MQ algorithm
 * <p>
 * SOURCE: Adapted from article "The Multi-Queue Replacement Algorithm for Second Level Buffer Caches", by
 * Y. Zhou, J. F. Philbin and K. Li
 *
 * @author Pavel Bžoch
 */
public class MQ implements ICache {

  /**
   * pocet front
   */
  private static int QUEUE_COUNT = 5;

  /**
   * pocet front
   */
  private static int LIFE_TIME = 100;

  /**
   * velikost fronty QOUT
   */
  private static int QOUT_CAPACITY = 10;

  /**
   * promenna pro pocitani logickeho casu
   */
  private long timeCounter;

  /**
   * velikost cache
   */
  private long capacity;

  /**
   * struktura pro ukladani souboru, ktere jsou vetsi nez cache
   */
  private final ArrayList<FileOnClient> fOverCapacity;


  /**
   * LRU fronty pro uchovani souboru, prvni Integer je pro pocitani referenci,
   * druhy je pro vypocty casu
   */
  private final Queue<Triplet<FileOnClient, Integer, Long>>[] fQueues;

  /**
   * LRU fronty pro uchovani souboru, prvni Integer je pro pocitani referenci,
   * druhy je pro vypocty casu
   */
  private final Queue<Triplet<FileOnClient, Integer, Long>> fQueueOut;


  @SuppressWarnings("unchecked")
  public MQ() {
    this.capacity = GlobalVariables.getCacheCapacity();
    this.fQueueOut = new LinkedList<>();
    this.fQueues = (Queue<Triplet<FileOnClient, Integer, Long>>[]) new Queue[QUEUE_COUNT];
    for (int i = 0; i < QUEUE_COUNT; i++) {
      this.fQueues[i] = new LinkedList<>();
    }
    this.timeCounter = 0;
    this.fOverCapacity = new ArrayList<>();
  }


  @Override
  public boolean contains(final String fileName) {
    for (final var fQueue : this.fQueues) {
      for (final var f : fQueue) {
        if (f.getFirst().getFileName().equalsIgnoreCase(fileName)) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public FileOnClient get(final String fileName) {
    Triplet<FileOnClient, Integer, Long> file = null;
    for (final var fQueue : this.fQueues) {
      if (file != null) {
        break;
      }
      for (final Triplet<FileOnClient, Integer, Long> f : fQueue) {
        if (f.getFirst().getFileName().equalsIgnoreCase(fileName)) {
          file = f;
          fQueue.remove(file);
          break;
        }
      }
    }
    if (file == null) {
      return null;
    } else {
      file.setSecond(file.getSecond() + 1);
      file.setThird(++this.timeCounter + LIFE_TIME);
      int index = (int) (Math.log10(file.getSecond()) / Math.log10(2));
      if (index >= QUEUE_COUNT) {
        index = QUEUE_COUNT - 1;
      }
      this.fQueues[index].add(file);
      Adjust();
      return file.getFirst();
    }
  }

  /**
   * metoda pro zarovnani LRU cache podle casu
   */
  private void Adjust() {
    for (int i = 1; i < this.fQueues.length; i++) {
      for (final Triplet<FileOnClient, Integer, Long> f : this.fQueues[i]) {
        if (f.getThird() < this.timeCounter) {
          this.fQueues[i].remove(f);
          this.fQueues[i - 1].add(f);
          i--;
          break;
        }
      }
    }
  }

  @Override
  public long freeCapacity() {
    long obsazeno = 0;
    for (final var fQueue : this.fQueues) {
      for (final Triplet<FileOnClient, Integer, Long> f : fQueue) {
        obsazeno += f.getFirst().getFileSize();
      }
    }
    return this.capacity - obsazeno;
  }

  @Override
  public void removeFile() {
    for (final var fQueue : this.fQueues) {
      if (!fQueue.isEmpty()) {
        final Triplet<FileOnClient, Integer, Long> out = fQueue.remove();
        // v qout jsou uchovany metadata souboru
        if (this.fQueueOut.size() > QOUT_CAPACITY) {
          this.fQueueOut.remove();
        }
        this.fQueueOut.add(out);
        return;
      }
    }
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
    // uvolneni mista pro dalsi soubor
    while (freeCapacity() < f.getFileSize()) {
      removeFile();
    }

    // soubor je v qout - musi se nove stahnout, ale zustavaji mu parametry
    Triplet<FileOnClient, Integer, Long> newFile = null;
    for (final Triplet<FileOnClient, Integer, Long> fout : this.fQueueOut) {
      if (fout.getFirst().getFileName().equalsIgnoreCase(f.getFileName())) {
        newFile = fout;
        this.fQueueOut.remove(fout);
        break;
      }
    }
    // soubor je uplne novy, zakladame parametry
    if (newFile == null) {
      newFile = new Triplet<>(f, 1, ++this.timeCounter
        + LIFE_TIME);
    }

    // umistime soubor do spravne LRU fronty
    final int refCount = newFile.getSecond();
    int index = (int) (Math.log10(refCount) / Math.log10(2));
    if (index >= QUEUE_COUNT) {
      index = QUEUE_COUNT - 1;
    }
    this.fQueues[index].add(newFile);
    Adjust();
  }

  @Override
  public String toString() {
    return "MQ";
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
    this.timeCounter = 0;
    this.fQueueOut.clear();
    for (int i = 0; i < QUEUE_COUNT; i++) {
      this.fQueues[i].clear();
    }
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
    return "MQ;MQ";
  }

  public static int getQUEUE_COUNT() {
    return QUEUE_COUNT;
  }

  public static void setQUEUE_COUNT(final int QUEUE_COUNT) {
    MQ.QUEUE_COUNT = QUEUE_COUNT;
  }

  public static int getLIFE_TIME() {
    return LIFE_TIME;
  }

  public static void setLIFE_TIME(final int LIFE_TIME) {
    MQ.LIFE_TIME = LIFE_TIME;
  }

  public static int getQOUT_CAPACITY() {
    return QOUT_CAPACITY;
  }

  public static void setQOUT_CAPACITY(final int QOUT_CAPACITY) {
    MQ.QOUT_CAPACITY = QOUT_CAPACITY;
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
