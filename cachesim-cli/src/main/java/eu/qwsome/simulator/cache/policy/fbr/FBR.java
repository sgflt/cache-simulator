package eu.qwsome.simulator.cache.policy.fbr;

import eu.qwsome.simulator.cache.core.FileTraffic;
import eu.qwsome.simulator.cache.core.SimulationCacheStub;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * @author Lukáš Kvídera
 */
class FBR extends SimulationCacheStub {
  private static final double OLD_SECTION = 0.3;
  private static final double NEW_SECTION = 0.6;
  private final List<MetaData> queue = new ArrayList<>();
  private final Set<String> files = new HashSet<>();
  private final long capacity;
  private long used;

  public FBR(final long capacity) {
    this.capacity = capacity;
  }

  @Override
  public FileTraffic get(final String fileName) {
    if (!this.files.contains(fileName)) {
      return null;
    }

    for (int i = 0; i < this.queue.size(); i++) {
      if (this.queue.get(i).getFileOnClient().getFileName().equalsIgnoreCase(fileName)) {
        final MetaData foundFile = this.queue.get(i);
        reorder(i, foundFile);
        this.queue.remove(foundFile);
        this.queue.add(foundFile);
        return foundFile.getFileOnClient();
      }
    }

    return null;
  }

  @Override
  public void put(final String key, final FileTraffic value) {
    if (value.getFileSize() > this.capacity) {
      return;
    }

    while (value.getFileSize() + this.used > this.capacity) {
      removeFile();
    }

    this.queue.add(new MetaData(value));
    this.files.add(value.getFileName());
    this.used += value.getFileSize();
  }

  private void reorder(final int i, final MetaData foundFile) {
    long sumCap = 0;
    int newIndex = 0;
    for (int j = 0; j < this.queue.size(); j++) {
      sumCap += this.queue.get(j).getFileOnClient().getFileSize();
      if (sumCap > (NEW_SECTION) * this.capacity) {
        newIndex = j;
        break;
      }
    }

    if (newIndex < i) {
      foundFile.incrementReadHits();
    }
  }

  private void removeFile() {
    long sumCap = 0;
    int oldIndex = -1;

    if (this.queue.isEmpty()) {
      return;
    }

    for (int i = 0; i < this.queue.size(); i++) {
      sumCap += this.queue.get(i).getFileOnClient().getFileSize();
      if (sumCap > (1 - OLD_SECTION) * this.capacity) {
        oldIndex = i;
        break;
      }
    }

    // LRU
    if (oldIndex == -1) {
      final MetaData removedFile = this.queue.remove(this.queue.size() - 1);
      this.used -= removedFile.getFileOnClient().getFileSize();
      this.files.remove(removedFile.getFileOnClient().getFileName());
      return;
    }

    // LFU from OLD section
    final MetaData metaData = this.queue.stream().skip(oldIndex).min(Comparator.comparing(MetaData::getReadHits)).orElseThrow();
    this.queue.remove(metaData);
    this.used -= metaData.getFileOnClient().getFileSize();
    this.files.remove(metaData.getFileOnClient().getFileName());
  }

  @Override
  public long getCapacity() {
    return this.capacity;
  }


  private static class MetaData {
    private final FileTraffic file;
    private int readHits = 1;

    MetaData(final FileTraffic file) {
      this.file = file;
    }

    FileTraffic getFileOnClient() {
      return this.file;
    }

    int getReadHits() {
      return this.readHits;
    }

    void incrementReadHits() {
      ++this.readHits;
    }
  }
}
