package cz.zcu.kiv.cacheSimulator.cachePolicies;

import cz.zcu.kiv.cacheSimulator.shared.FileOnClient;
import cz.zcu.kiv.cacheSimulator.shared.GlobalVariables;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;


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
  private long usedCapacity;


  /**
   * struktura pro ukladani souboru, ktere jsou vetsi nez cache
   */
  private final List<FileOnClient> fOverCapacity;


  /**
   * LRU fronty pro uchovani souboru, prvni Integer je pro pocitani referenci,
   * druhy je pro vypocty casu
   */
  private final Queue<MetaData>[] fQueues;

  /**
   * LRU fronty pro uchovani souboru, prvni Integer je pro pocitani referenci,
   * druhy je pro vypocty casu
   */
  private final Queue<MetaData> fQueueOut;

  private final Map<String, Queue<MetaData>> files = new HashMap<>();


  @SuppressWarnings("unchecked")
  public MQ() {
    this.capacity = GlobalVariables.getCacheCapacity();
    this.fQueueOut = new LinkedList<>();
    this.fQueues = new Queue[QUEUE_COUNT];
    for (int i = 0; i < QUEUE_COUNT; i++) {
      this.fQueues[i] = new LinkedList<>();
    }
    this.fOverCapacity = new ArrayList<>();
  }

  @Override
  public FileOnClient get(final String fileName) {
    final Queue<MetaData> fileQueue = this.files.get(fileName);
    if (fileQueue == null) {
      return null;
    }
    for (final var it = fileQueue.iterator(); it.hasNext(); ) {
      final MetaData metaData = it.next();
      if (metaData.getFileOnClient().getFileName().equalsIgnoreCase(fileName)) {
        return getInternal(it, metaData);
      }
    }

    return null;
  }

  private FileOnClient getInternal(final Iterator<MetaData> iterator, final MetaData metaData) {
    iterator.remove();
    metaData.increaseReadhits();
    metaData.setAccessTime(++this.timeCounter + LIFE_TIME);
    int index = (int) (Math.log10(metaData.getReadHits()) / Math.log10(2));
    if (index >= QUEUE_COUNT) {
      index = QUEUE_COUNT - 1;
    }
    final Queue<MetaData> destinationQueue = this.fQueues[index];
    destinationQueue.add(metaData);
    this.files.put(metaData.getFileOnClient().getFileName(), destinationQueue);
    adjust();
    return metaData.getFileOnClient();
  }

  /**
   * metoda pro zarovnani LRU cache podle casu
   */
  private void adjust() {
    for (int i = 1; i < this.fQueues.length; i++) {
      for (final var it = this.fQueues[i].iterator(); it.hasNext(); ) {
        final MetaData metaData = it.next();
        if (metaData.getAccessTime() < this.timeCounter) {
          it.remove();
          final Queue<MetaData> destinationQueue = this.fQueues[i - 1];
          destinationQueue.add(metaData);
          this.files.put(metaData.getFileOnClient().getFileName(), destinationQueue);
          i--;
          break;
        }
      }
    }
  }

  @Override
  public long freeCapacity() {
    return this.capacity - this.usedCapacity;
  }

  @Override
  public void removeFile() {
    for (final var fQueue : this.fQueues) {
      if (!fQueue.isEmpty()) {
        final var out = fQueue.remove();
        this.files.remove(out.getFileOnClient().getFileName());

        // v qout jsou uchovany metadata souboru
        if (this.fQueueOut.size() > QOUT_CAPACITY) {
          this.fQueueOut.remove();
        }
        this.fQueueOut.add(out);
        this.usedCapacity -= out.getFileOnClient().getFileSize();
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
    MetaData newFile = null;
    for (final var fout : this.fQueueOut) {
      if (fout.getFileOnClient().getFileName().equalsIgnoreCase(f.getFileName())) {
        newFile = fout;
        this.fQueueOut.remove(fout);
        break;
      }
    }
    // soubor je uplne novy, zakladame parametry
    if (newFile == null) {
      newFile = new MetaData(f, ++this.timeCounter + LIFE_TIME);
    }

    // umistime soubor do spravne LRU fronty
    final int refCount = newFile.getReadHits();
    int index = (int) (Math.log10(refCount) / Math.log10(2));
    if (index >= QUEUE_COUNT) {
      index = QUEUE_COUNT - 1;
    }
    this.fQueues[index].add(newFile);
    this.usedCapacity += newFile.getFileOnClient().getFileSize();
    this.files.put(newFile.getFileOnClient().getFileName(), this.fQueues[index]);
    adjust();
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

  private static class MetaData {

    private final FileOnClient fileOnClient;
    private int readHits = 1;
    private long accessTime;

    MetaData(final FileOnClient fileOnClient, final long accessTime) {
      this.fileOnClient = fileOnClient;
      this.accessTime = accessTime;
    }

    FileOnClient getFileOnClient() {
      return this.fileOnClient;
    }

    int getReadHits() {
      return this.readHits;
    }

    void increaseReadhits() {
      ++this.readHits;
    }

    long getAccessTime() {
      return this.accessTime;
    }

    void setAccessTime(final long accessTime) {
      this.accessTime = accessTime;
    }
  }

}
