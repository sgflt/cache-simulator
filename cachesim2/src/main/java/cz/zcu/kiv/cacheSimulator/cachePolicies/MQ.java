package cz.zcu.kiv.cacheSimulator.cachePolicies;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import cz.zcu.kiv.cacheSimulator.shared.GlobalVariables;
import cz.zcu.kiv.cacheSimulator.shared.Triplet;
import cz.zcu.kiv.cacheSimulator.simulation.FileOnClient;

/**
 * class for MQ algorithm
 * SOURCE: Adapted from article
 * "The Multi-Queue Replacement Algorithm for Second Level Buffer Caches", by Y.
 * Zhou, J. F. Philbin and K. Li
 *
 * @author Pavel Bžoch
 * @author Lukáš Kvídera
 * @version 2.1
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
  private long timeCounter = 0;

  /**
   * velikost cache
   */
  private long capacity = 0;

  /**
   * pocatecni kapacita cache
   */
  private long initialCapacity = 0;

  /**
   * struktura pro ukladani souboru, ktere jsou vetsi nez cache
   */
  private final List<FileOnClient> fOverCapacity;

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

  private long used;


  @SuppressWarnings("unchecked")
  public MQ() {
    super();
    this.fQueueOut = new LinkedList<>();
    this.fQueues = new Queue[QUEUE_COUNT];
    for (int i = 0; i < QUEUE_COUNT; i++) {
      this.fQueues[i] = new LinkedList<>();
    }
    this.timeCounter = 0;
    this.fOverCapacity = new ArrayList<>();
  }


  @Override
  public boolean contains(final String fName) {
    for (int i = 0; i < this.fQueues.length; i++) {
      for (final Triplet<FileOnClient, Integer, Long> f : this.fQueues[i]) {
        if (f.getFirst().getFileName().equalsIgnoreCase(fName))
          return true;
      }
    }
    return false;
  }


  @Override
  public FileOnClient getFile(final String fName) {
    Triplet<FileOnClient, Integer, Long> file = null;
    for (int i = 0; i < this.fQueues.length; i++) {
      if (file != null)
        break;
      for (final Triplet<FileOnClient, Integer, Long> f : this.fQueues[i]) {
        if (f.getFirst().getFileName().equalsIgnoreCase(fName)) {
          file = f;
          this.fQueues[i].remove(file);
          break;
        }
      }
    }
    if (file == null)
      return null;

    file.setSecond(file.getSecond() + 1);
    file.setThird(++this.timeCounter + LIFE_TIME);
    int index = (int) (Math.log10(file.getSecond()) / Math.log10(2));

    if (index >= QUEUE_COUNT) {
      index = QUEUE_COUNT - 1;
    }

    this.fQueues[index].add(file);
    this.adjust();

    return file.getFirst();
  }


  /**
   * metoda pro zarovnani LRU cache podle casu
   */
  private void adjust() {
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
    return this.capacity - this.used;
  }


  @Override
  public void removeFile() {
    for (int i = 0; i < this.fQueues.length; i++) {
      if (this.fQueues[i].isEmpty())
        continue;

      final Triplet<FileOnClient, Integer, Long> out = this.fQueues[i].remove();
      // v qout jsou uchovany metadata souboru
      if (this.fQueueOut.size() > QOUT_CAPACITY) {
       this.fQueueOut.remove();
      }

      this.fQueueOut.add(out);
      this.used -= out.getFirst().getFileSize();

      return;
    }
  }


  @Override
  public void insertFile(final FileOnClient f) {
    // napred zkontrolujeme, jestli se soubor vejde do cache
    // pokud se nevejde, vztvorime pro nej okenko
    if (f.getFileSize() > this.capacity) {
      if (!this.fOverCapacity.isEmpty()) {
        this.fOverCapacity.add(f);
        return;
      }
      while (this.freeCapacity() < (long) (this.capacity * GlobalVariables
          .getCacheCapacityForDownloadWindow()))
        this.removeFile();
      this.fOverCapacity.add(f);
      this.capacity = (long) (this.capacity * (1 - GlobalVariables
          .getCacheCapacityForDownloadWindow()));
      return;
    }

    if (!this.fOverCapacity.isEmpty())
      this.checkTimes();

    // pokud se soubor vejde, fungujeme spravne
    // uvolneni mista pro dalsi soubor
    while (this.freeCapacity() < f.getFileSize())
      this.removeFile();

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
    if (newFile == null)
      newFile = new Triplet<>(f, 1, ++this.timeCounter + LIFE_TIME);

    // umistime soubor do spravne LRU fronty
    final int refCount = newFile.getSecond();
    int index = (int) (Math.log10(refCount) / Math.log10(2));
    if (index >= QUEUE_COUNT)
      index = QUEUE_COUNT - 1;

    this.fQueues[index].add(newFile);
    this.used += newFile.getFirst().getFileSize();
    this.adjust();
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
    this.initialCapacity = capacity;
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
   * metoda pro kontrolu, zda jiz nejsou soubory s vetsi velikosti nez cache
   * stazene - pak odstranime okenko
   */
  private void checkTimes() {
    boolean hasBeenRemoved = true;
    while (hasBeenRemoved) {
      hasBeenRemoved = false;
      if (!this.fOverCapacity.isEmpty()
          && this.fOverCapacity.get(0).getFRemoveTime() < GlobalVariables.getActualTime()) {
        this.fOverCapacity.remove(0);
        hasBeenRemoved = true;
      }
    }
    if (this.fOverCapacity.isEmpty()) {
      this.capacity = this.initialCapacity;
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
    result = prime * result + (int) (this.initialCapacity ^ (this.initialCapacity >>> 32));
    result = prime * result + this.toString().hashCode();
    return result;
  }


  @Override
  public void removeFile(final FileOnClient f) {
    for (int i = 0; i < this.fQueues.length; i++) {
      if (this.fQueues[i].size() == 0)
        continue;

      for (final Triplet<FileOnClient, Integer, Long> file : this.fQueues[i]) {
        if (file.getFirst() == f) {
          this.fQueues[i].remove(file);
          this.used -= file.getFirst().getFileSize();

            this.fQueueOut.remove();
          this.fQueueOut.add(file);
          return;
        }
      }
    }
  }


  @Override
  public List<FileOnClient> getCachedFiles() {
    final List<FileOnClient> list = new ArrayList<>();
    for (int i = 0; i < this.fQueues.length; i++) {
      if (this.fQueues[i].size() == 0)
        continue;
      for (final Triplet<FileOnClient, Integer, Long> file : this.fQueues[i]) {
        list.add(file.getFirst());
      }
    }
    return list;
  }


  @Override
  public long getCapacity() {
    return this.initialCapacity;
  }
}
